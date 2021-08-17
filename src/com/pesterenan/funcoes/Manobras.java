package com.pesterenan.funcoes;

import java.io.IOException;
import java.util.List;

import org.javatuples.Triplet;

import com.pesterenan.MechPeste;
import com.pesterenan.gui.GUI;
import com.pesterenan.gui.Status;
import com.pesterenan.model.Nave;
import com.pesterenan.utils.ControlePID;

import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Node;

public class Manobras extends Nave {

	private Node noDeManobra;
	private ControlePID ctrlAcel = new ControlePID();

	public Manobras(boolean executar) throws RPCException, StreamException, IOException, InterruptedException {
		super(getConexao());
		ctrlAcel.ajustarPID(0.025, 0.01, 0.1);
		ctrlAcel.limitarSaida(0.1, 1);
		if (executar) {
			executarProximaManobra();
		}
	}

	private void executarProximaManobra() throws RPCException, StreamException, IOException, InterruptedException {
		// Procurar se h� manobras para executar
		GUI.setStatus("Buscando Manobras...");
		try {
			noDeManobra = naveAtual.getControl().getNodes().get(0);
		} catch (IndexOutOfBoundsException e) {
			GUI.setStatus("Não há Manobras disponíveis.");
			System.err.println("Não há Manobras disponíveis.");
			MechPeste.finalizarTarefa();
		}
		// Caso haja, calcular e executar
		if (noDeManobra != null) {
			GUI.setParametros("altitude", noDeManobra.getDeltaV());
			System.out.println("DELTA-V DA MANOBRA: " + noDeManobra.getDeltaV());

			double duracaoDaQueima = calcularTempoDeQueima(noDeManobra);
			orientarNaveParaNoDeManobra(noDeManobra);
			executarQueima(noDeManobra, duracaoDaQueima);

			naveAtual.getAutoPilot().disengage();
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setRCS(false);
			noDeManobra.remove();
			GUI.setStatus(Status.PRONTO.get());
			MechPeste.finalizarTarefa();
		}
	}

	public double calcularTempoDeQueima(Node noDeManobra) throws RPCException {
		// Calcular tempo de queima (equa��o de foguete)
		List<Engine> motores = naveAtual.getParts().getEngines();
		for (Engine motor : motores) {
			if (motor.getPart().getStage() == naveAtual.getControl().getCurrentStage() && motor.getActive() == false) {
				motor.setActive(true);
			}
		}
		double empuxoTotal = naveAtual.getAvailableThrust(); // pegar empuxo dispon�vel
		double isp = naveAtual.getSpecificImpulse() * CONST_GRAV; // pegar isp e multiplicar � constante grav
		double massaTotal = naveAtual.getMass(); // pegar massa atual
		double massaSeca = massaTotal / Math.exp(noDeManobra.getRemainingDeltaV() / isp); // pegar massa seca
		double taxaDeQueima = empuxoTotal / isp; // taxa de fluxo, empuxo / isp
		double duracaoDaQueima = (massaTotal - massaSeca) / taxaDeQueima;
		System.out.println("Tempo de Queima da Manobra: " + duracaoDaQueima + " segundos");
		return duracaoDaQueima;
	}

	public void orientarNaveParaNoDeManobra(Node noDeManobra) {
		GUI.setStatus("Orientando nave para o nó de Manobra...");
		try {
			naveAtual.getAutoPilot().setReferenceFrame(noDeManobra.getReferenceFrame());
			naveAtual.getAutoPilot().setTargetDirection(new Triplet<Double, Double, Double>(0.0, 1.0, 0.0));
			naveAtual.getAutoPilot().engage();
			naveAtual.getAutoPilot().wait_();
		} catch (RPCException e) {
			System.err.println("Não foi possível orientar a nave para a manobra:\n\t" + e.getMessage());
		}
	}

	public void executarQueima(Node noDeManobra, double duracaoDaQueima)
			throws RPCException, InterruptedException, StreamException {
		double inicioDaQueima = noDeManobra.getTimeTo();
		// Caso estiver muito distante da manobra, dar Warp:
		if (inicioDaQueima + duracaoDaQueima > 90) {
			centroEspacial.warpTo((centroEspacial.getUT() + inicioDaQueima - duracaoDaQueima - 10), 100000, 4);
		}
		// Mostrar tempo de ignição:
		GUI.setStatus("Duração da queima: " + duracaoDaQueima + " segundos.");
		while (inicioDaQueima > 0) {
			inicioDaQueima = noDeManobra.getTimeTo() - (duracaoDaQueima / 2.0);
			inicioDaQueima = inicioDaQueima > 0.0 ? inicioDaQueima : 0.0;
			GUI.setStatus(String.format("Ignição em: %1$.1f segundos...", inicioDaQueima));
			Thread.sleep(100);
		}
		// Executar a manobra:
		Stream<Triplet<Double, Double, Double>> queimaRestante = getConexao().addStream(noDeManobra,
				"remainingBurnVector", noDeManobra.getReferenceFrame());
		GUI.setStatus("Executando manobra!");
		ctrlAcel.setLimitePID(1);
		while (noDeManobra != null) {
			ctrlAcel.setEntradaPID(-queimaRestante.get().getValue1());
			if (queimaRestante.get().getValue1() > 1) {
				naveAtual.getControl().setThrottle((float) ctrlAcel.computarPID());
			} else {
				naveAtual.getControl().setThrottle(0.0f);
				queimaRestante.remove();
				break;
			}
			Thread.sleep(25);
		}
	}

	public Node circularizarApoastro() throws RPCException {
		// Planejar circulariza��o usando equa��o vis-viva
		double parGrav = naveAtual.getOrbit().getBody().getGravitationalParameter(); // parametro G do corpo
		double apo = naveAtual.getOrbit().getApoapsis(); // apoastro da orbita
		double sma = naveAtual.getOrbit().getSemiMajorAxis(); // semieixo da orbita
		double apo2 = apo; // apoastro alvo
		double v1 = Math.sqrt(parGrav * ((2.0 / apo) - (1.0 / sma))); // calculo da vel orbital atual
		double v2 = Math.sqrt(parGrav * ((2.0 / apo) - (1.0 / apo2))); // calculo da vel orbital alvo
		double deltaV = v2 - v1; // delta -v manobra
		Node noDeManobra = naveAtual.getControl()
				.addNode(centroEspacial.getUT() + naveAtual.getOrbit().getTimeToApoapsis(), (float) deltaV, 0, 0);
		return noDeManobra;
	}

}

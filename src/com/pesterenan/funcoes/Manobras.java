package com.pesterenan.funcoes;

import java.io.IOException;
import java.util.List;

import javax.swing.SwingWorker;

import org.javatuples.Triplet;

import com.pesterenan.MechPeste;
import com.pesterenan.gui.GUI;
import com.pesterenan.gui.Status;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Vessel;

public class Manobras extends SwingWorker<String, String> {

	private final static float CONST_GRAV = 9.81f;
	private static Connection conexao;
	private static SpaceCenter centroEspacial;
	private static Vessel naveAtual;
	private Node noDeManobra;

	public Manobras(Connection conexao, boolean executar)
			throws RPCException, StreamException, IOException, InterruptedException {
		Manobras.conexao = conexao;
		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = centroEspacial.getActiveVessel();
		if (executar) {
			executarProximaManobra();
		}
	}

	private void executarProximaManobra() throws RPCException, StreamException, IOException, InterruptedException {
		// Procurar se há manobras para executar
		GUI.setStatus("Buscando Manobras...");
		try {
			noDeManobra = naveAtual.getControl().getNodes().get(0);
		} catch (IndexOutOfBoundsException e) {
			GUI.setStatus("Não há Manobras disponíveis");
			MechPeste.finalizarTarefa();
		}
		// Caso haja, calcular e executar
		if (noDeManobra != null) {
			firePropertyChange("altitude", 0, noDeManobra.getDeltaV());
			System.out.println("DELTA-V DA MANOBRA: " + noDeManobra.getDeltaV());

			double duracaoDaQueima = calcularTempoDeQueima(noDeManobra);
			orientarNave(noDeManobra);
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
		// Calcular tempo de queima (equação de foguete)
		List<Engine> motores = naveAtual.getParts().getEngines();
		for (Engine motor : motores) {
			if (motor.getPart().getStage() == naveAtual.getControl().getCurrentStage() && motor.getActive() == false) {
				motor.setActive(true);
			}
		}
		double empuxoTotal = naveAtual.getAvailableThrust(); // pegar empuxo disponível
		double isp = naveAtual.getSpecificImpulse() * CONST_GRAV; // pegar isp e multiplicar à constante grav
		double massaTotal = naveAtual.getMass(); // pegar massa atual
		double massaSeca = massaTotal / Math.exp(noDeManobra.getRemainingDeltaV() / isp); // pegar massa seca
		double taxaDeQueima = empuxoTotal / isp; // taxa de fluxo, empuxo / isp
		double duracaoDaQueima = (massaTotal - massaSeca) / taxaDeQueima;
		System.out.println("Tempo de Queima da Manobra: " + duracaoDaQueima + " segundos");
		return duracaoDaQueima;
	}

	public void orientarNave(Node noDeManobra) throws RPCException {
		GUI.setStatus("Orientando nave para a queima de circularização...");
		naveAtual.getAutoPilot().setReferenceFrame(noDeManobra.getReferenceFrame());
		naveAtual.getAutoPilot().setTargetDirection(new Triplet<Double, Double, Double>(0.0, 1.0, 0.0));
		naveAtual.getAutoPilot().engage();
		naveAtual.getAutoPilot().wait_();
	}

	public void executarQueima(Node noDeManobra, double duracaoDaQueima)
			throws RPCException, InterruptedException, StreamException {
		double inicioDaQueima = 1;
		// Caso estiver muito distante da manobra, dar Warp:
		if (noDeManobra.getTimeTo() + duracaoDaQueima > 120) {
			centroEspacial.warpTo((centroEspacial.getUT() + noDeManobra.getTimeTo() - duracaoDaQueima - 10), 100000, 4);
		}
		// Mostrar tempo de ignição:
		GUI.setStatus("Duração da queima: " + duracaoDaQueima + " segundos.");
		while (inicioDaQueima > 0) {
			inicioDaQueima = noDeManobra.getTimeTo() - (duracaoDaQueima / 2.0);
			GUI.setStatus(String.format("Ignição em: %1$.1f segundos...", inicioDaQueima));
			Thread.sleep(50);
		}
		// Executar a manobra:
		Stream<Triplet<Double, Double, Double>> queimaRestante = conexao.addStream(noDeManobra, "remainingBurnVector",
				noDeManobra.getReferenceFrame());
		GUI.setStatus("Executando manobra!");
		while (noDeManobra != null) {
			if (queimaRestante.get().getValue1() > 10
					|| queimaRestante.get().getValue1() > (noDeManobra.getDeltaV() * 0.1)) {
				naveAtual.getControl().setThrottle(1.0f);
			} else if (queimaRestante.get().getValue1() > (0.5)) {
				GUI.setStatus("Ajustando...");
				naveAtual.getControl().setThrottle(0.15f);
			} else {
				naveAtual.getControl().setThrottle(0.0f);
				queimaRestante.remove();
				break;
			}
			Thread.sleep(25);
		}
	}

	public Node circularizarApoastro() throws RPCException {
		// Planejar circularização usando equação vis-viva
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

	@Override
	protected String doInBackground() throws Exception {
		return null;
	}

}

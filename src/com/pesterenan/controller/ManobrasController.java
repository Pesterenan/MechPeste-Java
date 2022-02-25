package com.pesterenan.controller;

import java.io.IOException;
import java.util.List;

import org.javatuples.Triplet;

import com.pesterenan.MechPeste;
import com.pesterenan.gui.MainGui;
import com.pesterenan.gui.StatusJPanel;
import com.pesterenan.model.Nave;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Status;

import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Node;

public class ManobrasController extends TelemetriaController implements Runnable {

	private Node noDeManobra;
	public final static float CONST_GRAV = 9.81f;
	private ControlePID aceleracaoCtrl = new ControlePID();
	private double parametroGravitacional;
	private String funcao;

	public ManobrasController(String funcao) {
		super(getConexao());
		this.funcao = funcao;
		aceleracaoCtrl.setAmostraTempo(25);
		aceleracaoCtrl.ajustarPID(0.025, 0.1, 1);
		aceleracaoCtrl.limitarSaida(0.1, 1.0);
	}

	@Override
	public void run() {
		try {
			calcularManobra();
			executarProximaManobra();
		} catch (RPCException | StreamException | IOException | InterruptedException e) {
		}
	}

	public void calcularManobra() throws RPCException {
		if (this.funcao.equals(Modulos.EXECUTAR.get()))
			return;
		parametroGravitacional = naveAtual.getOrbit().getBody().getGravitationalParameter();
		double altitudeInicial = 0, tempoAteAltitude = 0;
		if (this.funcao.equals(Modulos.APOASTRO.get())) {
			altitudeInicial = naveAtual.getOrbit().getApoapsis();
			tempoAteAltitude = naveAtual.getOrbit().getTimeToApoapsis();
		}
		if (this.funcao.equals(Modulos.PERIASTRO.get())) {
			altitudeInicial = naveAtual.getOrbit().getPeriapsis();
			tempoAteAltitude = naveAtual.getOrbit().getTimeToPeriapsis();
		}

		double semiEixoMaior = naveAtual.getOrbit().getSemiMajorAxis();
		double velOrbitalAtual = Math.sqrt(parametroGravitacional * ((2.0 / altitudeInicial) - (1.0 / semiEixoMaior)));
		double velOrbitalAlvo = Math.sqrt(parametroGravitacional * ((2.0 / altitudeInicial) - (1.0 / altitudeInicial)));
		double deltaVdaManobra = velOrbitalAlvo - velOrbitalAtual;
		double[] deltaV = { deltaVdaManobra, 0, 0 };
		criarManobra(tempoAteAltitude, deltaV);
	}

	private void criarManobra(double tempoPosterior, double[] deltaV) {
		try {
			naveAtual.getControl().addNode(centroEspacial.getUT() + tempoPosterior, (float) deltaV[0],
					(float) deltaV[1], (float) deltaV[2]);
		} catch (RPCException e) {
			StatusJPanel.setStatus("Não foi possível criar a manobra.");
		}
	}

	public void executarProximaManobra() throws RPCException, StreamException, IOException, InterruptedException {
		StatusJPanel.setStatus("Buscando Manobras...");
		try {
			noDeManobra = naveAtual.getControl().getNodes().get(0);
		} catch (IndexOutOfBoundsException e) {
			StatusJPanel.setStatus("Não há Manobras disponíveis.");
		}
		if (noDeManobra != null) {
			MainGui.getStatus().firePropertyChange("altitudeSup", 0, noDeManobra.getDeltaV());
			System.out.println("DELTA-V DA MANOBRA: " + noDeManobra.getDeltaV());

			double duracaoDaQueima = calcularTempoDeQueima(noDeManobra);
			orientarNaveParaNoDeManobra(noDeManobra);
			executarQueima(noDeManobra, duracaoDaQueima);

			naveAtual.getAutoPilot().disengage();
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setRCS(false);
			noDeManobra.remove();
			StatusJPanel.setStatus(Status.PRONTO.get());

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
		StatusJPanel.setStatus("Orientando nave para o nó de Manobra...");
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
		StatusJPanel.setStatus("Duração da queima: " + duracaoDaQueima + " segundos.");
		while (inicioDaQueima > 0) {
			inicioDaQueima = noDeManobra.getTimeTo() - (duracaoDaQueima / 2.0);
			inicioDaQueima = inicioDaQueima > 0.0 ? inicioDaQueima : 0.0;
			StatusJPanel.setStatus(String.format("Ignição em: %1$.1f segundos...", inicioDaQueima));
			Thread.sleep(100);
		}
// Executar a manobra:
		Stream<Triplet<Double, Double, Double>> queimaRestante = getConexao().addStream(noDeManobra,
				"remainingBurnVector", noDeManobra.getReferenceFrame());
		StatusJPanel.setStatus("Executando manobra!");
		aceleracaoCtrl.setLimitePID(0);
		while (noDeManobra != null) {
			aceleracaoCtrl.setEntradaPID(-queimaRestante.get().getValue1() * 100 / noDeManobra.getDeltaV());
			if (queimaRestante.get().getValue1() > 1) {
				naveAtual.getControl().setThrottle((float) aceleracaoCtrl.computarPID());
			} else {
				naveAtual.getControl().setThrottle(0.0f);
				queimaRestante.remove();
				break;
			}
			Thread.sleep(25);
		}
	}

	public void setFuncao(String funcao) {
		this.funcao = funcao;
	}

}

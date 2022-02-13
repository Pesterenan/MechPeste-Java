package com.pesterenan.controller;

import java.io.IOException;
import java.util.List;

import org.javatuples.Triplet;

import com.pesterenan.MechPeste;
import com.pesterenan.gui.MainGui;
import com.pesterenan.gui.StatusJPanel;
import com.pesterenan.model.Nave;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Status;

import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Node;

public class ManobrasController extends Nave implements Runnable{

	public enum Manobras {
		APOASTRO, PERIASTRO, EXECUTAR, AJUSTAR
	}

	private Node noDeManobra;
	private ControlePID aceleracaoCtrl = new ControlePID();
	private double parametroGravitacional;
	private String funcao;

	public ManobrasController() throws RPCException {
		super(getConexao());
		aceleracaoCtrl.setAmostraTempo(25);
		aceleracaoCtrl.ajustarPID(0.025, 0.1, 1);
		aceleracaoCtrl.limitarSaida(0.1, 1.0);
		parametroGravitacional = naveAtual.getOrbit().getBody().getGravitationalParameter();
	}

	@Override
	public void run() {
		if (this.funcao.equals("Executar")) {
			try {
				executarProximaManobra();
			} catch (RPCException | StreamException | IOException | InterruptedException e) {
			}
		}
	}
	
	public void circularizarOrbita(Manobras altitude)
			throws RPCException, StreamException, IOException, InterruptedException {
		double apoastroInicial = 1;
		double semiEixoMaior = naveAtual.getOrbit().getSemiMajorAxis();
		double apoastroAlvo = 1;
		double tempoAteAltitude = 60.0;
		switch (altitude) {
		case APOASTRO:
			apoastroInicial = naveAtual.getOrbit().getApoapsis();
			apoastroAlvo = apoastroInicial;
			tempoAteAltitude = naveAtual.getOrbit().getTimeToApoapsis();
			break;
		case PERIASTRO:
			apoastroInicial = naveAtual.getOrbit().getPeriapsis();
			apoastroAlvo = apoastroInicial;
			tempoAteAltitude = naveAtual.getOrbit().getTimeToPeriapsis();
			break;
		}
		double velOrbitalAtual = Math.sqrt(parametroGravitacional * ((2.0 / apoastroInicial) - (1.0 / semiEixoMaior)));
		double velOrbitalAlvo = Math.sqrt(parametroGravitacional * ((2.0 / apoastroInicial) - (1.0 / apoastroAlvo)));
		double deltaVdaManobra = velOrbitalAlvo - velOrbitalAtual;
		try {
			naveAtual.getControl().addNode(centroEspacial.getUT() + tempoAteAltitude, (float) deltaVdaManobra, 0, 0);
			executarProximaManobra();
		} catch (Exception e) {
			StatusJPanel.setStatus("Não foi possível criar a manobra.");
		}
	}

	public void executarProximaManobra() throws RPCException, StreamException, IOException, InterruptedException {
// Procurar se h� manobras para executar
		StatusJPanel.setStatus("Buscando Manobras...");
		try {
			noDeManobra = naveAtual.getControl().getNodes().get(0);
		} catch (IndexOutOfBoundsException e) {
			StatusJPanel.setStatus("Não há Manobras disponíveis.");
			System.err.println("Não há Manobras disponíveis.");
		}
// Caso haja, calcular e executar
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

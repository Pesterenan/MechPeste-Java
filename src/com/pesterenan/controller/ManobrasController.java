package com.pesterenan.controller;

import java.util.List;

import org.javatuples.Triplet;

import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Status;
import com.pesterenan.view.MainGui;
import com.pesterenan.view.StatusJPanel;

import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.VesselSituation;

public class ManobrasController extends FlightController implements Runnable {

	private Node noDeManobra;
	private ControlePID aceleracaoCtrl = new ControlePID();
	private String funcao;

	public ManobrasController(String funcao) {
		super(getConexao());
		this.funcao = funcao;
		aceleracaoCtrl.limitarSaida(0.05, 1.0);
	}

	@Override
	public void run() {
		calcularManobra();
		executarProximaManobra();
	}

	public void calcularManobra() {
		try {
			if (naveAtual.getSituation() == VesselSituation.LANDED ||
					naveAtual.getSituation() == VesselSituation.SPLASHED) {
				throw new InterruptedException();
			}
			if (this.funcao.equals(Modulos.EXECUTAR.get()))
				return;
			double parametroGravitacional = naveAtual.getOrbit().getBody().getGravitationalParameter();
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
			double velOrbitalAtual = Math
					.sqrt(parametroGravitacional * ((2.0 / altitudeInicial) - (1.0 / semiEixoMaior)));
			double velOrbitalAlvo = Math
					.sqrt(parametroGravitacional * ((2.0 / altitudeInicial) - (1.0 / altitudeInicial)));
			double deltaVdaManobra = velOrbitalAlvo - velOrbitalAtual;
			double[] deltaV = { deltaVdaManobra, 0, 0 };
			criarManobra(tempoAteAltitude, deltaV);
		} catch (RPCException | InterruptedException e) {
			StatusJPanel.setStatus("Não foi possível calcular a manobra.");
			return;
		}
	}

	private void criarManobra(double tempoPosterior, double[] deltaV) {
		try {
			naveAtual.getControl().addNode(centroEspacial.getUT() + tempoPosterior, (float) deltaV[0],
					(float) deltaV[1], (float) deltaV[2]);
		} catch (UnsupportedOperationException | RPCException e) {
			StatusJPanel.setStatus("Não foi possível criar a manobra.");
			return;
		}
	}

	public void executarProximaManobra() {
		try {
			StatusJPanel.setStatus("Buscando Manobras...");
			noDeManobra = naveAtual.getControl().getNodes().get(0);
			if (noDeManobra != null) {
				MainGui.getStatus().firePropertyChange("altitudeSup", 0, noDeManobra.getDeltaV());
				System.out.println("DELTA-V DA MANOBRA: " + noDeManobra.getDeltaV());

				double duracaoDaQueima = calcularTempoDeQueima(noDeManobra);
				orientarNaveParaNoDeManobra(noDeManobra);
				executarQueima(noDeManobra, duracaoDaQueima);
			}
		} catch (UnsupportedOperationException | IndexOutOfBoundsException e) {
			StatusJPanel.setStatus("Não há Manobras disponíveis.");
			return;
		} catch (RPCException e) {
			StatusJPanel.setStatus("Não foi possivel buscar dados da nave.");
		}
	}

	public double calcularTempoDeQueima(Node noDeManobra) throws RPCException {

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
		StatusJPanel.setStatus("Tempo de Queima da Manobra: " + duracaoDaQueima + " segundos");
		return duracaoDaQueima;
	}

	public void orientarNaveParaNoDeManobra(Node noDeManobra) {
		StatusJPanel.setStatus("Orientando nave para o nó de Manobra...");
		try {
			naveAtual.getControl().setSAS(true);
			naveAtual.getAutoPilot().setReferenceFrame(noDeManobra.getReferenceFrame());
			naveAtual.getAutoPilot().setTargetDirection(new Triplet<Double, Double, Double>(0.0, 1.0, 0.0));
			naveAtual.getAutoPilot().engage();
			naveAtual.getAutoPilot().wait_();
		} catch (RPCException e) {
			StatusJPanel.setStatus("Não foi possível orientar a nave para a manobra:\n\t" + e.getMessage());
		}
	}

	public void executarQueima(Node noDeManobra, double duracaoDaQueima) {
		try {
			double inicioDaQueima = noDeManobra.getTimeTo();
			// Caso estiver muito distante da manobra, dar Warp:
			if (inicioDaQueima + duracaoDaQueima > 60) {
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
			double duracao = duracaoDaQueima;
			while (duracao <= 0 || noDeManobra != null) {
				aceleracaoCtrl.setEntradaPID(-queimaRestante.get().getValue1() * 100 / noDeManobra.getDeltaV());
				if (queimaRestante.get().getValue1() > 1 && noDeManobra != null) {
					acelerar(aceleracaoCtrl.computarPID());
				} else {
					queimaRestante.remove();
					break;
				}
				duracao -= 0.025;
				MainGui.getParametros().getComponent(0).firePropertyChange("distancia", 0, duracao);
				Thread.sleep(25);
			}
			acelerar(0.0f);
			naveAtual.getAutoPilot().disengage();
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setRCS(false);
			noDeManobra.remove();
			StatusJPanel.setStatus(Status.PRONTO.get());
		} catch (StreamException | RPCException e) {
			StatusJPanel.setStatus("Não foi possivel buscar os dados da nave.");
		} catch (InterruptedException e) {
			StatusJPanel.setStatus("Manobra cancelada.");
		}
	}

	public void setFuncao(String funcao) {
		this.funcao = funcao;
	}

}

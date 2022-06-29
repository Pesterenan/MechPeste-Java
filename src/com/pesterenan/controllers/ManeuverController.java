package com.pesterenan.controllers;

import java.util.List;
import java.util.Map;

import org.javatuples.Triplet;

import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Status;
import com.pesterenan.utils.Utilities;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusJPanel;

import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.RCS;
import krpc.client.services.SpaceCenter.VesselSituation;

public class ManeuverController extends FlightController implements Runnable {

	private Node maneuverNode;
	private String function;
	private boolean fineAdjustment;
	private ControlePID ctrlRCS = new ControlePID();

	public ManeuverController(Map<String, String> commands) {
		super(getConexao());
		ctrlRCS.limitarSaida(0.25, 1.0);
		this.function = commands.get(Modulos.FUNCAO.get());
		this.fineAdjustment = canFineAdjust(commands.get(Modulos.AJUSTE_FINO.get()));
	}

	@Override
	public void run() {
		calculateManeuver();
		executeNextManeuver();
	}

	public void calculateManeuver() {
		try {
			if (naveAtual.getSituation() == VesselSituation.LANDED
					|| naveAtual.getSituation() == VesselSituation.SPLASHED) {
				throw new InterruptedException();
			}
			if (this.function.equals(Modulos.EXECUTAR.get()))
				return;
			double parametroGravitacional = naveAtual.getOrbit().getBody().getGravitationalParameter();
			double altitudeInicial = 0, tempoAteAltitude = 0;
			if (this.function.equals(Modulos.APOASTRO.get())) {
				altitudeInicial = naveAtual.getOrbit().getApoapsis();
				tempoAteAltitude = naveAtual.getOrbit().getTimeToApoapsis();
			}
			if (this.function.equals(Modulos.PERIASTRO.get())) {
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
			disengageAfterException("Não foi possível calcular a manobra.");
		}
	}

	private void criarManobra(double tempoPosterior, double[] deltaV) {
		try {
			naveAtual.getControl().addNode(centroEspacial.getUT() + tempoPosterior, (float) deltaV[0],
					(float) deltaV[1], (float) deltaV[2]);
		} catch (UnsupportedOperationException | RPCException e) {
			disengageAfterException("Não foi possível criar a manobra.");
		}
	}

	public void executeNextManeuver() {
		try {
			StatusJPanel.setStatus("Buscando Manobras...");
			maneuverNode = naveAtual.getControl().getNodes().get(0);

			double burnTime = calculateBurnTime(maneuverNode);
			orientToManeuverNode(maneuverNode);
			executeBurn(maneuverNode, burnTime);
		} catch (UnsupportedOperationException e) {
			disengageAfterException("A criação de Manobras não foi desbloqueada ainda.");
		} catch (IndexOutOfBoundsException e) {
			disengageAfterException("Não há Manobras disponíveis.");
		} catch (RPCException e) {
			disengageAfterException("Não foi possivel buscar dados da nave.");
		}
	}

	public void orientToManeuverNode(Node noDeManobra) {
		try {
			float roll = naveAtual.getAutoPilot().getTargetRoll();
			naveAtual.getAutoPilot().setReferenceFrame(noDeManobra.getReferenceFrame());
			naveAtual.getAutoPilot().setTargetDirection(new Triplet<Double, Double, Double>(0.0, 1.0, 0.0));
			naveAtual.getAutoPilot().setTargetRoll((roll + 180));
			naveAtual.getAutoPilot().engage();
			while (naveAtual.getAutoPilot().getError() > 5) {
				StatusJPanel.setStatus("Orientando nave para o nó de Manobra...");
				Thread.sleep(250);
			}
		} catch (InterruptedException | RPCException e) {
			disengageAfterException("Não foi possível orientar a nave para a manobra.");
		}
	}

	public double calculateBurnTime(Node noDeManobra) throws RPCException {

		List<Engine> motores = naveAtual.getParts().getEngines();
		for (Engine motor : motores) {
			if (motor.getPart().getStage() == naveAtual.getControl().getCurrentStage() && motor.getActive() == false) {
				motor.setActive(true);
			}
		}
		double empuxo = naveAtual.getAvailableThrust();
		double isp = naveAtual.getSpecificImpulse() * CONST_GRAV;
		double massaTotal = naveAtual.getMass();
		double massaSeca = massaTotal / Math.exp(noDeManobra.getDeltaV() / isp);
		double taxaDeQueima = empuxo / isp;
		double duracaoDaQueima = (massaTotal - massaSeca) / taxaDeQueima;

		StatusJPanel.setStatus("Tempo de Queima da Manobra: " + duracaoDaQueima + " segundos");
		return duracaoDaQueima;
	}

	public void executeBurn(Node noDeManobra, double duracaoDaQueima) throws RPCException {
		try {
			double inicioDaQueima = noDeManobra.getTimeTo() - (duracaoDaQueima / 2.0) - (fineAdjustment ? 5 : 0);
			StatusJPanel.setStatus("Warp temporal para próxima manobra...");
			if (inicioDaQueima > 30) {
				centroEspacial.warpTo((centroEspacial.getUT() + inicioDaQueima - 10), 100000, 4);
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
			double limiteParaDesacelerar = noDeManobra.getDeltaV() > 1000 ? 0.025
					: noDeManobra.getDeltaV() > 250 ? 0.10 : 0.25;

			while (!noDeManobra.equals(null)) {
				if (queimaRestante.get().getValue1() < (fineAdjustment ? 5 : 0.5)) {
					break;
				}
				throttle(Utilities.remap(noDeManobra.getDeltaV() * limiteParaDesacelerar, 0, 1, 0.1,
						queimaRestante.get().getValue1()));
				MainGui.getParametros().getComponent(0).firePropertyChange("distancia", 0,
						queimaRestante.get().getValue1());
				Thread.sleep(25);
			}
			throttle(0.0f);
			if (fineAdjustment) {
				adjustManeuverWithRCS(queimaRestante);
			}

			naveAtual.getAutoPilot().setReferenceFrame(pontoRefSuperficie);
			naveAtual.getAutoPilot().disengage();
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setRCS(false);
			noDeManobra.remove();
			StatusJPanel.setStatus(Status.PRONTO.get());
		} catch (StreamException | RPCException e) {
			disengageAfterException("Não foi possivel buscar os dados da nave.");
		} catch (InterruptedException e) {
			disengageAfterException("Manobra cancelada.");
		}
	}

	private void adjustManeuverWithRCS(Stream<Triplet<Double, Double, Double>> queimaRestante)
			throws RPCException, StreamException, InterruptedException {
		naveAtual.getControl().setRCS(true);
		while (queimaRestante.get().getValue1() >= 0.1) {
			naveAtual.getControl().setForward((float) ctrlRCS.computarPID(-queimaRestante.get().getValue1() * 10, 0));
			Thread.sleep(25);
		}
		naveAtual.getControl().setForward(0);
		queimaRestante.remove();
	}

	private boolean canFineAdjust(String string) {
		if (string.equals("true")) {
			try {
				List<RCS> rcsEngines = naveAtual.getParts().getRCS();
				if (rcsEngines.size() > 0) {
					for (RCS rcs : rcsEngines) {
						if (rcs.getHasFuel()) {
							return true;
						}
					}
				}
				return false;
			} catch (RPCException e) {
			}
		}
		return false;
	}
}

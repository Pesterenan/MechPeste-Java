package com.pesterenan.controllers;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.javatuples.Triplet;

import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Utilities;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusJPanel;

import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Orbit;
import krpc.client.services.SpaceCenter.RCS;
import krpc.client.services.SpaceCenter.VesselSituation;

public class ManeuverController extends FlightController implements Runnable {

	private Node maneuverNode;
	private String function;
	private boolean fineAdjustment;
	private ControlePID ctrlRCS = new ControlePID();
	private ControlePID ctrlManeuver = new ControlePID();

	public ManeuverController() {
		super(getConexao());
	}

	public ManeuverController(Map<String, String> commands) {
		super(getConexao());
		this.ctrlRCS.limitarSaida(0.5, 1.0);
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
			if (this.function.equals(Modulos.AJUSTAR.get())) {
				this.alignPlanes();
				return;
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
			createManeuver(tempoAteAltitude, deltaV);
		} catch (RPCException | InterruptedException e) {
			disengageAfterException(Bundle.getString("status_maneuver_not_possible"));
		}
	}

	public void alignPlanes() {
		try {
			// Create maneuver at Ascending Node
			Orbit vesselOrbit = centroEspacial.getActiveVessel().getOrbit();
			Orbit targetOrbit = getTargetOrbit();

			double ascendingNode = vesselOrbit.trueAnomalyAtAN(targetOrbit);
			double descendingNode = vesselOrbit.trueAnomalyAtDN(targetOrbit);
			double uTatAscendingNode = vesselOrbit.uTAtTrueAnomaly(ascendingNode);
			double uTatDescendingNode = vesselOrbit.uTAtTrueAnomaly(descendingNode);
			double closestTrueAnomaly = Math.min(uTatAscendingNode, uTatDescendingNode);
			boolean closestIsAN = closestTrueAnomaly == uTatAscendingNode;
			double[] dv = { 0, 0, 0 };

			createManeuver(closestTrueAnomaly - centroEspacial.getUT(), dv);
			//////////////////////

			// Get last maneuver and modify its inclination
			List<Node> nodes = centroEspacial.getActiveVessel().getControl().getNodes();
			Node maneuver = nodes.get(nodes.size() - 1);
//			System.out.println(String.format("%.3f", maneuver.getOrbit().getInclination()) + " MAN inc TGT " + String.format("%.3f", targetOrbit.getInclination()));
			double currentDeltaInc = compareInclination(maneuver.getOrbit(), targetOrbit);
			String deltaIncFormatted = String.format("%.3f", currentDeltaInc);
			while (!deltaIncFormatted.equals("0,000")) {
				currentDeltaInc = compareInclination(maneuver.getOrbit(), targetOrbit);
				System.out.println(currentDeltaInc);
				deltaIncFormatted = String.format("%.3f", currentDeltaInc);
				double dvNormal = maneuver.getNormal();
				double ctrlOutput = ctrlManeuver.computarPID(currentDeltaInc, 0.0)
						* limitPIDOutput(Math.abs(currentDeltaInc));
				if ((closestIsAN ? currentDeltaInc : -currentDeltaInc) > 0.0) {
					maneuver.setNormal(dvNormal + (ctrlOutput));
				} else {
					maneuver.setNormal(dvNormal - (ctrlOutput));
				}
				Thread.sleep(25);
			}

		} catch (Exception e) {
			disengageAfterException("Não foi possivel ajustar a inclinação");
		}
	}

	private double limitPIDOutput(double absDeltaInc) {
		if (absDeltaInc < 0.05)
			return 1;
		if (absDeltaInc < 0.5)
			return 10;
		if (absDeltaInc < 10.0)
			return 25;
		return 50;
	}

	private double compareInclination(Orbit maneuverOrbit, Orbit targetOrbit) {
		double deltaInclination = 0;
		try {
			double maneuverInc = Math.round(Math.toDegrees(maneuverOrbit.getInclination()) * 1000) / 100.0;
			double targetInc = Math.round(Math.toDegrees(targetOrbit.getInclination()) * 1000) / 100.0;
			deltaInclination = (targetInc - maneuverInc);
			System.out.println("manInc " + maneuverInc + "|" + targetInc + " tgtInc " + deltaInclination);
		} catch (RPCException e) {
			e.printStackTrace();
		}
		return deltaInclination;
	}

	private Orbit getTargetOrbit() throws RPCException {
		if (centroEspacial.getTargetBody() != null) {
			return centroEspacial.getTargetBody().getOrbit();
		}
		if (centroEspacial.getTargetVessel() != null) {
			return centroEspacial.getTargetVessel().getOrbit();
		}
		return null;
	}

	private void createManeuver(double tempoPosterior, double[] deltaV) {
		try {
			naveAtual.getControl().addNode(centroEspacial.getUT() + tempoPosterior, (float) deltaV[0],
					(float) deltaV[1], (float) deltaV[2]);
		} catch (UnsupportedOperationException | RPCException e) {
			disengageAfterException(Bundle.getString("status_maneuver_not_possible"));
		}
	}

	public void executeNextManeuver() {
		try {
			maneuverNode = naveAtual.getControl().getNodes().get(0);
			double burnTime = calculateBurnTime(maneuverNode);
			orientToManeuverNode(maneuverNode);
			executeBurn(maneuverNode, burnTime);
		} catch (UnsupportedOperationException e) {
			disengageAfterException(Bundle.getString("status_maneuver_not_unlocked"));
		} catch (IndexOutOfBoundsException e) {
			disengageAfterException(Bundle.getString("status_maneuver_unavailable"));
		} catch (RPCException e) {
			disengageAfterException(Bundle.getString("status_data_unavailable"));
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
				StatusJPanel.setStatus(Bundle.getString("status_orienting_ship"));
				Thread.sleep(250);
			}
		} catch (InterruptedException | RPCException e) {
			disengageAfterException(Bundle.getString("status_couldnt_orient"));
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
			StatusJPanel.setStatus(Bundle.getString("status_maneuver_warp"));
			if (inicioDaQueima > 30) {
				centroEspacial.warpTo((centroEspacial.getUT() + inicioDaQueima - 10), 100000, 4);
			}
			// Mostrar tempo de ignição:
			StatusJPanel.setStatus(String.format(Bundle.getString("status_maneuver_duration"), duracaoDaQueima));
			while (inicioDaQueima > 0) {
				inicioDaQueima = noDeManobra.getTimeTo() - (duracaoDaQueima / 2.0);
				inicioDaQueima = inicioDaQueima > 0.0 ? inicioDaQueima : 0.0;
				StatusJPanel.setStatus(String.format(Bundle.getString("status_maneuver_ignition_in"), inicioDaQueima));
				Thread.sleep(100);
			}
			// Executar a manobra:
			Stream<Triplet<Double, Double, Double>> queimaRestante = getConexao().addStream(noDeManobra,
					"remainingBurnVector", noDeManobra.getReferenceFrame());
			StatusJPanel.setStatus(Bundle.getString("status_maneuver_executing"));
			double limiteParaDesacelerar = noDeManobra.getDeltaV() > 1000 ? 0.025
					: noDeManobra.getDeltaV() > 250 ? 0.10 : 0.25;

			while (!noDeManobra.equals(null)) {
				if (queimaRestante.get().getValue1() < (fineAdjustment ? 3 : 0.5)) {
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
			StatusJPanel.setStatus(Bundle.getString("status_ready"));
		} catch (StreamException | RPCException e) {
			disengageAfterException(Bundle.getString("status_data_unavailable"));
		} catch (InterruptedException e) {
			disengageAfterException(Bundle.getString("status_maneuver_cancelled"));
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

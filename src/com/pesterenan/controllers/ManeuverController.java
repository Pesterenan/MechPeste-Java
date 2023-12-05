package com.pesterenan.controllers;

import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Attributes;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Navigation;
import com.pesterenan.utils.Vector;
import com.pesterenan.views.RunManeuverJPanel;

import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Orbit;
import krpc.client.services.SpaceCenter.RCS;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;
import org.javatuples.Triplet;

import java.util.List;
import java.util.Map;

import static com.pesterenan.MechPeste.getConnection;
import static com.pesterenan.MechPeste.getSpaceCenter;

public class ManeuverController extends Controller {

	public final static float CONST_GRAV = 9.81f;
	private final ControlePID ctrlRCS = new ControlePID();
	private final ControlePID ctrlManeuver = new ControlePID();
	private Navigation navigation;
	private boolean fineAdjustment;
	private double lowOrbitAltitude;

	public ManeuverController(Map<String, String> commands) {
		super();
		this.commands = commands;
		this.navigation = new Navigation(getNaveAtual());
		initializeParameters();
	}

	private void initializeParameters() {
		ctrlRCS.adjustOutput(0.5, 1.0);
		fineAdjustment = canFineAdjust(commands.get(Modulos.AJUSTE_FINO.get()));
		try {
			lowOrbitAltitude = new Attributes().getLowOrbitAltitude(currentBody.getName());
			System.out.println("lowOrbitAltitude: " + lowOrbitAltitude);
		} catch (RPCException e) {
		}
	}

	@Override
	public void run() {
		calculateManeuver();
		if (!(commands.get(Modulos.FUNCAO.get()).equals(Modulos.RENDEZVOUS.get())
				|| commands.get(Modulos.FUNCAO.get()).equals(Modulos.ORBITA_BAIXA.get())
				|| commands.get(Modulos.FUNCAO.get()).equals(Modulos.AJUSTAR.get()))) {
			executeNextManeuver();
		}
	}

	private Node biEllipticTransferToOrbit(double targetAltitude, double timeToStart) {
		double[] totalDv = { 0, 0, 0 };
		try {
			Orbit currentOrbit = getNaveAtual().getOrbit();
			double startingRadius = currentOrbit.getApoapsis();
			double gravParameter = currentBody.getGravitationalParameter();

			// Delta-v required to leave the current orbit
			double deltaV1 = Math.sqrt(2 * gravParameter / startingRadius) - Math.sqrt(gravParameter / startingRadius);

			// Calculate the intermediate radius for the intermediate orbit
			double intermediateRadius = currentBody.getEquatorialRadius() + targetAltitude;

			// Delta-v required to enter the intermediate orbit
			double deltaV2 = Math.sqrt(gravParameter / intermediateRadius)
					- Math.sqrt(2 * gravParameter / intermediateRadius);

			// Calculate the final radius for the target orbit
			double targetRadius = currentBody.getEquatorialRadius() + targetAltitude;

			// Delta-v required to leave the intermediate orbit and enter the target orbit
			double deltaV3 = Math.sqrt(2 * gravParameter / intermediateRadius)
					- Math.sqrt(gravParameter / intermediateRadius);
			double deltaV4 = Math.sqrt(gravParameter / targetRadius) - Math.sqrt(2 * gravParameter / targetRadius);

			// Total delta-v for the bi-elliptic transfer
			totalDv[0] = deltaV1 + deltaV2 + deltaV3 + deltaV4;
		} catch (RPCException e) {
			// Handle the exception
		}
		return createManeuver(timeToStart, totalDv);
	}

	public void calculateManeuver() {
		try {
			tuneAutoPilot();
			System.out.println(commands + " calculate maneuvers");
			if (commands.get(Modulos.FUNCAO.get()).equals(Modulos.EXECUTAR.get())) {
				return;
			}
			if (getNaveAtual().getSituation() == VesselSituation.LANDED ||
					getNaveAtual().getSituation() == VesselSituation.SPLASHED) {
				throw new InterruptedException();
			}
			if (commands.get(Modulos.FUNCAO.get()).equals(Modulos.AJUSTAR.get())) {
				this.alignPlanesWithTargetVessel();
				return;
			}
			if (commands.get(Modulos.FUNCAO.get()).equals(Modulos.RENDEZVOUS.get())) {
				this.rendezvousWithTargetVessel();
				return;
			}
			if (commands.get(Modulos.FUNCAO.get()).equals(Modulos.ORBITA_BAIXA.get())) {
				biEllipticTransferToOrbit(lowOrbitAltitude, getNaveAtual().getOrbit().getTimeToPeriapsis());
				return;
			}
			double gravParameter = currentBody.getGravitationalParameter();
			double altitudeInicial = 0, tempoAteAltitude = 0;
			if (commands.get(Modulos.FUNCAO.get()).equals(Modulos.APOASTRO.get())) {
				altitudeInicial = getNaveAtual().getOrbit().getApoapsis();
				tempoAteAltitude = getNaveAtual().getOrbit().getTimeToApoapsis();
			}
			if (commands.get(Modulos.FUNCAO.get()).equals(Modulos.PERIASTRO.get())) {
				altitudeInicial = getNaveAtual().getOrbit().getPeriapsis();
				tempoAteAltitude = getNaveAtual().getOrbit().getTimeToPeriapsis();
			}

			double semiEixoMaior = getNaveAtual().getOrbit().getSemiMajorAxis();
			double velOrbitalAtual = Math.sqrt(gravParameter * ((2.0 / altitudeInicial) - (1.0 / semiEixoMaior)));
			double velOrbitalAlvo = Math.sqrt(gravParameter * ((2.0 / altitudeInicial) - (1.0 / altitudeInicial)));
			double deltaVdaManobra = velOrbitalAlvo - velOrbitalAtual;
			double[] deltaV = { deltaVdaManobra, 0, 0 };
			createManeuver(tempoAteAltitude, deltaV);
		} catch (RPCException | InterruptedException e) {
			setCurrentStatus(Bundle.getString("status_maneuver_not_possible"));
		}
	}

	public void matchOrbitApoapsis() {
		try {
			Orbit targetOrbit = getTargetOrbit();
			System.out.println(targetOrbit.getApoapsis() + "-- APO");
			Node maneuver = biEllipticTransferToOrbit(targetOrbit.getApoapsis(),
					getNaveAtual().getOrbit().getTimeToPeriapsis());
			while (true) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				double currentDeltaApo = compareOrbitParameter(maneuver.getOrbit(), targetOrbit, Compare.AP);
				String deltaApoFormatted = String.format("%.2f", currentDeltaApo);
				System.out.println(deltaApoFormatted);
				if (deltaApoFormatted.equals(String.format("%.2f", 0.00))) {
					break;
				}
				double dvPrograde = maneuver.getPrograde();
				double ctrlOutput = ctrlManeuver.calcPID(currentDeltaApo, 0);

				maneuver.setPrograde(dvPrograde - (ctrlOutput));
				Thread.sleep(50);
			}
		} catch (Exception e) {
			setCurrentStatus("Não foi possivel ajustar a inclinação");
		}
	}

	private Node createManeuverAtClosestIncNode(Orbit targetOrbit) {
		double uTatClosestNode = 1;
		double[] dv = { 0, 0, 0 };
		try {
			double[] incNodesUt = getTimeToIncNodes(targetOrbit);
			uTatClosestNode = Math.min(incNodesUt[0], incNodesUt[1]) - getSpaceCenter().getUT();
		} catch (Exception ignored) {
		}
		return createManeuver(uTatClosestNode, dv);
	}

	private double[] getTimeToIncNodes(Orbit targetOrbit) throws RPCException {
		Orbit vesselOrbit = getNaveAtual().getOrbit();
		double ascendingNode = vesselOrbit.trueAnomalyAtAN(targetOrbit);
		double descendingNode = vesselOrbit.trueAnomalyAtDN(targetOrbit);
		return new double[] { vesselOrbit.uTAtTrueAnomaly(ascendingNode), vesselOrbit.uTAtTrueAnomaly(descendingNode) };
	}

	private void alignPlanesWithTargetVessel() {
		try {
			Vessel vessel = getNaveAtual();
			Orbit vesselOrbit = getNaveAtual().getOrbit();
			Orbit targetVesselOrbit = getSpaceCenter().getTargetVessel().getOrbit();
			boolean hasManeuverNodes = vessel.getControl().getNodes().size() > 0;
			System.out.println("hasManeuverNodes: " + hasManeuverNodes);
			if (!hasManeuverNodes) {
				RunManeuverJPanel.createManeuver();
			}
			java.util.List<Node> currentManeuvers = vessel.getControl().getNodes();
			Node currentManeuver = currentManeuvers.get(0);
			double[] incNodesUt = {
					vesselOrbit.uTAtTrueAnomaly(vesselOrbit.trueAnomalyAtAN(targetVesselOrbit)),
					vesselOrbit.uTAtTrueAnomaly(vesselOrbit.trueAnomalyAtDN(targetVesselOrbit))
			};
			boolean closestIsAN = incNodesUt[0] < incNodesUt[1];
			RunManeuverJPanel.positionManeuverAt(closestIsAN ? "ascending" : "descending");
			double currentInclination = Math
					.toDegrees(currentManeuver.getOrbit().relativeInclination(targetVesselOrbit));
			while (currentInclination > 0.05) {
				currentInclination = Math
						.toDegrees(currentManeuver.getOrbit().relativeInclination(targetVesselOrbit));
				double ctrlOutput = ctrlManeuver.calcPID(currentInclination * 100, 0);
				currentManeuver.setNormal(currentManeuver.getNormal() + (closestIsAN ? ctrlOutput : -ctrlOutput));
				Thread.sleep(25);
			}
		} catch (Exception err) {
			System.err.println(err);
		}
	}

	private void rendezvousWithTargetVessel() {
		try {
			Orbit targetVesselOrbit = getSpaceCenter().getTargetVessel().getOrbit();
			boolean hasManeuverNodes = getNaveAtual().getControl().getNodes().size() > 0;
			java.util.List<Node> currentManeuvers = getNaveAtual().getControl().getNodes();
			Node lastManeuverNode;
			double lastManeuverNodeUT = 60;
			if (hasManeuverNodes) {
				currentManeuvers = getNaveAtual().getControl().getNodes();
				lastManeuverNode = currentManeuvers.get(currentManeuvers.size() - 1);
				lastManeuverNodeUT += lastManeuverNode.getUT();
				RunManeuverJPanel.createManeuver(lastManeuverNodeUT);
			} else {
				RunManeuverJPanel.createManeuver();
			}
			currentManeuvers = getNaveAtual().getControl().getNodes();
			lastManeuverNode = currentManeuvers.get(currentManeuvers.size() - 1);
			double targetAP = targetVesselOrbit.getApoapsis();
			double targetPE = targetVesselOrbit.getPeriapsis();
			double maneuverAP = lastManeuverNode.getOrbit().getApoapsis();
			double maneuverPE = lastManeuverNode.getOrbit().getPeriapsis();
			double maneuverUT = lastManeuverNode.getUT();
			ctrlManeuver.adjustPID(0.25, 0.0, 0.01);
			ctrlManeuver.adjustOutput(-100, 100);
			if (targetAP < maneuverPE) {
				while (Math.floor(targetAP) != Math.floor(maneuverPE)) {
					lastManeuverNode.setPrograde(
							lastManeuverNode.getPrograde()
									+ ctrlManeuver.calcPID(maneuverPE / targetAP * 1000, 1000));
					maneuverPE = lastManeuverNode.getOrbit().getPeriapsis();
					Thread.sleep(25);
				}
			}
			if (targetPE > maneuverAP) {
				while (Math.floor(targetPE) != Math.floor(maneuverAP)) {
					lastManeuverNode.setPrograde(
							lastManeuverNode.getPrograde()
									+ ctrlManeuver.calcPID(maneuverAP / targetPE * 1000, 1000));
					maneuverAP = lastManeuverNode.getOrbit().getApoapsis();
					Thread.sleep(25);
				}
			}

			double mu = currentBody.getGravitationalParameter();
			double time = 1000;

			double hohmannTransferDistance = lastManeuverNode.getOrbit().getSemiMajorAxis();
			double timeOfFlight = Math.PI * Math.sqrt(Math.pow(hohmannTransferDistance, 3) / mu);
			double angle = getNaveAtual().getOrbit().getMeanAnomalyAtEpoch();
			double omegaInterceptor = Math
					.sqrt(mu / Math.pow(getNaveAtual().getOrbit().radiusAt(getSpaceCenter().getUT()), 3)); // rad/s
			double omegaTarget = Math.sqrt(mu / Math.pow(targetVesselOrbit.radiusAt(getSpaceCenter().getUT()), 3)); // rad/s
			// double leadAngle = omegaTarget * timeOfFlight; // rad
			double leadAngle = targetVesselOrbit.getMeanAnomalyAtEpoch(); // rad
			double phaseAngle = Math.PI - leadAngle; // rad
			double calcAngle = (phaseAngle - angle);
			calcAngle = calcAngle < 0 ? calcAngle + (Math.PI * 2) : calcAngle;
			double waitTime = calcAngle / (omegaTarget - omegaInterceptor);
			time = waitTime;

			lastManeuverNode.setUT(getSpaceCenter().getUT() + time);
			ctrlManeuver.adjustOutput(-100, 100);
			ctrlManeuver.adjustPID(0.05, 0.1, 0.01);
			double closestApproach = lastManeuverNode.getOrbit().distanceAtClosestApproach(targetVesselOrbit);
			System.out.println(closestApproach);
			System.out.println("Ajustando tempo de Rendezvous...");
			while (Math.round(closestApproach) > 100) {
				if (closestApproach < 100000) {
					ctrlManeuver.adjustOutput(-10, 10);
				} else if (closestApproach < 10000) {
					ctrlManeuver.adjustOutput(-1, 1);
				} else {
					ctrlManeuver.adjustOutput(-100, 100);
				}
				maneuverUT = ctrlManeuver.calcPID(-closestApproach, 0);
				lastManeuverNode.setUT(lastManeuverNode.getUT() + maneuverUT);
				System.out.println("Closest " + (closestApproach));
				closestApproach = targetVesselOrbit.distanceAtClosestApproach(lastManeuverNode.getOrbit());
				Thread.sleep(25);
			}
			// lastManeuverNode.setUT(lastManeuverNode.getUT() -
			// lastManeuverNode.getOrbit().getPeriod() / 2);
		} catch (Exception err) {
		}
	}

	private double compareOrbitParameter(Orbit maneuverOrbit, Orbit targetOrbit, Compare parameter) {
		double maneuverParameter;
		double targetParameter;
		double delta = 0;
		try {
			switch (parameter) {
				case INC:
					maneuverParameter = maneuverOrbit.getInclination();
					System.out.println(maneuverParameter + " maneuver");
					targetParameter = targetOrbit.getInclination();
					System.out.println(targetParameter + " target");
					delta = (maneuverParameter / targetParameter) * 10;
					break;
				case AP:
					maneuverParameter = Math.round(maneuverOrbit.getApoapsis() / 100000.0);
					targetParameter = Math.round(targetOrbit.getApoapsis() / 100000.0);
					delta = (targetParameter - maneuverParameter);
					break;
				case PE:
					maneuverParameter = Math.round(maneuverOrbit.getPeriapsis()) / 100.0;
					targetParameter = Math.round(targetOrbit.getPeriapsis()) / 100.0;
					delta = (targetParameter - maneuverParameter);
					break;
				default:
					break;
			}

		} catch (RPCException e) {
			e.printStackTrace();
		}
		return delta;
	}

	private Orbit getTargetOrbit() throws RPCException {
		if (getSpaceCenter().getTargetBody() != null) {
			return getSpaceCenter().getTargetBody().getOrbit();
		}
		if (getSpaceCenter().getTargetVessel() != null) {
			return getSpaceCenter().getTargetVessel().getOrbit();
		}
		return null;
	}

	private Node createManeuver(double laterTime, double[] deltaV) {
		Node maneuverNode = null;
		try {
			getNaveAtual().getControl()
					.addNode(getSpaceCenter().getUT() + laterTime, (float) deltaV[0], (float) deltaV[1],
							(float) deltaV[2]);
			List<Node> currentNodes = getNaveAtual().getControl().getNodes();
			maneuverNode = currentNodes.get(currentNodes.size() - 1);
		} catch (UnsupportedOperationException | RPCException e) {
			setCurrentStatus(Bundle.getString("status_maneuver_not_possible"));
		}
		return maneuverNode;
	}

	public void executeNextManeuver() {
		try {
			Node maneuverNode = getNaveAtual().getControl().getNodes().get(0);
			double burnTime = calculateBurnTime(maneuverNode);
			orientToManeuverNode(maneuverNode);
			executeBurn(maneuverNode, burnTime);
		} catch (UnsupportedOperationException e) {
			setCurrentStatus(Bundle.getString("status_maneuver_not_unlocked"));
		} catch (IndexOutOfBoundsException e) {
			setCurrentStatus(Bundle.getString("status_maneuver_unavailable"));
		} catch (RPCException e) {
			setCurrentStatus(Bundle.getString("status_data_unavailable"));
		} catch (InterruptedException e) {
			setCurrentStatus(Bundle.getString("status_couldnt_orient"));
		}
	}

	public void orientToManeuverNode(Node maneuverNode) throws InterruptedException, RPCException {
		setCurrentStatus(Bundle.getString("status_orienting_ship"));
		ap.engage();
		while (ap.getHeadingError() > 3 || ap.getPitchError() > 3) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			navigation.aimAtManeuver(maneuverNode);
			Thread.sleep(100);
		}

	}

	public double calculateBurnTime(Node noDeManobra) throws RPCException {

		List<Engine> motores = getNaveAtual().getParts().getEngines();
		for (Engine motor : motores) {
			if (motor.getPart().getStage() == getNaveAtual().getControl().getCurrentStage() && !motor.getActive()) {
				motor.setActive(true);
			}
		}
		double empuxo = getNaveAtual().getAvailableThrust();
		double isp = getNaveAtual().getSpecificImpulse() * CONST_GRAV;
		double massaTotal = getNaveAtual().getMass();
		double massaSeca = massaTotal / Math.exp(noDeManobra.getDeltaV() / isp);
		double taxaDeQueima = empuxo / isp;
		double duracaoDaQueima = (massaTotal - massaSeca) / taxaDeQueima;

		setCurrentStatus("Tempo de Queima da Manobra: " + duracaoDaQueima + " segundos");
		return duracaoDaQueima;
	}

	public void executeBurn(Node noDeManobra, double duracaoDaQueima) {
		try {
			double inicioDaQueima = noDeManobra.getTimeTo() - (duracaoDaQueima / 2.0) - (fineAdjustment ? 5 : 0);
			setCurrentStatus(Bundle.getString("status_maneuver_warp"));
			if (inicioDaQueima > 30) {
				getSpaceCenter().warpTo((getSpaceCenter().getUT() + inicioDaQueima - 10), 100000, 4);
			}
			// Mostrar tempo de ignição:
			setCurrentStatus(String.format(Bundle.getString("status_maneuver_duration"), duracaoDaQueima));
			while (inicioDaQueima > 0) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				inicioDaQueima = Math.max(noDeManobra.getTimeTo() - (duracaoDaQueima / 2.0), 0.0);
				navigation.aimAtManeuver(noDeManobra);
				setCurrentStatus(String.format(Bundle.getString("status_maneuver_ignition_in"), inicioDaQueima));
				Thread.sleep(100);
			}
			// Executar a manobra:
			Stream<Triplet<Double, Double, Double>> remainingBurn = getConnection().addStream(noDeManobra,
					"remainingBurnVector", noDeManobra.getReferenceFrame());
			setCurrentStatus(Bundle.getString("status_maneuver_executing"));
			while (noDeManobra != null) {
				double burnDvLeft = remainingBurn.get().getValue1();
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				if (burnDvLeft < (fineAdjustment ? 2 : 0.5)) {
					throttle(0.0f);
					break;
				}
				navigation.aimAtManeuver(noDeManobra);
				float limitValue = burnDvLeft > 100 ? 1000 : 100;
				throttle(ctrlManeuver.calcPID((noDeManobra.getDeltaV() - Math.floor(burnDvLeft)) /
						noDeManobra.getDeltaV() * limitValue, limitValue));
				Thread.sleep(50);
			}
			throttle(0.0f);
			if (fineAdjustment) {
				adjustManeuverWithRCS(remainingBurn);
			}
			ap.setReferenceFrame(surfaceReferenceFrame);
			ap.disengage();
			getNaveAtual().getControl().setSAS(true);
			getNaveAtual().getControl().setRCS(false);
			remainingBurn.remove();
			noDeManobra.remove();
			setCurrentStatus(Bundle.getString("status_ready"));
		} catch (StreamException | RPCException e) {
			setCurrentStatus(Bundle.getString("status_data_unavailable"));
		} catch (InterruptedException e) {
			setCurrentStatus(Bundle.getString("status_maneuver_cancelled"));
		}
	}

	private void adjustManeuverWithRCS(Stream<Triplet<Double, Double, Double>> remainingDeltaV) throws RPCException,
			StreamException, InterruptedException {
		getNaveAtual().getControl().setRCS(true);
		while (Math.floor(remainingDeltaV.get().getValue1()) > 0.2) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			getNaveAtual().getControl().setForward((float) ctrlRCS.calcPID(-remainingDeltaV.get().getValue1() * 10,
					0));
			Thread.sleep(25);
		}
		getNaveAtual().getControl().setForward(0);
	}

	private boolean canFineAdjust(String string) {
		if (string.equals("true")) {
			try {
				List<RCS> rcsEngines = getNaveAtual().getParts().getRCS();
				if (rcsEngines.size() > 0) {
					for (RCS rcs : rcsEngines) {
						if (rcs.getHasFuel()) {
							return true;
						}
					}
				}
				return false;
			} catch (RPCException ignored) {
			}
		}
		return false;
	}

	enum Compare {
		INC, AP, PE
	}

}
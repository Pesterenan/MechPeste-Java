package com.pesterenan.controllers;

import static com.pesterenan.MechPeste.getConnection;
import static com.pesterenan.MechPeste.getSpaceCenter;

import java.util.List;
import java.util.Map;

import org.javatuples.Triplet;

import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Attributes;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Module;
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

public class ManeuverController extends Controller {

	public final static float CONST_GRAV = 9.81f;
	private ControlePID ctrlRCS;
	private ControlePID ctrlManeuver;
	private Navigation navigation;
	private boolean fineAdjustment;
	private double lowOrbitAltitude;

	public ManeuverController(Map<String, String> commands) {
		super();
		this.commands = commands;
		this.navigation = new Navigation(getActiveVessel());
		initializeParameters();
	}

	private void initializeParameters() {
		ctrlRCS = new ControlePID(getSpaceCenter(), 25);
		ctrlManeuver = new ControlePID(getSpaceCenter(), 25);
		ctrlManeuver.setPIDValues(1, 0.001, 0.1);
		ctrlRCS.setOutput(0.5, 1.0);
		fineAdjustment = canFineAdjust(commands.get(Module.FINE_ADJUST.get()));
		try {
			lowOrbitAltitude = new Attributes().getLowOrbitAltitude(currentBody.getName());
			System.out.println("lowOrbitAltitude: " + lowOrbitAltitude);
		} catch (RPCException e) {
		}
	}

	@Override
	public void run() {
		calculateManeuver();
		if (!(commands.get(Module.FUNCTION.get()).equals(Module.RENDEZVOUS.get())
				|| commands.get(Module.FUNCTION.get()).equals(Module.LOW_ORBIT.get())
				|| commands.get(Module.FUNCTION.get()).equals(Module.ADJUST.get()))) {
			executeNextManeuver();
		}
	}

	private Node biEllipticTransferToOrbit(double targetAltitude, double timeToStart) {
		double[] totalDv = { 0, 0, 0 };
		try {
			Orbit currentOrbit = getActiveVessel().getOrbit();
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
			if (commands.get(Module.FUNCTION.get()).equals(Module.EXECUTE.get())) {
				return;
			}
			if (getActiveVessel().getSituation() == VesselSituation.LANDED ||
					getActiveVessel().getSituation() == VesselSituation.SPLASHED) {
				throw new InterruptedException();
			}
			if (commands.get(Module.FUNCTION.get()).equals(Module.ADJUST.get())) {
				this.alignPlanesWithTargetVessel();
				return;
			}
			if (commands.get(Module.FUNCTION.get()).equals(Module.RENDEZVOUS.get())) {
				this.rendezvousWithTargetVessel();
				return;
			}
			if (commands.get(Module.FUNCTION.get()).equals(Module.LOW_ORBIT.get())) {
				biEllipticTransferToOrbit(lowOrbitAltitude, getActiveVessel().getOrbit().getTimeToPeriapsis());
				return;
			}
			double gravParameter = currentBody.getGravitationalParameter();
			double startingAltitutde = 0, timeUntilAltitude = 0;
			if (commands.get(Module.FUNCTION.get()).equals(Module.APOAPSIS.get())) {
				startingAltitutde = getActiveVessel().getOrbit().getApoapsis();
				timeUntilAltitude = getActiveVessel().getOrbit().getTimeToApoapsis();
			}
			if (commands.get(Module.FUNCTION.get()).equals(Module.PERIAPSIS.get())) {
				startingAltitutde = getActiveVessel().getOrbit().getPeriapsis();
				timeUntilAltitude = getActiveVessel().getOrbit().getTimeToPeriapsis();
			}

			double semiMajorAxis = getActiveVessel().getOrbit().getSemiMajorAxis();
			double currentOrbitalVelocity = Math.sqrt(gravParameter * ((2.0 / startingAltitutde) - (1.0 / semiMajorAxis)));
			double targetOrbitalVelocity = Math.sqrt(gravParameter * ((2.0 / startingAltitutde) - (1.0 / startingAltitutde)));
			double maneuverDeltaV = targetOrbitalVelocity - currentOrbitalVelocity;
			double[] deltaV = { maneuverDeltaV, 0, 0 };
			createManeuver(timeUntilAltitude, deltaV);
		} catch (RPCException | InterruptedException e) {
			setCurrentStatus(Bundle.getString("status_maneuver_not_possible"));
		}
	}

	public void matchOrbitApoapsis() {
		try {
			Orbit targetOrbit = getTargetOrbit();
			System.out.println(targetOrbit.getApoapsis() + "-- APO");
			Node maneuver = biEllipticTransferToOrbit(targetOrbit.getApoapsis(),
					getActiveVessel().getOrbit().getTimeToPeriapsis());
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
				double ctrlOutput = ctrlManeuver.calculate(currentDeltaApo, 0);

				maneuver.setPrograde(dvPrograde - (ctrlOutput));
				Thread.sleep(25);
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
		Orbit vesselOrbit = getActiveVessel().getOrbit();
		double ascendingNode = vesselOrbit.trueAnomalyAtAN(targetOrbit);
		double descendingNode = vesselOrbit.trueAnomalyAtDN(targetOrbit);
		return new double[] { vesselOrbit.uTAtTrueAnomaly(ascendingNode), vesselOrbit.uTAtTrueAnomaly(descendingNode) };
	}

	private void alignPlanesWithTargetVessel() {
		try {
			Vessel vessel = getActiveVessel();
			Orbit vesselOrbit = getActiveVessel().getOrbit();
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
			ctrlManeuver.setTimeSample(25);
			while (currentInclination > 0.05) {
				currentInclination = Math
						.toDegrees(currentManeuver.getOrbit().relativeInclination(targetVesselOrbit));
				double ctrlOutput = ctrlManeuver.calculate(currentInclination * 100, 0);
				currentManeuver.setNormal(currentManeuver.getNormal() + (closestIsAN ? ctrlOutput : -ctrlOutput));
				Thread.sleep(25);
			}
		} catch (Exception err) {
			System.err.println(err);
		}
	}

	private void rendezvousWithTargetVessel() {
		try {
			boolean hasManeuverNodes = getActiveVessel().getControl().getNodes().size() > 0;
			List<Node> currentManeuvers = getActiveVessel().getControl().getNodes();
			Node lastManeuverNode;
			double lastManeuverNodeUT = 60;
			if (hasManeuverNodes) {
				currentManeuvers = getActiveVessel().getControl().getNodes();
				lastManeuverNode = currentManeuvers.get(currentManeuvers.size() - 1);
				lastManeuverNodeUT += lastManeuverNode.getUT();
				RunManeuverJPanel.createManeuver(lastManeuverNodeUT);
			} else {
				RunManeuverJPanel.createManeuver();
			}
			currentManeuvers = getActiveVessel().getControl().getNodes();
			lastManeuverNode = currentManeuvers.get(currentManeuvers.size() - 1);

			Orbit activeVesselOrbit = getActiveVessel().getOrbit();
			Orbit targetVesselOrbit = getSpaceCenter().getTargetVessel().getOrbit();
			ReferenceFrame currentBodyRefFrame = activeVesselOrbit.getBody().getNonRotatingReferenceFrame();

			double angularDiff = 10;
			while (angularDiff >= 0.005) {
				double maneuverUT = lastManeuverNode.getUT();
				double targetOrbitPosition = new Vector(
						targetVesselOrbit.positionAt(maneuverUT, currentBodyRefFrame))
						.magnitude();
				double maneuverAP = lastManeuverNode.getOrbit().getApoapsis();
				double maneuverPE = lastManeuverNode.getOrbit().getPeriapsis();
				ctrlManeuver.setPIDValues(0.25, 0.0, 0.01);
				ctrlManeuver.setOutput(-100, 100);

				if (targetOrbitPosition < maneuverPE) {
					while (Math.floor(targetOrbitPosition) != Math.floor(maneuverPE)) {
						lastManeuverNode.setPrograde(
								lastManeuverNode.getPrograde()
										+ ctrlManeuver.calculate(maneuverPE / targetOrbitPosition * 1000, 1000));
						maneuverPE = lastManeuverNode.getOrbit().getPeriapsis();
						Thread.sleep(25);
					}
				}

				if (targetOrbitPosition > maneuverAP) {
					while (Math.floor(targetOrbitPosition) != Math.floor(maneuverAP)) {
						lastManeuverNode.setPrograde(
								lastManeuverNode.getPrograde()
										+ ctrlManeuver.calculate(maneuverAP / targetOrbitPosition * 1000, 1000));
						maneuverAP = lastManeuverNode.getOrbit().getApoapsis();
						Thread.sleep(25);
					}
				}
				angularDiff = calculatePhaseAngle(lastManeuverNode.getOrbit().positionAt(maneuverUT, currentBodyRefFrame),
						getSpaceCenter().getTargetVessel().getOrbit().positionAt(maneuverUT, currentBodyRefFrame));
				maneuverUT = lastManeuverNode.getUT();
				lastManeuverNode.setUT(
						lastManeuverNode.getUT()
								+ ctrlManeuver.calculate(-angularDiff * 100, 0));
				System.out.println(angularDiff);
				Thread.sleep(25);
			}
			// double mu = currentBody.getGravitationalParameter();
			// double time = 1000;
			//
			// double hohmannTransferDistance =
			// lastManeuverNode.getOrbit().getSemiMajorAxis();
			// double timeOfFlight = Math.PI * Math.sqrt(Math.pow(hohmannTransferDistance,
			// 3) / mu);
			// double angle = activeVesselOrbit.getMeanAnomalyAtEpoch();
			// double omegaInterceptor = Math
			// .sqrt(mu /
			// Math.pow(activeVesselOrbit.radiusAt(getSpaceCenter().getUT()), 3));
			// // rad/s
			// double omegaTarget = Math.sqrt(mu /
			// Math.pow(targetVesselOrbit.radiusAt(getSpaceCenter().getUT()), 3)); // rad/s
			// // double leadAngle = omegaTarget * timeOfFlight; // rad
			// double leadAngle = targetVesselOrbit.getMeanAnomalyAtEpoch(); // rad
			// double phaseAngle = Math.PI - leadAngle; // rad
			// double calcAngle = (phaseAngle - angle);
			// calcAngle = calcAngle < 0 ? calcAngle + (Math.PI * 2) : calcAngle;
			// double waitTime = calcAngle / (omegaTarget - omegaInterceptor);
			// time = waitTime;
			//
			// lastManeuverNode.setUT(getSpaceCenter().getUT() + time);
			// ctrlManeuver.setOutput(-100, 100);
			// ctrlManeuver.setPIDValues(0.05, 0.1, 0.01);
			// double closestApproach =
			// lastManeuverNode.getOrbit().distanceAtClosestApproach(targetVesselOrbit);
			// System.out.println(closestApproach);
			// System.out.println("Ajustando tempo de Rendezvous...");
			// while (Math.round(closestApproach) > 100) {
			// if (closestApproach < 100000) {
			// ctrlManeuver.setOutput(-10, 10);
			// } else if (closestApproach < 10000) {
			// ctrlManeuver.setOutput(-1, 1);
			// } else {
			// ctrlManeuver.setOutput(-100, 100);
			// }
			// maneuverUT = ctrlManeuver.calculate(-closestApproach, 0);
			// lastManeuverNode.setUT(lastManeuverNode.getUT() + maneuverUT);
			// System.out.println("Closest " + (closestApproach));
			// closestApproach =
			// targetVesselOrbit.distanceAtClosestApproach(lastManeuverNode.getOrbit());
			// Thread.sleep(25);
			// }
			// lastManeuverNode.setUT(lastManeuverNode.getUT() -
			// lastManeuverNode.getOrbit().getPeriod() / 2);
		} catch (

		Exception err) {
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
			getActiveVessel().getControl()
					.addNode(getSpaceCenter().getUT() + laterTime, (float) deltaV[0], (float) deltaV[1],
							(float) deltaV[2]);
			List<Node> currentNodes = getActiveVessel().getControl().getNodes();
			maneuverNode = currentNodes.get(currentNodes.size() - 1);
		} catch (UnsupportedOperationException | RPCException e) {
			setCurrentStatus(Bundle.getString("status_maneuver_not_possible"));
		}
		return maneuverNode;
	}

	public void executeNextManeuver() {
		try {
			Node maneuverNode = getActiveVessel().getControl().getNodes().get(0);
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

	public double calculateBurnTime(Node maneuverNode) throws RPCException {

		List<Engine> engines = getActiveVessel().getParts().getEngines();
		for (Engine engine : engines) {
			if (engine.getPart().getStage() == getActiveVessel().getControl().getCurrentStage() && !engine.getActive()) {
				engine.setActive(true);
			}
		}
		double thrust = getActiveVessel().getAvailableThrust();
		double isp = getActiveVessel().getSpecificImpulse() * CONST_GRAV;
		double totalMass = getActiveVessel().getMass();
		double dryMass = totalMass / Math.exp(maneuverNode.getDeltaV() / isp);
		double burnRatio = thrust / isp;
		double burnDuration = (totalMass - dryMass) / burnRatio;

		setCurrentStatus("Tempo de Queima da Manobra: " + burnDuration + " segundos");
		return burnDuration;
	}

	public void executeBurn(Node maneuverNode, double burnDuration) {
		try {
			double burnStartTime = maneuverNode.getTimeTo() - (burnDuration / 2.0) - (fineAdjustment ? 5 : 0);
			setCurrentStatus(Bundle.getString("status_maneuver_warp"));
			if (burnStartTime > 30) {
				getSpaceCenter().warpTo((getSpaceCenter().getUT() + burnStartTime - 10), 100000, 4);
			}
			// Mostrar tempo de ignição:
			setCurrentStatus(String.format(Bundle.getString("status_maneuver_duration"), burnDuration));
			while (burnStartTime > 0) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				burnStartTime = Math.max(maneuverNode.getTimeTo() - (burnDuration / 2.0), 0.0);
				navigation.aimAtManeuver(maneuverNode);
				setCurrentStatus(String.format(Bundle.getString("status_maneuver_ignition_in"), burnStartTime));
				Thread.sleep(100);
			}
			// Executar a manobra:
			Stream<Triplet<Double, Double, Double>> remainingBurn = getConnection().addStream(maneuverNode,
					"remainingBurnVector", maneuverNode.getReferenceFrame());
			setCurrentStatus(Bundle.getString("status_maneuver_executing"));
			while (maneuverNode != null) {
				double burnDvLeft = remainingBurn.get().getValue1();
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				if (burnDvLeft < (fineAdjustment ? 2 : 0.5)) {
					throttle(0.0f);
					break;
				}
				navigation.aimAtManeuver(maneuverNode);
				float limitValue = burnDvLeft > 100 ? 1000 : 100;
				throttle(ctrlManeuver.calculate((maneuverNode.getDeltaV() - Math.floor(burnDvLeft)) /
						maneuverNode.getDeltaV() * limitValue, limitValue));
				Thread.sleep(25);
			}
			throttle(0.0f);
			if (fineAdjustment) {
				adjustManeuverWithRCS(remainingBurn);
			}
			ap.setReferenceFrame(surfaceReferenceFrame);
			ap.disengage();
			getActiveVessel().getControl().setSAS(true);
			getActiveVessel().getControl().setRCS(false);
			remainingBurn.remove();
			maneuverNode.remove();
			setCurrentStatus(Bundle.getString("status_ready"));
		} catch (StreamException | RPCException e) {
			setCurrentStatus(Bundle.getString("status_data_unavailable"));
		} catch (InterruptedException e) {
			setCurrentStatus(Bundle.getString("status_maneuver_cancelled"));
		}
	}

	private void adjustManeuverWithRCS(Stream<Triplet<Double, Double, Double>> remainingDeltaV) throws RPCException,
			StreamException, InterruptedException {
		getActiveVessel().getControl().setRCS(true);
		while (Math.floor(remainingDeltaV.get().getValue1()) > 0.2) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			getActiveVessel().getControl().setForward((float) ctrlRCS.calculate(-remainingDeltaV.get().getValue1() * 10,
					0));
			Thread.sleep(25);
		}
		getActiveVessel().getControl().setForward(0);
	}

	private boolean canFineAdjust(String string) {
		if (string.equals("true")) {
			try {
				List<RCS> rcsEngines = getActiveVessel().getParts().getRCS();
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

	private double calculatePhaseAngle(Triplet<Double, Double, Double> startPos, Triplet<Double, Double, Double> endPos)
			throws RPCException, InterruptedException {
		double targetPhaseAngle = 10;
		double angularDifference = 15;
		Vector startPosition = new Vector(startPos);
		Vector endPosition = new Vector(endPos);

		// Phase angle
		double dot = endPosition.dotProduct(startPosition);
		double det = endPosition.determinant(startPosition);
		targetPhaseAngle = Math.atan2(det, dot);

		double targetOrbit = endPosition.magnitude();

		double activeVesselSMA = getActiveVessel().getOrbit().getSemiMajorAxis();
		angularDifference = targetPhaseAngle + Math.PI
				* (1 - (1 / (2 * Math.sqrt(2))) * Math.sqrt(Math.pow((activeVesselSMA / targetOrbit + 1), 3)));

		return Math.abs(angularDifference);
	}

	enum Compare {
		INC, AP, PE
	}

}

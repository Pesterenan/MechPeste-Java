package com.pesterenan.controllers;

import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Navigation;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Orbit;
import krpc.client.services.SpaceCenter.RCS;
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
		lowOrbitAltitude = calculateSafeLowOrbitAltitude();
	}

	@Override
	public void run() {
		calculateManeuver();
		executeNextManeuver();
	}

	private double calculateSafeLowOrbitAltitude() {
		final double safeAltitude = 20000;
		double bodyRadius = 0, atmosphereDepth = 0;
		try {
			bodyRadius = currentBody.getEquatorialRadius();
			atmosphereDepth = currentBody.getAtmosphereDepth();
		} catch (RPCException ignored) {
		}
		return bodyRadius + (atmosphereDepth > 0 ? atmosphereDepth + safeAltitude : safeAltitude);
	}

	public void calculateManeuver() {
		try {
			tuneAutoPilot();
			System.out.println(commands);
			if (commands.get(Modulos.FUNCAO.get()).equals(Modulos.EXECUTAR.get())) {
				return;
			}
			if (getNaveAtual().getSituation() == VesselSituation.LANDED ||
					getNaveAtual().getSituation() == VesselSituation.SPLASHED) {
				throw new InterruptedException();
			}
			if (commands.get(Modulos.FUNCAO.get()).equals(Modulos.AJUSTAR.get())) {
				this.alignPlanes();
				return;
			}
			if (commands.get(Modulos.FUNCAO.get()).equals(Modulos.ORBITA_BAIXA.get())) {
				hohmannTransferToOrbit(lowOrbitAltitude, getNaveAtual().getOrbit().getTimeToPeriapsis());
				hohmannTransferToOrbit(lowOrbitAltitude, getNaveAtual().getOrbit().getTimeToPeriapsis());
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
			Node maneuver = hohmannTransferToOrbit(targetOrbit.getApoapsis(),
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

	private Node hohmannTransferToOrbit(double targetAltitude, double timeToStart) {
		double[] totalDv = { 0, 0, 0 };
		try {
			double startingRadius = Math.max(getNaveAtual().getOrbit().getApoapsis(),
					getNaveAtual().getOrbit().getPeriapsis());
			System.out.println(startingRadius + " --- " + targetAltitude);
			double gravParameter = currentBody.getGravitationalParameter();
			// Delta-v required to leave the current orbit
			double deltaV1 = Math.sqrt(2 * gravParameter / startingRadius) - Math.sqrt(gravParameter / startingRadius);
			// Delta-v required to enter the target orbit
			double deltaV2 = Math.sqrt(gravParameter / targetAltitude) - Math.sqrt(2 * gravParameter / targetAltitude);
			System.out.println(deltaV1);
			System.out.println(deltaV2);

			// Dv taken between the two points
			totalDv[0] = deltaV2 + deltaV1;
		} catch (RPCException e) {
		}
		return createManeuver(timeToStart, totalDv);
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

	public void alignPlanes() {
		try {
			Orbit targetOrbit = getTargetOrbit();
			Node maneuver = createManeuverAtClosestIncNode(targetOrbit);
			double[] incNodesUt = getTimeToIncNodes(targetOrbit);
			boolean closestIsAN = incNodesUt[0] < incNodesUt[1];
			double timeToExecute = 0;
			while (timeToExecute < 5000) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				double currentDeltaInc = compareOrbitParameter(maneuver.getOrbit(), targetOrbit, Compare.INC);
				String deltaIncFormatted = String.format("%.2f", currentDeltaInc);
				System.out.println(deltaIncFormatted);
				if (deltaIncFormatted.equals(String.format("%.2f", 10.00))) {
					break;
				}
				double dvNormal = maneuver.getNormal();
				double ctrlOutput = ctrlManeuver.calcPID(currentDeltaInc, 10.0);// * limitPIDOutput(Math.abs
				// (currentDeltaInc));
				if ((closestIsAN ? currentDeltaInc : -currentDeltaInc) > 0.0) {
					maneuver.setNormal(dvNormal + (ctrlOutput));
				} else {
					maneuver.setNormal(dvNormal - (ctrlOutput));
				}
				timeToExecute += 25;
				Thread.sleep(25);
			}
		} catch (Exception e) {
			setCurrentStatus("Não foi possivel ajustar a inclinação");
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
			Stream<Triplet<Double, Double, Double>> queimaRestante = getConnection().addStream(noDeManobra,
					"remainingBurnVector", noDeManobra.getReferenceFrame());
			setCurrentStatus(Bundle.getString("status_maneuver_executing"));
			while (noDeManobra != null) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				if (queimaRestante.get().getValue1() < (fineAdjustment ? 2 : 0.5)) {
					throttle(0.0f);
					break;
				}
				navigation.aimAtManeuver(noDeManobra);
				throttle(ctrlManeuver.calcPID((noDeManobra.getDeltaV() - Math.floor(queimaRestante.get().getValue1())) /
						noDeManobra.getDeltaV() * 1000, 1000));
				Thread.sleep(50);
			}
			throttle(0.0f);
			if (fineAdjustment) {
				adjustManeuverWithRCS(queimaRestante);
			}
			ap.setReferenceFrame(surfaceReferenceFrame);
			ap.disengage();
			getNaveAtual().getControl().setSAS(true);
			getNaveAtual().getControl().setRCS(false);
			queimaRestante.remove();
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
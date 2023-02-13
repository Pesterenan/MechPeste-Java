package com.pesterenan.controllers;

import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.PathFinding;
import com.pesterenan.utils.Utilities;
import com.pesterenan.utils.Vector;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.SolarPanel;
import krpc.client.services.SpaceCenter.SolarPanelState;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.pesterenan.MechPeste.getSpaceCenter;

public class RoverController extends Controller {
	private final ControlePID sterringCtrl = new ControlePID();
	private final ControlePID acelCtrl = new ControlePID();
	float distanceFromTargetLimit = 50;
	private float maxSpeed = 3;
	private ReferenceFrame roverReferenceFrame;
	private boolean isAutoRoverRunning;
	private PathFinding pathFinding;
	private Vector targetPoint = new Vector();
	private Vector roverDirection = new Vector();
	private MODE currentMode;

	public RoverController(Map<String, String> commands) {
		super();
		this.commands = commands;
		initializeParameters();
	}

	private void initializeParameters() {
		try {
			maxSpeed = Float.parseFloat(commands.get(Modulos.VELOCIDADE_MAX.get()));
			roverReferenceFrame = getNaveAtual().getReferenceFrame();
			roverDirection = new Vector(getNaveAtual().direction(roverReferenceFrame));
			pathFinding = new PathFinding();
			acelCtrl.adjustOutput(0, 1);
			sterringCtrl.adjustOutput(-1, 1);
			isAutoRoverRunning = true;
		} catch (RPCException ignored) {
		}
	}

	private boolean isSolarPanelNotBroken(SolarPanel sp) {
		try {
			return sp.getState() != SolarPanelState.BROKEN;
		} catch (RPCException e) {
			return false;
		}
	}

	@Override
	public void run() {
		if (commands.get(Modulos.MODULO.get()).equals(Modulos.MODULO_ROVER.get())) {
			setTarget();
			driveRoverToTarget();
		}
	}

	private void setTarget() {
		try {
			if (commands.get(Modulos.TIPO_ALVO_ROVER.get()).equals(Modulos.MARCADOR_MAPA.get())) {
				pathFinding.addWaypointsOnSameBody(commands.get(Modulos.NOME_MARCADOR.get()));
				setCurrentStatus("Calculando rota até o alvo...");
				pathFinding.buildPathToTarget(pathFinding.findNearestWaypoint());
			}
			if (commands.get(Modulos.TIPO_ALVO_ROVER.get()).equals(Modulos.NAVE_ALVO.get())) {
				Vector targetVesselPosition = new Vector(
						getSpaceCenter().getTargetVessel().position(orbitalReferenceFrame));
				setCurrentStatus("Calculando rota até o alvo...");
				pathFinding.buildPathToTarget(targetVesselPosition);
			}
		} catch (RPCException | IOException | InterruptedException ignored) {
		}
	}

	private void changeControlMode() throws RPCException, IOException, StreamException, InterruptedException {
		switch (currentMode) {
			case DRIVE:
				driveRover();
				break;
			case CHARGING:
				rechargeRover();
				break;
			case NEXT_POINT:
				setNextPointInPath();
				break;
		}
	}

	private void driveRoverToTarget() {
		currentMode = MODE.NEXT_POINT;
		try {
			while (isAutoRoverRunning) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				changeControlMode();
				if (isFarFromTarget()) {
					currentMode = needToChargeBatteries() ? MODE.CHARGING : MODE.DRIVE;
				} else { // Rover arrived at destiny
					currentMode = MODE.NEXT_POINT;
				}
				Thread.sleep(100);
			}
		} catch (InterruptedException | RPCException | IOException | StreamException ignored) {
			try {
				getNaveAtual().getControl().setBrakes(true);
				pathFinding.removeDrawnPath();
				isAutoRoverRunning = false;
				setCurrentStatus(Bundle.getString("lbl_stat_ready"));
			} catch (RPCException ignored2) {
			}
		}
	}

	private void setNextPointInPath() throws RPCException, IOException, InterruptedException {
		pathFinding.removePathsCurrentPoint();
		getNaveAtual().getControl().setBrakes(true);
		if (pathFinding.isPathToTargetEmpty()) {
			if (commands.get(Modulos.TIPO_ALVO_ROVER.get()).equals(Modulos.MARCADOR_MAPA.get())) {
				pathFinding.removeWaypointFromList();
				if (pathFinding.isWaypointsToReachEmpty()) {
					throw new InterruptedException();
				}
				pathFinding.buildPathToTarget(pathFinding.findNearestWaypoint());
			}

		} else {
			targetPoint = pathFinding.getPathsFirstPoint();
		}
	}

	private boolean isFarFromTarget() throws RPCException {
		double distance = Vector.distance(new Vector(getNaveAtual().position(orbitalReferenceFrame)), targetPoint);
		return distance > distanceFromTargetLimit;
	}

	private boolean needToChargeBatteries() throws RPCException, IOException, StreamException, InterruptedException {
		float totalCharge = getNaveAtual().getResources().max("ElectricCharge");
		float currentCharge = getNaveAtual().getResources().amount("ElectricCharge");
		float minChargeLevel = 10.0f;
		float chargePercentage = (float) Math.ceil(currentCharge * 100 / totalCharge);
		return (chargePercentage < minChargeLevel);
	}

	private void rechargeRover() throws RPCException, StreamException, InterruptedException {

		float totalCharge = getNaveAtual().getResources().max("ElectricCharge");
		float currentCharge = getNaveAtual().getResources().amount("ElectricCharge");

		setRoverThrottle(0);
		getNaveAtual().getControl().setLights(false);
		getNaveAtual().getControl().setBrakes(true);

		if (velHorizontal.get() < 1 && getNaveAtual().getControl().getBrakes()) {
			Thread.sleep(1000);
			double chargeTime;
			double totalEnergyFlow = 0;
			List<SolarPanel> solarPanels = getNaveAtual().getParts()
					.getSolarPanels()
					.stream()
					.filter(this::isSolarPanelNotBroken)
					.collect(Collectors.toList());

			for (SolarPanel sp : solarPanels) {
				totalEnergyFlow += sp.getEnergyFlow();
			}
			chargeTime = ((totalCharge - currentCharge) / totalEnergyFlow);
			setCurrentStatus("Segundos de Carga: " + chargeTime);
			if (chargeTime < 1 || chargeTime > 21600) {
				chargeTime = 3600;
			}
			getSpaceCenter().warpTo((getSpaceCenter().getUT() + chargeTime), 10000, 4);
			getNaveAtual().getControl().setLights(true);
		}
	}

	private void driveRover() throws RPCException, IOException, StreamException {
		Vector targetDirection = posSurfToRover(posOrbToSurf(targetPoint)).normalize();
		Vector radarSourcePosition = posRoverToSurf(
				new Vector(getNaveAtual().position(roverReferenceFrame)).sum(new Vector(0.0, 3.0,
						0.0)));

		double roverAngle = (roverDirection.heading());
		// fazer um raycast pra frente e verificar a distancia
		double obstacleAhead = pathFinding.raycastDistance(radarSourcePosition, transformDirection(roverDirection),
				surfaceReferenceFrame, 30);
		double steeringPower = Utilities.remap(3, 30, 0.1, 0.5, obstacleAhead, true);
		// usar esse valor pra muiltiplicar a direcao alvo
		double targetAndRadarAngle = (targetDirection.multiply(steeringPower)
				.sum(directionFromRadar(
						getNaveAtual().boundingBox(roverReferenceFrame)))
				.normalize()).heading();
		double deltaAngle = Math.abs(targetAndRadarAngle - roverAngle);
		getNaveAtual().getControl().setSAS(deltaAngle < 1);
		// Control Rover Throttle
		setRoverThrottle(acelCtrl.calcPID(velHorizontal.get() / maxSpeed * 50, 50));
		// Control Rover Steering
		if (deltaAngle > 1) {
			setRoverSteering(sterringCtrl.calcPID(roverAngle / (targetAndRadarAngle) * 100, 100));
		} else {
			setRoverSteering(0.0f);
		}
		setCurrentStatus("Driving... " + deltaAngle);
	}

	private Vector directionFromRadar(
			Pair<Triplet<Double, Double, Double>, Triplet<Double, Double, Double>> boundingBox)
			throws RPCException, IOException {
		// PONTO REF ROVER: X = DIREITA, Y = FRENTE, Z = BAIXO;
		// Bounding box points from rover (LBU: Left, Back, Up - RFD: Right, Front,
		// Down):
		Vector LBU = new Vector(boundingBox.getValue0());
		Vector RFD = new Vector(boundingBox.getValue1());

		// Pre-calculated bbox positions
		Vector lateralEsq = new Vector(LBU.x, LBU.y * 0.5 + RFD.y * 0.5, LBU.z * 0.5 + RFD.z * 0.5);
		Vector latFrontEsq = new Vector(LBU.x, RFD.y * 0.5, LBU.z * 0.5 + RFD.z * 0.5);
		Vector frontalEsq = new Vector(LBU.x, RFD.y, LBU.z * 0.5 + RFD.z * 0.5);
		Vector frontalEsq2 = new Vector(LBU.x * 0.5, RFD.y, LBU.z * 0.5 + RFD.z * 0.5);
		Vector frontal = new Vector(LBU.x * 0.5 + RFD.x * 0.5, RFD.y, LBU.z * 0.5 + RFD.z * 0.5);
		Vector frontalDir2 = new Vector(RFD.x * 0.5, RFD.y, LBU.z * 0.5 + RFD.z * 0.5);
		Vector frontalDir = new Vector(RFD.x, RFD.y, LBU.z * 0.5 + RFD.z * 0.5);
		Vector latFrontDir = new Vector(RFD.x, RFD.y * 0.5, LBU.z * 0.5 + RFD.z * 0.5);
		Vector lateralDir = new Vector(RFD.x, LBU.y * 0.5 + RFD.y * 0.5, LBU.z * 0.5 + RFD.z * 0.5);

		// Pre-calculated bbox directions
		Vector lateralEsqAngulo = new Vector(-Math.sin(Math.toRadians(90)), Math.cos(Math.toRadians(90)), 0.0);
		Vector latFrontEsqAngulo = new Vector(-Math.sin(Math.toRadians(67.5)), Math.cos(Math.toRadians(67.5)), 0.0);
		Vector frontalEsqAngulo = new Vector(-Math.sin(Math.toRadians(45)), Math.cos(Math.toRadians(45)), 0.0);
		Vector frontalEsqAngulo2 = new Vector(-Math.sin(Math.toRadians(22.5)), Math.cos(Math.toRadians(22.5)), 0.0);
		Vector frontalAngulo = new Vector(0.0, 1.0, 0.0);
		Vector frontalDirAngulo2 = new Vector(Math.sin(Math.toRadians(22.5)), Math.cos(Math.toRadians(22.5)), 0.0);
		Vector frontalDirAngulo = new Vector(Math.sin(Math.toRadians(45)), Math.cos(Math.toRadians(45)), 0.0);
		Vector latFrontDirAngulo = new Vector(Math.sin(Math.toRadians(67.5)), Math.cos(Math.toRadians(67.5)), 0.0);
		Vector lateralDirAngulo = new Vector(Math.sin(Math.toRadians(90)), Math.cos(Math.toRadians(90)), 0.0);

		// Raytracing distance from points:
		Vector lateralEsqRay = calculateRaycastDirection(lateralEsq, lateralEsqAngulo, 15);
		Vector latFrontEsqRay = calculateRaycastDirection(latFrontEsq, latFrontEsqAngulo, 19);
		Vector frontalEsqRay = calculateRaycastDirection(frontalEsq, frontalEsqAngulo, 23);
		Vector frontalEsqRay2 = calculateRaycastDirection(frontalEsq2, frontalEsqAngulo2, 27);
		Vector frontalRay = calculateRaycastDirection(frontal, frontalAngulo, 35);
		Vector frontalDirRay2 = calculateRaycastDirection(frontalDir2, frontalDirAngulo2, 27);
		Vector frontalDirRay = calculateRaycastDirection(frontalDir, frontalDirAngulo, 23);
		Vector latFrontDirRay = calculateRaycastDirection(latFrontDir, latFrontDirAngulo, 19);
		Vector lateralDirRay = calculateRaycastDirection(lateralDir, lateralDirAngulo, 15);

		Vector calculatedDirection = new Vector().sum(lateralEsqRay)
				.sum(latFrontEsqRay)
				.sum(frontalEsqRay)
				.sum(frontalEsqRay2)
				.sum(frontalRay)
				.sum(frontalDirRay2)
				.sum(frontalDirRay)
				.sum(latFrontDirRay)
				.sum(lateralDirRay);

		return (calculatedDirection.normalize());
	}

	private Vector calculateRaycastDirection(Vector point, Vector direction, double distance) throws RPCException {
		double raycast = pathFinding.raycastDistance(posRoverToSurf(point), transformDirection(direction),
				surfaceReferenceFrame,
				distance);
		return direction.multiply(raycast);
	}

	private Vector transformDirection(Vector vector) throws RPCException {
		return new Vector(
				getSpaceCenter().transformDirection(vector.toTriplet(), roverReferenceFrame, surfaceReferenceFrame));
	}

	private Vector posSurfToRover(Vector vector) throws RPCException {
		return new Vector(
				getSpaceCenter().transformPosition(vector.toTriplet(), surfaceReferenceFrame, roverReferenceFrame));
	}

	private Vector posRoverToSurf(Vector vector) throws RPCException {
		return new Vector(
				getSpaceCenter().transformPosition(vector.toTriplet(), roverReferenceFrame, surfaceReferenceFrame));
	}

	private Vector posOrbToSurf(Vector vector) throws RPCException {
		return new Vector(
				getSpaceCenter().transformPosition(vector.toTriplet(), orbitalReferenceFrame, surfaceReferenceFrame));
	}

	private void setRoverThrottle(double throttle) throws RPCException, StreamException {
		if (velHorizontal.get() < (maxSpeed * 1.01)) {
			getNaveAtual().getControl().setBrakes(false);
			getNaveAtual().getControl().setWheelThrottle((float) throttle);
		} else {
			getNaveAtual().getControl().setBrakes(true);
		}
	}

	private void setRoverSteering(double steering) throws RPCException {
		getNaveAtual().getControl().setWheelSteering((float) steering);
	}

	private enum MODE {
		DRIVE, NEXT_POINT, CHARGING
	}
}
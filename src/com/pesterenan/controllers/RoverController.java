package com.pesterenan.controllers;

import com.pesterenan.model.ActiveVessel;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.PathFinding;
import com.pesterenan.utils.Utilities;
import com.pesterenan.utils.Vector;
import com.pesterenan.views.StatusJPanel;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.Drawing;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.SolarPanel;
import krpc.client.services.SpaceCenter.SolarPanelState;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoverController extends ActiveVessel implements Runnable {
	private static final int MAX_RADAR_LINES = 9;
	private final ControlePID sterringCtrl = new ControlePID();
	private final ControlePID acelCtrl = new ControlePID();
	private final Map<String, String> commands;
	private final List<Drawing.Line> radarLines = new ArrayList<>();
	float distanceFromTargetLimit = 50;
	float velocidadeCurva = 3;
	private float maxSpeed = 3;
	private ReferenceFrame pontoRefRover;
	private boolean isAutoRoverRunning = true;
	private Drawing drawing;
	private Stream<Float> bateriaAtual;
	private Drawing.Line steeringLine;
	private PathFinding pathFinding;
	private Vector targetPoint = new Vector();
	private Vector roverDirection = new Vector();
	private Drawing.Line dirRover;
	private boolean haveSolarPanels;

	public RoverController(Map<String, String> commands) {
		super(getConexao());
		this.commands = commands;
		initializeParameters();
	}

	private static boolean isSolarPanelNotBroken(SolarPanel sp) {
		try {
			return sp.getState() != SolarPanelState.BROKEN;
		} catch (RPCException e) {
			return false;
		}
	}

	private void initializeParameters() {
		try {
			currentBody = naveAtual.getOrbit().getBody();
			parametrosDeVoo = naveAtual.flight(pontoRefOrbital);
			pontoRefRover = naveAtual.getReferenceFrame();
			velHorizontal = getConexao().addStream(parametrosDeVoo, "getHorizontalSpeed");
			bateriaAtual = getConexao().addStream(naveAtual.getResources(), "amount", "ElectricCharge");
			maxSpeed = Float.parseFloat(commands.get(Modulos.VELOCIDADE_MAX.get()));
			roverDirection = new Vector(naveAtual.direction(pontoRefRover));
			drawing = Drawing.newInstance(getConexao());
			dirRover = drawing.addDirection(roverDirection.toTriplet(), pontoRefRover, 10, true);
			dirRover.setColor(new Triplet<>(1.0, 0.0, 0.0));
			dirRover.setThickness(0.2f);
			steeringLine = drawing.addDirection(roverDirection.toTriplet(), pontoRefRover, 10, true);
			steeringLine.setColor(new Triplet<>(1.0, 0.0, 1.0));
			for (int i = 0; i < MAX_RADAR_LINES; i++) {
				Drawing.Line line = drawing.addDirection(roverDirection.toTriplet(), pontoRefSuperficie, 1, true);
				line.setColor(new Triplet<>(0.0, (1.0 / MAX_RADAR_LINES), 0.0));
				line.setThickness(0.2f);
				radarLines.add(line);
			}
			pathFinding = new PathFinding(getConexao());
			// AJUSTAR CONTROLES PID:
			acelCtrl.adjustOutput(0, 1);
			sterringCtrl.adjustOutput(-1, 1);

		} catch (RPCException | StreamException ignored) {
		}
	}

	@Override
	public void run() {
		if (commands.get(Modulos.MODULO.get()).equals(Modulos.MODULO_ROVER.get())) {
			try {
				setTarget();
				driveRoverToTarget();
				drawing.clear(false);
			} catch (RPCException | StreamException | IOException | InterruptedException e) {
				try {
					drawing.clear(false);
					isAutoRoverRunning = false;
					naveAtual.getControl().setBrakes(true);
				} catch (RPCException ex) {
					throw new RuntimeException(ex);
				}
				disengageAfterException("Rovering cancelled");
			}
		}
	}

	private void setTarget() throws IOException, RPCException, InterruptedException {
		if (commands.get(Modulos.TIPO_ALVO_ROVER.get()).equals(Modulos.MARCADOR_MAPA.get())) {
			pathFinding.addWaypointsOnSameBody(commands.get(Modulos.NOME_MARCADOR.get()));
			pathFinding.buildPathToTarget(pathFinding.findNearestWaypoint());
		}
		if (commands.get(Modulos.TIPO_ALVO_ROVER.get()).equals(Modulos.NAVE_ALVO.get())) {
			Vector targetVesselPosition = new Vector(centroEspacial.getTargetVessel().position(pontoRefOrbital));
			pathFinding.buildPathToTarget(targetVesselPosition);
		}
	}

	private void driveRoverToTarget() throws IOException, RPCException, InterruptedException, StreamException {
		while (isAutoRoverRunning) {
			if (pathFinding.isPathToTargetEmpty()) {
				isAutoRoverRunning = false;
			} else {
				setNextPointInPath();
			}
			if (!needToChargeBatteries()) {
				if (isFarFromTarget()) {
					naveAtual.getControl().setBrakes(false);
					driveRover();
				} else {
					naveAtual.getControl().setBrakes(true);
					pathFinding.removePathsCurrentPoint();
					if (commands.get(Modulos.TIPO_ALVO_ROVER.get()).equals(Modulos.MARCADOR_MAPA.get()) &&
							pathFinding.isPathToTargetEmpty()) {
						pathFinding.removeWaypointFromList();
						pathFinding.findNearestWaypoint();
					}
				}
			} else {
				rechargeRover();
			}
			Thread.sleep(50);
		}
		naveAtual.getControl().setBrakes(true);
		Thread.sleep(1000);
	}

	private void setNextPointInPath() {
		targetPoint = pathFinding.getPathsFirstPoint();
	}

	private boolean isFarFromTarget() throws RPCException {
		double distance = Vector.distance(new Vector(naveAtual.position(pontoRefOrbital)), targetPoint);
		return distance > distanceFromTargetLimit;
	}

	// According to the clean code rules, this function bellow has 2 problems
	// It is doing a lot of different things, functions are suppose to do only one thing
	// This function is doing more than what it's name declare, such functions are hiding places for bugs 
	private boolean needToChargeBatteries() throws RPCException, IOException, StreamException, InterruptedException {
		float totalCharge = naveAtual.getResources().max("ElectricCharge");
		float currentCharge = naveAtual.getResources().amount("ElectricCharge");
		float minChargeLevel = 10.0f;
		float chargePercentage = (float) Math.ceil(currentCharge * 100 / totalCharge);
		if (chargePercentage > minChargeLevel) {
			return false;
		}

		return true;
	}
	
	private void rechargeRover() throws RPCException, StreamException, InterruptedException {
		
		float totalCharge = naveAtual.getResources().max("ElectricCharge");
		float currentCharge = naveAtual.getResources().amount("ElectricCharge");
		
		setRoverThrottle(0);
		naveAtual.getControl().setLights(false);
		naveAtual.getControl().setBrakes(true);
		Thread.sleep(3000); // Give some time for the rover to slow down
		if (velHorizontal.get() < 1 && naveAtual.getControl().getBrakes()) {
			double chargeTime = 0;
			double TotalEnergyFlow = 0;
			List<SolarPanel> solarPanels = naveAtual.getParts()
			                                        .getSolarPanels()
			                                        .stream()
			                                        .filter(RoverController::isSolarPanelNotBroken)
			                                        .collect(Collectors.toList());
			if (solarPanels.isEmpty()) {
				isAutoRoverRunning = false;
				return;
			}
			for (SolarPanel sp : solarPanels) {
				TotalEnergyFlow += sp.getEnergyFlow();
			}
			chargeTime = ((totalCharge - currentCharge) / TotalEnergyFlow);
			StatusJPanel.setStatus("Segundos de Carga: " + chargeTime);
			if (chargeTime < 1 || chargeTime > 21600) {
				chargeTime = 3600;
			}
			centroEspacial.warpTo((centroEspacial.getUT() + chargeTime), 10000, 4);
			naveAtual.getControl().setLights(true);
		}
	}
	
	private boolean detectSolarPanels() throws RPCException {
		List<SolarPanel> solarPanels = naveAtual.getParts()
                .getSolarPanels()
                .stream()
                .filter(RoverController::isSolarPanelNotBroken)
                .collect(Collectors.toList());
		
		if (solarPanels.isEmpty()) {
		return false;
		} else {
			return true;
		}
	}

	private void driveRover() throws IOException, RPCException, StreamException {
		Vector targetDirection = posSurfToRover(posOrbToSurf(targetPoint)).normalize();
		Vector radarSourcePosition =
				posRoverToSurf(new Vector(naveAtual.position(pontoRefRover)).sum(new Vector(0.0, 3.0, 0.0)));

		double roverAngle = (roverDirection.heading());
		// fazer um raycast pra frente e verificar a distancia
		double obstacleAhead =
				pathFinding.raycastDistance(radarSourcePosition, transformDirection(roverDirection),
				                            pontoRefSuperficie,
				                            30
				                           );
		double steeringPower = Utilities.remap(3, 30, 0.1, 0.5, obstacleAhead, true);
		// usar esse valor pra muiltiplicar a direcao alvo
		double targetAndRadarAngle = (targetDirection.multiply(steeringPower)
		                                             .sum(directionFromRadar(naveAtual.boundingBox(pontoRefRover)))
		                                             .normalize()).heading();
		double deltaAngle = Math.abs(targetAndRadarAngle - roverAngle);
		naveAtual.getControl().setSAS(velHorizontal.get() > velocidadeCurva && deltaAngle < 1);
		// Control Rover Throttle
		setRoverThrottle(acelCtrl.calcPID(velHorizontal.get() / maxSpeed * 50, 50));
		// Control Rover Steering
		if (deltaAngle > 1) {
			setRoverSteering(sterringCtrl.calcPID(roverAngle / (targetAndRadarAngle) * 100, 100));
		} else {
			setRoverSteering(0.0f);
		}
	}


	private Vector directionFromRadar(Pair<Triplet<Double, Double, Double>, Triplet<Double, Double, Double>> boundingBox) throws RPCException, IOException {
		// PONTO REF ROVER: X = DIREITA, Y = FRENTE, Z = BAIXO;
		// Bounding box points from rover (LBU: Left, Back, Up - RFD: Right, Front, Down):
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
		Vector lateralEsqRay = calculateRaycastDirection(lateralEsq, lateralEsqAngulo, 20);
		Vector latFrontEsqRay = calculateRaycastDirection(latFrontEsq, latFrontEsqAngulo, 22);
		Vector frontalEsqRay = calculateRaycastDirection(frontalEsq, frontalEsqAngulo, 24);
		Vector frontalEsqRay2 = calculateRaycastDirection(frontalEsq2, frontalEsqAngulo2, 26);
		Vector frontalRay = calculateRaycastDirection(frontal, frontalAngulo, 30);
		Vector frontalDirRay2 = calculateRaycastDirection(frontalDir2, frontalDirAngulo2, 26);
		Vector frontalDirRay = calculateRaycastDirection(frontalDir, frontalDirAngulo, 24);
		Vector latFrontDirRay = calculateRaycastDirection(latFrontDir, latFrontDirAngulo, 22);
		Vector lateralDirRay = calculateRaycastDirection(lateralDir, lateralDirAngulo, 20);

		Drawing.Line line0 = radarLines.get(0);
		line0.setStart(posRoverToSurf(lateralEsq).toTriplet());
		line0.setEnd(posRoverToSurf(lateralEsqRay).toTriplet());
		Drawing.Line line1 = radarLines.get(1);
		line1.setStart(posRoverToSurf(latFrontEsq).toTriplet());
		line1.setEnd(posRoverToSurf(latFrontEsqRay).toTriplet());
		Drawing.Line line2 = radarLines.get(2);
		line2.setStart(posRoverToSurf(frontalEsq).toTriplet());
		line2.setEnd(posRoverToSurf(frontalEsqRay).toTriplet());
		Drawing.Line line3 = radarLines.get(3);
		line3.setStart(posRoverToSurf(frontalEsq2).toTriplet());
		line3.setEnd(posRoverToSurf(frontalEsqRay2).toTriplet());
		Drawing.Line line4 = radarLines.get(4);
		line4.setStart(posRoverToSurf(frontal).toTriplet());
		line4.setEnd(posRoverToSurf(frontalRay).toTriplet());
		Drawing.Line line5 = radarLines.get(5);
		line5.setStart(posRoverToSurf(frontalDir2).toTriplet());
		line5.setEnd(posRoverToSurf(frontalDirRay2).toTriplet());
		Drawing.Line line6 = radarLines.get(6);
		line6.setStart(posRoverToSurf(frontalDir).toTriplet());
		line6.setEnd(posRoverToSurf(frontalDirRay).toTriplet());
		Drawing.Line line7 = radarLines.get(7);
		line7.setStart(posRoverToSurf(latFrontDir).toTriplet());
		line7.setEnd(posRoverToSurf(latFrontDirRay).toTriplet());
		Drawing.Line line8 = radarLines.get(8);
		line8.setStart(posRoverToSurf(lateralDir).toTriplet());
		line8.setEnd(posRoverToSurf(lateralDirRay).toTriplet());


		Vector calculatedDirection = new Vector().sum(lateralEsqRay)
		                                         .sum(latFrontEsqRay)
		                                         .sum(frontalEsqRay)
		                                         .sum(frontalEsqRay2)
		                                         .sum(frontalRay)
		                                         .sum(frontalDirRay2)
		                                         .sum(frontalDirRay)
		                                         .sum(latFrontDirRay)
		                                         .sum(lateralDirRay);

		steeringLine.setReferenceFrame(pontoRefSuperficie);
		steeringLine.setStart(posRoverToSurf(frontal).toTriplet());
		steeringLine.setEnd(
				posRoverToSurf(frontal).sum(transformDirection(calculatedDirection.normalize()).multiply(10))
				                       .toTriplet());
		return (calculatedDirection.normalize());
	}

	private Vector calculateRaycastDirection(Vector point, Vector direction, double distance) throws RPCException {
		double raycast =
				pathFinding.raycastDistance(posRoverToSurf(point), transformDirection(direction), pontoRefSuperficie,
				                            distance
				                           );
		return direction.multiply(raycast);
	}

	private Vector createRadarDirection(double angle) {
		int sign = angle > 90 ? -1 : 1;
		angle = angle >= 90 ? angle - 90 : angle;
		return new Vector(Math.sin(Math.toRadians(angle)) * sign, Math.cos(Math.toRadians(angle)), 0.0);
	}

	private Vector transformDirection(Vector vector) throws RPCException {
		return new Vector(centroEspacial.transformDirection(vector.toTriplet(), pontoRefRover, pontoRefSuperficie));
	}

	private Vector posSurfToRover(Vector vector) throws RPCException {
		return new Vector(centroEspacial.transformPosition(vector.toTriplet(), pontoRefSuperficie, pontoRefRover));
	}

	private Vector posRoverToSurf(Vector vector) throws RPCException {
		return new Vector(centroEspacial.transformPosition(vector.toTriplet(), pontoRefRover, pontoRefSuperficie));
	}

	private Vector posOrbToSurf(Vector vector) throws RPCException {
		return new Vector(centroEspacial.transformPosition(vector.toTriplet(), pontoRefOrbital, pontoRefSuperficie));
	}

	private void setRoverThrottle(double throttle) throws RPCException, StreamException {
		if (velHorizontal.get() < (maxSpeed * 1.01)) {
			naveAtual.getControl().setBrakes(false);
			naveAtual.getControl().setWheelThrottle((float) throttle);
		} else {
			naveAtual.getControl().setBrakes(true);
		}
	}

	private void setRoverSteering(double steering) throws RPCException {
		naveAtual.getControl().setWheelSteering((float) steering);
	}

	private void drawLineBetweenPoints(Vector pointA, Vector pointB) throws RPCException {
		Drawing.Line line = drawing.addLine(posRoverToSurf(pointA).toTriplet(), posRoverToSurf(pointB).toTriplet(),
		                                    pontoRefSuperficie, true
		                                   );
		line.setThickness(0.5f);
		line.setColor(new Triplet<>(1.0, 0.5, 0.0));
	}

}
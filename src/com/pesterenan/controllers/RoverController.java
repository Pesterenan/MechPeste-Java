package com.pesterenan.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.javatuples.Triplet;

import com.pesterenan.utils.PIDcontrol;
import com.pesterenan.utils.Vector;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.SASMode;
import krpc.client.services.SpaceCenter.SolarPanel;
import krpc.client.services.SpaceCenter.SolarPanelState;
import krpc.client.services.SpaceCenter.SpeedMode;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.Waypoint;
import krpc.client.services.SpaceCenter.WaypointManager;

// M�dulo de Piloto autom�tico de Rovers
// Autor: Renan Torres <pesterenan@gmail.com>
// Data: 14/02/2019

public class RoverController {
	private static final int SEARCH_DISTANCE = 4400000;
	// Declara��o de vari�veis:
	static private SpaceCenter spaceCenter;
	WaypointManager markerManager;
	List<Waypoint> markersToFollow = new ArrayList<Waypoint>();
	private List<Vector> followingSpot = new ArrayList<Vector>();
	private List<Triplet<Double, Double, Double>> verticesSpots = new ArrayList<Triplet<Double, Double, Double>>();
	private static Vessel rover, targetShip;
	Waypoint targetMarker;
	private ReferenceFrame roverSpot;
	private ReferenceFrame orbitalRefSpot;
	private ReferenceFrame surfaceRefSpot;
	Flight roverParameters;
	Vector roverPosition, roverAnglePosition, targetPosition, roverDirection, pathDirection;
	double targetAngle = 0, anguloRover = 0;
	float targetDistanceLimit = 100;
	static float maxSpeed = 6;
	float curveSpeed = 3;
	PIDcontrol DirectionControl = new PIDcontrol(), accelerationControl = new PIDcontrol();
	PIDcontrol rollControl = new PIDcontrol(), pitchControl = new PIDcontrol();
	private static String markerName = "target";
	public static boolean searchingMarkers = true;
	private boolean executingAutoRover = true;
	Stream<Double> roverSpeed;
	private int minimunBatteryLevel = 10;
	double totalCharge = 100;
	double currentCharge = 10;

	Vector distanceToTarget;
	int spots;
	private boolean loading;
	private Stream<Double> gameTime;
	private double previousTime;

	public RoverController(Connection connection)
			throws IOException, RPCException, InterruptedException, StreamException {
		startParameters(connection);
		setTarget();
		controlRover();
	}

	private void startParameters(Connection connection) throws RPCException, StreamException {
		spaceCenter = SpaceCenter.newInstance(connection);
		markerManager = spaceCenter.getWaypointManager();
		rover = spaceCenter.getActiveVessel();
		// REFERENCIA PARA BUSCAR ANGULO DE DIRE��O DO ROVER:
		roverSpot = rover.getReferenceFrame();
		// REFERENCIA PARA VELOCIDADE DO ROVER:
		orbitalRefSpot = rover.getOrbit().getBody().getReferenceFrame();
		// REFERENCIA PARA BUSCAR POSICOES DE target:
		surfaceRefSpot = rover.getSurfaceReferenceFrame();
		roverParameters = rover.flight(orbitalRefSpot);
		roverSpeed = connection.addStream(roverParameters, "getHorizontalSpeed");
		gameTime = connection.addStream(spaceCenter.getClass(), "getUT");

		// AJUST CONTROLES PID:
		accelerationControl.ajustPID(0.5, 0.1, 0.01);
		accelerationControl.limitarSaida(0, 1);
		DirectionControl.ajustPID(0.03, 0.05, 0.3);
		DirectionControl.limitarSaida(-1, 1);
		rollControl.ajustPID(0.5, 0.015, 0.5);
		rollControl.limitarSaida(-1, 1);
		pitchControl.ajustPID(0.5, 0.015, 0.5);
		pitchControl.limitarSaida(-1, 1);
		previousTime = gameTime.get();
		antiTipping();
	}

	private void setTarget() throws IOException, RPCException {
		if (searchingMarkers) {
			for (Waypoint marker : markerManager.getWaypoints()) {
				if (marker.getName().contains(markerName)) {
					markersToFollow.add(marker);
				}
			}
			if (markersToFollow.isEmpty()) {
				executingAutoRover = false;
			} else {
				checkDistance();
			}
		} else {
			try {
				targetShip = spaceCenter.getTargetVessel();
				distanceToTarget = new Vector(targetShip.position(surfaceRefSpot));
				generatePathList();
			} catch (NullPointerException e) {
				executingAutoRover = false;
			}
		}
	}

	private void chargeBatteries() throws RPCException, IOException, StreamException, InterruptedException {
		totalCharge = rover.getResources().max("ElectricCharge");
		currentCharge = rover.getResources().amount("ElectricCharge");
		int chargePercentage = (int) Math.ceil(currentCharge * 100 / totalCharge);
		if (chargePercentage > minimunBatteryLevel) {
			loading = false;
		} else {
			loading = true;
			accelerateRover(0);
			rover.getControl().setLights(false);
			rover.getControl().setBrakes(true);
			rover.getControl().setWheelSteering(0.0f);
			if (roverSpeed.get() < 1 && rover.getControl().getBrakes()) {
				Thread.sleep(1000);
				double setCharge = 0;
				List<SolarPanel> panels = new ArrayList<SolarPanel>();
				panels = rover.getParts().getSolarPanels();
				for (Iterator<SolarPanel> iter = panels.iterator(); iter.hasNext();) {
					SolarPanel painel = iter.next();
					if (painel.getState() == SolarPanelState.BROKEN) {
						iter.remove();
					} else {
						setCharge += painel.getEnergyFlow();
					}
				}
				if (panels.isEmpty()) {
					executingAutoRover = false;
				}
				setCharge = ((totalCharge - currentCharge) / setCharge);
				System.out.println("Segundos de Carga: " + setCharge);
				if (setCharge < 1 || setCharge > 21600) {
					setCharge = 3600;
				}
				spaceCenter.warpTo((spaceCenter.getUT() + setCharge), 10000, 4);
				previousTime = gameTime.get();
				rover.getControl().setLights(true);
			}
		}
	}

	private void checkDistance() throws RPCException, IOException {
		double searchDistance = SEARCH_DISTANCE;
		for (Waypoint marker : markersToFollow) {
			double markerDistance = setMarkerPosition(marker).Magnitude3d();
			if (searchDistance > markerDistance) {
				searchDistance = markerDistance;
				targetMarker = marker;
			}
		}
		distanceToTarget = (setMarkerPosition(targetMarker));
		generatePathList();

	}

	private void generatePathList() throws IOException, RPCException {
		// posi��o ultimo spot
		System.out.println("distanceToTarget" + distanceToTarget);
		// dividir distancia at� spot por 1000 para gerar spots intermediarios
		spots = (int) distanceToTarget.Magnitude3d() / 1000;
		System.out.println("spots" + spots);
		// dividir distancia final por spots para conseguir distancia do segmento
		Vector spot = distanceToTarget.divide((double) spots);
		System.out.println("spot" + spot);
		System.out.println();
		// adicionar spot na lista
		followingSpot.add(setVectorPosition(spot));
		System.out.println(followingSpot.get(0));
		for (int i = 1; i < spots; i++) {
			Vector pontoSeguinte = (setVectorPosition(spot.multiply(i)));
			followingSpot.add(pontoSeguinte);
		}
		for (int j = 1; j < followingSpot.size(); j++) {
			verticesSpots.add(setVectorPosition(followingSpot.get(j)).toTriplet());
		}
	}

	private void controlRover() throws IOException, RPCException, InterruptedException, StreamException {
		while (executingAutoRover) {
			try {
				defineDirectionVector();
				antiTipping();
				logData();
			} catch (Exception erro) {
				executingAutoRover = false;
			}
			chargeBatteries();
			if (!loading) {
				if (targetPosition.Magnitude3d() > targetDistanceLimit) {
					if (rover.getControl().getBrakes()) {
						rover.getControl().setBrakes(false);
					}
					accelerateRover(accelerationControl.computePID(roverSpeed.get(), 0));
					pilotRover();
				} else {
					rover.getControl().setBrakes(true);
					if (!followingSpot.isEmpty()) {
						followingSpot.remove(0);
					} else {
						if (!markersToFollow.isEmpty()) {
							if (!targetMarker.getHasContract()) {
								targetMarker.remove();
							}
							markersToFollow.remove(targetMarker);
							checkDistance();
						} else {
							executingAutoRover = false;
						}
					}
				}
			}
			Thread.sleep(250);
		}
		rover.getAutoPilot().disengage();
		roverSpeed.remove();
		Thread.sleep(1000);
	}

	private void accelerateRover(double arg) throws IOException, RPCException, StreamException {
		if (roverSpeed.get() < (maxSpeed * 1.01)) {
			rover.getControl().setBrakes(false);
			rover.getControl().setWheelThrottle((float) arg);
		} else {
			rover.getControl().setBrakes(true);
		}
	}

	private void pilotRover() throws IOException, RPCException, StreamException {
		// Calcular diferen�a de angulo entre o target e o rover
		double angleDifference = Math.abs(targetAngle - anguloRover);
		if (roverSpeed.get() > curveSpeed && angleDifference < 20) {
			try {
				if (rover.getControl().getSpeedMode() == SpeedMode.TARGET) {
					rover.getControl().setSpeedMode(SpeedMode.SURFACE);
				}
				rover.getControl().setSAS(true);
				rover.getControl().setSASMode(SASMode.PROGRADE);
			} catch (Exception e) {
			}
		} else {
			rover.getControl().setSAS(false);
		}

		// Controlar a speed para fazer curvas
		if (angleDifference > 20) {
//			accelerationControl.setLimitePID(curveSpeed);
		} else {
//			accelerationControl.setLimitePID(maxSpeed);
		}
		if (angleDifference > 3) {
			// Dirigir o Rover ao Alvo
//			rover.getControl().setWheelSteering((float) DirectionControl.computePID());
		} else {
			rover.getControl().setWheelSteering(0f);
		}
	}

	private void defineDirectionVector() throws IOException, RPCException {
		// Definir posicao do Alvo, sendo ele um Waypoint, ou um Vessel
		if (!followingSpot.isEmpty()) {
			targetPosition = positionForRover(positionSpot(followingSpot.get(0)));
		} else {
			if (searchingMarkers) {
				targetPosition = positionForRover(setMarkerPosition(targetMarker));
			} else {
				targetPosition = positionForRover(new Vector(targetShip.position(surfaceRefSpot)));
			}
		}

		// Definir a direction do Rover e do Trajeto
		roverDirection = new Vector(rover.direction(roverSpot));
		pathDirection = targetPosition.normalizeVector();
		// Definir o angulo entre os dois
		targetAngle = (Vector.angleDirection(pathDirection));
		anguloRover = (Vector.angleDirection(roverDirection));
//		DirectionControl.setEntradaPID(anguloRover * 0.5);
//		DirectionControl.setLimitePID(targetAngle * 0.5);
	}

	private void antiTipping() throws RPCException {

		// Vetores dire��o para Ponto de Ref Rover:
		// Vector ( -ESQ/DIR , -TRAS/FRENTE , -CIMA/BAIXO)

		Vector leftDir = new Vector(rover.direction(roverSpot)).sum(new Vector(-0.2, -1.0, 0.8));
		Vector rightDir = new Vector(rover.direction(roverSpot)).sum(new Vector(0.2, -1.0, 0.8));
		Vector backwardDir = new Vector(rover.direction(roverSpot)).sum(new Vector(0.0, -1.2, 0.8));
		Vector forwardDir = new Vector(rover.direction(roverSpot)).sum(new Vector(0.0, -0.8, 0.8));

		// BOUNDING BOX: ( ESQ, TRAS, CIMA / DIR, FRENTE, BAIXO)
		double leftDist = spaceCenter.raycastDistance(
				new Vector(rover.boundingBox(roverSpot).getValue0().getValue0(), 0,
						rover.boundingBox(roverSpot).getValue0().getValue2()).toTriplet(),
				leftDir.toTriplet(), roverSpot);
		double rightDist = spaceCenter.raycastDistance(
				new Vector(rover.boundingBox(roverSpot).getValue1().getValue0(), 0,
						rover.boundingBox(roverSpot).getValue0().getValue2()).toTriplet(),
				rightDir.toTriplet(), roverSpot);
		double backwardDist = spaceCenter.raycastDistance(
				new Vector(0, rover.boundingBox(roverSpot).getValue0().getValue1(),
						rover.boundingBox(roverSpot).getValue0().getValue2()).toTriplet(),
				backwardDir.toTriplet(), roverSpot);
		double forwardDist = spaceCenter.raycastDistance(
				new Vector(0, rover.boundingBox(roverSpot).getValue1().getValue1(),
						rover.boundingBox(roverSpot).getValue0().getValue2()).toTriplet(),
				forwardDir.toTriplet(), roverSpot);

		double difED = leftDist - rightDist;
		if (Double.compare(difED, Double.NaN) == 0) {
			difED = 0;
		} else if (Double.compare(difED, Double.NEGATIVE_INFINITY) == 0) {
			difED = -20;
		} else if (Double.compare(difED, Double.POSITIVE_INFINITY) == 0) {
			difED = 20;
		}

		double difFT = forwardDist - backwardDist;
		if (Double.compare(difFT, Double.NaN) == 0) {
			difFT = 0;
		} else if (Double.compare(difFT, Double.NEGATIVE_INFINITY) == 0) {
			difFT = -20;
		} else if (Double.compare(difFT, Double.POSITIVE_INFINITY) == 0) {
			difFT = 20;
		}
//		rollControl.setEntradaPID(difED);
//		pitchControl.setEntradaPID(difFT);
//		rover.getControl().setRoll((float) (rollControl.computePID()));
//		rover.getControl().setPitch((float) (pitchControl.computePID()));
	}

	private void logData() throws IOException, RPCException, StreamException {
		if (searchingMarkers) {
			distanceToTarget = positionForRover(setMarkerPosition(targetMarker));
		} else {
			distanceToTarget = positionForRover(new Vector(targetShip.position(surfaceRefSpot)));
		}
		double timeVariation = gameTime.get() - previousTime;
		if (timeVariation > 1) {
			previousTime = gameTime.get();
		}
	}

	private Vector setMarkerPosition(Waypoint marker) throws RPCException {
		return new Vector(rover.getOrbit().getBody().surfacePosition(marker.getLatitude(), marker.getLongitude(),
				surfaceRefSpot));
	}

	private Vector positionSpot(Vector vector) throws RPCException {
		return new Vector(rover.getOrbit().getBody().surfacePosition(
				rover.getOrbit().getBody().latitudeAtPosition(vector.toTriplet(), orbitalRefSpot),
				rover.getOrbit().getBody().longitudeAtPosition(vector.toTriplet(), orbitalRefSpot),
				surfaceRefSpot));
	}

	private Vector positionForRover(Vector vector) throws IOException, RPCException {
		return new Vector(spaceCenter.transformPosition(vector.toTriplet(), surfaceRefSpot, roverSpot));
	}

	private Vector setVectorPosition(Vector vector) throws IOException, RPCException {
		return new Vector(spaceCenter.transformPosition(vector.toTriplet(), surfaceRefSpot, orbitalRefSpot));
	}

	public static void setTarget(String target) {
		markerName = target;
	}

	public static void setMaxSpeed(float inputSpeed) {
		maxSpeed = inputSpeed;
	}

}
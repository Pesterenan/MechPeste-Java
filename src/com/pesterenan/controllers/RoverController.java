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
import krpc.client.services.SpaceCenter.Vessel;
import org.javatuples.Triplet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoverController extends ActiveVessel implements Runnable {
	private static final int MAX_RADAR_LINES = 10;
	Vector nextPoint, pathDirection;
	float distanceFromTargetLimit = 50;
	float velocidadeCurva = 3;
	ControlePID ctrlRolagem = new ControlePID(), ctrlArfagem = new ControlePID();
	private String targetWaypointName = "";
	private Vessel targetVessel;
	private float maxSpeed = 5;
	private final ControlePID sterringCtrl = new ControlePID();
	private final ControlePID acelCtrl = new ControlePID();
	private ReferenceFrame pontoRefRover;
	private boolean isAutoRoverRunning = true;
	private final Map<String, String> commands;
	private Drawing drawing;
	private Stream<Float> bateriaAtual;
	private Drawing.Line right30Line;
	private Drawing.Line left30Line;
	private Drawing.Line steeringLine;
	private PathFinding pathFinding;
	private Vector targetPoint = new Vector();
	private Vector roverDirection;

	private Drawing.Line dirRover;
	private final List<Drawing.Line> radarLines = new ArrayList<>();


	public RoverController(Map<String, String> commands) {
		super(getConexao());
		this.commands = commands;
		initializeParameters();
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

	private void initializeParameters() {
		try {
			currentBody = naveAtual.getOrbit().getBody();
			parametrosDeVoo = naveAtual.flight(pontoRefOrbital);
			pontoRefRover = naveAtual.getReferenceFrame();
			velHorizontal = getConexao().addStream(parametrosDeVoo, "getHorizontalSpeed");
			bateriaAtual = getConexao().addStream(naveAtual.getResources(), "amount", "ElectricCharge");
			targetWaypointName = commands.get(Modulos.NOME_MARCADOR.get());
			maxSpeed = Float.parseFloat(commands.get(Modulos.VELOCIDADE_MAX.get()));
			roverDirection = new Vector(naveAtual.direction(pontoRefRover));
			drawing = Drawing.newInstance(getConexao());
			dirRover = drawing.addDirection(roverDirection.toTriplet(), pontoRefRover, 10, true);
			dirRover.setColor(new Triplet<>(1.0, 0.0, 0.0));
			dirRover.setThickness(0.2f);
			steeringLine = drawing.addDirection(roverDirection.toTriplet(), pontoRefRover, 10, true);
			steeringLine.setColor(new Triplet<>(1.0, 0.0, 1.0));
			for (int i = 0; i <= MAX_RADAR_LINES; i++) {
				Drawing.Line line = drawing.addDirection(roverDirection.toTriplet(), pontoRefRover, 10, true);
				line.setColor(new Triplet<>(0.0, (1.0 / MAX_RADAR_LINES), 0.0));
				line.setThickness(0.2f);
				radarLines.add(line);
			}
			pathFinding = new PathFinding(getConexao());
			// AJUSTAR CONTROLES PID:
			acelCtrl.adjustOutput(0, 1);
			sterringCtrl.adjustOutput(-1, 1);

			ctrlRolagem.adjustPID(0.5, 0.015, 0.5);
			ctrlRolagem.adjustOutput(-1, 1);
			ctrlArfagem.adjustPID(0.5, 0.015, 0.5);
			ctrlArfagem.adjustOutput(-1, 1);
		} catch (RPCException | StreamException ignored) {
		}
	}

	private void setTarget() throws IOException, RPCException, InterruptedException {
		if (commands.get(Modulos.TIPO_ALVO_ROVER.get()).equals(Modulos.MARCADOR_MAPA.get())) {
			pathFinding.addWaypointsOnSameBody(commands.get(Modulos.NOME_MARCADOR.get()));
			pathFinding.buildPathToTarget(pathFinding.findNearestWaypoint());
		}
		if (commands.get(Modulos.TIPO_ALVO_ROVER.get()).equals(Modulos.NAVE_ALVO.get())) {
			targetVessel = centroEspacial.getTargetVessel();
			pathFinding.buildPathToTarget(new Vector(targetVessel.position(pontoRefOrbital)));
		}
	}

	private void drawLineBetweenPoints(Vector pointA, Vector pointB) throws RPCException {
		Drawing.Line line = drawing.addLine(pointA.toTriplet(), pointB.toTriplet(), pontoRefOrbital, true);
		line.setThickness(0.5f);
		line.setColor(new Triplet<>(1.0, 0.5, 0.0));
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
//				pathFinding.removeWaypointFromList();
				}
			}
			Thread.sleep(50);
		}
		naveAtual.getControl().setBrakes(true);
		Thread.sleep(1000);
	}

	private boolean isFarFromTarget() throws RPCException {
		double distance = Vector.distance(new Vector(naveAtual.position(pontoRefOrbital)), targetPoint);
		return distance > distanceFromTargetLimit;
	}

	private boolean needToChargeBatteries() throws RPCException, IOException, StreamException, InterruptedException {
		float totalCharge = naveAtual.getResources().max("ElectricCharge");
		float currentCharge = naveAtual.getResources().amount("ElectricCharge");
		float minChargeLevel = 10.0f;
		float chargePercentage = (float) Math.ceil(currentCharge * 100 / totalCharge);
		if (chargePercentage > minChargeLevel) {
			return false;
		}
		setRoverThrottle(0);
		naveAtual.getControl().setLights(false);
		naveAtual.getControl().setBrakes(true);
		if (velHorizontal.get() < 1 && naveAtual.getControl().getBrakes()) {
			Thread.sleep(1000);
			double chargeTime = 0;
			List<SolarPanel> solarPanels = naveAtual.getParts().getSolarPanels();
			for (SolarPanel sp : solarPanels) {
				if (sp.getState() != SolarPanelState.BROKEN) {
					chargeTime += sp.getEnergyFlow();
				}
			}

			if (solarPanels.isEmpty()) {
				isAutoRoverRunning = false;
			}
			chargeTime = ((totalCharge - currentCharge) / chargeTime);
			StatusJPanel.setStatus("Segundos de Carga: " + chargeTime);
			if (chargeTime < 1 || chargeTime > 21600) {
				chargeTime = 3600;
			}
			centroEspacial.warpTo((centroEspacial.getUT() + chargeTime), 10000, 4);
			naveAtual.getControl().setLights(true);
		}
		return true;
	}

	private void setRoverThrottle(double throttle) throws IOException, RPCException, StreamException {
		if (velHorizontal.get() < (maxSpeed * 1.01)) {
			naveAtual.getControl().setBrakes(false);
			naveAtual.getControl().setWheelThrottle((float) throttle);
		} else {
			naveAtual.getControl().setBrakes(true);
		}
	}

	private void setRoverSteering(double steering) throws IOException, RPCException, StreamException {
		naveAtual.getControl().setWheelSteering((float) steering);
	}

	private void driveRover() throws IOException, RPCException, StreamException {
		Vector roverDirection = new Vector(naveAtual.direction(pontoRefRover));
		Vector targetDirection = posSurfToRover(posOrbToSurf(targetPoint)).normalize();
		Vector radarSourcePosition =
				posRoverToSurf(new Vector(naveAtual.position(pontoRefRover)).sum(new Vector(0.0, 3.0, 0.0)));

		double roverAngle = (roverDirection.heading());
		double targetAngle = (targetDirection.heading());
		// fazer um raycast pra frente e verificar a distancia
		double obstacleAhead = pathFinding.raycastDistance(radarSourcePosition, transformDirection(roverDirection),
		                                                   pontoRefSuperficie
		                                                  );
		// transformar a distancia num valor entre 0.25 e 1.0
		double steeringPower = Utilities.remap(2, 20, 0.05, 0.5, obstacleAhead, true);
		// usar esse valor pra muiltiplicar a direcao alvo
		double targetDirectionWithRadar =
				(targetDirection.multiply(steeringPower).sum(directionFromRadar()).normalize()).heading();
		double deltaAngle = Math.abs(targetDirectionWithRadar - roverAngle);
		naveAtual.getControl().setSAS(velHorizontal.get() > velocidadeCurva && deltaAngle < 5);
		// Control Rover Throttle
		setRoverThrottle(acelCtrl.calcPID(velHorizontal.get() / maxSpeed * 50, 50));
		// Control Rover Steering
		if (deltaAngle > 3) {
			setRoverSteering(sterringCtrl.calcPID(roverAngle / (targetDirectionWithRadar) * 100, 100));
		} else {
			setRoverSteering(0.0f);
		}
	}

	private void setNextPointInPath() throws IOException, RPCException {
		targetPoint = pathFinding.getPathsFirstPoint();
	}

	private Vector directionFromRadar() throws RPCException, IOException {
		// PONTO REF ROVER: X = DIREITA, Y = FRENTE, Z = BAIXO;
		Vector radarSourcePosition =
				posRoverToSurf(new Vector(naveAtual.position(pontoRefRover)).sum(new Vector(0.0, 3.0, 0.0)));
		Vector roverDirection = new Vector(naveAtual.direction(pontoRefRover));

		Vector bboxEsqSuperior = new Vector(naveAtual.boundingBox(pontoRefRover).getValue0());
		Vector bboxDirInferior = new Vector(naveAtual.boundingBox(pontoRefRover).getValue1());
		Vector radarSourceBoundingBoxPosition = posRoverToSurf(
				new Vector((bboxEsqSuperior.x + bboxDirInferior.x) / 2, bboxDirInferior.y,
				           (bboxEsqSuperior.z + bboxDirInferior.z) / 2
				));
		// Raycasting and calculating directions:
		Vector calculatedDirection = new Vector();
		for (int i = 1; i <= MAX_RADAR_LINES; i++) {
			Drawing.Line line = radarLines.get(i);
			Vector currentDirection = createRadarDirection(i);
			// descobrir a distancia do obstaculo na frente do vetor
			double distanceToObstacle =
					pathFinding.raycastDistance(radarSourceBoundingBoxPosition, transformDirection(currentDirection),
					                            pontoRefSuperficie
					                           );
			// criar vetor com a distancia calculada
			Vector directionEndPoint = currentDirection.multiply(distanceToObstacle);
			// Desenha a linha no jogo
			line.setReferenceFrame(pontoRefSuperficie);
			line.setStart(radarSourceBoundingBoxPosition.toTriplet());
			line.setEnd(radarSourcePosition.sum(transformDirection(currentDirection).multiply(distanceToObstacle))
			                               .toTriplet());

			// soma o vetor na direcao calculada
			calculatedDirection = calculatedDirection.sum(directionEndPoint);
		}

		steeringLine.setReferenceFrame(pontoRefSuperficie);
		steeringLine.setStart(radarSourcePosition.toTriplet());
		steeringLine.setEnd(
				radarSourcePosition.sum(transformDirection(calculatedDirection.normalize()).multiply(50)).toTriplet());
		return calculatedDirection.normalize();
	}

	private Vector createRadarDirection(int iteration) {
		double angle = (180.0 / MAX_RADAR_LINES) * iteration;
		int sign = angle > 90 ? -1 : 1;
		angle = angle > 90 ? angle - 90 : angle;
		return new Vector(Math.sin(Math.toRadians(angle)) * sign, Math.cos(Math.toRadians(angle)), 0.0);
	}

	private Vector transformDirection(Vector vector) throws RPCException {
		return new Vector(centroEspacial.transformDirection(vector.toTriplet(), pontoRefRover, pontoRefSuperficie));
	}

	private Vector posSurfToRover(Vector vector) throws IOException, RPCException {
		return new Vector(centroEspacial.transformPosition(vector.toTriplet(), pontoRefSuperficie, pontoRefRover));
	}

	private Vector posRoverToSurf(Vector vector) throws IOException, RPCException {
		return new Vector(centroEspacial.transformPosition(vector.toTriplet(), pontoRefRover, pontoRefSuperficie));
	}

	private Vector posOrbToSurf(Vector vector) throws IOException, RPCException {
		return new Vector(centroEspacial.transformPosition(vector.toTriplet(), pontoRefOrbital, pontoRefSuperficie));
	}
}
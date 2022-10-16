package com.pesterenan.utils;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.Drawing;
import krpc.client.services.SpaceCenter;
import org.javatuples.Triplet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.pesterenan.utils.PathFinding.*;

public class TestDev {

	private SpaceCenter.Vessel targetVessel;
	private SpaceCenter.ReferenceFrame pontoRefRover;
	private SpaceCenter.ReferenceFrame pontoRefSuperficie;
	private SpaceCenter.ReferenceFrame pontoRefOrbital;
	private Drawing drawing;
	private Drawing.Line steeringLine;
	private PathFinding pathFinding;
	private final Vector targetPoint = new Vector();
	private Vector roverDirection;
	private Connection connection;
	private SpaceCenter centroEspacial;
	private SpaceCenter.Vessel naveAtual;
	private final int MAX_RADAR_LINES = 8;

	private final List<Drawing.Line> radarLines = new ArrayList<>();
	private final Vector[] directionAngles =
			{ L90DEG, L60DEG, L30DEG, new Vector(0.0, 1.0, 0.0), R30DEG, R60DEG, R90DEG };

	public TestDev() {
		initializeParameters();
	}

	public static void main(String[] args) throws RPCException, IOException, InterruptedException {
		TestDev td = new TestDev();
		td.setTarget();
		td.lidar();
	}

	private void initializeParameters() {
		try {
			connection = Connection.newInstance("MechPeste - Pesterenan");
			centroEspacial = SpaceCenter.newInstance(connection);
			naveAtual = centroEspacial.getActiveVessel();
			drawing = Drawing.newInstance(connection);
			pontoRefRover = naveAtual.getReferenceFrame();
			pontoRefSuperficie = naveAtual.getSurfaceReferenceFrame();
			pontoRefOrbital = naveAtual.getOrbit().getBody().getReferenceFrame();
			roverDirection = new Vector(naveAtual.direction(pontoRefRover));
			// Create lines
			for (int i = 0; i <= MAX_RADAR_LINES; i++) {
				Drawing.Line line = drawing.addDirection(roverDirection.toTriplet(), pontoRefRover, 10, true);
				line.setColor(new Triplet<>(0.0, (1.0 / MAX_RADAR_LINES), 0.0));
				line.setThickness(0.2f);
				radarLines.add(line);
			}
			steeringLine = drawing.addDirection(roverDirection.toTriplet(), pontoRefRover, 10, true);
			steeringLine.setColor(new Triplet<>(1.0, 0.0, 1.0));
			steeringLine.setThickness(0.4f);
			pathFinding = new PathFinding(connection);
		} catch (RPCException | IOException ignored) {
		}
	}

	private void setTarget() throws IOException, RPCException, InterruptedException {
		targetVessel = centroEspacial.getTargetVessel();
		pathFinding.buildPathToTarget(new Vector(targetVessel.position(pontoRefOrbital)));
	}

	private void drawLineBetweenPoints(Vector pointA, Vector pointB) throws RPCException {
		Drawing.Line line = drawing.addLine(pointA.toTriplet(), pointB.toTriplet(), pontoRefOrbital, true);
		line.setThickness(0.5f);
		line.setColor(new Triplet<>(1.0, 0.5, 0.0));
	}


	private void driveRover() throws IOException, RPCException, StreamException, InterruptedException {
		Vector roverDirection = new Vector(naveAtual.direction(pontoRefRover));
		Vector targetDirection = posSurfToRover(posOrbToSurf(targetPoint)).normalize();
		double roverAngle = (roverDirection.heading());
		double targetAngle = (targetDirection.heading());
		double deltaAngle = Math.abs(targetAngle - roverAngle);
	}

	private void lidar() throws RPCException, IOException, InterruptedException {
		while (!naveAtual.getControl().getLights()) {
			// PONTO REF ROVER: X = DIREITA, Y = FRENTE, Z = BAIXO;
			Vector radarSourcePosition =
					posRoverToSurf(new Vector(naveAtual.position(pontoRefRover)).sum(new Vector(0.0, 3.0, 0.0)));
			Vector roverDirection = new Vector(naveAtual.direction(pontoRefRover));

			// Raycasting and calculating directions:
			Vector calculatedDirection = new Vector();
			for (int i = 1; i <= MAX_RADAR_LINES; i++) {
				Drawing.Line line = radarLines.get(i);
				Vector currentDirection = createRadarDirection(i);

				// descobrir a distancia do obstaculo na frente do vetor
				double distanceToObstacle =
						pathFinding.raycastDistance(radarSourcePosition, transformDirection(currentDirection),
						                            pontoRefSuperficie, 50
						                           );
				// criar vetor com a distancia calculada
				Vector directionEndPoint = currentDirection.multiply(distanceToObstacle);
				// Desenha a linha no jogo
				line.setReferenceFrame(pontoRefSuperficie);
				line.setStart(radarSourcePosition.toTriplet());
				line.setEnd(radarSourcePosition.sum(transformDirection(currentDirection).multiply(distanceToObstacle))
				                               .toTriplet());

				// soma o vetor na direcao calculada
				calculatedDirection = calculatedDirection.sum(directionEndPoint);
			}

			steeringLine.setReferenceFrame(pontoRefSuperficie);
			steeringLine.setStart(radarSourcePosition.toTriplet());
			steeringLine.setEnd(
					radarSourcePosition.sum(transformDirection(calculatedDirection.normalize()).multiply(50))
					                   .toTriplet());
			Thread.sleep(50);
		}
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

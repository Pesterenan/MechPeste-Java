package com.pesterenan.controllers;

import static com.pesterenan.MechPeste.getConnection;
import static com.pesterenan.MechPeste.getSpaceCenter;

import java.util.Map;

import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Utilities;
import com.pesterenan.utils.Vector;
import com.pesterenan.views.StatusJPanel;

import krpc.client.RPCException;
import krpc.client.services.Drawing;
import krpc.client.services.Drawing.Line;
import krpc.client.services.SpaceCenter.Control;
import krpc.client.services.SpaceCenter.DockingPort;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.SASMode;
import krpc.client.services.SpaceCenter.Vessel;

public class DockingController extends Controller {

	private Drawing drawing;
	private Vessel targetVessel;
	private Control control;

	private ReferenceFrame orbitalRefVessel;
	private ReferenceFrame vesselRefFrame;
	private ReferenceFrame orbitalRefBody;
	private Line distanceLine;
	private Line distLineXAxis;
	private Line distLineYAxis;
	private Line distLineZAxis;
	private DockingPort myDockingPort;
	private DockingPort targetDockingPort;
	private Vector positionMyDockingPort;
	private Vector positionTargetDockingPort;

	private final double DISTANCE_LIMIT = 25.0;
	private double SPEED_LIMIT = 3.0;
	private double currentXAxisSpeed = 0.0;
	private double currentYAxisSpeed = 0.0;
	private double currentZAxisSpeed = 0.0;
	private double lastXTargetPos = 0.0;
	private double lastYTargetPos = 0.0;
	private double lastZTargetPos = 0.0;
	private long sleepTime = 25;
	private DOCKING_STEPS dockingStep;

	public DockingController(Map<String, String> commands) {
		super();
		this.commands = commands;
		initializeParameters();
	}

	private void initializeParameters() {
		try {
			SPEED_LIMIT = Double.parseDouble(commands.get(Modulos.VELOCIDADE_MAX.get()));
			drawing = Drawing.newInstance(getConnection());
			targetVessel = getSpaceCenter().getTargetVessel();
			control = getNaveAtual().getControl();
			vesselRefFrame = getNaveAtual().getReferenceFrame();
			orbitalRefVessel = getNaveAtual().getOrbitalReferenceFrame();
			orbitalRefBody = getNaveAtual().getOrbit().getBody().getReferenceFrame();

			myDockingPort = getNaveAtual().getParts().getDockingPorts().get(0);
			targetDockingPort = targetVessel.getParts().getDockingPorts().get(0);

			positionMyDockingPort = new Vector(myDockingPort.position(orbitalRefVessel));
			positionTargetDockingPort = new Vector(targetDockingPort.position(orbitalRefVessel));
		} catch (RPCException ignored) {
		}
	}

	@Override
	public void run() {
		if (commands.get(Modulos.MODULO.get()).equals(Modulos.MODULO_DOCKING.get())) {
			startDocking();
		}
	}

	private void pointToTarget(Vector targetDirection) throws RPCException, InterruptedException {
		getNaveAtual().getAutoPilot().setReferenceFrame(orbitalRefVessel);
		getNaveAtual().getAutoPilot().setTargetDirection(targetDirection.toTriplet());
		getNaveAtual().getAutoPilot().setTargetRoll(90);
		getNaveAtual().getAutoPilot().engage();
		// Fazer a nave apontar usando o piloto automático, na marra
		while (Math.abs(getNaveAtual().getAutoPilot().getError()) > 3) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			Thread.sleep(100);
			System.out.println(getNaveAtual().getAutoPilot().getError());
		}
		getNaveAtual().getAutoPilot().disengage();
		control.setSAS(true);
		control.setSASMode(SASMode.STABILITY_ASSIST);
	}

	private void getCloserToTarget(Vector targetPosition) throws InterruptedException, RPCException {
		lastXTargetPos = targetPosition.x;
		lastYTargetPos = targetPosition.y;
		lastZTargetPos = targetPosition.z;

		while (Math.abs(lastYTargetPos) >= DISTANCE_LIMIT) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			targetPosition = new Vector(targetVessel.position(vesselRefFrame));
			controlShipRCS(targetPosition, DISTANCE_LIMIT);
			Thread.sleep(sleepTime);
		}

	}

	public void startDocking() {
		try {
			// Setting up the control
			control.setSAS(true);
			control.setRCS(false);
			control.setSASMode(SASMode.STABILITY_ASSIST);
			createLines(positionMyDockingPort, positionTargetDockingPort);

			// PRIMEIRA PARTE DO DOCKING: APROXIMAÇÃO
			Vector targetPosition = new Vector(targetVessel.position(vesselRefFrame));
			if (targetPosition.magnitude() > DISTANCE_LIMIT) {
				// Apontar para o alvo:
				Vector targetDirection = new Vector(getNaveAtual().position(orbitalRefVessel))
						.subtract(new Vector(targetVessel.position(orbitalRefVessel))).multiply(-1);
				pointToTarget(targetDirection);

				control.setRCS(true);

				getCloserToTarget(targetPosition);
			}

			control.setSAS(false);
			control.setRCS(false);

			// SEGUNDA PARTE FICAR DE FRENTE COM A DOCKING PORT:
			Vector targetDockingPortDirection = new Vector(targetDockingPort.direction(orbitalRefVessel))
					.multiply(-1);
			pointToTarget(targetDockingPortDirection);

			Thread.sleep(1000);
			control.setRCS(true);
			double safeDistance = 10;
			while (true) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				targetPosition = new Vector(targetDockingPort.position(vesselRefFrame))
						.subtract(new Vector(myDockingPort.position(vesselRefFrame)));
				if (targetPosition.magnitude() < safeDistance) {
					safeDistance = 1;
				}
				controlShipRCS(targetPosition, safeDistance);
				Thread.sleep(sleepTime);
			}
		} catch (RPCException | InterruptedException | IllegalArgumentException e) {
			StatusJPanel.setStatusMessage("Docking aborted.");
		}
	}

	/*
	 * Possibilidades do docking:
	 * primeiro: a nave ta na orientação certa, e só precisa seguir em frente X e Z
	 * = 0, Y positivo
	 * segundo: a nave ta na orientação certa, mas precisa corrigir a posição X e Z,
	 * Y positivo
	 * terceiro: a nave está atrás da docking port, precisa corrigir Y primeiro, Y
	 * negativo
	 * quarto: a nave está atrás da docking port, precisa afastar X e Z longe da
	 * nave primeiro, Y negativo
	 */

	private enum DOCKING_STEPS {
		APPROACH, LINE_UP_WITH_TARGET, GO_IN_FRONT_OF_TARGET
	}

	private DOCKING_STEPS checkDockingStep(Vector targetPosition, double forwardsDistanceLimit) {
		double sidewaysDistance = Math.abs(targetPosition.x);
		double upwardsDistance = Math.abs(targetPosition.z);
		boolean isInFrontOfTarget = Math.signum(targetPosition.y) == 1;
		boolean isOnTheBackOfTarget = Math.signum(targetPosition.y) == -1 && targetPosition.y < forwardsDistanceLimit;

		if (isOnTheBackOfTarget) {
			return DOCKING_STEPS.GO_IN_FRONT_OF_TARGET;
		}
		if (isInFrontOfTarget && (sidewaysDistance > 5 || upwardsDistance > 5)) {
			return DOCKING_STEPS.LINE_UP_WITH_TARGET;
		}
		return DOCKING_STEPS.APPROACH;
	}

	private void controlShipRCS(Vector targetPosition, double forwardsDistanceLimit) {
		try {
			// Atualizar posições para linhas
			positionMyDockingPort = new Vector(myDockingPort.position(vesselRefFrame));
			updateLines(positionMyDockingPort, targetPosition);

			// Calcular velocidade de cada eixo:
			currentXAxisSpeed = (targetPosition.x - lastXTargetPos) * sleepTime;
			currentYAxisSpeed = (targetPosition.y - lastYTargetPos) * sleepTime;
			currentZAxisSpeed = (targetPosition.z - lastZTargetPos) * sleepTime;

			dockingStep = checkDockingStep(targetPosition, forwardsDistanceLimit);
			float forwardsError, upwardsError, sidewaysError = 0;
			switch (dockingStep) {
				case APPROACH:
					// Calcular a aceleração para cada eixo no RCS:
					forwardsError = calculateThrottle(forwardsDistanceLimit, forwardsDistanceLimit * 3, currentYAxisSpeed,
							targetPosition.y, SPEED_LIMIT);
					sidewaysError = calculateThrottle(0, 5, currentXAxisSpeed, targetPosition.x, SPEED_LIMIT);
					upwardsError = calculateThrottle(0, 5, currentZAxisSpeed, targetPosition.z, SPEED_LIMIT);
					control.setForward(forwardsError);
					control.setRight(sidewaysError);
					control.setUp(-upwardsError);
					break;
				case LINE_UP_WITH_TARGET:
					forwardsError = calculateThrottle(forwardsDistanceLimit, forwardsDistanceLimit * 3, currentYAxisSpeed,
							targetPosition.y, 0);
					sidewaysError = calculateThrottle(0, 10, currentXAxisSpeed, targetPosition.x, SPEED_LIMIT);
					upwardsError = calculateThrottle(0, 10, currentZAxisSpeed, targetPosition.z, SPEED_LIMIT);
					control.setForward(forwardsError);
					control.setRight(sidewaysError);
					control.setUp(-upwardsError);
					break;
				case GO_IN_FRONT_OF_TARGET:
					forwardsError = calculateThrottle(-20, -10, currentYAxisSpeed,
							targetPosition.y, SPEED_LIMIT);
					sidewaysError = calculateThrottle(0, 5, currentXAxisSpeed, targetPosition.x, 0);
					upwardsError = calculateThrottle(0, 5, currentZAxisSpeed, targetPosition.z, 0);
					control.setForward(forwardsError);
					control.setRight(sidewaysError);
					control.setUp(-upwardsError);
					break;
			}
			System.out.println(dockingStep);

			// Guardar últimas posições:
			lastXTargetPos = targetPosition.x;
			lastYTargetPos = targetPosition.y;
			lastZTargetPos = targetPosition.z;
		} catch (RPCException ignored) {
		}
	}

	private float calculateThrottle(double minDistance, double maxDistance, double currentSpeed,
			double currentPosition, double speedLimit) {
		double limiter = Utilities.remap(minDistance, maxDistance, 0, 1, Math.abs(currentPosition), true);
		double change = (Utilities.remap(-speedLimit, speedLimit, -1.0, 1.0,
				currentSpeed + (Math.signum(currentPosition) * (limiter * speedLimit)), true));
		return (float) change;
	}

	private void createLines(Vector start, Vector end) {
		try {
			distanceLine = drawing.addLine(start.toTriplet(),
					end.toTriplet(), vesselRefFrame, true);
			distLineXAxis = drawing.addLine(start.toTriplet(),
					new Vector(end.x, 0.0, 0.0).toTriplet(),
					vesselRefFrame, true);
			distLineYAxis = drawing.addLine(start.toTriplet(),
					new Vector(end.x, end.y, 0.0).toTriplet(),
					vesselRefFrame, true);
			distLineZAxis = drawing.addLine(start.toTriplet(),
					end.toTriplet(),
					vesselRefFrame, true);
			distanceLine.setThickness(0.5f);
			distLineXAxis.setThickness(0.25f);
			distLineYAxis.setThickness(0.25f);
			distLineZAxis.setThickness(0.25f);
			distLineXAxis.setColor(new Vector(1.0, 0.0, 0.0).toTriplet());
			distLineYAxis.setColor(new Vector(0.0, 1.0, 0.0).toTriplet());
			distLineZAxis.setColor(new Vector(0.0, 0.0, 1.0).toTriplet());
		} catch (RPCException e) {
		}
	}

	private void updateLines(Vector start, Vector end) {
		// Updating drawing lines:
		try {
			distanceLine.setStart(start.toTriplet());
			distanceLine.setEnd(end.toTriplet());
			distLineXAxis.setStart(start.toTriplet());
			distLineXAxis.setEnd(new Vector(end.x, 0.0, 0.0).toTriplet());
			distLineYAxis.setStart(distLineXAxis.getEnd());
			distLineYAxis.setEnd(new Vector(end.x, end.y, 0.0).toTriplet());
			distLineZAxis.setStart(distLineYAxis.getEnd());
			distLineZAxis.setEnd(end.toTriplet());
		} catch (RPCException e) {
		}
	}

}

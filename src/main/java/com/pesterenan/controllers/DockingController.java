package com.pesterenan.controllers;

import com.pesterenan.model.ConnectionManager;
import com.pesterenan.model.VesselManager;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Module;
import com.pesterenan.utils.Utilities;
import com.pesterenan.utils.Vector;
import com.pesterenan.views.DockingJPanel;
import java.util.Map;
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
  private Line distanceLine;
  private Line distLineXAxis;
  private Line distLineYAxis;
  private Line distLineZAxis;
  private DockingPort myDockingPort;
  private DockingPort targetDockingPort;
  private Vector positionMyDockingPort;
  private Vector positionTargetDockingPort;

  private double DOCKING_MAX_SPEED = 3.0;
  private double SAFE_DISTANCE = 25.0;
  private double currentXAxisSpeed = 0.0;
  private double currentYAxisSpeed = 0.0;
  private double currentZAxisSpeed = 0.0;
  private double lastXTargetPos = 0.0;
  private double lastYTargetPos = 0.0;
  private double lastZTargetPos = 0.0;
  private long sleepTime = 25;
  private DOCKING_STEPS dockingStep;
  private ConnectionManager connectionManager;

  public DockingController(
      ConnectionManager connectionManager,
      VesselManager vesselManager,
      Map<String, String> commands) {
    super(connectionManager, vesselManager);
    this.connectionManager = connectionManager;
    this.commands = commands;
    initializeParameters();
  }

  private void initializeParameters() {
    try {
      DOCKING_MAX_SPEED = Double.parseDouble(commands.get(Module.MAX_SPEED.get()));
      SAFE_DISTANCE = Double.parseDouble(commands.get(Module.SAFE_DISTANCE.get()));
      drawing = Drawing.newInstance(connectionManager.getConnection());
      targetVessel = connectionManager.getSpaceCenter().getTargetVessel();
      control = getActiveVessel().getControl();
      vesselRefFrame = getActiveVessel().getReferenceFrame();
      orbitalRefVessel = getActiveVessel().getOrbitalReferenceFrame();

      myDockingPort = getActiveVessel().getParts().getDockingPorts().get(0);
      targetDockingPort = targetVessel.getParts().getDockingPorts().get(0);
      dockingStep = DOCKING_STEPS.STOP_RELATIVE_SPEED;

      positionMyDockingPort = new Vector(myDockingPort.position(orbitalRefVessel));
      positionTargetDockingPort = new Vector(targetDockingPort.position(orbitalRefVessel));
    } catch (RPCException ignored) {
    }
  }

  @Override
  public void run() {
    if (commands.get(Module.MODULO.get()).equals(Module.DOCKING.get())) {
      startDocking();
    }
  }

  private void pointToTarget(Vector targetDirection) throws RPCException, InterruptedException {
    getActiveVessel().getAutoPilot().setReferenceFrame(orbitalRefVessel);
    getActiveVessel().getAutoPilot().setTargetDirection(targetDirection.toTriplet());
    getActiveVessel().getAutoPilot().setTargetRoll(90);
    getActiveVessel().getAutoPilot().engage();
    // Fazer a nave apontar usando o piloto automático, na marra
    while (Math.abs(getActiveVessel().getAutoPilot().getError()) > 3) {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      Thread.sleep(100);
      System.out.println(getActiveVessel().getAutoPilot().getError());
    }
    getActiveVessel().getAutoPilot().disengage();
    control.setSAS(true);
    control.setSASMode(SASMode.STABILITY_ASSIST);
  }

  private void getCloserToTarget(Vector targetPosition) throws InterruptedException, RPCException {
    lastXTargetPos = targetPosition.x;
    lastYTargetPos = targetPosition.y;
    lastZTargetPos = targetPosition.z;

    while (Math.abs(lastYTargetPos) >= SAFE_DISTANCE) {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      targetPosition = new Vector(targetVessel.position(vesselRefFrame));
      controlShipRCS(targetPosition, SAFE_DISTANCE);
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
      if (targetPosition.magnitude() > SAFE_DISTANCE) {
        // Apontar para o alvo:
        Vector targetDirection =
            new Vector(getActiveVessel().position(orbitalRefVessel))
                .subtract(new Vector(targetVessel.position(orbitalRefVessel)))
                .multiply(-1);
        pointToTarget(targetDirection);

        control.setRCS(true);

        getCloserToTarget(targetPosition);
      }

      control.setSAS(false);
      control.setRCS(false);

      // SEGUNDA PARTE FICAR DE FRENTE COM A DOCKING PORT:
      Vector targetDockingPortDirection =
          new Vector(targetDockingPort.direction(orbitalRefVessel)).multiply(-1);
      pointToTarget(targetDockingPortDirection);

      Thread.sleep(1000);
      control.setRCS(true);
      double safeDistance = 10;
      while (true) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        targetPosition =
            new Vector(targetDockingPort.position(vesselRefFrame))
                .subtract(new Vector(myDockingPort.position(vesselRefFrame)));
        if (targetPosition.magnitude() < safeDistance) {
          safeDistance = 1;
        }
        controlShipRCS(targetPosition, safeDistance);
        Thread.sleep(sleepTime);
      }
    } catch (RPCException | InterruptedException | IllegalArgumentException e) {
      cleanup();
      setCurrentStatus("Docking interrupted.");
    }
  }

  /*
   * Possibilidades do docking: primeiro: a nave ta na orientação certa, e só precisa seguir em
   * frente X e Z = 0, Y positivo segundo: a nave ta na orientação certa, mas precisa corrigir a
   * posição X e Z, Y positivo terceiro: a nave está atrás da docking port, precisa corrigir Y
   * primeiro, Y negativo quarto: a nave está atrás da docking port, precisa afastar X e Z longe da
   * nave primeiro, Y negativo
   */

  private enum DOCKING_STEPS {
    APPROACH,
    STOP_RELATIVE_SPEED,
    LINE_UP_WITH_TARGET,
    GO_IN_FRONT_OF_TARGET
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

      double sidewaysDistance = Math.abs(targetPosition.x);
      double upwardsDistance = Math.abs(targetPosition.z);
      boolean isInFrontOfTarget = Math.signum(targetPosition.y) == 1;
      boolean isOnTheBackOfTarget =
          Math.signum(targetPosition.y) == -1 && targetPosition.y < forwardsDistanceLimit;
      float forwardsError, upwardsError, sidewaysError = 0;

      switch (dockingStep) {
        case APPROACH:
          // Calcular a aceleração para cada eixo no RCS:
          forwardsError =
              calculateThrottle(
                  forwardsDistanceLimit,
                  forwardsDistanceLimit * 3,
                  currentYAxisSpeed,
                  targetPosition.y,
                  DOCKING_MAX_SPEED);
          sidewaysError =
              calculateThrottle(0, 5, currentXAxisSpeed, targetPosition.x, DOCKING_MAX_SPEED);
          upwardsError =
              calculateThrottle(0, 5, currentZAxisSpeed, targetPosition.z, DOCKING_MAX_SPEED);
          control.setForward(forwardsError);
          control.setRight(sidewaysError);
          control.setUp(-upwardsError);
          DockingJPanel.setDockingStep(Bundle.getString("pnl_docking_step_approach"));
          break;
        case LINE_UP_WITH_TARGET:
          forwardsError =
              calculateThrottle(
                  forwardsDistanceLimit,
                  forwardsDistanceLimit * 3,
                  currentYAxisSpeed,
                  targetPosition.y,
                  0);
          sidewaysError =
              calculateThrottle(0, 10, currentXAxisSpeed, targetPosition.x, DOCKING_MAX_SPEED);
          upwardsError =
              calculateThrottle(0, 10, currentZAxisSpeed, targetPosition.z, DOCKING_MAX_SPEED);
          control.setForward(forwardsError);
          control.setRight(sidewaysError);
          control.setUp(-upwardsError);
          DockingJPanel.setDockingStep(Bundle.getString("pnl_docking_step_line_up_with_target"));
          break;
        case GO_IN_FRONT_OF_TARGET:
          forwardsError =
              calculateThrottle(-20, -10, currentYAxisSpeed, targetPosition.y, DOCKING_MAX_SPEED);
          sidewaysError = calculateThrottle(0, 5, currentXAxisSpeed, targetPosition.x, 0);
          upwardsError = calculateThrottle(0, 5, currentZAxisSpeed, targetPosition.z, 0);
          control.setForward(forwardsError);
          control.setRight(sidewaysError);
          control.setUp(-upwardsError);
          if (isInFrontOfTarget) {
            dockingStep = DOCKING_STEPS.STOP_RELATIVE_SPEED;
            break;
          }
          DockingJPanel.setDockingStep(Bundle.getString("pnl_docking_step_go_in_front_of_target"));
          break;
        case STOP_RELATIVE_SPEED:
          forwardsError = calculateThrottle(0, 5, currentYAxisSpeed, targetPosition.y, 0);
          sidewaysError = calculateThrottle(0, 5, currentXAxisSpeed, targetPosition.x, 0);
          upwardsError = calculateThrottle(0, 5, currentZAxisSpeed, targetPosition.z, 0);
          control.setForward(forwardsError);
          control.setRight(sidewaysError);
          control.setUp(-upwardsError);
          if ((Math.abs(currentXAxisSpeed) < 1)
              && (Math.abs(currentYAxisSpeed) < 1)
              && (Math.abs(currentZAxisSpeed) < 1)) {
            dockingStep = DOCKING_STEPS.APPROACH;
            break;
          }
          DockingJPanel.setDockingStep(Bundle.getString("pnl_docking_step_stop_relative_speed"));
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

  private float calculateThrottle(
      double minDistance,
      double maxDistance,
      double currentSpeed,
      double currentPosition,
      double speedLimit) {
    double limiter =
        Utilities.remap(minDistance, maxDistance, 0, 1, Math.abs(currentPosition), true);
    double change =
        (Utilities.remap(
            -speedLimit,
            speedLimit,
            -1.0,
            1.0,
            currentSpeed + (Math.signum(currentPosition) * (limiter * speedLimit)),
            true));
    return (float) change;
  }

  private void createLines(Vector start, Vector end) {
    try {
      distanceLine = drawing.addLine(start.toTriplet(), end.toTriplet(), vesselRefFrame, true);
      distLineXAxis =
          drawing.addLine(
              start.toTriplet(), new Vector(end.x, 0.0, 0.0).toTriplet(), vesselRefFrame, true);
      distLineYAxis =
          drawing.addLine(
              start.toTriplet(), new Vector(end.x, end.y, 0.0).toTriplet(), vesselRefFrame, true);
      distLineZAxis = drawing.addLine(start.toTriplet(), end.toTriplet(), vesselRefFrame, true);
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

  private void cleanup() {
    try {
      distanceLine.remove();
      distLineXAxis.remove();
      distLineYAxis.remove();
      distLineZAxis.remove();
      ap.disengage();
      throttle(0);
    } catch (RPCException e) {
      // ignore
    }
  }
}

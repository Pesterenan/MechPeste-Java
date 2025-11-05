package com.pesterenan.controllers;

import com.pesterenan.model.ActiveVessel;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Module;
import com.pesterenan.utils.PathFinding;
import com.pesterenan.utils.Utilities;
import com.pesterenan.utils.Vector;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.SolarPanel;
import krpc.client.services.SpaceCenter.SolarPanelState;
import org.javatuples.Pair;
import org.javatuples.Triplet;

public class RoverController extends Controller {
  private final ControlePID sterringCtrl = new ControlePID();
  private final ControlePID acelCtrl = new ControlePID();
  float distanceFromTargetLimit = 50;
  private float maxSpeed = 3;
  private ReferenceFrame roverReferenceFrame;
  private PathFinding pathFinding;
  private Vector targetPoint = new Vector();
  private Vector roverDirection = new Vector();
  private MODE currentMode;
  private final Map<String, String> commands;

  private volatile double currentDistanceToTarget = 0;
  private volatile float currentChargePercentage = 100;
  private volatile float currentChargeAmount = 0;
  private int distanceCallbackTag, chargeCallbackTag;
  private Stream<Triplet<Double, Double, Double>> positionStream;
  private Stream<Float> chargeAmountStream;
  private ScheduledExecutorService roverExecutor;

  public RoverController(ActiveVessel vessel, Map<String, String> commands) {
    super(vessel);
    this.commands = commands;
    initializeParameters();
  }

  private void initializeParameters() {
    try {
      maxSpeed = Float.parseFloat(commands.get(Module.MAX_SPEED.get()));
      roverReferenceFrame = vessel.getActiveVessel().getReferenceFrame();
      roverDirection = new Vector(vessel.getActiveVessel().direction(roverReferenceFrame));
      pathFinding = new PathFinding(vessel.getConnectionManager(), vessel.getVesselManager());
      acelCtrl.setOutput(0, 1);
      sterringCtrl.setOutput(-1, 1);
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
    if (commands.get(Module.MODULO.get()).equals(Module.ROVER.get())) {
      setTarget();
      currentMode = MODE.NEXT_POINT;
      try {
        setupCallbacks();
        roverExecutor = Executors.newSingleThreadScheduledExecutor();
        roverExecutor.scheduleAtFixedRate(this::roverStateMachine, 0, 100, TimeUnit.MILLISECONDS);
      } catch (IOException | RPCException | StreamException e) {
        cleanup();
      }
    }
  }

  private void roverStateMachine() {
    try {
      if (!isRunning()) {
        cleanup();
        return;
      }
      changeControlMode();
      if (isFarFromTarget()) {
        currentMode = needToChargeBatteries() ? MODE.CHARGING : MODE.DRIVE;
      } else { // Rover arrived at destiny
        currentMode = MODE.NEXT_POINT;
      }
    } catch (RPCException | IOException | StreamException | InterruptedException e) {
      cleanup();
    }
  }

  private void setupCallbacks() throws RPCException, IOException, StreamException {
    positionStream =
        vessel.connection.addStream(
            vessel.getActiveVessel(), "position", vessel.orbitalReferenceFrame);
    distanceCallbackTag =
        positionStream.addCallback(
            (pos) -> {
              currentDistanceToTarget = Vector.distance(new Vector(pos), targetPoint);
            });
    positionStream.start();

    chargeAmountStream =
        vessel.connection.addStream(
            vessel.getActiveVessel().getResources(), "amount", "ElectricCharge");
    chargeCallbackTag =
        chargeAmountStream.addCallback(
            (amount) -> {
              try {
                currentChargeAmount = amount;
                float totalCharge = vessel.getActiveVessel().getResources().max("ElectricCharge");
                currentChargePercentage = (float) Math.ceil(amount * 100 / totalCharge);
              } catch (RPCException e) {
              }
            });
    chargeAmountStream.start();
  }

  private void setTarget() {
    try {
      if (commands.get(Module.ROVER_TARGET_TYPE.get()).equals(Module.MAP_MARKER.get())) {
        pathFinding.addWaypointsOnSameBody(commands.get(Module.MARKER_NAME.get()));
        setCurrentStatus("Calculando rota até o alvo...");
        pathFinding.buildPathToTarget(pathFinding.findNearestWaypoint());
      }
      if (commands.get(Module.ROVER_TARGET_TYPE.get()).equals(Module.TARGET_VESSEL.get())) {
        Vector targetVesselPosition =
            new Vector(
                vessel
                    .getConnectionManager()
                    .getSpaceCenter()
                    .getTargetVessel()
                    .position(vessel.orbitalReferenceFrame));
        setCurrentStatus("Calculando rota até o alvo...");
        pathFinding.buildPathToTarget(targetVesselPosition);
      }
    } catch (RPCException | IOException | InterruptedException ignored) {
    }
  }

  private void changeControlMode()
      throws RPCException, IOException, StreamException, InterruptedException {
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

  private void cleanup() {
    try {
      if (roverExecutor != null && !roverExecutor.isShutdown()) {
        roverExecutor.shutdownNow();
      }
      vessel.getActiveVessel().getControl().setBrakes(true);
      pathFinding.removeDrawnPath();
      if (positionStream != null) {
        positionStream.removeCallback(distanceCallbackTag);
        positionStream.remove();
      }
      if (chargeAmountStream != null) {
        chargeAmountStream.removeCallback(chargeCallbackTag);
        chargeAmountStream.remove();
      }
      setCurrentStatus(Bundle.getString("lbl_stat_ready"));
    } catch (RPCException ignored) {
    }
  }

  private void setNextPointInPath() throws RPCException, IOException {
    pathFinding.removePathsCurrentPoint();
    vessel.getActiveVessel().getControl().setBrakes(true);
    if (pathFinding.isPathToTargetEmpty()) {
      if (commands.get(Module.ROVER_TARGET_TYPE.get()).equals(Module.MAP_MARKER.get())) {
        pathFinding.removeWaypointFromList();
        if (pathFinding.isWaypointsToReachEmpty()) {
          stop();
          return;
        }
        try {
          pathFinding.buildPathToTarget(pathFinding.findNearestWaypoint());
        } catch (InterruptedException e) {
          stop();
        }
      }

    } else {
      targetPoint = pathFinding.getPathsFirstPoint();
    }
  }

  private boolean isFarFromTarget() {
    return currentDistanceToTarget > distanceFromTargetLimit;
  }

  private boolean needToChargeBatteries() {
    float minChargeLevel = 10.0f;
    return (currentChargePercentage < minChargeLevel);
  }

  private void rechargeRover() throws RPCException, StreamException, InterruptedException {

    float totalCharge = vessel.getActiveVessel().getResources().max("ElectricCharge");

    setRoverThrottle(0);
    vessel.getActiveVessel().getControl().setLights(false);
    vessel.getActiveVessel().getControl().setBrakes(true);

    if (vessel.horizontalVelocity.get() < 1 && vessel.getActiveVessel().getControl().getBrakes()) {
      Thread.sleep(1000);
      double chargeTime;
      double totalEnergyFlow = 0;
      List<SolarPanel> solarPanels =
          vessel.getActiveVessel().getParts().getSolarPanels().stream()
              .filter(this::isSolarPanelNotBroken)
              .collect(Collectors.toList());

      for (SolarPanel sp : solarPanels) {
        totalEnergyFlow += sp.getEnergyFlow();
      }
      chargeTime = ((totalCharge - currentChargeAmount) / totalEnergyFlow);
      setCurrentStatus("Segundos de Carga: " + chargeTime);
      if (chargeTime < 1 || chargeTime > 21600) {
        chargeTime = 3600;
      }
      vessel
          .getConnectionManager()
          .getSpaceCenter()
          .warpTo((vessel.getConnectionManager().getSpaceCenter().getUT() + chargeTime), 10000, 4);
      vessel.getActiveVessel().getControl().setLights(true);
    }
  }

  private void driveRover() throws RPCException, IOException, StreamException {
    Vector targetDirection = posSurfToRover(posOrbToSurf(targetPoint)).normalize();
    Vector radarSourcePosition =
        posRoverToSurf(
            new Vector(vessel.getActiveVessel().position(roverReferenceFrame))
                .sum(new Vector(0.0, 3.0, 0.0)));

    double roverAngle = (roverDirection.heading());
    // fazer um raycast pra frente e verificar a distancia
    double obstacleAhead =
        pathFinding.raycastDistance(
            radarSourcePosition,
            transformDirection(roverDirection),
            vessel.surfaceReferenceFrame,
            30);
    double steeringPower = Utilities.remap(3, 30, 0.1, 0.5, obstacleAhead, true);
    // usar esse valor pra muiltiplicar a direcao alvo
    double targetAndRadarAngle =
        (targetDirection
                .multiply(steeringPower)
                .sum(directionFromRadar(vessel.getActiveVessel().boundingBox(roverReferenceFrame)))
                .normalize())
            .heading();
    double deltaAngle = Math.abs(targetAndRadarAngle - roverAngle);
    vessel.getActiveVessel().getControl().setSAS(deltaAngle < 1);
    // Control Rover Throttle
    setRoverThrottle(acelCtrl.calculate(vessel.horizontalVelocity.get() / maxSpeed * 50, 50));
    // Control Rover Steering
    if (deltaAngle > 1) {
      setRoverSteering(sterringCtrl.calculate(roverAngle / (targetAndRadarAngle) * 100, 100));
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
    Vector lateralLeft = new Vector(LBU.x, LBU.y * 0.5 + RFD.y * 0.5, LBU.z * 0.5 + RFD.z * 0.5);
    Vector latFrontLeft = new Vector(LBU.x, RFD.y * 0.5, LBU.z * 0.5 + RFD.z * 0.5);
    Vector frontLeft = new Vector(LBU.x, RFD.y, LBU.z * 0.5 + RFD.z * 0.5);
    Vector frontLeft2 = new Vector(LBU.x * 0.5, RFD.y, LBU.z * 0.5 + RFD.z * 0.5);
    Vector front = new Vector(LBU.x * 0.5 + RFD.x * 0.5, RFD.y, LBU.z * 0.5 + RFD.z * 0.5);
    Vector frontRight2 = new Vector(RFD.x * 0.5, RFD.y, LBU.z * 0.5 + RFD.z * 0.5);
    Vector frontRight = new Vector(RFD.x, RFD.y, LBU.z * 0.5 + RFD.z * 0.5);
    Vector latFrontRight = new Vector(RFD.x, RFD.y * 0.5, LBU.z * 0.5 + RFD.z * 0.5);
    Vector lateralRight = new Vector(RFD.x, LBU.y * 0.5 + RFD.y * 0.5, LBU.z * 0.5 + RFD.z * 0.5);

    // Pre-calculated bbox directions
    Vector lateralLeftAngle =
        new Vector(-Math.sin(Math.toRadians(90)), Math.cos(Math.toRadians(90)), 0.0);
    Vector latFrontLeftAngle =
        new Vector(-Math.sin(Math.toRadians(67.5)), Math.cos(Math.toRadians(67.5)), 0.0);
    Vector frontLeftAngle =
        new Vector(-Math.sin(Math.toRadians(45)), Math.cos(Math.toRadians(45)), 0.0);
    Vector frontLeftAngle2 =
        new Vector(-Math.sin(Math.toRadians(22.5)), Math.cos(Math.toRadians(22.5)), 0.0);
    Vector frontAngle = new Vector(0.0, 1.0, 0.0);
    Vector frontRightAngle2 =
        new Vector(Math.sin(Math.toRadians(22.5)), Math.cos(Math.toRadians(22.5)), 0.0);
    Vector frontRightAngle =
        new Vector(Math.sin(Math.toRadians(45)), Math.cos(Math.toRadians(45)), 0.0);
    Vector latFrontRightAngle =
        new Vector(Math.sin(Math.toRadians(67.5)), Math.cos(Math.toRadians(67.5)), 0.0);
    Vector lateralRightAngle =
        new Vector(Math.sin(Math.toRadians(90)), Math.cos(Math.toRadians(90)), 0.0);

    // Raytracing distance from points:
    Vector lateralLeftRay = calculateRaycastDirection(lateralLeft, lateralLeftAngle, 15);
    Vector lateralFrontLeftRay = calculateRaycastDirection(latFrontLeft, latFrontLeftAngle, 19);
    Vector frontLeftRay = calculateRaycastDirection(frontLeft, frontLeftAngle, 23);
    Vector frontLeftRay2 = calculateRaycastDirection(frontLeft2, frontLeftAngle2, 27);
    Vector frontRay = calculateRaycastDirection(front, frontAngle, 35);
    Vector frontRightRay2 = calculateRaycastDirection(frontRight2, frontRightAngle2, 27);
    Vector frontRightRay = calculateRaycastDirection(frontRight, frontRightAngle, 23);
    Vector lateralFrontRightRay = calculateRaycastDirection(latFrontRight, latFrontRightAngle, 19);
    Vector lateralRightRay = calculateRaycastDirection(lateralRight, lateralRightAngle, 15);

    Vector calculatedDirection =
        new Vector()
            .sum(lateralLeftRay)
            .sum(lateralFrontLeftRay)
            .sum(frontLeftRay)
            .sum(frontLeftRay2)
            .sum(frontRay)
            .sum(frontRightRay2)
            .sum(frontRightRay)
            .sum(lateralFrontRightRay)
            .sum(lateralRightRay);

    return (calculatedDirection.normalize());
  }

  private Vector calculateRaycastDirection(Vector point, Vector direction, double distance)
      throws RPCException {
    double raycast =
        pathFinding.raycastDistance(
            posRoverToSurf(point),
            transformDirection(direction),
            vessel.surfaceReferenceFrame,
            distance);
    return direction.multiply(raycast);
  }

  private Vector transformDirection(Vector vector) throws RPCException {
    return new Vector(
        vessel
            .getConnectionManager()
            .getSpaceCenter()
            .transformDirection(
                vector.toTriplet(), roverReferenceFrame, vessel.surfaceReferenceFrame));
  }

  private Vector posSurfToRover(Vector vector) throws RPCException {
    return new Vector(
        vessel
            .getConnectionManager()
            .getSpaceCenter()
            .transformPosition(
                vector.toTriplet(), vessel.surfaceReferenceFrame, roverReferenceFrame));
  }

  private Vector posRoverToSurf(Vector vector) throws RPCException {
    return new Vector(
        vessel
            .getConnectionManager()
            .getSpaceCenter()
            .transformPosition(
                vector.toTriplet(), roverReferenceFrame, vessel.surfaceReferenceFrame));
  }

  private Vector posOrbToSurf(Vector vector) throws RPCException {
    return new Vector(
        vessel
            .getConnectionManager()
            .getSpaceCenter()
            .transformPosition(
                vector.toTriplet(), vessel.orbitalReferenceFrame, vessel.surfaceReferenceFrame));
  }

  private void setRoverThrottle(double throttle) throws RPCException, StreamException {
    if (vessel.horizontalVelocity.get() < (maxSpeed * 1.01)) {
      vessel.getActiveVessel().getControl().setBrakes(false);
      vessel.getActiveVessel().getControl().setWheelThrottle((float) throttle);
    } else {
      vessel.getActiveVessel().getControl().setBrakes(true);
    }
  }

  private void setRoverSteering(double steering) throws RPCException {
    vessel.getActiveVessel().getControl().setWheelSteering((float) steering);
  }

  private enum MODE {
    DRIVE,
    NEXT_POINT,
    CHARGING
  }
}

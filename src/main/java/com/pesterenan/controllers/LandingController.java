package com.pesterenan.controllers;

import com.pesterenan.model.ConnectionManager;
import com.pesterenan.model.VesselManager;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Module;
import com.pesterenan.utils.Navigation;
import com.pesterenan.utils.Utilities;
import java.io.IOException;
import java.util.Map;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.VesselSituation;

public class LandingController extends Controller {

  private enum MODE {
    DEORBITING,
    APPROACHING,
    GOING_UP,
    HOVERING,
    GOING_DOWN,
    LANDING,
    WAITING
  }

  public static final double MAX_VELOCITY = 5;
  private static final long sleepTime = 50;
  private static final double velP = 0.05;
  private static final double velI = 0.005;
  private static final double velD = 0.001;
  private ControlePID altitudeCtrl;
  private ControlePID velocityCtrl;
  private Navigation navigation;
  private final int HUNDRED_PERCENT = 100;
  private double altitudeErrorPercentage, hoverAltitude;
  private boolean hoveringMode, hoverAfterApproximation, landingMode;
  private MODE currentMode;
  private float maxTWR;

  private volatile boolean isDeorbitBurnDone, isOrientedForDeorbit, isFalling = false;
  private int isOrientedCallbackTag, isDeorbitBurnDoneCallbackTag, isFallingCallbackTag;
  private Stream<Float> apErrorStream;

  public LandingController(
      ConnectionManager connectionManager,
      VesselManager vesselManager,
      Map<String, String> commands) {
    super(connectionManager, vesselManager);
    this.commands = commands;
    this.navigation = new Navigation(connectionManager, getActiveVessel());
    this.initializeParameters();
  }

  @Override
  public void run() {
    if (commands.get(Module.MODULO.get()).equals(Module.HOVERING.get())) {
      hoverArea();
    }
    if (commands.get(Module.MODULO.get()).equals(Module.LANDING.get())) {
      autoLanding();
    }
  }

  private void initializeParameters() {
    altitudeCtrl = new ControlePID(getConnectionManager().getSpaceCenter(), sleepTime);
    velocityCtrl = new ControlePID(getConnectionManager().getSpaceCenter(), sleepTime);
    altitudeCtrl.setOutput(0, 1);
    velocityCtrl.setOutput(0, 1);
  }

  private void hoverArea() {
    try {
      hoverAltitude = Double.parseDouble(commands.get(Module.HOVER_ALTITUDE.get()));
      hoveringMode = true;
      ap.engage();
      tuneAutoPilot();
      while (hoveringMode) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        altitudeErrorPercentage = surfaceAltitude.get() / hoverAltitude * HUNDRED_PERCENT;
        if (altitudeErrorPercentage > HUNDRED_PERCENT) {
          currentMode = MODE.GOING_DOWN;
        } else if (altitudeErrorPercentage < HUNDRED_PERCENT * 0.9) {
          currentMode = MODE.GOING_UP;
        } else {
          currentMode = MODE.HOVERING;
        }
        changeControlMode();
      }
    } catch (InterruptedException | RPCException | StreamException | IOException e) {
      cleanup();
      setCurrentStatus(Bundle.getString("status_ready"));
    }
  }

  private void autoLanding() {
    try {
      setupCallbacks();
      landingMode = true;
      maxTWR = Float.parseFloat(commands.get(Module.MAX_TWR.get()));
      hoverAfterApproximation =
          Boolean.parseBoolean(commands.get(Module.HOVER_AFTER_LANDING.get()));
      hoverAltitude = Double.parseDouble(commands.get(Module.HOVER_ALTITUDE.get()));
      if (!hoverAfterApproximation) {
        hoverAltitude = 100;
      }
      setCurrentStatus(
          Bundle.getString("status_starting_landing_at") + " " + currentBody.getName());
      currentMode = MODE.DEORBITING;
      ap.engage();
      changeControlMode();
      tuneAutoPilot();
      setCurrentStatus(Bundle.getString("status_starting_landing"));
      while (landingMode) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        getActiveVessel().getControl().setBrakes(true);
        changeControlMode();
      }
    } catch (RPCException | StreamException | InterruptedException | IOException e) {
      cleanup();
      System.err.println("landing: " + e.getMessage());
      setCurrentStatus(Bundle.getString("status_ready"));
    }
  }

  private void setupCallbacks() throws IOException, RPCException, StreamException {
    // Callback for ship orientation
    apErrorStream = connection.addStream(ap, "getError");
    isOrientedCallbackTag =
        apErrorStream.addCallback(
            (error) -> {
              if (error <= 5.0) {
                isOrientedForDeorbit = true;
                apErrorStream.removeCallback(isOrientedCallbackTag);
              }
            });

    // Callback for de-orbit burn completion
    isDeorbitBurnDoneCallbackTag =
        periapsis.addCallback(
            p -> {
              try {
                if (p <= -apoapsis.get()) {
                  isDeorbitBurnDone = true;
                  periapsis.removeCallback(isDeorbitBurnDoneCallbackTag);
                }
              } catch (RPCException | StreamException e) {
                System.err.println("isDeorbitBurnDoneCallbackTag error: " + e.getMessage());
              }
            });

    // Callback for when the ship starts falling back to the ground
    isFallingCallbackTag =
        verticalVelocity.addCallback(
            (vv) -> {
              if (vv <= 0) {
                isFalling = true;
                verticalVelocity.removeCallback(isFallingCallbackTag);
              }
            });

    // Start all streams
    apErrorStream.start();
    apoapsis.start();
    periapsis.start();
    verticalVelocity.start();
  }

  private void changeControlMode()
      throws RPCException, StreamException, InterruptedException, IOException {
    adjustPIDbyTWR();
    // Change vessel behavior depending on which mode is active
    switch (currentMode) {
      case DEORBITING:
        deOrbitShip();
        currentMode = MODE.WAITING;
        break;
      case WAITING:
        if (isFalling) {
          currentMode = MODE.APPROACHING;
        } else {
          setCurrentStatus(Bundle.getString("status_waiting_for_landing"));
          throttle(0.0f);
        }
        break;
      case APPROACHING:
        altitudeCtrl.setOutput(0, 1);
        velocityCtrl.setOutput(0, 1);

        double[] currentDistanceToGround = calculateCurrentDistanceToGround();
        double[] currentVelocities = calculateZeroVelocityMagnitude();
        double aMax = getMaxAcel(maxTWR);
        double landingDistanceThreshold = Math.max(hoverAltitude, aMax * 3);

        double stoppingDistance = (currentVelocities[0] * currentVelocities[0]) / (2 * aMax);

        double threshold = Utilities.clamp(stoppingDistance / landingDistanceThreshold, 0, 1);

        double altPID =
            altitudeCtrl.calculate(
                (currentDistanceToGround[0]), (stoppingDistance + landingDistanceThreshold));
        double velPID =
            velocityCtrl.calculate(
                verticalVelocity.get(), (-Utilities.clamp(surfaceAltitude.get() * 0.1, 3, 20)));

        double interpolation = Utilities.linearInterpolation(velPID, altPID, threshold);

        throttle(interpolation);
        navigation.aimForLanding();

        if (threshold < 0.15 && surfaceAltitude.get() < landingDistanceThreshold * 2) {
          hoverAltitude = landingDistanceThreshold;
          getActiveVessel().getControl().setGear(true);
          if (hoverAfterApproximation) {
            landingMode = false;
            hoverArea();
            break;
          }
          currentMode = MODE.LANDING;
        }
        setCurrentStatus("Se aproximando do momento do pouso...");
        break;
      case GOING_UP:
        altitudeCtrl.setOutput(-0.5, 0.5);
        velocityCtrl.setOutput(-0.5, 0.5);
        throttle(
            altitudeCtrl.calculate(altitudeErrorPercentage, HUNDRED_PERCENT)
                + velocityCtrl.calculate(verticalVelocity.get(), MAX_VELOCITY));
        navigation.aimAtRadialOut();
        setCurrentStatus("Subindo altitude...");
        break;
      case GOING_DOWN:
        controlThrottleByMatchingVerticalVelocity(-MAX_VELOCITY);
        navigation.aimAtRadialOut();
        setCurrentStatus("Baixando altitude...");
        break;
      case LANDING:
        if (hasTheVesselLanded()) break;
        controlThrottleByMatchingVerticalVelocity(
            horizontalVelocity.get() > 4
                ? 0
                : -Utilities.clamp(surfaceAltitude.get() * 0.1, 1, 10));
        navigation.aimForLanding();
        setCurrentStatus("Pousando...");
        break;
      case HOVERING:
        altitudeCtrl.setOutput(-0.5, 0.5);
        velocityCtrl.setOutput(-0.5, 0.5);
        throttle(
            altitudeCtrl.calculate(altitudeErrorPercentage, HUNDRED_PERCENT)
                + velocityCtrl.calculate(verticalVelocity.get(), 0));
        navigation.aimAtRadialOut();
        setCurrentStatus("Sobrevoando area...");
        break;
    }
  }

  private void adjustPIDbyTWR() throws RPCException, StreamException {
    double currentTWR = getMaxAcel(maxTWR);
    double pGain = Math.min(getTWR(), maxTWR);
    altitudeCtrl.setPIDValues(currentTWR * velP * velP, 0.1, velD);
    velocityCtrl.setPIDValues(pGain * velP, 0.1, velD);
  }

  private void controlThrottleByMatchingVerticalVelocity(double velocityToMatch)
      throws RPCException, StreamException {
    velocityCtrl.setOutput(0, 1);
    throttle(
        velocityCtrl.calculate(verticalVelocity.get(), velocityToMatch + horizontalVelocity.get()));
  }

  private void deOrbitShip() throws RPCException, InterruptedException, StreamException {
    throttle(0.0f);
    if (getActiveVessel().getSituation().equals(VesselSituation.ORBITING)
        || getActiveVessel().getSituation().equals(VesselSituation.SUB_ORBITAL)) {

      setCurrentStatus(Bundle.getString("status_going_suborbital"));
      ap.engage();
      getActiveVessel().getControl().setRCS(true);

      while (!isOrientedForDeorbit) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        navigation.aimForLanding();
        setCurrentStatus(Bundle.getString("status_orienting_ship"));
        Thread.sleep(100); // Prevent tight loop while waiting for event
      }

      double targetPeriapsis = currentBody.getAtmosphereDepth();
      targetPeriapsis =
          targetPeriapsis > 0 ? targetPeriapsis / 2 : -currentBody.getEquatorialRadius() / 2;

      while (!isDeorbitBurnDone) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        navigation.aimForLanding();
        throttle(altitudeCtrl.calculate(targetPeriapsis, periapsis.get()));
        setCurrentStatus(Bundle.getString("status_lowering_periapsis"));
      }
      getActiveVessel().getControl().setRCS(false);
      throttle(0.0f);
    }
  }

  private boolean hasTheVesselLanded() throws RPCException {
    if (getActiveVessel().getSituation().equals(VesselSituation.LANDED)
        || getActiveVessel().getSituation().equals(VesselSituation.SPLASHED)) {
      setCurrentStatus(Bundle.getString("status_landed"));
      hoveringMode = false;
      landingMode = false;
      throttle(0.0f);
      getActiveVessel().getControl().setSAS(true);
      getActiveVessel().getControl().setRCS(true);
      getActiveVessel().getControl().setBrakes(false);
      ap.disengage();
      return true;
    }
    return false;
  }

  private double[] calculateCurrentDistanceToGround() throws RPCException, StreamException {
    double vertVel = verticalVelocity.get();
    double horzVel = horizontalVelocity.get();
    double vertAlt = surfaceAltitude.get();
    double horzDist = horzVel * (vertAlt / Math.abs(vertVel));
    double euclidianDistance = Math.sqrt(vertAlt * vertAlt + horzDist * horzDist);
    return new double[] {euclidianDistance, 0};
  }

  private double[] calculateZeroVelocityMagnitude() throws RPCException, StreamException {
    double vertVel = verticalVelocity.get();
    double horzVel = horizontalVelocity.get();
    return new double[] {Math.sqrt(horzVel * horzVel + vertVel * vertVel), 0};
  }

  private void cleanup() {
    try {
      apErrorStream.removeCallback(isOrientedCallbackTag);
      periapsis.removeCallback(isDeorbitBurnDoneCallbackTag);
      verticalVelocity.removeCallback(isFallingCallbackTag);
      apErrorStream.remove();
      ap.disengage();
      throttle(0);
    } catch (RPCException e) {
      // ignore
    }
  }
}

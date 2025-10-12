package com.pesterenan.controllers;

import com.pesterenan.model.ActiveVessel;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Module;
import com.pesterenan.utils.Navigation;
import com.pesterenan.utils.Utilities;
import java.util.Map;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.VesselSituation;

public class LandingController extends Controller {

  private enum MODE {
    APPROACHING,
    DEORBITING,
    ORIENTING_DEORBIT,
    EXECUTING_DEORBIT_BURN,
    GOING_DOWN,
    GOING_UP,
    HOVERING,
    LANDING,
    WAITING
  }

  public static final double MAX_VELOCITY = 5;
  private static final double velP = 0.05;
  private static final double velI = 0.000001;
  private static final double velD = 0.001;
  private ControlePID altitudeCtrl;
  private ControlePID velocityCtrl;
  private Navigation navigation;
  private final int HUNDRED_PERCENT = 100;
  private double altitudeErrorPercentage, hoverAltitude, targetPeriapsis;
  private boolean hoveringMode, hoverAfterApproximation, landingMode;
  private MODE currentMode;
  private float maxTWR;
  private final Map<String, String> commands;

  private volatile boolean isDeorbitBurnDone, isOrientedForDeorbit, isFalling, wasAirborne = false;
  private int isOrientedCallbackTag,
      isDeorbitBurnDoneCallbackTag,
      isFallingCallbackTag,
      utCallbackTag;
  private Stream<Float> apErrorStream;

  public LandingController(ActiveVessel vessel, Map<String, String> commands) {
    super(vessel);
    this.commands = commands;
    this.navigation = new Navigation(vessel.getConnectionManager(), vessel.getActiveVessel());
    this.initializeParameters();
  }

  @Override
  public void run() {
    try {
      if (commands.get(Module.MODULO.get()).equals(Module.HOVERING.get())) {
        hoverArea();
      }
      if (commands.get(Module.MODULO.get()).equals(Module.LANDING.get())) {
        autoLanding();
      }
      while (landingMode || hoveringMode) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        Thread.sleep(100);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt(); // Restore interrupted status
      System.err.println("Controle de Pouso finalizado via interrupção.");
      setCurrentStatus(Bundle.getString("status_ready"));
    } catch (RPCException | StreamException e) {
      System.err.println("Erro no run do landingController:" + e.getMessage());
      setCurrentStatus(Bundle.getString("status_data_unavailable"));
    } finally {
      cleanup();
    }
  }

  private void initializeParameters() {
    maxTWR = Float.parseFloat(commands.get(Module.MAX_TWR.get()));
    hoverAltitude = Double.parseDouble(commands.get(Module.HOVER_ALTITUDE.get()));
    altitudeCtrl = new ControlePID(vessel.spaceCenter);
    velocityCtrl = new ControlePID(vessel.spaceCenter);
    altitudeCtrl.setOutput(0, 1);
    velocityCtrl.setOutput(0, 1);
  }

  private void hoverArea() throws RPCException, StreamException, InterruptedException {
    hoveringMode = true;
    vessel.ap.engage();
    vessel.tuneAutoPilot();
    setupCallbacks();
  }

  private void autoLanding() throws RPCException, StreamException, InterruptedException {
    landingMode = true;
    hoverAfterApproximation = Boolean.parseBoolean(commands.get(Module.HOVER_AFTER_LANDING.get()));
    if (!hoverAfterApproximation) {
      hoverAltitude = 100;
    }
    altitudeCtrl.reset();
    velocityCtrl.reset();
    setCurrentStatus(
        Bundle.getString("status_starting_landing_at") + " " + vessel.currentBody.getName());
    currentMode = MODE.DEORBITING;
    vessel.ap.engage();
    vessel.tuneAutoPilot();
    vessel.getActiveVessel().getControl().setBrakes(true);
    setCurrentStatus(Bundle.getString("status_starting_landing"));
    setupCallbacks();
  }

  private void setupCallbacks() throws RPCException, StreamException {
    // Callback for ship orientation
    apErrorStream = vessel.connection.addStream(vessel.ap, "getError");
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
        vessel.periapsis.addCallback(
            p -> {
              try {
                if (isDeorbitBurnDone) return; // Stop if flag is already set

                if (currentMode == MODE.EXECUTING_DEORBIT_BURN) {
                  // During de-orbit, actively control throttle
                  if (p <= targetPeriapsis) {
                    isDeorbitBurnDone = true;
                    vessel.throttle(0.0f);
                    vessel.periapsis.removeCallback(isDeorbitBurnDoneCallbackTag);
                  } else {
                    vessel.throttle(altitudeCtrl.calculate(targetPeriapsis, p));
                  }
                }
              } catch (RPCException e) {
                System.err.println("isDeorbitBurnDoneCallbackTag error: " + e.getMessage());
              }
            });

    // Callback for when the ship starts falling back to the ground
    isFallingCallbackTag =
        vessel.verticalVelocity.addCallback(
            (vv) -> {
              if (vv <= 0) {
                isFalling = true;
                vessel.verticalVelocity.removeCallback(isFallingCallbackTag);
              }
            });

    utCallbackTag =
        vessel.missionTime.addCallback(
            (ut) -> {
              try {
                if (landingMode) {
                  executeLandingStep();
                } else if (hoveringMode) {
                  altitudeErrorPercentage =
                      vessel.surfaceAltitude.get() / hoverAltitude * HUNDRED_PERCENT;
                  if (altitudeErrorPercentage > HUNDRED_PERCENT * 1.05) {
                    currentMode = MODE.GOING_DOWN;
                  } else if (altitudeErrorPercentage < HUNDRED_PERCENT * 0.95) {
                    currentMode = MODE.GOING_UP;
                  } else {
                    currentMode = MODE.HOVERING;
                  }
                  executeHoverStep();
                } else {
                  vessel.missionTime.removeCallback(utCallbackTag);
                }
              } catch (Exception e) {
                System.err.println("UT Callback error: " + e.getMessage());
              }
            });

    // Start all streams
    apErrorStream.start();
    vessel.apoapsis.start();
    vessel.periapsis.start();
    vessel.verticalVelocity.start();
  }

  private void executeLandingStep() throws RPCException, StreamException, InterruptedException {
    adjustLandingPID();
    // Change vessel behavior depending on which mode is active
    switch (currentMode) {
      case DEORBITING:
        if (vessel.getActiveVessel().getSituation().equals(VesselSituation.ORBITING)
            || vessel.getActiveVessel().getSituation().equals(VesselSituation.SUB_ORBITAL)) {
          setCurrentStatus(Bundle.getString("status_going_suborbital"));
          vessel.getActiveVessel().getControl().setRCS(true);
          vessel.throttle(0.0f);
          targetPeriapsis = vessel.currentBody.getAtmosphereDepth();
          targetPeriapsis =
              targetPeriapsis > 0
                  ? targetPeriapsis / 2
                  : -vessel.currentBody.getEquatorialRadius() / 2;
          currentMode = MODE.ORIENTING_DEORBIT;
        } else {
          currentMode = MODE.APPROACHING;
        }
        break;
      case ORIENTING_DEORBIT:
        setCurrentStatus(Bundle.getString("status_orienting_ship"));
        navigation.aimForDeorbit();
        if (isOrientedForDeorbit) {
          currentMode = MODE.EXECUTING_DEORBIT_BURN;
        }
        break;
      case EXECUTING_DEORBIT_BURN:
        setCurrentStatus(Bundle.getString("status_lowering_periapsis"));
        if (isDeorbitBurnDone) {
          vessel.getActiveVessel().getControl().setRCS(false);
          vessel.throttle(0.0f);
          currentMode = MODE.WAITING;
        }
        break;
      case WAITING:
        if (isFalling) {
          currentMode = MODE.APPROACHING;
        } else {
          setCurrentStatus(Bundle.getString("status_waiting_for_landing"));
          vessel.throttle(0.0f);
        }
        break;
      case APPROACHING:
        for (Engine engine : vessel.getActiveVessel().getParts().getEngines()) {
          if (engine.getPart().getStage() == vessel.getActiveVessel().getControl().getCurrentStage()
              && !engine.getActive()) {
            engine.setActive(true);
          }
        }
        altitudeCtrl.setOutput(0, 1);
        velocityCtrl.setOutput(0, 1);
        double currentVelocity = calculateCurrentVelocityMagnitude();
        double zeroVelocity = calculateZeroVelocityMagnitude();
        double landingDistanceThreshold = Math.max(hoverAltitude, vessel.getMaxAcel(maxTWR) * 3);
        double threshold =
            Utilities.clamp(
                ((currentVelocity + zeroVelocity) - landingDistanceThreshold)
                    / landingDistanceThreshold,
                0,
                1);
        double altPID = altitudeCtrl.calculate(currentVelocity, zeroVelocity);
        double velPID =
            velocityCtrl.calculate(
                vessel.verticalVelocity.get(),
                (-Utilities.clamp(vessel.surfaceAltitude.get() * 0.1, 3, 20)));
        vessel.throttle(Utilities.linearInterpolation(velPID, altPID, threshold));
        navigation.aimForLanding();
        if (threshold < 0.15 || vessel.surfaceAltitude.get() < landingDistanceThreshold) {
          hoverAltitude = landingDistanceThreshold;
          vessel.getActiveVessel().getControl().setGear(true);
          if (hoverAfterApproximation) {
            landingMode = false;
            hoverArea();
            break;
          }
          currentMode = MODE.LANDING;
        }
        setCurrentStatus("Se aproximando do momento do pouso...");
        break;

      case LANDING:
        if (hasTheVesselLanded()) break;
        controlThrottleByMatchingVerticalVelocity(
            vessel.horizontalVelocity.get() > 5
                ? 0
                : -Utilities.clamp(vessel.surfaceAltitude.get() * 0.1, 3, 20));
        navigation.aimForLanding();
        setCurrentStatus("Pousando...");
        break;
      default:
        break;
    }
  }

  private void executeHoverStep() throws RPCException, StreamException, InterruptedException {
    if (hasTheVesselLanded()) {
      hoveringMode = false;
      return;
    }
    adjustHoverPID();
    altitudeCtrl.setOutput(-0.5, 0.5);
    velocityCtrl.setOutput(-0.5, 0.5);
    switch (currentMode) {
      case GOING_UP:
        vessel.throttle(
            altitudeCtrl.calculate(altitudeErrorPercentage, HUNDRED_PERCENT)
                + velocityCtrl.calculate(vessel.verticalVelocity.get(), MAX_VELOCITY));
        navigation.aimAtRadialOut();
        setCurrentStatus("Subindo altitude...");
        break;
      case GOING_DOWN:
        controlThrottleByMatchingVerticalVelocity(-MAX_VELOCITY);
        navigation.aimAtRadialOut();
        setCurrentStatus("Baixando altitude...");
        break;
      case HOVERING:
        vessel.throttle(
            altitudeCtrl.calculate(altitudeErrorPercentage, HUNDRED_PERCENT)
                + velocityCtrl.calculate(0, -vessel.verticalVelocity.get()));
        navigation.aimAtRadialOut();
        setCurrentStatus("Sobrevoando area...");
        break;
      default:
        break;
    }
  }

  private void adjustLandingPID() throws RPCException, StreamException {
    double maxAccel = vessel.getMaxAcel(maxTWR);
    double currentTWR = Math.min(vessel.getTWR(), maxTWR);

    if (currentMode == MODE.APPROACHING) {
      // 1. Calcula a distância de trajetória restante (usando o método que já temos)
      double trajectoryLength = calculateCurrentVelocityMagnitude();
      // 2. Calcula a velocidade total (vetorial)
      double totalVelocity =
          Math.sqrt(
              Math.pow(vessel.verticalVelocity.get(), 2)
                  + Math.pow(vessel.horizontalVelocity.get(), 2));

      // 3. Calcula o tempo de impacto baseado na TRAJETÓRIA
      double timeToImpact = 10.0; // Default seguro
      if (totalVelocity > 1) { // Evita divisão por zero
        timeToImpact = Utilities.clamp(trajectoryLength / totalVelocity, 0.5, 20);
      }

      // O resto da lógica permanece o mesmo, mas agora usando o novo timeToImpact
      double Kp = (currentTWR * velP) / timeToImpact;
      double Kd = Kp * (velD / velP);
      double Ki = 0.0001;
      altitudeCtrl.setPIDValues(Kp, Ki, Kd);
    } else {
      // Para outros modos, usa um PID mais simples e estável
      altitudeCtrl.setPIDValues(maxAccel * velP, velI, velD);
    }

    // O controlador de velocidade pode usar uma sintonia mais simples
    velocityCtrl.setPIDValues(currentTWR * velP, velI, velD);
  }

  private void adjustHoverPID() throws RPCException, StreamException {
    double currentTWR = Math.min(vessel.getTWR(), maxTWR);
    altitudeCtrl.setPIDValues(currentTWR * velP, currentTWR * velI, currentTWR * velD);
    velocityCtrl.setPIDValues(currentTWR * velP, currentTWR * velI, currentTWR * velD);
  }

  private void controlThrottleByMatchingVerticalVelocity(double velocityToMatch)
      throws RPCException, StreamException {
    velocityCtrl.setOutput(0, 1);
    vessel.throttle(
        velocityCtrl.calculate(
            vessel.verticalVelocity.get(), velocityToMatch + vessel.horizontalVelocity.get()));
  }

  private boolean hasTheVesselLanded() throws RPCException {
    VesselSituation situation = vessel.getActiveVessel().getSituation();
    if (wasAirborne
        && (situation.equals(VesselSituation.LANDED)
            || situation.equals(VesselSituation.SPLASHED))) {
      setCurrentStatus(Bundle.getString("status_landed"));
      hoveringMode = false;
      landingMode = false;
      vessel.throttle(0.0f);
      vessel.getActiveVessel().getControl().setSAS(true);
      vessel.getActiveVessel().getControl().setRCS(true);
      vessel.getActiveVessel().getControl().setBrakes(false);
      vessel.ap.disengage();
      return true;
    }
    // If we are not landed, we must be airborne
    if (!situation.equals(VesselSituation.LANDED)
        && !situation.equals(VesselSituation.SPLASHED)
        && !situation.equals(VesselSituation.PRE_LAUNCH)) {
      wasAirborne = true;
    }
    return false;
  }

  private double calculateCurrentVelocityMagnitude() throws RPCException, StreamException {
    double timeToGround = vessel.surfaceAltitude.get() / vessel.verticalVelocity.get();
    double horizontalDistance = vessel.horizontalVelocity.get() * timeToGround;
    return calculateEllipticTrajectory(horizontalDistance, vessel.surfaceAltitude.get());
  }

  private double calculateZeroVelocityMagnitude() throws RPCException, StreamException {
    double zeroVelocityDistance =
        calculateEllipticTrajectory(vessel.horizontalVelocity.get(), vessel.verticalVelocity.get());
    double zeroVelocityBurnTime = zeroVelocityDistance / vessel.getMaxAcel(maxTWR);
    return zeroVelocityDistance * zeroVelocityBurnTime;
  }

  private double calculateEllipticTrajectory(double a, double b) {
    double semiMajor = Math.max(a * 2, b * 2);
    double semiMinor = Math.min(a * 2, b * 2);
    return Math.PI * Math.sqrt((semiMajor * semiMajor + semiMinor * semiMinor)) / 4;
  }

  private void cleanup() {
    try {
      landingMode = false;
      hoveringMode = false;
      if (vessel.ap != null) {
        vessel.ap.disengage();
      }
      if (apErrorStream != null) {
        apErrorStream.remove();
      }
      vessel.throttle(0);
      setCurrentStatus(Bundle.getString("status_ready"));
    } catch (RPCException | NullPointerException e) {
      System.err.println("Erro durante a limpeza do LandingController: " + e.getMessage());
    }
  }
}

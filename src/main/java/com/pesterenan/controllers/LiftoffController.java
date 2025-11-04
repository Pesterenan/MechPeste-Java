package com.pesterenan.controllers;

import com.pesterenan.model.ActiveVessel;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Module;
import com.pesterenan.utils.Navigation;
import com.pesterenan.utils.Utilities;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Fairing;
import krpc.client.services.SpaceCenter.VesselSituation;

public class LiftoffController extends Controller {

  private enum LIFTOFF_MODE {
    GRAVITY_TURN,
    FINALIZE_ORBIT,
    CIRCULARIZE
  }

  private static final float PITCH_UP = 90;
  private ControlePID thrControl;
  private float currentPitch, finalApoapsisAlt, heading, roll, maxTWR;
  private volatile boolean targetApoapsisReached,
      dynamicPressureLowEnough,
      isLiftoffRunning = false;
  private int apoapsisCallbackTag, pressureCallbackTag, utCallbackTag;
  private Stream<Float> pressureStream;
  private boolean willDecoupleStages, willOpenPanelsAndAntenna;
  private String gravityCurveModel = Module.CIRCULAR.get();
  private Navigation navigation;
  private LIFTOFF_MODE liftoffMode;
  private double startCurveAlt;
  private final Map<String, String> commands;

  public LiftoffController(ActiveVessel vessel, Map<String, String> commands) {
    super(vessel);
    this.commands = commands;
    this.navigation = new Navigation(vessel.getConnectionManager(), vessel.getActiveVessel());
    initializeParameters();
  }

  @Override
  public void run() {
    try {
      isLiftoffRunning = true;

      // Part 1: Blocking Countdown and Launch
      if (vessel.getActiveVessel().getSituation().equals(VesselSituation.PRE_LAUNCH)) {
        vessel.throttleUp(vessel.getMaxThrottleForTWR(1.4), 1);
        for (double count = 5.0; count >= 0; count -= 0.1) {
          if (Thread.interrupted()) throw new InterruptedException();
          setCurrentStatus(String.format(Bundle.getString("status_launching_in"), count));
          Thread.sleep(100);
        }
        setCurrentStatus(Bundle.getString("status_liftoff"));
        vessel.getActiveVessel().getControl().activateNextStage();
      } else {
        vessel.throttle(1.0f);
      }

      // Part 2: Async Gravity Turn
      liftoffMode = LIFTOFF_MODE.GRAVITY_TURN; // Set initial state for async phase
      setupCallbacks(); // This starts the UT stream
      vessel.tuneAutoPilot();
      startCurveAlt = vessel.altitude.get();
      vessel.ap.setReferenceFrame(vessel.surfaceReferenceFrame);
      vessel.ap.targetPitchAndHeading(currentPitch, heading);
      vessel.ap.setTargetRoll(this.roll);
      vessel.ap.engage();

      // Loop to keep thread alive while async part runs
      while (isLiftoffRunning) {
        if (Thread.interrupted()) throw new InterruptedException();
        Thread.sleep(250);
      }
    } catch (RPCException | InterruptedException | StreamException e) {
      cleanup();
      setCurrentStatus(Bundle.getString("status_ready"));
    }
  }

  public void setHeading(float heading) {
    final int MIN_HEADING = 0;
    final int MAX_HEADING = 360;
    this.heading = (float) Utilities.clamp(heading, MIN_HEADING, MAX_HEADING);
  }

  public void setRoll(float roll) {
    final int MIN_ROLL = 0;
    final int MAX_ROLL = 360;
    this.roll = (float) Utilities.clamp(roll, MIN_ROLL, MAX_ROLL);
  }

  public void setFinalApoapsisAlt(float finalApoapsisAlt) {
    final int MIN_FINAL_APOAPSIS = 10000;
    final int MAX_FINAL_APOAPSIS = 2000000;
    this.finalApoapsisAlt =
        (float) Utilities.clamp(finalApoapsisAlt, MIN_FINAL_APOAPSIS, MAX_FINAL_APOAPSIS);
  }

  private void initializeParameters() {
    currentPitch = PITCH_UP;
    setFinalApoapsisAlt(Float.parseFloat(commands.get(Module.APOAPSIS.get())));
    setHeading(Float.parseFloat(commands.get(Module.DIRECTION.get())));
    setRoll(Float.parseFloat(commands.get(Module.ROLL.get())));
    maxTWR =
        (float) Utilities.clamp(Float.parseFloat(commands.get(Module.MAX_TWR.get())), 1.2, 5.0);
    gravityCurveModel = commands.get(Module.INCLINATION.get());
    willOpenPanelsAndAntenna = Boolean.parseBoolean(commands.get(Module.OPEN_PANELS.get()));
    willDecoupleStages = Boolean.parseBoolean(commands.get(Module.STAGE.get()));
    thrControl = new ControlePID(vessel.getConnectionManager().getSpaceCenter());
    thrControl.setOutput(0.0, 1.0);
  }

  private void setupCallbacks() throws RPCException, StreamException {
    apoapsisCallbackTag =
        vessel.apoapsis.addCallback(
            (apo) -> {
              if (apo >= finalApoapsisAlt) {
                targetApoapsisReached = true;
              }
            });
    vessel.apoapsis.start();

    pressureStream = vessel.connection.addStream(vessel.flightParameters, "getDynamicPressure");
    pressureCallbackTag =
        pressureStream.addCallback(
            (pressure) -> {
              if (pressure <= 10) {
                dynamicPressureLowEnough = true;
              }
            });
    pressureStream.start();

    utCallbackTag =
        vessel.missionTime.addCallback(
            (ut) -> {
              try {
                if (!isLiftoffRunning) {
                  vessel.missionTime.removeCallback(utCallbackTag);
                  return;
                }
                handleLiftoff();
              } catch (Exception e) {
                System.err.println("Liftoff UT Callback error: " + e.getMessage());
              }
            });
  }

  private void handleLiftoff() throws RPCException, StreamException, InterruptedException {
    switch (liftoffMode) {
      case GRAVITY_TURN:
        gravityTurn();
        break;
      case FINALIZE_ORBIT:
        finalizeOrbit();
        break;
      case CIRCULARIZE:
        circularizeOrbitOnApoapsis();
        isLiftoffRunning = false;
        break;
    }
  }

  private void gravityTurn() throws RPCException, StreamException, InterruptedException {
    if (currentPitch > 1 && !targetApoapsisReached) {
      double altitudeProgress =
          Utilities.remap(startCurveAlt, finalApoapsisAlt, 1, 0.01, vessel.altitude.get(), false);
      currentPitch = (float) (calculateCurrentPitch(altitudeProgress));
      double currentMaxTWR = calculateTWRBasedOnPressure(currentPitch);
      vessel.ap.setTargetPitch(currentPitch);
      double throttleValue =
          Math.min(
              thrControl.calculate(vessel.apoapsis.get() / finalApoapsisAlt * 1000, 1000),
              vessel.getMaxThrottleForTWR(currentMaxTWR));
      vessel.throttle(Utilities.clamp(throttleValue, 0.05, 1.0));

      if (willDecoupleStages && isCurrentStageWithoutFuel()) {
        vessel.decoupleStage();
      }
      setCurrentStatus(
          String.format(Bundle.getString("status_liftoff_inclination") + " %.1f", currentPitch));
    } else {
      vessel.throttle(0);
      liftoffMode = LIFTOFF_MODE.FINALIZE_ORBIT;
    }
  }

  private void finalizeOrbit() throws RPCException, StreamException, InterruptedException {
    if (!dynamicPressureLowEnough) {
      setCurrentStatus(Bundle.getString("status_maintaining_until_orbit"));
      vessel.getActiveVessel().getControl().setRCS(true);
      navigation.aimAtPrograde();
      vessel.throttle(thrControl.calculate(vessel.apoapsis.get() / finalApoapsisAlt * 1000, 1000));
    } else {
      vessel.throttle(0.0f);
      if (willDecoupleStages) {
        jettisonFairings();
      }
      if (willOpenPanelsAndAntenna) {
        openPanelsAndAntenna();
      }
      vessel.apoapsis.removeCallback(apoapsisCallbackTag);
      pressureStream.removeCallback(pressureCallbackTag);
      liftoffMode = LIFTOFF_MODE.CIRCULARIZE;
    }
  }

  private void cleanup() {
    try {
      isLiftoffRunning = false;
      vessel.apoapsis.removeCallback(apoapsisCallbackTag);
      pressureStream.removeCallback(pressureCallbackTag);
      vessel.missionTime.removeCallback(utCallbackTag);
      pressureStream.remove();
      vessel.ap.disengage();
      vessel.throttle(0);
    } catch (RPCException | NullPointerException e) {
      // ignore
    }
  }

  private double calculateTWRBasedOnPressure(float currentPitch) throws RPCException {
    float currentPressure = vessel.flightParameters.getDynamicPressure();
    if (currentPressure <= 10) {
      return Utilities.remap(90.0, 0.0, maxTWR, 5.0, currentPitch, true);
    }
    return Utilities.remap(22000.0, 10.0, maxTWR, 5.0, currentPressure, true);
  }

  private void circularizeOrbitOnApoapsis() {
    setCurrentStatus(Bundle.getString("status_planning_orbit"));
    Map<String, String> commands = new HashMap<>();
    commands.put(Module.MODULO.get(), Module.MANEUVER.get());
    commands.put(Module.FUNCTION.get(), Module.APOAPSIS.get());
    commands.put(Module.FINE_ADJUST.get(), String.valueOf(false));
    vessel.getVesselManager().startModule(commands);
  }

  private void jettisonFairings() throws RPCException, InterruptedException {
    List<Fairing> fairings = vessel.getActiveVessel().getParts().getFairings();
    if (fairings.size() > 0) {
      setCurrentStatus(Bundle.getString("status_jettisoning_shields"));
      for (Fairing f : fairings) {
        if (f.getJettisoned()) {
          String eventName = f.getPart().getModules().get(0).getEvents().get(0);
          f.getPart().getModules().get(0).triggerEvent(eventName);
          Thread.sleep(5000);
        }
      }
    }
  }

  private void openPanelsAndAntenna() throws RPCException, InterruptedException {
    vessel.getActiveVessel().getControl().setSolarPanels(true);
    vessel.getActiveVessel().getControl().setRadiators(true);
    vessel.getActiveVessel().getControl().setAntennas(true);
  }

  private double calculateCurrentPitch(double currentAltitude) {
    if (gravityCurveModel.equals(Module.QUADRATIC.get())) {
      return Utilities.easeInQuad(currentAltitude) * PITCH_UP;
    }
    if (gravityCurveModel.equals(Module.CUBIC.get())) {
      return Utilities.easeInCubic(currentAltitude) * PITCH_UP;
    }
    if (gravityCurveModel.equals(Module.SINUSOIDAL.get())) {
      return Utilities.easeInSine(currentAltitude) * PITCH_UP;
    }
    if (gravityCurveModel.equals(Module.EXPONENCIAL.get())) {
      return Utilities.easeInExpo(currentAltitude) * PITCH_UP;
    }
    return Utilities.easeInCirc(currentAltitude) * PITCH_UP;
  }

  private boolean isCurrentStageWithoutFuel() throws RPCException {
    for (Engine engine : vessel.getActiveVessel().getParts().getEngines()) {
      if (engine.getPart().getStage() == vessel.getActiveVessel().getControl().getCurrentStage()
          && !engine.getHasFuel()) {
        return true;
      }
    }
    return false;
  }
}

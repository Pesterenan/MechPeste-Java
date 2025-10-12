package com.pesterenan.model;

import com.pesterenan.controllers.Controller;
import com.pesterenan.controllers.DockingController;
import com.pesterenan.controllers.LandingController;
import com.pesterenan.controllers.LiftoffController;
import com.pesterenan.controllers.ManeuverController;
import com.pesterenan.controllers.RoverController;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Module;
import com.pesterenan.utils.Telemetry;
import com.pesterenan.utils.Vector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.AutoPilot;
import krpc.client.services.SpaceCenter.CelestialBody;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;

public class ActiveVessel {

  public Vessel activeVessel;
  public SpaceCenter spaceCenter;
  public Connection connection;
  private final Map<Telemetry, Double> telemetryData = new ConcurrentHashMap<>();
  public AutoPilot ap;
  public Flight flightParameters;
  public ReferenceFrame orbitalReferenceFrame;
  public Stream<Float> totalMass;
  public ReferenceFrame surfaceReferenceFrame;
  public float totalCharge;
  public double gravityAcel;
  public CelestialBody currentBody;
  public Stream<Double> altitude;
  public Stream<Double> surfaceAltitude;
  public Stream<Double> apoapsis;
  public Stream<Double> periapsis;
  public Stream<Double> verticalVelocity;
  public Stream<Double> missionTime;
  public Stream<Double> horizontalVelocity;
  public Map<String, String> commands;
  public int currentVesselId = 0;
  public Thread controllerThread = null;
  public Controller controller;
  public long timer = 0;
  private String currentStatus = Bundle.getString("status_ready");
  private boolean runningModule;
  private ConnectionManager connectionManager;
  private VesselManager vesselManager;

  public ActiveVessel(ConnectionManager connectionManager, VesselManager vesselManager) {
    this.connectionManager = connectionManager;
    this.vesselManager = vesselManager;
    this.connection = connectionManager.getConnection();
    this.spaceCenter = connectionManager.getSpaceCenter();
    reinitialize();
  }

  public ConnectionManager getConnectionManager() {
    return connectionManager;
  }

  public VesselManager getVesselManager() {
    return vesselManager;
  }

  public String getCurrentStatus() {
    if (controller != null) {
      return controller.getCurrentStatus();
    }
    return currentStatus;
  }

  public void setCurrentStatus(String status) {
    if (controller != null) {
      controller.setCurrentStatus(status);
    } else {
      this.currentStatus = status;
    }
  }

  public int getCurrentVesselId() {
    return currentVesselId;
  }

  public Vessel getActiveVessel() {
    return activeVessel;
  }

  public void setActiveVessel(Vessel currentVessel) {
    activeVessel = currentVessel;
  }

  public void throttle(float acel) throws RPCException {
    getActiveVessel().getControl().setThrottle(Math.min(acel, 1.0f));
  }

  public void throttle(double acel) throws RPCException {
    throttle((float) acel);
  }

  public void tuneAutoPilot() throws RPCException {
    ap.setTimeToPeak(new Vector(2, 2, 2).toTriplet());
    ap.setDecelerationTime(new Vector(5, 5, 5).toTriplet());
  }

  public void liftoff() {
    try {
      getActiveVessel().getControl().setSAS(true);
      throttleUp(getMaxThrottleForTWR(2.0), 1);
      if (getActiveVessel().getSituation().equals(VesselSituation.PRE_LAUNCH)) {
        for (double count = 5.0; count >= 0; count -= 0.1) {
          if (Thread.interrupted()) {
            throw new InterruptedException();
          }
          setCurrentStatus(String.format(Bundle.getString("status_launching_in"), count));
          Thread.sleep(100);
        }
        spaceCenter.setActiveVessel(activeVessel);
        getActiveVessel().getControl().activateNextStage();
      }
      setCurrentStatus(Bundle.getString("status_liftoff"));
    } catch (RPCException | InterruptedException | StreamException ignored) {
      setCurrentStatus(Bundle.getString("status_liftoff_abort"));
    }
  }

  public double getTWR() throws RPCException, StreamException {
    return getActiveVessel().getAvailableThrust() / (totalMass.get() * gravityAcel);
  }

  public double getMaxThrottleForTWR(double targetTWR) throws RPCException, StreamException {
    return targetTWR / getTWR();
  }

  public double getMaxAcel(float maxTWR) throws RPCException, StreamException {
    return Math.min(maxTWR, getTWR()) * gravityAcel - gravityAcel;
  }

  public void startModule(Map<String, String> commands) {
    String currentFunction = commands.get(Module.MODULO.get());
    if (controllerThread != null) {
      controllerThread.interrupt();
      runningModule = false;
    }
    if (currentFunction.equals(Module.LIFTOFF.get())) {
      controller = new LiftoffController(this, commands);
      runningModule = true;
    }
    if (currentFunction.equals(Module.HOVERING.get())
        || currentFunction.equals(Module.LANDING.get())) {
      controller = new LandingController(this, commands);
      runningModule = true;
    }
    if (currentFunction.equals(Module.MANEUVER.get())) {
      controller = new ManeuverController(this, commands);
      runningModule = true;
    }
    if (currentFunction.equals(Module.ROVER.get())) {
      controller = new RoverController(this, commands);
      runningModule = true;
    }
    if (currentFunction.equals(Module.DOCKING.get())) {
      controller = new DockingController(this, commands);
      runningModule = true;
    }
    String controllerThreadName = "MP_CTRL_" + currentFunction + "_" + currentVesselId;
    controllerThread = new Thread(controller, controllerThreadName);
    controllerThread.start();
  }

  public Thread getControllerThread() {
    return controllerThread;
  }

  public void cancelControl() {
    if (controllerThread != null) {
      controllerThread.interrupt();
      runningModule = false;
    }
  }

  public void removeStreams() {
    // Interrupt any running controller thread
    if (controllerThread != null && controllerThread.isAlive()) {
      controllerThread.interrupt();
      runningModule = false;
    }
    // Close all streams safely
    try {
      if (totalMass != null) totalMass.remove();
    } catch (Exception e) {
      /* ignore */
    }
    try {
      if (altitude != null) altitude.remove();
    } catch (Exception e) {
      /* ignore */
    }
    try {
      if (surfaceAltitude != null) surfaceAltitude.remove();
    } catch (Exception e) {
      /* ignore */
    }
    try {
      if (apoapsis != null) apoapsis.remove();
    } catch (Exception e) {
      /* ignore */
    }
    try {
      if (periapsis != null) periapsis.remove();
    } catch (Exception e) {
      /* ignore */
    }
    try {
      if (verticalVelocity != null) verticalVelocity.remove();
    } catch (Exception e) {
      /* ignore */
    }
    try {
      if (horizontalVelocity != null) horizontalVelocity.remove();
    } catch (Exception e) {
      /* ignore */
    }
    // Clear the telemetry data map
    telemetryData.clear();
  }

  public Map<Telemetry, Double> getTelemetryData() {
    return telemetryData;
  }

  public boolean hasModuleRunning() {
    return runningModule;
  }

  public void decoupleStage() throws RPCException, InterruptedException {
    setCurrentStatus(Bundle.getString("status_separating_stage"));
    spaceCenter.setActiveVessel(getActiveVessel());
    double currentThrottle = getActiveVessel().getControl().getThrottle();
    throttle(0);
    Thread.sleep(1000);
    getActiveVessel().getControl().activateNextStage();
    throttleUp(currentThrottle, 1);
  }

  public void throttleUp(double throttleAmount, double seconds)
      throws RPCException, InterruptedException {
    double secondsElapsed = 0;
    while (secondsElapsed < seconds) {
      throttle(secondsElapsed / seconds * throttleAmount);
      secondsElapsed += 0.1;
      Thread.sleep(100);
    }
  }

  public void reinitialize() {
    // Clean up any existing streams or threads before creating new ones
    removeStreams();

    System.out.println("DEBUG: Re-initializing ActiveVessel parameters...");
    try {
      setActiveVessel(spaceCenter.getActiveVessel());
      currentVesselId = getActiveVessel().hashCode();
      ap = getActiveVessel().getAutoPilot();
      currentBody = getActiveVessel().getOrbit().getBody();
      gravityAcel = currentBody.getSurfaceGravity();
      orbitalReferenceFrame = currentBody.getReferenceFrame();
      surfaceReferenceFrame = getActiveVessel().getSurfaceReferenceFrame();
      flightParameters = getActiveVessel().flight(orbitalReferenceFrame);

      System.out.println("DEBUG: Basic parameters set. Creating streams...");
      altitude = connection.addStream(flightParameters, "getMeanAltitude");
      apoapsis = connection.addStream(getActiveVessel().getOrbit(), "getApoapsisAltitude");
      horizontalVelocity = connection.addStream(flightParameters, "getHorizontalSpeed");
      periapsis = connection.addStream(getActiveVessel().getOrbit(), "getPeriapsisAltitude");
      surfaceAltitude = connection.addStream(flightParameters, "getSurfaceAltitude");
      totalMass = connection.addStream(getActiveVessel(), "getMass");
      verticalVelocity = connection.addStream(flightParameters, "getVerticalSpeed");
      missionTime = connection.addStream(spaceCenter.getClass(), "getUT");

      altitude.addCallback(val -> telemetryData.put(Telemetry.ALTITUDE, val < 0 ? 0 : val));
      apoapsis.addCallback(val -> telemetryData.put(Telemetry.APOAPSIS, val < 0 ? 0 : val));
      horizontalVelocity.addCallback(
          val -> telemetryData.put(Telemetry.HORZ_SPEED, val < 0 ? 0 : val));
      periapsis.addCallback(val -> telemetryData.put(Telemetry.PERIAPSIS, val < 0 ? 0 : val));
      surfaceAltitude.addCallback(val -> telemetryData.put(Telemetry.ALT_SURF, val < 0 ? 0 : val));
      verticalVelocity.addCallback(val -> telemetryData.put(Telemetry.VERT_SPEED, val));

      altitude.start();
      apoapsis.start();
      horizontalVelocity.start();
      periapsis.start();
      surfaceAltitude.start();
      totalMass.start();
      verticalVelocity.start();
      missionTime.start();
      System.out.println("DEBUG: All streams created successfully.");
    } catch (RPCException | StreamException e) {
      System.err.println(
          "DEBUG: CRITICAL ERROR while re-initializing parameters for active vessel: "
              + e.getMessage());
    }
  }
}

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

  protected Vessel activeVessel;
  protected SpaceCenter spaceCenter;
  protected Connection connection;
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
  protected int currentVesselId = 0;
  protected Thread controllerThread = null;
  protected Controller controller;
  protected long timer = 0;
  private String currentStatus = Bundle.getString("status_ready");
  private boolean runningModule;
  private ConnectionManager connectionManager;
  private VesselManager vesselManager;

  public ActiveVessel(ConnectionManager connectionManager, VesselManager vesselManager) {
    this.connectionManager = connectionManager;
    this.vesselManager = vesselManager;
    this.connection = connectionManager.getConnection();
    this.spaceCenter = connectionManager.getSpaceCenter();
    initializeParameters();
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
      controller = new LiftoffController(this.connectionManager, this.vesselManager, commands);
      runningModule = true;
    }
    if (currentFunction.equals(Module.HOVERING.get())
        || currentFunction.equals(Module.LANDING.get())) {
      controller = new LandingController(this.connectionManager, this.vesselManager, commands);
      runningModule = true;
    }
    if (currentFunction.equals(Module.MANEUVER.get())) {
      controller = new ManeuverController(this.connectionManager, this.vesselManager, commands);
      runningModule = true;
    }
    if (currentFunction.equals(Module.ROVER.get())) {
      controller = new RoverController(this.connectionManager, this.vesselManager, commands);
      runningModule = true;
    }
    if (currentFunction.equals(Module.DOCKING.get())) {
      controller = new DockingController(this.connectionManager, this.vesselManager, commands);
      runningModule = true;
    }
    controllerThread = new Thread(controller, currentVesselId + " - " + currentFunction);
    controllerThread.start();
  }

  public Map<Telemetry, Double> getTelemetryData() {
    return telemetryData;
  }

  public void cancelControl() {
    try {
      ap.disengage();
      throttle(0);
    } catch (RPCException ignored) {
    }
    if (controllerThread != null) {
      controllerThread.interrupt();
      runningModule = false;
    }
  }

  public boolean hasModuleRunning() {
    return runningModule;
  }

  protected void decoupleStage() throws RPCException, InterruptedException {
    setCurrentStatus(Bundle.getString("status_separating_stage"));
    spaceCenter.setActiveVessel(getActiveVessel());
    double currentThrottle = getActiveVessel().getControl().getThrottle();
    throttle(0);
    Thread.sleep(1000);
    getActiveVessel().getControl().activateNextStage();
    throttleUp(currentThrottle, 1);
  }

  protected void throttleUp(double throttleAmount, double seconds)
      throws RPCException, InterruptedException {
    double secondsElapsed = 0;
    while (secondsElapsed < seconds) {
      throttle(secondsElapsed / seconds * throttleAmount);
      secondsElapsed += 0.1;
      Thread.sleep(100);
    }
  }

  private void initializeParameters() {
    try {
      setActiveVessel(spaceCenter.getActiveVessel());
      currentVesselId = getActiveVessel().hashCode();
      ap = getActiveVessel().getAutoPilot();
      currentBody = getActiveVessel().getOrbit().getBody();
      gravityAcel = currentBody.getSurfaceGravity();
      orbitalReferenceFrame = currentBody.getReferenceFrame();
      surfaceReferenceFrame = getActiveVessel().getSurfaceReferenceFrame();
      flightParameters = getActiveVessel().flight(orbitalReferenceFrame);
      totalMass = connection.addStream(getActiveVessel(), "getMass");
      totalMass.start();

      // Add callbacks to update telemetry data automatically
      altitude = connection.addStream(flightParameters, "getMeanAltitude");
      altitude.addCallback(val -> telemetryData.put(Telemetry.ALTITUDE, val < 0 ? 0 : val));
      altitude.start();

      surfaceAltitude = connection.addStream(flightParameters, "getSurfaceAltitude");
      surfaceAltitude.addCallback(val -> telemetryData.put(Telemetry.ALT_SURF, val < 0 ? 0 : val));
      surfaceAltitude.start();

      apoapsis = connection.addStream(getActiveVessel().getOrbit(), "getApoapsisAltitude");
      apoapsis.addCallback(val -> telemetryData.put(Telemetry.APOAPSIS, val < 0 ? 0 : val));
      apoapsis.start();

      periapsis = connection.addStream(getActiveVessel().getOrbit(), "getPeriapsisAltitude");
      periapsis.addCallback(val -> telemetryData.put(Telemetry.PERIAPSIS, val < 0 ? 0 : val));
      periapsis.start();

      verticalVelocity = connection.addStream(flightParameters, "getVerticalSpeed");
      verticalVelocity.addCallback(val -> telemetryData.put(Telemetry.VERT_SPEED, val));
      verticalVelocity.start();

      horizontalVelocity = connection.addStream(flightParameters, "getHorizontalSpeed");
      horizontalVelocity.addCallback(
          val -> telemetryData.put(Telemetry.HORZ_SPEED, val < 0 ? 0 : val));
      horizontalVelocity.start();

    } catch (RPCException | StreamException e) {
      System.err.println(
          "Error while initializing parameters for active vessel: " + e.getMessage());
    }
  }
}

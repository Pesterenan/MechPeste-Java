package com.pesterenan.model;

import com.pesterenan.MechPeste;
import com.pesterenan.controllers.Controller;
import com.pesterenan.controllers.LandingController;
import com.pesterenan.controllers.LiftoffController;
import com.pesterenan.controllers.ManeuverController;
import com.pesterenan.controllers.RoverController;
import com.pesterenan.controllers.DockingController;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Module;
import com.pesterenan.utils.Telemetry;
import com.pesterenan.utils.Vector;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.AutoPilot;
import krpc.client.services.SpaceCenter.CelestialBody;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;

import java.util.HashMap;
import java.util.Map;

import static com.pesterenan.MechPeste.getConnection;
import static com.pesterenan.MechPeste.getSpaceCenter;

public class ActiveVessel {

    protected Vessel activeVessel;
    private final Map<Telemetry,Double> telemetryData = new HashMap<>();
    public AutoPilot ap;
    public Flight flightParameters;
    public ReferenceFrame orbitalReferenceFrame;
    protected Stream<Float> totalMass;
    public ReferenceFrame surfaceReferenceFrame;
    public float totalCharge;
    public float gravityAcel;
    public CelestialBody currentBody;
    public Stream<Double> altitude;
    public Stream<Double> surfaceAltitude;
    public Stream<Double> apoapsis;
    public Stream<Double> periapsis;
    public Stream<Double> verticalVelocity;
    public Stream<Double> missionTime;
    public Stream<Double> horizontalVelocity;
    public Map<String,String> commands;
    protected int currentVesselId = 0;
    protected Thread controllerThread = null;
    protected Controller controller;
    protected long timer = 0;
    private String currentStatus = Bundle.getString("status_ready");
    private boolean runningModule;

    public ActiveVessel() {
        initializeParameters();
    }

    private void initializeParameters() {
        try {
            setActiveVessel(getSpaceCenter().getActiveVessel());
            currentVesselId = getActiveVessel().hashCode();
            ap = getActiveVessel().getAutoPilot();
            currentBody = getActiveVessel().getOrbit().getBody();
            gravityAcel = currentBody.getSurfaceGravity();
            orbitalReferenceFrame = currentBody.getReferenceFrame();
            surfaceReferenceFrame = getActiveVessel().getSurfaceReferenceFrame();
            flightParameters = getActiveVessel().flight(orbitalReferenceFrame);
            totalMass = getConnection().addStream(getActiveVessel(), "getMass");
            altitude = getConnection().addStream(flightParameters, "getMeanAltitude");
            surfaceAltitude = getConnection().addStream(flightParameters, "getSurfaceAltitude");
            apoapsis = getConnection().addStream(getActiveVessel().getOrbit(), "getApoapsisAltitude");
            periapsis = getConnection().addStream(getActiveVessel().getOrbit(), "getPeriapsisAltitude");
            verticalVelocity = getConnection().addStream(flightParameters, "getVerticalSpeed");
            horizontalVelocity = getConnection().addStream(flightParameters, "getHorizontalSpeed");
        } catch (RPCException | StreamException e) {
            MechPeste.newInstance().checkConnection();
        }
    }

    public String getCurrentStatus() {
        if (controller != null) {
            return controller.getCurrentStatus();
        }
        return currentStatus;
    }

    public void setCurrentStatus(String status) {
        currentStatus = status;
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
                getSpaceCenter().setActiveVessel(activeVessel);
                getActiveVessel().getControl().activateNextStage();
            }
            setCurrentStatus(Bundle.getString("status_liftoff"));
        } catch (RPCException | InterruptedException | StreamException ignored) {
            setCurrentStatus(Bundle.getString("status_liftoff_abort"));
        }
    }

    protected void decoupleStage() throws RPCException, InterruptedException {
        setCurrentStatus(Bundle.getString("status_separating_stage"));
        MechPeste.getSpaceCenter().setActiveVessel(getActiveVessel());
        double currentThrottle = getActiveVessel().getControl().getThrottle();
        throttle(0);
        Thread.sleep(1000);
        getActiveVessel().getControl().activateNextStage();
        throttleUp(currentThrottle, 1);
    }

    protected void throttleUp(double throttleAmount, double seconds) throws RPCException, InterruptedException {
        double secondsElapsed = 0;
        while (secondsElapsed < seconds) {
            throttle(secondsElapsed / seconds * throttleAmount);
            secondsElapsed += 0.1;
            Thread.sleep(100);
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

    public void startModule(Map<String,String> commands) {
        String currentFunction = commands.get(Module.MODULO.get());
        if (controllerThread != null) {
            controllerThread.interrupt();
            runningModule = false;
        }
        if (currentFunction.equals(Module.LIFTOFF.get())) {
            controller = new LiftoffController(commands);
            runningModule = true;
        }
        if (currentFunction.equals(Module.HOVERING.get()) || currentFunction.equals(Module.LANDING.get())) {
            controller = new LandingController(commands);
            runningModule = true;
        }
        if (currentFunction.equals(Module.MANEUVER.get())) {
            controller = new ManeuverController(commands);
            runningModule = true;
        }
        if (currentFunction.equals(Module.ROVER.get())) {
            controller = new RoverController(commands);
            runningModule = true;
        }
        if (currentFunction.equals(Module.DOCKING.get())) {
            controller = new DockingController(commands);
            System.out.println("escolheu modulo docking");
            runningModule = true;
        }
        controllerThread = new Thread(controller, currentVesselId + " - " + currentFunction);
        controllerThread.start();
    }

    public void recordTelemetryData() throws RPCException {
        if (getActiveVessel().getOrbit().getBody() != currentBody) {
            initializeParameters();
        }
        synchronized (telemetryData) {
            try {
                telemetryData.put(Telemetry.ALTITUDE, altitude.get() < 0 ? 0 : altitude.get());
                telemetryData.put(Telemetry.ALT_SURF, surfaceAltitude.get() < 0 ? 0 : surfaceAltitude.get());
                telemetryData.put(Telemetry.APOAPSIS, apoapsis.get() < 0 ? 0 : apoapsis.get());
                telemetryData.put(Telemetry.PERIAPSIS, periapsis.get() < 0 ? 0 : periapsis.get());
                telemetryData.put(Telemetry.VERT_SPEED, verticalVelocity.get());
                telemetryData.put(Telemetry.HORZ_SPEED, horizontalVelocity.get() < 0 ? 0 : horizontalVelocity.get());
            } catch (RPCException | StreamException ignored) {
            }
        }
    }

    public Map<Telemetry,Double> getTelemetryData() {
        return telemetryData;
    }

    public void cancelControl() {
        try {
            ap.disengage();
            throttle(0);
            if (controllerThread != null) {
                controllerThread.interrupt();
                runningModule = false;
            }
        } catch (RPCException ignored) {
        }
    }

    public boolean hasModuleRunning() {
        return runningModule;
    }
}

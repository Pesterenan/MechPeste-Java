package com.pesterenan.model;

import com.pesterenan.MechPeste;
import com.pesterenan.controllers.Controller;
import com.pesterenan.controllers.LandingController;
import com.pesterenan.controllers.LiftoffController;
import com.pesterenan.controllers.ManeuverController;
import com.pesterenan.controllers.RoverController;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Modulos;
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

	protected Vessel naveAtual;
	private final Map<Telemetry, Double> telemetryData = new HashMap<>();
	public AutoPilot ap;
	public Flight parametrosDeVoo;
	public ReferenceFrame orbitalReferenceFrame;
	protected Stream<Float> massaTotal;
	public ReferenceFrame surfaceReferenceFrame;
	public float bateriaTotal;
	public float gravityAcel;
	public CelestialBody currentBody;
	public Stream<Double> altitude;
	public Stream<Double> altitudeSup;
	public Stream<Double> apoastro;
	public Stream<Double> periastro;
	public Stream<Double> velVertical;
	public Stream<Double> tempoMissao;
	public Stream<Double> velHorizontal;
	public Map<String, String> commands;
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
			setNaveAtual(getSpaceCenter().getActiveVessel());
			currentVesselId = getNaveAtual().hashCode();
			ap = getNaveAtual().getAutoPilot();
			currentBody = getNaveAtual().getOrbit().getBody();
			gravityAcel = currentBody.getSurfaceGravity();
			orbitalReferenceFrame = currentBody.getReferenceFrame();
			surfaceReferenceFrame = getNaveAtual().getSurfaceReferenceFrame();
			parametrosDeVoo = getNaveAtual().flight(orbitalReferenceFrame);
			massaTotal = getConnection().addStream(getNaveAtual(), "getMass");
			altitude = getConnection().addStream(parametrosDeVoo, "getMeanAltitude");
			altitudeSup = getConnection().addStream(parametrosDeVoo, "getSurfaceAltitude");
			apoastro = getConnection().addStream(getNaveAtual().getOrbit(), "getApoapsisAltitude");
			periastro = getConnection().addStream(getNaveAtual().getOrbit(), "getPeriapsisAltitude");
			velVertical = getConnection().addStream(parametrosDeVoo, "getVerticalSpeed");
			velHorizontal = getConnection().addStream(parametrosDeVoo, "getHorizontalSpeed");
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

	public Vessel getNaveAtual() {
		return naveAtual;
	}

	public void setNaveAtual(Vessel currentVessel) {
		naveAtual = currentVessel;
	}

	public void throttle(float acel) throws RPCException {
		getNaveAtual().getControl().setThrottle(Math.min(acel, 1.0f));
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
			getNaveAtual().getControl().setSAS(true);
			throttleUp(getMaxThrottleForTWR(2.0), 1);
			if (getNaveAtual().getSituation().equals(VesselSituation.PRE_LAUNCH)) {
				for (double count = 5.0; count >= 0; count -= 0.1) {
					if (Thread.interrupted()) {
						throw new InterruptedException();
					}
					setCurrentStatus(String.format(Bundle.getString("status_launching_in"), count));
					Thread.sleep(100);
				}
				getSpaceCenter().setActiveVessel(naveAtual);
				getNaveAtual().getControl().activateNextStage();
			}
			setCurrentStatus(Bundle.getString("status_liftoff"));
		} catch (RPCException | InterruptedException | StreamException ignored) {
			setCurrentStatus(Bundle.getString("status_liftoff_abort"));
		}
	}

	protected void decoupleStage() throws RPCException, InterruptedException {
		setCurrentStatus(Bundle.getString("status_separating_stage"));
		MechPeste.getSpaceCenter().setActiveVessel(getNaveAtual());
		double currentThrottle = getNaveAtual().getControl().getThrottle();
		throttle(0);
		Thread.sleep(1000);
		getNaveAtual().getControl().activateNextStage();
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
		return getNaveAtual().getAvailableThrust() / (massaTotal.get() * gravityAcel);
	}

	public double getMaxThrottleForTWR(double targetTWR) throws RPCException, StreamException {
		return targetTWR / getTWR();
	}

	public double getMaxAcel(float maxTWR) throws RPCException, StreamException {
		return Math.min(maxTWR, getTWR()) * gravityAcel - gravityAcel;
	}

	public void startModule(Map<String, String> commands) {
		String currentFunction = commands.get(Modulos.MODULO.get());
		if (controllerThread != null) {
			controllerThread.interrupt();
			runningModule = false;
		}
		if (currentFunction.equals(Modulos.MODULO_DECOLAGEM.get())) {
			controller = new LiftoffController(commands);
			runningModule = true;
		}
		if (currentFunction.equals(Modulos.MODULO_POUSO_SOBREVOAR.get()) ||
				currentFunction.equals(Modulos.MODULO_POUSO.get())) {
			controller = new LandingController(commands);
			runningModule = true;
		}
		if (currentFunction.equals(Modulos.MODULO_MANOBRAS.get())) {
			controller = new ManeuverController(commands);
			runningModule = true;
		}
		if (currentFunction.equals(Modulos.MODULO_ROVER.get())) {
			controller = new RoverController(commands);
			runningModule = true;
		}
		controllerThread = new Thread(controller, currentVesselId + " - " + currentFunction);
		controllerThread.start();
	}

	public void recordTelemetryData() throws RPCException {
		if (getNaveAtual().getOrbit().getBody() != currentBody) {
			initializeParameters();
		}
		synchronized (telemetryData) {
			try {
				telemetryData.put(Telemetry.ALTITUDE, altitude.get() < 0 ? 0 : altitude.get());
				telemetryData.put(Telemetry.ALT_SURF, altitudeSup.get() < 0 ? 0 : altitudeSup.get());
				telemetryData.put(Telemetry.APOAPSIS, apoastro.get() < 0 ? 0 : apoastro.get());
				telemetryData.put(Telemetry.PERIAPSIS, periastro.get() < 0 ? 0 : periastro.get());
				telemetryData.put(Telemetry.VERT_SPEED, velVertical.get());
				telemetryData.put(Telemetry.HORZ_SPEED, velHorizontal.get() < 0 ? 0 : velHorizontal.get());
			} catch (RPCException | StreamException ignored) {
			}
		}
	}

	public Map<Telemetry, Double> getTelemetryData() {
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

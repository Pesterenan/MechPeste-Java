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
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.KRPC.GameScene;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.AutoPilot;
import krpc.client.services.SpaceCenter.CelestialBody;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;

import java.util.HashMap;
import java.util.Map;

import static com.pesterenan.views.StatusJPanel.isBtnConnectVisible;

public class ActiveVessel implements Runnable {

	public final static float CONST_GRAV = 9.81f;

	public static SpaceCenter centroEspacial;
	private static Connection conexao;
	protected Vessel naveAtual;
	private final Map<Telemetry, Double> telemetryData = new HashMap<>();
	public AutoPilot ap;
	public Flight parametrosDeVoo;
	public ReferenceFrame pontoRefOrbital;
	protected Stream<Float> massaTotal;
	public ReferenceFrame pontoRefSuperficie;
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
	protected Thread activeVesselThread;
	protected Thread controllerThread;
	private long timer = 0;
	private String currentStatus = Bundle.getString("status_ready");

	public ActiveVessel() {
	}

	public ActiveVessel(Connection connection, Vessel currentVessel, int currentVesselId) {
		setConnection(connection);
		setNaveAtual(currentVessel);
		this.currentVesselId = currentVesselId;
		initializeParameters();
		activeVesselThread = new Thread(this, "Vessel nº" + getNaveAtual().hashCode());
		activeVesselThread.start();
	}

	public static Connection getConnection() {
		return conexao;
	}

	private void setConnection(Connection con) {
		conexao = con;
	}

	private void initializeParameters() {
		try {
			ap = getNaveAtual().getAutoPilot();
			centroEspacial = SpaceCenter.newInstance(getConnection());
			currentBody = getNaveAtual().getOrbit().getBody();
			pontoRefOrbital = currentBody.getReferenceFrame();
			pontoRefSuperficie = getNaveAtual().getSurfaceReferenceFrame();
			parametrosDeVoo = getNaveAtual().flight(pontoRefOrbital);
			massaTotal = getConnection().addStream(getNaveAtual(), "getMass");
			altitude = getConnection().addStream(parametrosDeVoo, "getMeanAltitude");
			altitudeSup = getConnection().addStream(parametrosDeVoo, "getSurfaceAltitude");
			apoastro = getConnection().addStream(getNaveAtual().getOrbit(), "getApoapsisAltitude");
			periastro = getConnection().addStream(getNaveAtual().getOrbit(), "getPeriapsisAltitude");
			velVertical = getConnection().addStream(parametrosDeVoo, "getVerticalSpeed");
			velHorizontal = getConnection().addStream(parametrosDeVoo, "getHorizontalSpeed");
			timer = System.currentTimeMillis();
		} catch (RPCException | StreamException e) {
			checarConexao();
		}
	}

	public String getCurrentStatus() {
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

	public void checarConexao() {
		try {
			if (MechPeste.newInstance().getCurrentGameScene().equals(GameScene.FLIGHT)) {
				setNaveAtual(centroEspacial.getActiveVessel());
				setCurrentStatus(Bundle.getString("status_connected"));
				isBtnConnectVisible(false);
			} else {
				setCurrentStatus(Bundle.getString("status_ready"));
			}
		} catch (RPCException | NullPointerException e) {
			setCurrentStatus(Bundle.getString("status_error_connection"));
			isBtnConnectVisible(true);
		}
	}

	public void throttle(float acel) throws RPCException {
		getNaveAtual().getControl().setThrottle(acel);
	}

	public void throttle(double acel) throws RPCException {
		throttle((float) acel);
	}

	public void tuneAutoPilot() throws RPCException {
		ap.setTimeToPeak(new Vector(5, 5, 5).toTriplet());
		ap.setDecelerationTime(new Vector(5, 5, 5).toTriplet());
	}

	public void liftoff() {
		try {
			getNaveAtual().getControl().setSAS(true);
			throttle(1f);
			if (getNaveAtual().getSituation().equals(VesselSituation.PRE_LAUNCH)) {
				double count = 5.0;
				while (count > 0) {
					long currentTime = System.currentTimeMillis();
					if (currentTime > timer + 100) {
						setCurrentStatus(String.format(Bundle.getString("status_launching_in"), count));
						count -= 0.1;
						timer = currentTime;
					}
				}
				centroEspacial.setActiveVessel(naveAtual);
				getNaveAtual().getControl().activateNextStage();
			}
			setCurrentStatus(Bundle.getString("status_liftoff"));
		} catch (RPCException ignored) {
		}
	}

	public double getTWR() throws RPCException, StreamException {
		return getNaveAtual().getAvailableThrust() / ((massaTotal.get() * gravityAcel));
	}

	public double getMaxAcel() throws RPCException, StreamException {
		return getTWR() * gravityAcel - gravityAcel;
	}

	public void disengageAfterException(String statusMessage) {
		try {
			setCurrentStatus(statusMessage);
			ap.setReferenceFrame(pontoRefSuperficie);
			ap.disengage();
			throttle(0);
			Thread.sleep(3000);
			setCurrentStatus(Bundle.getString("status_ready"));
		} catch (Exception ignored) {
		}
	}

	public void startModule(Map<String, String> commands) {
		this.commands = commands;
		String currentFunction = commands.get(Modulos.MODULO.get());
		Controller controller;
		if (currentFunction.equals(Modulos.MODULO_DECOLAGEM.get())) {
			controller = new LiftoffController(this);
			controllerThread = new Thread(controller, activeVesselThread.getName() + "Liftoff");
			controllerThread.start();
		}
		if (currentFunction.equals(Modulos.MODULO_POUSO_SOBREVOAR.get()) ||
				currentFunction.equals(Modulos.MODULO_POUSO.get())) {
			controller = new LandingController(this);
			controllerThread = new Thread(controller, activeVesselThread.getName() + "Landing");
			controllerThread.start();
		}
		if (currentFunction.equals(Modulos.MODULO_MANOBRAS.get())) {
			controller = new ManeuverController(this);
			controllerThread = new Thread(controller, activeVesselThread.getName() + "Maneuver");
			controllerThread.start();
		}
		if (currentFunction.equals(Modulos.MODULO_ROVER.get())) {
			controller = new RoverController(this);
			controllerThread = new Thread(controller, activeVesselThread.getName() + "Rover");
			controllerThread.start();
		}
		System.out.println(currentFunction);
	}

	public void recordTelemetryData() {
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

	@Override
	public void run() {
		try {
			while (activeVesselThread.isAlive()) {
				long currentTime = System.currentTimeMillis();
				if (currentTime > timer + 100) {
					recordTelemetryData();
					timer = currentTime;
				}
			}
		} catch (Exception ignored) {
		}
	}
}

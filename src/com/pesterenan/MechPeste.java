package com.pesterenan;

import static com.pesterenan.utils.Dictionary.ERROR_CONNECTING;
import static com.pesterenan.utils.Dictionary.MECHPESTE;
import static com.pesterenan.utils.Dictionary.TELEMETRY;
import static com.pesterenan.utils.Status.CONNECTED;
import static com.pesterenan.utils.Status.CONNECTING;
import static com.pesterenan.utils.Status.CONNECTION_ERROR;

import java.io.IOException;
import java.util.Map;

import com.pesterenan.controllers.FlightController;
import com.pesterenan.controllers.LandingController;
import com.pesterenan.controllers.LiftoffController;
import com.pesterenan.controllers.ManeuverController;
import com.pesterenan.utils.Modules;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusJPanel;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

public class MechPeste {

	private static MechPeste mechPeste = null;

	private static Connection connection;
	private static Thread threadModules;
	private static Thread threadTelemetry;
	private static FlightController flightCtrl = null;
	private static FlightController module;

	public static void main(String[] args) throws StreamException, RPCException, IOException, InterruptedException {
		MechPeste.getInstance();
	}

	private MechPeste() {
		MainGui.getInstance();
		startConnection();
	}

	public static MechPeste getInstance() {
		if (mechPeste == null) {
			mechPeste = new MechPeste();
		}
		return mechPeste;
	}

	public void startConnection() {
		StatusJPanel.setStatus(CONNECTING.get());
		try {
			MechPeste.connection = Connection.newInstance(MECHPESTE.get());
			StatusJPanel.setStatus(CONNECTED.get());
			StatusJPanel.visibleConnectButton(false);
			startTelemetry();
		} catch (IOException e) {
			System.err.println(ERROR_CONNECTING.get() + e.getMessage());
			StatusJPanel.setStatus(CONNECTION_ERROR.get());
			StatusJPanel.visibleConnectButton(true);
		}
	}

	private void startTelemetry() {
		flightCtrl = null;
		flightCtrl = new FlightController(getConnection());
		setThreadTelemetry(null);
		setThreadTelemetry(new Thread(flightCtrl));
		getThreadTelemetry().start();
	}

	public static void startModule(Map<String, String> commands) {
		String executeModule = commands.get(Modules.MODULE.get());

		if (executeModule.equals(Modules.MANEUVER_MODULE.get())) {
			module = new ManeuverController(commands.get(Modules.FUNCTION.get()));
		}
		if (executeModule.equals(Modules.LIFTOFF_MODULE.get())) {
			module = new LiftoffController(commands);
		}
		if (executeModule.equals(Modules.LANGING_FLIGHT_MODULE.get())
				|| executeModule.equals(Modules.LANDING_MODULE.get())) {
			module = new LandingController(commands);
		}
		setThreadModules(new Thread(module));
		getThreadModules().start();
		System.out.println(Thread.getAllStackTraces());
		MainGui.getParameters().firePropertyChange(TELEMETRY.get(), 0, 1);
	}

	public static void endTask() {
		try {
			if (getThreadModules() != null && getThreadModules().isAlive()) {
				getThreadModules().interrupt();
				setThreadModules(null);
				module = null;
			}
		} catch (Exception e) {
		}

	}

	public static Connection getConnection() {
		return connection;
	}

	private static Thread getThreadModules() {
		return threadModules;
	}

	private static void setThreadModules(Thread threadModules) {
		MechPeste.threadModules = threadModules;
	}

	private static Thread getThreadTelemetry() {
		return threadTelemetry;
	}

	private static void setThreadTelemetry(Thread threadTelemetry) {
		MechPeste.threadTelemetry = threadTelemetry;
	}
}

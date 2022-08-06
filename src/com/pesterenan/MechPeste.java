package com.pesterenan;

import com.pesterenan.controllers.*;
import com.pesterenan.resources.Bundle;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusJPanel;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.KRPC;

import java.io.IOException;
import java.util.Map;

import static com.pesterenan.utils.Modulos.*;
import static com.pesterenan.views.StatusJPanel.setStatus;

public class MechPeste {
private static MechPeste mechPeste = null;
private static Connection connection;
private static Thread threadModulos;
private static Thread threadTelemetria = null;
private static FlightController flightCtrl = null;
private static KRPC krpc;

private MechPeste() {
	MainGui.getInstance();
	connectToKSP();
}

public static void main(String[] args) {
	MechPeste.getInstance();
}

public static MechPeste getInstance() {
	if (mechPeste == null) {
		mechPeste = new MechPeste();
	}
	return mechPeste;
}

public static void startModule(Map<String, String> commands) {
	String moduleToRun = commands.get(MODULO.get());
	if (moduleToRun.equals(MODULO_DECOLAGEM.get())) {
		executeLiftoffModule(commands);
	}
	if (moduleToRun.equals(MODULO_POUSO_SOBREVOAR.get()) || moduleToRun.equals(MODULO_POUSO.get())) {
		executeLandingModule(commands);
	}
	if (moduleToRun.equals(MODULO_MANOBRAS.get())) {
		executeManeuverModule(commands);
	}
	if (moduleToRun.equals(MODULO_ROVER.get())) {
		executeRoverModule(commands);
	}
	MainGui.getParametros().firePropertyChange("Telemetria", false, true);
}

private static void executeLiftoffModule(Map<String, String> commands) {
	LiftoffController liftOffController = new LiftoffController(commands);
	setModuleThread(new Thread(liftOffController));
	getModuleThread().start();
}

private static void executeLandingModule(Map<String, String> commands) {
	LandingController landingController = new LandingController(commands);
	setModuleThread(new Thread(landingController));
	getModuleThread().start();
}

private static void executeManeuverModule(Map<String, String> commands) {
	ManeuverController maneuverController = new ManeuverController(commands);
	setModuleThread(new Thread(maneuverController));
	getModuleThread().start();
}

private static void executeRoverModule(Map<String, String> commands) {
	RoverController roverController = new RoverController(commands);
	setModuleThread(new Thread(roverController));
	getModuleThread().start();
}

public static void finalizarTarefa() {
	System.out.println("Active Threads: " + Thread.activeCount());
	try {
		if (getModuleThread() != null && getModuleThread().isAlive()) {
			getModuleThread().interrupt();
			setModuleThread(null);
		}
	} catch (Exception e) {
	}
	System.out.println("Active Threads: " + Thread.activeCount());
}

public static Connection getConnection() {
	return connection;
}

private static Thread getModuleThread() {
	return threadModulos;
}

private static void setModuleThread(Thread thread) {
	threadModulos = thread;
}

private static Thread getTelemetry() {
	return threadTelemetria;
}

private static void setThreadTelemetria(Thread thread) {
	threadTelemetria = thread;
}

public void connectToKSP() {
	setStatus(Bundle.getString("status_connecting"));
	try {
		connection = null;
		connection = Connection.newInstance("MechPeste - Pesterenan");
		krpc = KRPC.newInstance(connection);
		startTelemetry();
		setStatus(Bundle.getString("status_connected"));
		StatusJPanel.isBtnConnectVisible(false);
	} catch (IOException e) {
		setStatus(Bundle.getString("status_error_connection"));
		StatusJPanel.isBtnConnectVisible(true);
	}
}

private void startTelemetry() {
	flightCtrl = new FlightController(getConnection());
	setThreadTelemetria(new Thread(flightCtrl));
	getTelemetry().start();
}

public static KRPC.GameScene getCurrentGameScene() throws RPCException {
	return krpc.getCurrentGameScene();
}
}
package com.pesterenan;

import com.pesterenan.controllers.FlightController;
import com.pesterenan.controllers.LandingController;
import com.pesterenan.controllers.LiftoffController;
import com.pesterenan.controllers.ManeuverController;
import com.pesterenan.controllers.RoverController;
import com.pesterenan.resources.Bundle;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusJPanel;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.KRPC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
private static List<Thread> runningThreads = new ArrayList<Thread>();

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
	Thread liftoffThread = new Thread(new LiftoffController(commands));
	runningThreads.add(liftoffThread);
	liftoffThread.start();
}

private static void executeLandingModule(Map<String, String> commands) {
	Thread landingThread = new Thread(new LandingController(commands));
	runningThreads.add(landingThread);
	landingThread.start();
}

private static void executeManeuverModule(Map<String, String> commands) {
	Thread maneuverThread = new Thread(new ManeuverController(commands));
	runningThreads.add(maneuverThread);
	maneuverThread.start();
}

private static void executeRoverModule(Map<String, String> commands) {
	Thread roverThread = new Thread(new RoverController(commands));
	runningThreads.add(roverThread);
	roverThread.start();
}

public static void finalizarTarefa() {
	System.out.println("Active Threads: " + Thread.activeCount());
	System.out.println(runningThreads);
	for (int i = runningThreads.size() - 1; i >= 0; i--) {
		System.out.println(runningThreads.get(i).getName());
		System.out.println(runningThreads.get(i).getState());
		runningThreads.get(i).interrupt();
		if (runningThreads.get(i).getState().equals(Thread.State.TERMINATED)) {
			runningThreads.remove(i);
		}
	}
	System.out.println("Active Threads: " + Thread.activeCount());
	System.out.println(runningThreads);
}

public static Connection getConnection() {
	return connection;
}

private static Thread getTelemetry() {
	return threadTelemetria;
}

private static void setThreadTelemetria(Thread thread) {
	threadTelemetria = thread;
}

public static KRPC.GameScene getCurrentGameScene() throws RPCException {
	return krpc.getCurrentGameScene();
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
}
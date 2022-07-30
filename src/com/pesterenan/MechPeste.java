package com.pesterenan;

import com.pesterenan.controllers.*;
import com.pesterenan.resources.Bundle;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusJPanel;
import krpc.client.Connection;

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

public static void main(String[] args) {
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
	setStatus(Bundle.getString("status_connecting"));
	try {
		MechPeste.connection = null;
		MechPeste.connection = Connection.newInstance("MechPeste - Pesterenan");
		if (getThreadTelemetria() == null)
			startTelemetry();
		setStatus(Bundle.getString("status_connected"));
		StatusJPanel.botConectarVisivel(false);
	} catch (IOException e) {
		setStatus(Bundle.getString("status_error_connection"));
		StatusJPanel.botConectarVisivel(true);
	}
}

private void startTelemetry() {
	flightCtrl = new FlightController(getConexao());
	setThreadTelemetria(new Thread(flightCtrl));
	getThreadTelemetria().start();
}

public static void iniciarModulo(Map<String, String> commands) {
	String executarModulo = commands.get(MODULO.get());
	if (executarModulo.equals(MODULO_DECOLAGEM.get())) {
		executeLiftoffModule(commands);
	}
	if (executarModulo.equals(MODULO_POUSO_SOBREVOAR.get()) || executarModulo.equals(MODULO_POUSO.get())) {
		executeLandingModule(commands);
	}
	if (executarModulo.equals(MODULO_MANOBRAS.get())) {
		executeManeuverModule(commands);
	}
	if (executarModulo.equals(MODULO_ROVER.get())) {
		executeRoverModule(commands);
	}
	MainGui.getParametros().firePropertyChange("Telemetria", false, true);
}

private static void executeLiftoffModule(Map<String, String> commands) {
	LiftoffController liftOffController = new LiftoffController(commands);
	setThreadModulos(new Thread(liftOffController));
	getThreadModulos().start();
}

private static void executeLandingModule(Map<String, String> commands) {
	LandingController landingController = new LandingController(commands);
	setThreadModulos(new Thread(landingController));
	getThreadModulos().start();
}

private static void executeManeuverModule(Map<String, String> commands) {
	ManeuverController maneuverController = new ManeuverController(commands);
	setThreadModulos(new Thread(maneuverController));
	getThreadModulos().start();
}

private static void executeRoverModule(Map<String, String> commands) {
	RoverController roverController = new RoverController(commands);
	setThreadModulos(new Thread(roverController));
	getThreadModulos().start();
}

public static void finalizarTarefa() {
	System.out.println(Thread.activeCount() + "Before");
	try {
		if (getThreadModulos() != null && getThreadModulos().isAlive()) {
			getThreadModulos().interrupt();
			setThreadModulos(null);
		}
	} catch (Exception e) {
	}
	System.out.println(Thread.activeCount() + "After");
}

public static Connection getConexao() {
	return connection;
}

private static Thread getThreadModulos() {
	return threadModulos;
}

private static void setThreadModulos(Thread thread) {
	threadModulos = thread;
}

private static Thread getThreadTelemetria() {
	return threadTelemetria;
}

private static void setThreadTelemetria(Thread thread) {
	threadTelemetria = thread;
}
}

package com.pesterenan;

import static com.pesterenan.utils.Dicionario.TELEMETRIA;
import static com.pesterenan.utils.Modulos.MODULO;
import static com.pesterenan.utils.Modulos.MODULO_DECOLAGEM;
import static com.pesterenan.utils.Modulos.MODULO_MANOBRAS;
import static com.pesterenan.utils.Modulos.MODULO_POUSO;
import static com.pesterenan.utils.Modulos.MODULO_POUSO_SOBREVOAR;
import static com.pesterenan.utils.Modulos.MODULO_ROVER;
import static com.pesterenan.utils.Status.CONECTADO;
import static com.pesterenan.utils.Status.CONECTANDO;
import static com.pesterenan.utils.Status.ERRO_CONEXAO;
import static com.pesterenan.views.StatusJPanel.setStatus;

import java.io.IOException;
import java.util.Map;

import com.pesterenan.controllers.FlightController;
import com.pesterenan.controllers.LandingController;
import com.pesterenan.controllers.LiftoffController;
import com.pesterenan.controllers.ManeuverController;
import com.pesterenan.controllers.RoverController;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusJPanel;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

public class MechPeste {
	private static MechPeste mechPeste = null;
	private static Connection connection;
	private static Thread threadModulos;
	private static Thread threadTelemetria;
	private static FlightController flightCtrl = null;

	public static void main(String[] args) throws StreamException, RPCException, IOException, InterruptedException {
//		Locale.setDefault(Locale.US);
		MechPeste.getInstance();
	}

	private MechPeste() {
		MainGui.getInstance();
		startConnection();
		startTelemetry();
	}

	public static MechPeste getInstance() {
		if (mechPeste == null) {
			mechPeste = new MechPeste();
		}
		return mechPeste;
	}

	public void startConnection() {
		setStatus(CONECTANDO.get());
		try {
			MechPeste.connection = null;
			MechPeste.connection = Connection.newInstance("MechPeste - Pesterenan");
			setStatus(CONECTADO.get());
			StatusJPanel.botConectarVisivel(false);
		} catch (IOException e) {
			setStatus(ERRO_CONEXAO.get());
			StatusJPanel.botConectarVisivel(true);
		}
	}

	private void startTelemetry() {
		flightCtrl = null;
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
		MainGui.getParametros().firePropertyChange(TELEMETRIA.get(), false, true);
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
		threadModulos = null;
		threadModulos = thread;
	}

	private static Thread getThreadTelemetria() {
		return threadTelemetria;
	}

	private static void setThreadTelemetria(Thread thread) {
		threadTelemetria = null;
		threadTelemetria = thread;
	}
}

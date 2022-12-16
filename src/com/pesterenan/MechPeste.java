package com.pesterenan;

import com.pesterenan.model.ActiveVessel;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Modulos;
import com.pesterenan.views.FunctionsAndTelemetryJPanel;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusJPanel;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.KRPC;
import krpc.client.services.SpaceCenter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.pesterenan.views.StatusJPanel.setStatus;

public class MechPeste {
	private static final Map<Integer, ActiveVessel> currentVessels = new HashMap<>();
	private static KRPC krpc;
	private static MechPeste mechPeste;
	private static SpaceCenter centroEspacial;
	private static Connection connection;
	private static long activeVesselTimer = 0;
	private static long vesselStatusTimer = 0;
	private static int currentVesselId = -1;

	private MechPeste() {
		MainGui.getInstance();
		connectToKSP();
	}

	public static void main(String[] args) {
		MechPeste.getInstance();
		checkActiveVessel();
	}

	public static MechPeste getInstance() {
		if (mechPeste == null) {
			mechPeste = new MechPeste();

			Thread.getAllStackTraces().keySet().forEach(Thread::toString);

		}
		return mechPeste;
	}

	private static void connectToActiveVessel() {
		try {
			SpaceCenter.Vessel currentVessel = centroEspacial.getActiveVessel();
			currentVesselId = currentVessel.hashCode();
			ActiveVessel activeVessel = new ActiveVessel(connection, currentVessel, currentVesselId);
			currentVessels.put(currentVesselId, activeVessel);
		} catch (RPCException e) {
			throw new RuntimeException(e);
		}
	}

	private static void checkActiveVessel() {
		while (getConnection() != null) {
			long currentTime = System.currentTimeMillis();
			try {
				int activeVesselId = centroEspacial.getActiveVessel().hashCode();
				if (currentTime > activeVesselTimer + 1000) {
					// If the current active vessel changes, create a new connection
					if (currentVesselId != activeVesselId) {
						if (!currentVessels.containsKey(activeVesselId)) {
							connectToActiveVessel();
						}
						currentVesselId = activeVesselId;
					}
					System.out.println(currentTime + " " + currentVessels);
					Thread.getAllStackTraces().keySet().forEach(t -> {
						String name = t.getName();
						if (name.contains("Vessel")) {
							Thread.State state = t.getState();
							int priority = t.getPriority();
							String type = t.isDaemon() ? "Daemon" : "Normal";
							System.out.printf("%-12s \t %s \t %d \t %s\n", name, state, priority, type);
						}
					});
					activeVesselTimer = currentTime;
				}
				if (currentTime > vesselStatusTimer + 100) {
					if (currentVesselId != -1) {
						setStatus(currentVessels.get(currentVesselId).getCurrentStatus());
						FunctionsAndTelemetryJPanel.updateTelemetry(
								currentVessels.get(currentVesselId).getTelemetryData());
					}
					vesselStatusTimer = currentTime;
				}
			} catch (RPCException e) {
				System.out.println("couldn't get active vessel");
			}
		}
	}

	public static void startModule(int currentVesselId, Map<String, String> commands) {
		int vesselId = currentVesselId;
		try {
			if (currentVesselId == -1) {
				vesselId = centroEspacial.getActiveVessel().hashCode();
			}
			currentVessels.get(vesselId).startModule(commands);
			MainGui.getCardJPanels().firePropertyChange(Modulos.MODULO_TELEMETRIA.get(), false, true);
		} catch (RPCException ignored) {
		}
	}

	public static void finalizarTarefa() {
		System.out.println("Active Threads: " + Thread.activeCount());
		System.out.println(currentVessels);
	}

	public static Connection getConnection() {
		return connection;
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
			centroEspacial = SpaceCenter.newInstance(getConnection());
			activeVesselTimer = System.currentTimeMillis();
			setStatus(Bundle.getString("status_connected"));
			StatusJPanel.isBtnConnectVisible(false);
		} catch (IOException e) {
			setStatus(Bundle.getString("status_error_connection"));
			StatusJPanel.isBtnConnectVisible(true);
		}
	}
}
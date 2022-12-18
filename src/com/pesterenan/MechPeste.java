package com.pesterenan;

import com.pesterenan.model.ActiveVessel;
import com.pesterenan.resources.Bundle;
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
	public static final int CHECK_VESSEL_INTERVAL_IN_MS = 1000;
	private static final Map<Integer, ActiveVessel> currentVessels = new HashMap<>();
	public static final int CHECK_STATUS_INTERVAL_IN_MS = 100;
	private static KRPC krpc;
	private static MechPeste mechPeste;
	private static SpaceCenter centroEspacial;
	private static Connection connection;
	private static long checkVesselTimer = 0;
	private static long checkStatusTimer = 0;
	private static int currentVesselId = -1;

	private MechPeste() {
		MainGui.newInstance();
	}

	public static void main(String[] args) {
		MechPeste.newInstance().connectToKSP();
		MechPeste.newInstance().checkActiveVessel();
	}

	public static MechPeste newInstance() {
		if (mechPeste == null) {
			mechPeste = new MechPeste();
		}
		return mechPeste;
	}

	private void checkActiveVessel() {
		while (getConnection() != null) {
			try {
				if (!MechPeste.newInstance().getCurrentGameScene().equals(KRPC.GameScene.FLIGHT)) {
					return;
				}

				long currentTime = System.currentTimeMillis();
				int activeVesselId = centroEspacial.getActiveVessel().hashCode();
				if (currentTime > checkVesselTimer + CHECK_VESSEL_INTERVAL_IN_MS) {
					// If the current active vessel changes, create a new connection
					if (currentVesselId != activeVesselId) {
						if (!currentVessels.containsKey(activeVesselId)) {
							connectToActiveVessel();
						}
						currentVesselId = activeVesselId;
					}
					Thread.getAllStackTraces().keySet().forEach(t -> {
						String name = t.getName();
						if (name.contains("Vessel")) {
							Thread.State state = t.getState();
							String type = t.isDaemon() ? "Daemon" : "Normal";
							System.out.printf("%-12s \t %s \t %d \t %s\n", name, state, currentTime, type);
						}
					});
					checkVesselTimer = currentTime;
				}
				if (currentTime > checkStatusTimer + CHECK_STATUS_INTERVAL_IN_MS) {
					if (currentVesselId != -1) {
						ActiveVessel av = currentVessels.get(currentVesselId);
						setStatus(av.getCurrentStatus());
						FunctionsAndTelemetryJPanel.updateTelemetry(av.getTelemetryData());
					}
					checkStatusTimer = currentTime;
				}

			} catch (RPCException ignored) {
			}
		}
	}

	private void connectToActiveVessel() {
		try {
			SpaceCenter.Vessel currentVessel = centroEspacial.getActiveVessel();
			currentVesselId = currentVessel.hashCode();
			ActiveVessel activeVessel = new ActiveVessel(connection, currentVessel, currentVesselId);
			currentVessels.put(currentVesselId, activeVessel);
		} catch (RPCException ignored) {
			System.out.println(Bundle.getString("status_couldnt_switch_vessel"));
		}
	}

	public void startModule(Map<String, String> commands) {
		startModule(-1, commands);
	}

	public void startModule(int currentVesselId, Map<String, String> commands) {
		int vesselId = currentVesselId;
		try {
			if (currentVesselId == -1) {
				vesselId = centroEspacial.getActiveVessel().hashCode();
			}
			currentVessels.get(vesselId).startModule(commands);
		} catch (RPCException ignored) {
		}
	}

	public void finalizarTarefa() {
		System.out.println("Active Threads: " + Thread.activeCount());
		System.out.println(currentVessels);
	}

	public Connection getConnection() {
		return connection;
	}

	public KRPC.GameScene getCurrentGameScene() throws RPCException {
		return krpc.getCurrentGameScene();
	}

	public void connectToKSP() {
		setStatus(Bundle.getString("status_connecting"));
		try {
			connection = Connection.newInstance("MechPeste - Pesterenan");
			krpc = KRPC.newInstance(connection);
			centroEspacial = SpaceCenter.newInstance(getConnection());
			checkVesselTimer = System.currentTimeMillis();
			setStatus(Bundle.getString("status_connected"));
			StatusJPanel.isBtnConnectVisible(false);
		} catch (IOException e) {
			setStatus(Bundle.getString("status_error_connection"));
			StatusJPanel.isBtnConnectVisible(true);
		}
	}
}
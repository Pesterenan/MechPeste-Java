package com.pesterenan;

import com.pesterenan.model.ActiveVessel;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Vector;
import com.pesterenan.views.FunctionsAndTelemetryJPanel;
import com.pesterenan.views.MainGui;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.KRPC;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Vessel;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.pesterenan.views.StatusJPanel.isBtnConnectVisible;
import static com.pesterenan.views.StatusJPanel.setStatus;

public class MechPeste {
	public static final int CHECK_VESSEL_INTERVAL_IN_MS = 1000;
	public static final int CHECK_STATUS_INTERVAL_IN_MS = 100;
	private static KRPC krpc;
	private static MechPeste mechPeste;
	private static SpaceCenter spaceCenter;
	private static Connection connection;
	private static int currentVesselId = -1;
	private static ActiveVessel currentVessel = null;

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

	public static Connection getConnection() {
		return connection;
	}

	public static SpaceCenter getSpaceCenter() {
		return spaceCenter;
	}

	public static ListModel<String> getActiveVessels(String search) {
		DefaultListModel<String> list = new DefaultListModel<>();
		try {
			List<Vessel> vessels = spaceCenter.getVessels();
			vessels = vessels.stream().filter(v -> filterVessels(v, search)).collect(Collectors.toList());
			vessels.forEach(v -> {
				try {
					String naveStr = v.hashCode() + " - \t" + v.getName();
					list.addElement(naveStr);
				} catch (RPCException ignored) {
				}
			});
		} catch (RPCException | NullPointerException ignored) {
		}
		return list;
	}

	private static boolean filterVessels(Vessel vessel, String search) {
		if (search == "all") {
			return true;
		}
		double TWO_KILOMETERS = 2000.0;
		try {
			Vessel active = MechPeste.getSpaceCenter().getActiveVessel();
			if (vessel.getOrbit().getBody().getName().equals(active.getOrbit().getBody().getName())) {
				final Vector activePos = new Vector(active.position(active.getSurfaceReferenceFrame()));
				final Vector vesselPos = new Vector(vessel.position(active.getSurfaceReferenceFrame()));
				final double distance = Vector.distance(activePos, vesselPos);
				switch (search) {
					case "closest":
						if (distance < TWO_KILOMETERS) {
							return true;
						}
						break;
					case "samebody":
						return true;
				}
			}
		} catch (RPCException ignored) {
		}
		return false;
	}

	public static String getVesselInfo(int selectedIndex) {
		try {
			Vessel naveAtual =
					spaceCenter.getVessels().stream().filter(v -> v.hashCode() == selectedIndex).findFirst().get();
			String name = naveAtual.getName().length() > 40
			              ? naveAtual.getName().substring(0, 40) + "..."
			              : naveAtual.getName();
			String vesselInfo =
					String.format("Nome: %s\t\t\t | Corpo: %s", name, naveAtual.getOrbit().getBody().getName());
			return vesselInfo;
		} catch (RPCException | NullPointerException ignored) {
		}
		return "";
	}

	public static void changeToVessel(int selectedIndex) {
		try {
			Vessel naveAtual =
					spaceCenter.getVessels().stream().filter(v -> v.hashCode() == selectedIndex).findFirst().get();
			spaceCenter.setActiveVessel(naveAtual);
		} catch (RPCException | NullPointerException e) {
			System.out.println(Bundle.getString("status_couldnt_switch_vessel"));
		}
	}

	public KRPC.GameScene getCurrentGameScene() throws RPCException {
		return krpc.getCurrentGameScene();
	}

	private void checkActiveVessel() {
		while (getConnection() != null) {
			try {
				if (!MechPeste.newInstance().getCurrentGameScene().equals(KRPC.GameScene.FLIGHT)) {
					Thread.sleep(100);
					return;
				}
				int activeVesselId = spaceCenter.getActiveVessel().hashCode();
				// If the current active vessel changes, create a new connection
				if (currentVesselId != activeVesselId) {
					currentVessel = new ActiveVessel();
					currentVesselId = currentVessel.getCurrentVesselId();
				}
				if (currentVesselId != -1) {
					currentVessel.recordTelemetryData();
					setStatus(currentVessel.getCurrentStatus());
					FunctionsAndTelemetryJPanel.updateTelemetry(currentVessel.getTelemetryData());
				}
				Thread.sleep(100);
			} catch (RPCException | InterruptedException ignored) {
			}
		}
	}

	public void startModule(Map<String, String> commands) {
		currentVessel.startModule(commands);
	}

	public void connectToKSP() {
		setStatus(Bundle.getString("status_connecting"));
		try {
			connection = Connection.newInstance("MechPeste - Pesterenan");
			krpc = KRPC.newInstance(connection);
			spaceCenter = SpaceCenter.newInstance(getConnection());
			setStatus(Bundle.getString("status_connected"));
			isBtnConnectVisible(false);
		} catch (IOException e) {
			setStatus(Bundle.getString("status_error_connection"));
			isBtnConnectVisible(true);
		}
	}

	public void checkConnection() {
		try {
			if (!MechPeste.newInstance().getCurrentGameScene().equals(KRPC.GameScene.FLIGHT)) {
				setStatus(Bundle.getString("status_ready"));
				return;
			}
			getConnection().close();
		} catch (RPCException | NullPointerException | IOException e) {
			setStatus(Bundle.getString("status_error_connection"));
			isBtnConnectVisible(true);
		}
	}

	public void cancelControl() {
		currentVessel.cancelControl();
	}
}
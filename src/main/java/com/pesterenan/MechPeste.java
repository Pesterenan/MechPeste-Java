package com.pesterenan;

import static com.pesterenan.views.StatusJPanel.isBtnConnectVisible;
import static com.pesterenan.views.StatusJPanel.setStatusMessage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import com.pesterenan.model.ActiveVessel;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Vector;
import com.pesterenan.views.CreateManeuverJPanel;
import com.pesterenan.views.FunctionsAndTelemetryJPanel;
import com.pesterenan.views.MainGui;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.KRPC;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Vessel;

public class MechPeste {
    private static KRPC krpc;
    private static MechPeste mechPeste;
    private static SpaceCenter spaceCenter;
    private static Connection connection;
    private static int currentVesselId = -1;
    private static ActiveVessel currentVessel = null;

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
                    String vesselName = v.hashCode() + " - \t" + v.getName();
                    list.addElement(vesselName);
                } catch (RPCException ignored) {
                }
            });
        } catch (RPCException | NullPointerException ignored) {
        }
        return list;
    }

    public static ListModel<String> getCurrentManeuvers() {
        DefaultListModel<String> list = new DefaultListModel<>();
        try {
            List<Node> maneuvers = getSpaceCenter().getActiveVessel().getControl().getNodes();
            maneuvers.forEach(m -> {
                try {
                    String maneuverStr = String.format("%d - Dv: %.1f {P: %.1f, N: %.1f, R: %.1f} AP: %.1f, PE: %.1f",
                            maneuvers.indexOf(m) + 1, m.getDeltaV(), m.getPrograde(), m.getNormal(), m.getRadial(),
                            m.getOrbit().getApoapsisAltitude(), m.getOrbit().getPeriapsisAltitude());
                    list.addElement(maneuverStr);
                } catch (RPCException ignored) {
                }
            });
        } catch (RPCException | NullPointerException ignored) {
        }
        return list;
    }

    public static String getVesselInfo(int selectedIndex) {
        try {
            Vessel activeVessel = spaceCenter.getVessels().stream().filter(v -> v.hashCode() == selectedIndex)
                    .findFirst().get();
            String name = activeVessel.getName().length() > 40
                    ? activeVessel.getName().substring(0, 40) + "..."
                    : activeVessel.getName();
            String vesselInfo = String.format("Nome: %s\t\t\t | Corpo: %s", name,
                    activeVessel.getOrbit().getBody().getName());
            return vesselInfo;
        } catch (RPCException | NullPointerException ignored) {
        }
        return "";
    }

    public static void changeToVessel(int selectedIndex) {
        try {
            Vessel activeVessel = spaceCenter.getVessels().stream().filter(v -> v.hashCode() == selectedIndex)
                    .findFirst().get();
            spaceCenter.setActiveVessel(activeVessel);
        } catch (RPCException | NullPointerException e) {
            System.out.println(Bundle.getString("status_couldnt_switch_vessel"));
        }
    }

    public static void cancelControl(ActionEvent e) {
        currentVessel.cancelControl();
    }

    private static boolean filterVessels(Vessel vessel, String search) {
        if (search == "all") {
            return true;
        }
        double TEN_KILOMETERS = 10000.0;
        try {
            Vessel active = MechPeste.getSpaceCenter().getActiveVessel();
            if (vessel.getOrbit().getBody().getName().equals(active.getOrbit().getBody().getName())) {
                final Vector activePos = new Vector(active.position(active.getSurfaceReferenceFrame()));
                final Vector vesselPos = new Vector(vessel.position(active.getSurfaceReferenceFrame()));
                final double distance = Vector.distance(activePos, vesselPos);
                switch (search) {
                    case "closest" :
                        if (distance < TEN_KILOMETERS) {
                            return true;
                        }
                        break;
                    case "samebody" :
                        return true;
                }
            }
        } catch (RPCException ignored) {
        }
        return false;
    }

    private MechPeste() {
        MainGui.newInstance();
    }

    public KRPC.GameScene getCurrentGameScene() throws RPCException {
        return krpc.getCurrentGameScene();
    }

    public void startModule(Map<String,String> commands) {
        currentVessel.startModule(commands);
    }

    public void connectToKSP() {
        setStatusMessage(Bundle.getString("status_connecting"));
        try {
            connection = Connection.newInstance("MechPeste - Pesterenan");
            krpc = KRPC.newInstance(connection);
            spaceCenter = SpaceCenter.newInstance(getConnection());
            setStatusMessage(Bundle.getString("status_connected"));
            isBtnConnectVisible(false);
        } catch (IOException e) {
            setStatusMessage(Bundle.getString("status_error_connection"));
            isBtnConnectVisible(true);
        }
    }

    public void checkConnection() {
        try {
            if (!MechPeste.newInstance().getCurrentGameScene().equals(KRPC.GameScene.FLIGHT)) {
                setStatusMessage(Bundle.getString("status_ready"));
                return;
            }
            getConnection().close();
        } catch (RPCException | NullPointerException | IOException e) {
            setStatusMessage(Bundle.getString("status_error_connection"));
            isBtnConnectVisible(true);
        }
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
                    if (currentVessel.hasModuleRunning()) {
                        setStatusMessage(currentVessel.getCurrentStatus());
                    }
                    FunctionsAndTelemetryJPanel.updateTelemetry(currentVessel.getTelemetryData());
                    CreateManeuverJPanel.updatePanel(getCurrentManeuvers());
                }
                Thread.sleep(100);
            } catch (RPCException | InterruptedException ignored) {
            }
        }
    }
}

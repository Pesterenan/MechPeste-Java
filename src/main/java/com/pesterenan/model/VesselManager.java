package com.pesterenan.model;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;

import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Vector;
import com.pesterenan.views.FunctionsAndTelemetryJPanel;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusDisplay;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Vessel;

public class VesselManager {
    private ConnectionManager connectionManager;
    private ActiveVessel currentVessel;
    private int currentVesselId = -1;
    private StatusDisplay statusDisplay;

    public VesselManager(ConnectionManager connectionManager, StatusDisplay statusDisplay) {
        this.connectionManager = connectionManager;
        this.statusDisplay = statusDisplay;
    }

    public Connection getConnection() {
        return connectionManager.getConnection();
    }

    public SpaceCenter getSpaceCenter() {
        return connectionManager.getSpaceCenter();
    }

    public ActiveVessel getCurrentVessel() {
        return currentVessel;
    }

    public int getCurrentVesselId() {
        return currentVesselId;
    }

    public void startUpdateLoop() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (getConnection() == null || getSpaceCenter() == null) {
                System.out.println("Connection not established yet, skipping update cycle.");
                return;
            }
            try {
                if (!connectionManager.isOnFlightScene()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return;
                }
                checkAndUpdateActiveVessel();
                updateTelemetryData();
                updateUI();
            } catch (RPCException e) {
                System.err.println("RPC Error: " + e.getMessage());
            }
        }, 0, 250, TimeUnit.MILLISECONDS);
    }

    public ListModel<String> getCurrentManeuvers() {
        DefaultListModel<String> list = new DefaultListModel<>();
        try {
            List<Node> maneuvers = this.getSpaceCenter().getActiveVessel().getControl().getNodes();
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

    public void startModule(Map<String,String> commands) {
        currentVessel.startModule(commands);
    }

    public ListModel<String> getActiveVessels(String search) {
        DefaultListModel<String> list = new DefaultListModel<>();
        try {
            List<Vessel> vessels = getSpaceCenter().getVessels();
            vessels = vessels.stream().filter(v -> filterVessels(v, search)).collect(Collectors.toList());
            vessels.forEach(v -> {
                try {
                    String vesselName = v.hashCode() + " - \t" + v.getName();
                    list.addElement(vesselName);
                } catch (RPCException vesselNameError) {
                    System.err.println("Couldn't add vessel name to list. Error: " + vesselNameError.getMessage());
                }
            });
        } catch (RPCException rpcOrNpeException) {
            System.err.println("Couldn't get vessel list, Error: " + rpcOrNpeException.getMessage());
        }
        return list;
    }

    public String getVesselInfo(int selectedIndex) {
        try {
            Vessel activeVessel = this.getSpaceCenter().getVessels().stream().filter(v -> v.hashCode() == selectedIndex)
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

    public void changeToVessel(int selectedIndex) {
        try {
            Vessel activeVessel = getSpaceCenter().getVessels().stream().filter(v -> v.hashCode() == selectedIndex)
                    .findFirst().get();
            getSpaceCenter().setActiveVessel(activeVessel);
        } catch (RPCException | NullPointerException e) {
            System.out.println(Bundle.getString("status_couldnt_switch_vessel"));
        }
    }

    public void cancelControl(ActionEvent e) {
        currentVessel.cancelControl();
    }

    private void checkAndUpdateActiveVessel() throws RPCException {
        Vessel activeVessel = getSpaceCenter().getActiveVessel();
        int activeVesselId = activeVessel.hashCode();
        if (currentVesselId != activeVesselId) {
            currentVessel = new ActiveVessel(connectionManager, this);
            currentVesselId = currentVessel.getCurrentVesselId();
            MainGui.getInstance().setVesselManager(this);
        }
    }

    private void updateTelemetryData() {
        if (currentVesselId == -1 || currentVessel == null)
            return;
        try {
            currentVessel.recordTelemetryData();
        } catch (RPCException e) {
            System.err.println("Error while recording telemetry: " + e.getMessage());
            currentVessel = null;
            currentVesselId = -1;
        }
    }

    private void updateUI() {
        SwingUtilities.invokeLater(() -> {
            if (currentVessel != null) {
                FunctionsAndTelemetryJPanel.updateTelemetry(currentVessel.getTelemetryData());
                MainGui.getInstance().getCreateManeuverPanel().updatePanel(getCurrentManeuvers());
                if (currentVessel.hasModuleRunning()) {
                    statusDisplay.setStatusMessage(currentVessel.getCurrentStatus());
                }
            }
        });
    }

    private boolean filterVessels(Vessel vessel, String search) {
        if ("all".equals(search)) {
            return true;
        }
        double TEN_KILOMETERS = 10000.0;
        try {
            Vessel active = this.getSpaceCenter().getActiveVessel();
            if (vessel.getOrbit().getBody().getName().equals(active.getOrbit().getBody().getName())) {
                final Vector activePos = new Vector(active.position(active.getSurfaceReferenceFrame()));
                final Vector vesselPos = new Vector(vessel.position(active.getSurfaceReferenceFrame()));
                final double distance = Vector.distance(activePos, vesselPos);
                switch (search) {
                    case "closest":
                        if (distance < TEN_KILOMETERS) {
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
}

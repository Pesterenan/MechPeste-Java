package com.pesterenan.model;

import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Vector;
import com.pesterenan.views.FunctionsAndTelemetryJPanel;
import com.pesterenan.views.CreateManeuverJPanel;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusDisplay;
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
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Vessel;

public class VesselManager {
  private ActiveVessel currentVessel;
  private ConnectionManager connectionManager;
  private FunctionsAndTelemetryJPanel telemetryPanel;
  private StatusDisplay statusDisplay;
  private int currentVesselId = -1;
  private ScheduledExecutorService telemetryMonitor;

  public VesselManager(
      final ConnectionManager connectionManager,
      final StatusDisplay statusDisplay,
      final FunctionsAndTelemetryJPanel telemetryPanel) {
    this.connectionManager = connectionManager;
    this.statusDisplay = statusDisplay;
    this.telemetryPanel = telemetryPanel;
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

  public void startTelemetryLoop() {
    if (telemetryMonitor != null && !telemetryMonitor.isShutdown()) return;
    telemetryMonitor =
        Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "MP_TELEMETRY_UPDATER"));
    telemetryMonitor.scheduleAtFixedRate(
        () -> {
          if (connectionManager.isConnectionAlive()) {
            checkAndUpdateActiveVessel();
          }
          if (currentVessel != null && connectionManager.isOnFlightScene()) {
            updateUI();
          }
        },
        0,
        100,
        TimeUnit.MILLISECONDS);
  }

  public ListModel<String> getCurrentManeuvers() {
    DefaultListModel<String> list = new DefaultListModel<>();
    try {
      List<Node> maneuvers = getSpaceCenter().getActiveVessel().getControl().getNodes();
      maneuvers.forEach(
          m -> {
            try {
              String maneuverStr =
                  String.format(
                      "%d - Dv: %.1f {P: %.1f, N: %.1f, R: %.1f} AP: %.1f, PE: %.1f",
                      maneuvers.indexOf(m) + 1,
                      m.getDeltaV(),
                      m.getPrograde(),
                      m.getNormal(),
                      m.getRadial(),
                      m.getOrbit().getApoapsisAltitude(),
                      m.getOrbit().getPeriapsisAltitude());
              list.addElement(maneuverStr);
            } catch (RPCException ignored) {
            }
          });
    } catch (RPCException | NullPointerException | UnsupportedOperationException e) {
      System.err.println("ERRO: Não foi possivel atualizar as manobras atuais. " + e.getClass().getSimpleName());
    }
    return list;
  }

  public void startModule(Map<String, String> commands) {
    currentVessel.startModule(commands);
  }

  public ListModel<String> getActiveVessels(String search) {
    DefaultListModel<String> list = new DefaultListModel<>();
    try {
      List<Vessel> vessels = getSpaceCenter().getVessels();
      vessels = vessels.stream().filter(v -> filterVessels(v, search)).collect(Collectors.toList());
      vessels.forEach(
          v -> {
            try {
              String vesselName = v.hashCode() + " - \t" + v.getName();
              list.addElement(vesselName);
            } catch (RPCException e) {
              System.err.println(
                  "ERRO: Não foi possível adicionar nave na lista. " + e.getMessage());
            }
          });
    } catch (RPCException | NullPointerException e) {
      System.err.println("ERRO: Não foi possível buscar lista de naves. " + e.getMessage());
    }
    return list;
  }

  public String getVesselInfo(int selectedIndex) {
    try {
      Vessel activeVessel =
          getSpaceCenter().getVessels().stream()
              .filter(v -> v.hashCode() == selectedIndex)
              .findFirst()
              .get();
      String name =
          activeVessel.getName().length() > 40
              ? activeVessel.getName().substring(0, 40) + "..."
              : activeVessel.getName();
      String vesselInfo =
          String.format(
              "Nome: %s\t\t\t | Corpo: %s", name, activeVessel.getOrbit().getBody().getName());
      return vesselInfo;
    } catch (RPCException | NullPointerException e) {
      System.err.println("ERRO: Não foi possível buscar informações da nave. " + e.getMessage());
    }
    return "";
  }

  public void changeToVessel(int selectedIndex) {
    try {
      Vessel activeVessel =
          getSpaceCenter().getVessels().stream()
              .filter(v -> v.hashCode() == selectedIndex)
              .findFirst()
              .get();
      getSpaceCenter().setActiveVessel(activeVessel);
    } catch (RPCException | NullPointerException e) {
      System.out.println(Bundle.getString("status_couldnt_switch_vessel"));
    }
  }

  public void cancelControl(ActionEvent e) {
    if (currentVessel != null) {
      currentVessel.cancelControl();
    }
  }

  private void checkAndUpdateActiveVessel() {
    try {
      int activeVesselId = getSpaceCenter().getActiveVessel().hashCode();
      if (currentVesselId != activeVesselId) {
        System.out.println("DEBUG: Active vessel changed. Re-initializing...");
        if (currentVessel != null) {
          currentVessel.reinitialize();
        } else {
          currentVessel = new ActiveVessel(connectionManager, this);
        }
        currentVesselId = currentVessel.getCurrentVesselId();
        MainGui.getInstance().setVesselManager(this);
      }
    } catch (IllegalArgumentException e) {
    } catch (RPCException e) {
      System.out.println("DEBUG: Could not get active vessel. Cleaning up.");
      clearVessel();
    }
  }

  public void clearVessel() {
    if (currentVessel != null) {
      currentVessel.removeStreams();
    }
    currentVessel = null;
    currentVesselId = -1;
  }

  private void updateUI() {
    SwingUtilities.invokeLater(
                  () -> {
                    if (currentVessel != null && telemetryPanel != null) {
                      telemetryPanel.updateTelemetry(currentVessel.getTelemetryData());
                      CreateManeuverJPanel createManeuverPanel = MainGui.getInstance().getCreateManeuverPanel();
                      if (createManeuverPanel.isShowing()) {
                        createManeuverPanel.updatePanel(getCurrentManeuvers());
                      }
                      if (currentVessel.hasModuleRunning()) {
                        statusDisplay.setStatusMessage(currentVessel.getCurrentStatus());
                      }
                    }
                  });  }

  private boolean filterVessels(Vessel vessel, String search) {
    if ("all".equals(search)) {
      return true;
    }
    double TEN_KILOMETERS = 10000.0;
    try {
      Vessel active = getSpaceCenter().getActiveVessel();
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

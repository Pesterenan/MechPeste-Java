package com.pesterenan;

import com.pesterenan.model.ConnectionManager;
import com.pesterenan.model.VesselManager;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusDisplay;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

public class MechPeste {
  private static MechPeste instance;
  private ConnectionManager connectionManager = null;
  private VesselManager vesselManager;

  public ConnectionManager getConnectionManager() {
    return connectionManager;
  }

  public VesselManager getVesselManager() {
    return vesselManager;
  }

  private MechPeste() {}

  public static MechPeste newInstance() {
    if (instance == null) {
      instance = new MechPeste();
    }
    return instance;
  }

  public static void main(String[] args) {
    MechPeste app = MechPeste.newInstance();
    try {
      SwingUtilities.invokeAndWait(() -> MainGui.newInstance());
    } catch (InvocationTargetException | InterruptedException e) {
      System.err.println("Error while invoking GUI: " + e.getMessage());
      e.printStackTrace();
    }

    StatusDisplay statusDisplay = MainGui.newInstance().getStatusPanel();
    app.connectionManager = new ConnectionManager("MechPeste - Pesterenan", statusDisplay);
    app.vesselManager = new VesselManager(app.connectionManager, statusDisplay);
    app.vesselManager.setTelemetryPanel(MainGui.getInstance().getFunctionsAndTelemetryPanel());
    app.vesselManager.startUpdateLoop();
  }
}

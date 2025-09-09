package com.pesterenan.controllers;

import com.pesterenan.model.ActiveVessel;
import com.pesterenan.model.ConnectionManager;
import com.pesterenan.model.VesselManager;

public class Controller extends ActiveVessel implements Runnable {
  private String currentStatus = "";

  public Controller(ConnectionManager connectionManager, VesselManager vesselManager) {
    super(connectionManager, vesselManager);
  }

  public void run() {
    // This method should be overridden by subclasses.
  }

  public String getCurrentStatus() {
    return currentStatus;
  }

  public void setCurrentStatus(String status) {
    this.currentStatus = status;
  }
}

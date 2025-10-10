package com.pesterenan.controllers;

import com.pesterenan.model.ActiveVessel;

public class Controller implements Runnable {
  protected final ActiveVessel vessel;
  private String currentStatus = "";

  public Controller(ActiveVessel vessel) {
    this.vessel = vessel;
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

package com.pesterenan.model;

import com.pesterenan.resources.Bundle;
import com.pesterenan.views.StatusDisplay;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.KRPC;
import krpc.client.services.SpaceCenter;

public class ConnectionManager {
  private KRPC krpc;
  private Connection connection;
  private SpaceCenter spaceCenter;
  private StatusDisplay statusDisplay;
  private volatile boolean isConnecting = false;
  private ScheduledExecutorService connectionMonitor;
  private String connectionName;

  public ConnectionManager(final String connectionName, final StatusDisplay statusDisplay) {
    this.statusDisplay = statusDisplay;
    this.connectionName = connectionName;
    startMonitoring();
  }

  public Connection getConnection() {
    return connection;
  }

  public SpaceCenter getSpaceCenter() {
    return spaceCenter;
  }

  public KRPC getKRPC() {
    return krpc;
  }

  public void connect() {
    attemptConnection();
  }

  private void startMonitoring() {
    if (connectionMonitor != null && !connectionMonitor.isShutdown()) return;
    connectionMonitor =
        Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "MP_CONNECTION_MONITOR"));
    connectionMonitor.scheduleAtFixedRate(
        () -> {
          if (!isConnectionAlive()) {
            attemptConnection();
          }
        },
        0,
        5,
        TimeUnit.SECONDS);
  }

  private void attemptConnection() {
    if (isConnecting) return;
    try {
      isConnecting = true;
      SwingUtilities.invokeLater(
          () -> {
            statusDisplay.setStatusMessage(Bundle.getString("status_connecting"));
            statusDisplay.setBtnConnectVisible(false);
          });

      connection = Connection.newInstance(connectionName);
      krpc = KRPC.newInstance(connection);
      spaceCenter = SpaceCenter.newInstance(connection);

      SwingUtilities.invokeLater(
          () -> statusDisplay.setStatusMessage(Bundle.getString("status_connected")));
    } catch (IOException e) {
      connection = null;
      krpc = null;
      spaceCenter = null;
      SwingUtilities.invokeLater(
          () -> {
            statusDisplay.setStatusMessage(Bundle.getString("status_error_connection"));
            statusDisplay.setBtnConnectVisible(true);
          });
    } finally {
      isConnecting = false;
    }
  }

  public boolean isConnectionAlive() {
    try {
      if (krpc == null || connection == null)  return false;
      krpc.getStatus();
      return true;
    } catch (RPCException e) {
      return false;
    }
  }

  public boolean isOnFlightScene() {
    try {
      if (krpc == null) return false;
      return krpc.getCurrentGameScene().equals(KRPC.GameScene.FLIGHT);
    } catch (final RPCException e) {
      return false;
    }
  }
}

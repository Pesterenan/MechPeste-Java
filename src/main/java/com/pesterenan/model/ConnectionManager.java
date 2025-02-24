package com.pesterenan.model;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import com.pesterenan.resources.Bundle;
import com.pesterenan.views.StatusDisplay;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.KRPC;
import krpc.client.services.SpaceCenter;

public class ConnectionManager {
    private KRPC krpc;
    private Connection connection;
    private SpaceCenter spaceCenter;
    private final StatusDisplay statusDisplay;
    private volatile boolean isConnecting = false;
    private ScheduledExecutorService connectionScheduler;

    public ConnectionManager(final String connectionName, final StatusDisplay statusDisplay) {
        this.statusDisplay = statusDisplay;
        connectAndMonitor(connectionName);
    }

    public Connection getConnection() {
        return connection;
    }

    public SpaceCenter getSpaceCenter() {
        return spaceCenter;
    }

    public void connectAndMonitor(final String connectionName) {
        if (connectionScheduler != null && !connectionScheduler.isShutdown()) {
            return;
        }
        connectionScheduler = Executors.newSingleThreadScheduledExecutor();
        connectionScheduler.scheduleAtFixedRate(() -> {
            if (isConnectionAlive() || isConnecting) {
                return;
            }

            isConnecting = true;
            SwingUtilities.invokeLater(() -> {
                statusDisplay.setStatusMessage(Bundle.getString("status_connecting"));
                statusDisplay.setBtnConnectVisible(false);
            });

            try {
                connection = Connection.newInstance(connectionName);
                krpc = KRPC.newInstance(getConnection());
                spaceCenter = SpaceCenter.newInstance(getConnection());
                SwingUtilities.invokeLater(() -> {
                    statusDisplay.setStatusMessage(Bundle.getString("status_connected"));
                    statusDisplay.setBtnConnectVisible(false);
                });
            } catch (final IOException e) {
                SwingUtilities.invokeLater(() -> {
                    statusDisplay.setStatusMessage(Bundle.getString("status_error_connection"));
                    statusDisplay.setBtnConnectVisible(true);
                });
            } finally {
                isConnecting = false;
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public boolean isConnectionAlive() {
        try {
            if (krpc == null)
                return false;
            krpc.getStatus();
            return true;
        } catch (final RPCException e) {
            return false;
        }
    }

    public KRPC.GameScene getCurrentGameScene() throws RPCException {
        return krpc.getCurrentGameScene();
    }

    public boolean isOnFlightScene() {
        try {
            return this.getCurrentGameScene().equals(KRPC.GameScene.FLIGHT);
        } catch (final RPCException e) {
            return false;
        }
    }

}

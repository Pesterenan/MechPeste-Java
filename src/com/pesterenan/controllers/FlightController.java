package com.pesterenan.controllers;

import com.pesterenan.model.Nave;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusJPanel;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Vessel;

public class FlightController extends Nave implements Runnable {

	protected final static float CONST_GRAV = 9.81f;

	public FlightController(Connection con) {
		super(con);
		startStreams(this.currentShip);
	}

	private void startStreams(Vessel currentShip) {
		try {
			orbitalRefSpot = currentShip.getOrbit().getBody().getReferenceFrame();
			surfaceRefSpot = currentShip.getSurfaceReferenceFrame();
			flightParameters = currentShip.flight(orbitalRefSpot);
			altitude = getConnection().addStream(flightParameters, "getMeanAltitude");
			surfAltitude = getConnection().addStream(flightParameters, "getSurfaceAltitude");
			apoapsis = getConnection().addStream(currentShip.getOrbit(), "getApoapsisAltitude");
			periapsis = getConnection().addStream(currentShip.getOrbit(), "getPeriapsisAltitude");
			verticalSpeed = getConnection().addStream(flightParameters, "getVerticalSpeed");
			horizontalSpeed = getConnection().addStream(flightParameters, "getHorizontalSpeed");
			totalMass = getConnection().addStream(currentShip, "getMass");
			missionTime = getConnection().addStream(currentShip, "getMET");
			currentBattery = getConnection().addStream(currentShip.getResources(), "amount", "ElectricCharge");
			totalBattery = currentShip.getResources().max("ElectricCharge");
			gravityAcceleration = currentShip.getOrbit().getBody().getSurfaceGravity();
			celestialBody = currentShip.getOrbit().getBody().getName();
		} catch (StreamException | RPCException | NullPointerException | IllegalArgumentException e) {
			checkConnection();
		}
	}

	@Override
	public void run() {
		while (!getConnection().equals(null)) {
			try {
				switchShip();
				sendTelemetry();
				Thread.sleep(250);
			} catch (InterruptedException e) {
			} catch (RPCException | StreamException | NullPointerException e) {
				checkConnection();
				switchShip();
			}
		}
	}

	private void switchShip() {
		try {
			if (!spaceCenter.getActiveVessel().equals(this.currentShip)) {
				this.currentShip = spaceCenter.getActiveVessel();
				startStreams(this.currentShip);
			}
		} catch (RPCException e) {
			StatusJPanel.visibleConnectButton(true);
			StatusJPanel.setStatus("Não foi possível trocar de nave.");
		}
	}

	private void sendTelemetry() throws RPCException, StreamException {
		chargePercentage = (int) Math.ceil(currentBattery.get() * 100 / totalBattery);
		MainGui.getParameters().getTelemetry().firePropertyChange("altitude", 0.0, altitude.get());
		MainGui.getParameters().getTelemetry().firePropertyChange("surfAltitude", 0.0, surfAltitude.get());
		MainGui.getParameters().getTelemetry().firePropertyChange("apoapsis", 0.0, apoapsis.get());
		MainGui.getParameters().getTelemetry().firePropertyChange("periapsis", 0.0, periapsis.get());
		MainGui.getParameters().getTelemetry().firePropertyChange("verticalSpeed", 0.0, verticalSpeed.get());
		MainGui.getParameters().getTelemetry().firePropertyChange("horizontalSpeed", 0.0, horizontalSpeed.get());
		MainGui.getParameters().getTelemetry().firePropertyChange("bateria", 0.0, chargePercentage);
		MainGui.getParameters().getTelemetry().firePropertyChange("missionTime", 0.0, missionTime.get());

	}

}

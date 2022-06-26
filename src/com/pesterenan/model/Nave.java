package com.pesterenan.model;

import static com.pesterenan.utils.Status.CONNECTION_ERROR;

import com.pesterenan.views.StatusJPanel;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.KRPC;
import krpc.client.services.KRPC.GameScene;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;

public class Nave {
	private static Connection connection;

	protected static SpaceCenter spaceCenter;
	protected Vessel currentShip;
	protected Flight flightParameters;
	protected ReferenceFrame orbitalRefSpot;
	protected ReferenceFrame surfaceRefSpot;
	protected Stream<Double> altitude, surfAltitude, apoapsis, periapsis;
	protected Stream<Double> verticalSpeed, missionTime, horizontalSpeed;
	protected Stream<Float> totalMass, currentBattery;
	protected float totalBattery, gravityAcceleration;
	protected String celestialBody;
	protected int chargePercentage;

	public Nave(Connection conn) {
		setConnection(conn);
		try {
			spaceCenter = SpaceCenter.newInstance(getConnection());
			this.currentShip = spaceCenter.getActiveVessel();
		} catch (RPCException e) {
			System.err.println("Erro ao buscar Nave Atual: \n\t" + e.getMessage());
			checkConnection();
		}
	}

	protected void checkConnection() {
		KRPC krpc = KRPC.newInstance(getConnection());
		try {
			if (krpc.getCurrentGameScene().equals(GameScene.FLIGHT)) {
				this.currentShip = spaceCenter.getActiveVessel();
			} else {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
			}
		} catch (RPCException e) {
			StatusJPanel.setStatus(CONNECTION_ERROR.get());
			StatusJPanel.visibleConnectButton(true);
		}
	}

	public static Connection getConnection() {
		return connection;
	}

	private void setConnection(Connection conn) {
		connection = conn;
	}

	protected void throttle(float acceleration) throws RPCException {
		currentShip.getControl().setThrottle(acceleration);
	}

	protected void throttle(double acceleration) throws RPCException {
		throttle((float) acceleration);
	}

	protected void liftoff() throws InterruptedException {
		try {
			currentShip.getControl().setSAS(true);
			throttle(1f);
			if (currentShip.getSituation().equals(VesselSituation.PRE_LAUNCH)) {
				float launchCount = 5f;
				while (launchCount > 0) {
					StatusJPanel.setStatus(String.format("Lançamento em: %.1f segundos...", launchCount));
					launchCount -= 0.1;
					Thread.sleep(100);
				}
				currentShip.getControl().activateNextStage();
			}
			StatusJPanel.setStatus("Decolagem!");
		} catch (RPCException erro) {
			System.err.println("Não foi possivel decolar a nave. Erro: " + erro.getMessage());
		}
	}

	protected double calculateTWR() throws RPCException, StreamException {
		return currentShip.getAvailableThrust() / ((totalMass.get() * gravityAcceleration));
	}

	protected double calculateMaxAcceleration() throws RPCException, StreamException {
		return calculateTWR() * gravityAcceleration - gravityAcceleration;
	}
}

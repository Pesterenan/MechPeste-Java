package com.pesterenan.controllers;

import static com.pesterenan.utils.Status.AUTOMATIC_LAND_STATUS;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.pesterenan.utils.PIDcontrol;
import com.pesterenan.utils.Modules;
import com.pesterenan.utils.Navigation;
import com.pesterenan.utils.Utilities;
import com.pesterenan.utils.Vector;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusJPanel;

import krpc.client.RPCException;
import krpc.client.StreamException;

public class LandingController extends FlightController {

	private static final int AUTOMATIC_LANDING_HEIGHT = 8000;

	private PIDcontrol PIDheightAcceleration = new PIDcontrol();
	private PIDcontrol PIDspeedAcceleration = new PIDcontrol();
	private Navigation navigation = new Navigation();

	private static double Pspeed = 0.025, Ispeed = 0.001, Dspeed = 0.01;

	private double flightHeight = 100;
	private double burnDistance = 0, totalSpeed = 0;
	private Map<String, String> commands = new HashMap<>();
	private boolean executingAutoLand = false;
	private boolean executingFlight = false;
	private static boolean descentFlight = false;

	public LandingController(Map<String, String> commands) {
		super(getConnection());
		this.commands = commands;
		this.PIDheightAcceleration.limitarSaida(0, 1);
		this.PIDspeedAcceleration.limitarSaida(0, 1);
		StatusJPanel.setStatus(AUTOMATIC_LAND_STATUS.get());
	}

	@Override
	public void run() {
		if (commands.get(Modules.MODULE.get()).equals(Modules.LANGING_FLIGHT_MODULE.get())) {
			this.flightHeight = Double.parseDouble(commands.get(Modules.FLIGHT_ALTITUDE.get()));
			executingFlight = true;
			PIDheightAcceleration.limitarSaida(-0.5, 1);
			overflyArea();
		}
		if (commands.get(Modules.MODULE.get()).equals(Modules.LANDING_MODULE.get())) {
			landAutomaticaly();
		}
	}

	private void overflyArea() {
		try {
			liftoff();
			currentShip.getAutoPilot().engage();
			while (executingFlight) {
				try {
					if (horizontalSpeed.get() > 15) {
						navigation.pointRetrograde();
					} else {
						navigation.pointRadialOut();
					}
					ajustPIDcontrol();
					double PIDheight = PIDheightAcceleration.computePID(surfAltitude.get(), flightHeight);
					double PIDspeed = PIDspeedAcceleration.computePID(verticalSpeed.get(), PIDheight * gravityAcceleration);
					throttle(PIDspeed);
					if (descentFlight == true) {
						currentShip.getControl().setGear(true);
						flightHeight = 0;
						checkLanding();
					}
					Thread.sleep(25);
				} catch (RPCException | StreamException | IOException e) {
					StatusJPanel.setStatus("Função abortada.");
					currentShip.getAutoPilot().disengage();
					break;
				}
			}
		} catch (InterruptedException | RPCException e) {
			StatusJPanel.setStatus("Decolagem abortada.");
			try {
				currentShip.getAutoPilot().disengage();
			} catch (RPCException e1) {
			}
			return;
		}
	}

	private void landAutomaticaly() {
		try {
			liftoff();
			throttle(0.0f);
			currentShip.getAutoPilot().engage();
			StatusJPanel.setStatus("Iniciando pouso automático em: " + celestialBody);
			checkLandingForHeight();
			startAutomaticLand();
		} catch (RPCException | StreamException | InterruptedException | IOException e) {
			StatusJPanel.setStatus("Não foi possível pousar a nave, operação abortada.");
			try {
				currentShip.getAutoPilot().disengage();
			} catch (RPCException e1) {
			}
		}
	}

	private void checkLandingForHeight() throws RPCException, StreamException, InterruptedException {
		while (!executingAutoLand) {
			burnDistance = calculateBurnDistance();
			currentShip.getControl().setBrakes(true);
			navigation.pointRetrograde();
			if (surfAltitude.get() < AUTOMATIC_LANDING_HEIGHT) {
				if (surfAltitude.get() < burnDistance && verticalSpeed.get() < -1) {
					executingAutoLand = true;
				}
			}
			Thread.sleep(50);
		}
	}

	private void startAutomaticLand() throws InterruptedException, RPCException, StreamException, IOException {
		StatusJPanel.setStatus("Iniciando Pouso Automático!");
		while (executingAutoLand) {
			burnDistance = calculateBurnDistance();
			checkHeight();
			checkLanding();
			Thread.sleep(25);
		}
	}

	private void ajustPIDcontrol() throws RPCException, StreamException {
		double TWRvalue = calculateTWR();
		PIDspeedAcceleration.ajustPID(TWRvalue * Pspeed, TWRvalue * Ispeed, TWRvalue * Dspeed);
	}

	private void checkHeight() throws RPCException, StreamException, IOException, InterruptedException {
		burnDistance = calculateBurnDistance();
		ajustPIDcontrol();
		if (horizontalSpeed.get() > 3) {
			navigation.pointRetrograde();
		} else {
			navigation.pointRadialOut();
		}

		double landingThreshold = calculateMaxAcceleration() * 3;
		if (surfAltitude.get() - landingThreshold < landingThreshold) {
			currentShip.getControl().setGear(true);
		}

		double acceleration = PIDheightAcceleration.computePID(surfAltitude.get(), burnDistance);
		double speed = PIDspeedAcceleration.computePID(verticalSpeed.get(), -5);
		double limit = (surfAltitude.get() - landingThreshold) / landingThreshold;
		throttle(Utilities.linearInterpolation(speed, acceleration, limit));
	}

	private void checkLanding() throws RPCException, IOException, InterruptedException {
		switch (currentShip.getSituation()) {
		case LANDED:
		case SPLASHED:
			StatusJPanel.setStatus("Pouso Finalizado!");
			executingAutoLand = false;
			executingFlight = false;
			descentFlight = false;
			throttle(0.0f);
			currentShip.getControl().setSAS(true);
			currentShip.getControl().setRCS(true);
			currentShip.getControl().setBrakes(false);
			currentShip.getAutoPilot().disengage();
		default:
			break;
		}
	}

	private double calculateBurnDistance() throws RPCException, StreamException {
		totalSpeed = Math.abs(new Vector(horizontalSpeed.get(), verticalSpeed.get(), 0).Magnitude());
		double burnTime = totalSpeed / calculateMaxAcceleration();
		double burnDistance = (calculateMaxAcceleration() * burnTime) * burnTime;
		MainGui.getParameters().getComponent(0).firePropertyChange("distancia", 0, burnDistance);
		return burnDistance;
	}

	public static void descer() {
		descentFlight = true;
	}
}

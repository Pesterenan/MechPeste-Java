package com.pesterenan.controllers;

import static com.pesterenan.utils.Status.ORBITAL_LIFTOFF_STATUS;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.pesterenan.MechPeste;
import com.pesterenan.utils.PIDcontrol;
import com.pesterenan.utils.Modules;
import com.pesterenan.utils.Utilities;
import com.pesterenan.views.StatusJPanel;

import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;

public class LiftoffController extends FlightController {

	private static final float PITCH_UP = 90;

	private float currentPitch;
	private float startCurveAlt = 100;
	private float finalApoapsisAlt = 80000;
	private float heading = 90;
	private float roll = 90;
	private String gravityCurveModel = Modules.CIRCLE.get();
	private PIDcontrol thrControl = new PIDcontrol();

	public LiftoffController(Map<String, String> commands) {
		super(getConnection());
		this.currentPitch = PITCH_UP;
		setFinalApoapsisAlt(Float.parseFloat(commands.get(Modules.APOAPSIS.get())));
		setHeading(Float.parseFloat(commands.get(Modules.DIRECTION.get())));
		setRoll(Float.parseFloat(commands.get(Modules.ROLL.get())));
		setGravityCurveModel(commands.get(Modules.INCLINATION.get()));
		StatusJPanel.setStatus(ORBITAL_LIFTOFF_STATUS.get());
	}

	@Override
	public void run() {
		try {
			liftoff();
			gravityCurve();
			finalizeOrbit();
		} catch (RPCException | InterruptedException | StreamException | IOException e) {
			StatusJPanel.setStatus("Decolagem abortada.");
			try {
				throttle(0f);
				currentShip.getAutoPilot().disengage();
			} catch (RPCException e1) {
				e1.printStackTrace();
			}
			return;
		}
	}

	private void finalizeOrbit() throws RPCException, StreamException, IOException, InterruptedException {
		StatusJPanel.setStatus("Mantendo apoapsis target até sair da atmosfera...");
		currentShip.getAutoPilot().disengage();
		currentShip.getControl().setSAS(true);
		currentShip.getControl().setRCS(true);
		while (flightParameters.getStaticPressure() > 100) {
			currentShip.getAutoPilot().setTargetDirection(flightParameters.getPrograde());
			throttle(thrControl.computePID(apoapsis.get(), getFinalApoapsis()));
			Thread.sleep(500);
		}

		StatusJPanel.setStatus("Planejando Manobra de circularização...");
		Map<String, String> commands = new HashMap<>();
		commands.put(Modules.MODULE.get(), Modules.MANEUVER_MODULE.get());
		commands.put(Modules.FUNCTION.get(), Modules.APOAPSIS.get());
		MechPeste.startModule(commands);
	}

	private void gravityCurve() throws RPCException, StreamException, InterruptedException {
		currentShip.getAutoPilot().targetPitchAndHeading(this.currentPitch, getHeading());
		currentShip.getAutoPilot().setTargetRoll(this.getRoll());

		currentShip.getAutoPilot().engage();
		throttle(1f);

		while (this.currentPitch > 1) {
			if (apoapsis.get() > getFinalApoapsis()) {
				break;
			}
			double currentAltitude = Utilities.remap(startCurveAlt, getFinalApoapsis(), 1, 0.01, altitude.get());
			double inclinationCurve = calculateInclinationCurve(currentAltitude);
			this.currentPitch = (float) (inclinationCurve * PITCH_UP);
			currentShip.getAutoPilot().targetPitchAndHeading(this.currentPitch, getHeading());
			throttle(thrControl.computePID(apoapsis.get(), getFinalApoapsis()));

			if (stageWithoutFuel()) {
				StatusJPanel.setStatus("Separando estágio...");
				Thread.sleep(1000);
				currentShip.getControl().activateNextStage();
				Thread.sleep(1000);
			}
			StatusJPanel.setStatus(String.format("A inclinação do foguete é: %.1f", currentPitch));
			Thread.sleep(250);
		}
	}

	private double calculateInclinationCurve(double currentAltitude) {
		if (gravityCurveModel.equals(Modules.QUADRATIC.get())) {
			return Utilities.easeInQuad(currentAltitude);
		}
		if (gravityCurveModel.equals(Modules.CUBIC.get())) {
			return Utilities.easeInCubic(currentAltitude);
		}
		if (gravityCurveModel.equals(Modules.SINUSOIDAL.get())) {
			return Utilities.easeInSine(currentAltitude);
		}
		if (gravityCurveModel.equals(Modules.EXPONENTIAL.get())) {
			return Utilities.easeInExpo(currentAltitude);
		}
		return Utilities.easeInCirc(currentAltitude);
	}

	private boolean stageWithoutFuel() throws RPCException, StreamException {
		for (Engine engine : currentShip.getParts().getEngines()) {
			if (engine.getPart().getStage() == currentShip.getControl().getCurrentStage() && !engine.getHasFuel()) {
				return true;
			}
		}
		return false;
	}

	public float getHeading() {
		return heading;
	}

	public float getFinalApoapsis() {
		return finalApoapsisAlt;
	}

	public float getRoll() {
		return this.roll;
	}

	private void setGravityCurveModel(String model) {
		this.gravityCurveModel = model;
	}

	public void setHeading(float heading) {
		final int MIN_HEADING = 0;
		final int MAX_HEADING = 360;
		this.heading = (float) Utilities.clamp(heading, MIN_HEADING, MAX_HEADING);
	}

	public void setRoll(float heading) {
		final int MIN_HEADING = 0;
		final int MAX_HEADING = 360;
		this.roll = (float) Utilities.clamp(heading, MIN_HEADING, MAX_HEADING);
	}

	public void setFinalApoapsisAlt(float finalApoapsisAlt) {
		final int MIN_FINAL_APOAPSIS = 10000;
		final int MAX_FINAL_APOAPSIS = 2000000;
		this.finalApoapsisAlt = (float) Utilities.clamp(finalApoapsisAlt, MIN_FINAL_APOAPSIS, MAX_FINAL_APOAPSIS);
	}
}

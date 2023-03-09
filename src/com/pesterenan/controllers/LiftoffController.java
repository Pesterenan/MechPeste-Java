package com.pesterenan.controllers;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Navigation;
import com.pesterenan.utils.Utilities;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Fairing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiftoffController extends Controller {

	private static final float PITCH_UP = 90;
	private final ControlePID thrControl = new ControlePID();
	private float currentPitch;
	private float finalApoapsisAlt;
	private float heading;
	private float roll;
	private float maxTWR;

	private boolean willDecoupleStages, willDeployPanelsAndRadiators;
	private String gravityCurveModel = Modulos.CIRCULAR.get();
	private Navigation navigation;

	public LiftoffController(Map<String, String> commands) {
		super();
		this.commands = commands;
		this.navigation = new Navigation(getNaveAtual());
		initializeParameters();
	}

	private void initializeParameters() {
		currentPitch = PITCH_UP;
		setFinalApoapsisAlt(Float.parseFloat(commands.get(Modulos.APOASTRO.get())));
		setHeading(Float.parseFloat(commands.get(Modulos.DIRECAO.get())));
		setRoll(Float.parseFloat(commands.get(Modulos.ROLAGEM.get())));
		maxTWR = (float) Utilities.clamp(Float.parseFloat(commands.get(Modulos.MAX_TWR.get())), 1.2, 5.0);
		setGravityCurveModel(commands.get(Modulos.INCLINACAO.get()));
		willDeployPanelsAndRadiators = Boolean.parseBoolean(commands.get(Modulos.ABRIR_PAINEIS.get()));
		willDecoupleStages = Boolean.parseBoolean(commands.get(Modulos.USAR_ESTAGIOS.get()));
		thrControl.adjustOutput(0.0, 1.0);
	}

	@Override
	public void run() {
		try {
			tuneAutoPilot();
			liftoff();
			gravityCurve();
			finalizeCurve();
			circularizeOrbitOnApoapsis();
		} catch (RPCException | InterruptedException | StreamException ignored) {
			setCurrentStatus(Bundle.getString("status_ready"));
		}
	}

	private void gravityCurve() throws RPCException, StreamException, InterruptedException {
		ap.setReferenceFrame(surfaceReferenceFrame);
		ap.targetPitchAndHeading(currentPitch, getHeading());
		ap.setTargetRoll(getRoll());
		ap.engage();
		throttle(getMaxThrottleForTWR(maxTWR));
		double startCurveAlt = altitude.get();

		while (currentPitch > 1) {
			if (apoastro.get() > getFinalApoapsis()) {
				throttle(0);
				break;
			}
			double altitudeProgress = Utilities.remap(startCurveAlt, getFinalApoapsis(), 1, 0.01, altitude.get(),
					false);
			currentPitch = (float) (calculateCurrentPitch(altitudeProgress));
			double currentMaxTWR = calculateTWRBasedOnPressure(currentPitch);
			ap.setTargetPitch(currentPitch);
			throttle(Math.min(thrControl.calcPID(apoastro.get() / getFinalApoapsis() * 1000, 1000),
					getMaxThrottleForTWR(currentMaxTWR)));

			if (willDecoupleStages && isCurrentStageWithoutFuel()) {
				decoupleStage();
			}
			setCurrentStatus(String.format(Bundle.getString("status_liftoff_inclination") + " %.1f", currentPitch));

			Thread.sleep(250);
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
		}
	}

	private double calculateTWRBasedOnPressure(float currentPitch) throws RPCException {
		float currentPressure = parametrosDeVoo.getDynamicPressure();
		if (currentPressure <= 10) {
			return Utilities.remap(90.0, 0.0, maxTWR, 5.0, currentPitch, true);
		}
		return Utilities.remap(22000.0, 10.0, maxTWR, 5.0, currentPressure, true);
	}

	private void finalizeCurve() throws RPCException, StreamException, InterruptedException {
		setCurrentStatus(Bundle.getString("status_maintaining_until_orbit"));
		getNaveAtual().getControl().setRCS(true);

		while (parametrosDeVoo.getDynamicPressure() > 10) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			navigation.aimAtPrograde();
			throttle(thrControl.calcPID(apoastro.get() / getFinalApoapsis() * 1000, 1000));
			Thread.sleep(100);
		}
		throttle(0.0f);
		if (willDecoupleStages) {
			jettisonFairings();
		}
		if (willDeployPanelsAndRadiators) {
			deployPanelsAndRadiators();
		}
	}

	private void circularizeOrbitOnApoapsis() {
		setCurrentStatus(Bundle.getString("status_planning_orbit"));
		Map<String, String> commands = new HashMap<>();
		commands.put(Modulos.MODULO.get(), Modulos.MODULO_EXEC_MANOBRAS.get());
		commands.put(Modulos.FUNCAO.get(), Modulos.APOASTRO.get());
		commands.put(Modulos.AJUSTE_FINO.get(), String.valueOf(false));
		MechPeste.newInstance().startModule(commands);
	}

	private void jettisonFairings() throws RPCException, InterruptedException {
		List<Fairing> fairings = getNaveAtual().getParts().getFairings();
		if (fairings.size() > 0) {
			setCurrentStatus(Bundle.getString("status_jettisoning_shields"));
			for (Fairing f : fairings) {
				if (f.getJettisoned()) {
					// Overly complicated way of getting the event from the button in the fairing
					// to jettison the fairing, since the jettison method doesn't work.
					String eventName = f.getPart().getModules().get(0).getEvents().get(0);
					f.getPart().getModules().get(0).triggerEvent(eventName);
					Thread.sleep(10000);
				}
			}
		}
	}

	private void deployPanelsAndRadiators() throws RPCException, InterruptedException {
		getNaveAtual().getControl().setSolarPanels(true);
		getNaveAtual().getControl().setRadiators(true);
	}

	private double calculateCurrentPitch(double currentAltitude) {
		if (gravityCurveModel.equals(Modulos.QUADRATICA.get())) {
			return Utilities.easeInQuad(currentAltitude) * PITCH_UP;
		}
		if (gravityCurveModel.equals(Modulos.CUBICA.get())) {
			return Utilities.easeInCubic(currentAltitude) * PITCH_UP;
		}
		if (gravityCurveModel.equals(Modulos.SINUSOIDAL.get())) {
			return Utilities.easeInSine(currentAltitude) * PITCH_UP;
		}
		if (gravityCurveModel.equals(Modulos.EXPONENCIAL.get())) {
			return Utilities.easeInExpo(currentAltitude) * PITCH_UP;
		}
		return Utilities.easeInCirc(currentAltitude) * PITCH_UP;
	}

	private boolean isCurrentStageWithoutFuel() throws RPCException {
		for (Engine engine : getNaveAtual().getParts().getEngines()) {
			if (engine.getPart().getStage() == getNaveAtual().getControl().getCurrentStage() && !engine.getHasFuel()) {
				return true;
			}
		}
		return false;
	}

	public float getHeading() {
		return heading;
	}

	public void setHeading(float heading) {
		final int MIN_HEADING = 0;
		final int MAX_HEADING = 360;
		this.heading = (float) Utilities.clamp(heading, MIN_HEADING, MAX_HEADING);
	}

	public float getFinalApoapsis() {
		return finalApoapsisAlt;
	}

	public float getRoll() {
		return this.roll;
	}

	public void setRoll(float roll) {
		final int MIN_ROLL = 0;
		final int MAX_ROLL = 360;
		this.roll = (float) Utilities.clamp(roll, MIN_ROLL, MAX_ROLL);
	}

	private void setGravityCurveModel(String model) {
		this.gravityCurveModel = model;
	}

	public void setFinalApoapsisAlt(float finalApoapsisAlt) {
		final int MIN_FINAL_APOAPSIS = 10000;
		final int MAX_FINAL_APOAPSIS = 2000000;
		this.finalApoapsisAlt = (float) Utilities.clamp(finalApoapsisAlt, MIN_FINAL_APOAPSIS, MAX_FINAL_APOAPSIS);
	}
}

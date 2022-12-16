package com.pesterenan.controllers;

import com.pesterenan.model.ActiveVessel;
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
	private float finalApoapsisAlt = 80000;
	private float heading = 90;
	private float roll = 90;
	private boolean willDecoupleStages, willDeployPanelsAndRadiators;
	private String gravityCurveModel = Modulos.CIRCULAR.get();
	private Navigation navigation;
	private long timer;

	public LiftoffController(ActiveVessel activeVessel) {
		super(activeVessel);
		this.navigation = new Navigation(activeVessel);
		initializeParameters();
	}

	private void initializeParameters() {
		currentPitch = PITCH_UP;
		setFinalApoapsisAlt(Float.parseFloat(activeVessel.commands.get(Modulos.APOASTRO.get())));
		setHeading(Float.parseFloat(activeVessel.commands.get(Modulos.DIRECAO.get())));
		setRoll(Float.parseFloat(activeVessel.commands.get(Modulos.ROLAGEM.get())));
		setGravityCurveModel(activeVessel.commands.get(Modulos.INCLINACAO.get()));
		willDeployPanelsAndRadiators = Boolean.parseBoolean(activeVessel.commands.get(Modulos.ABRIR_PAINEIS.get()));
		willDecoupleStages = Boolean.parseBoolean(activeVessel.commands.get(Modulos.USAR_ESTAGIOS.get()));
		thrControl.adjustOutput(0.0, 1.0);
	}

	@Override
	public void run() {
		try {
			activeVessel.liftoff();
			gravityCurve();
			finalizeCurve();
			circularizeOrbitOnApoapsis();
		} catch (RPCException | InterruptedException | StreamException e) {
			activeVessel.disengageAfterException(Bundle.getString("status_liftoff_abort"));
		}
	}


	private void gravityCurve() throws RPCException, StreamException {
		activeVessel.ap.setReferenceFrame(activeVessel.pontoRefSuperficie);
		activeVessel.ap.targetPitchAndHeading(currentPitch, getHeading());
		activeVessel.ap.setTargetRoll(getRoll());
		activeVessel.tuneAutoPilot();
		activeVessel.ap.engage();
		activeVessel.throttle(1f);

		while (currentPitch > 1) {
			long currentTime = System.currentTimeMillis();
			if (currentTime > timer + 250) {
				if (activeVessel.apoastro.get() > getFinalApoapsis()) {
					activeVessel.throttle(0);
					break;
				}
				float startCurveAlt = 100;
				double altitudeProgress =
						Utilities.remap(startCurveAlt, getFinalApoapsis(), 1, 0.01, activeVessel.altitude.get(),
						                false);
				currentPitch = (float) (calculateCurrentPitch(altitudeProgress));
				activeVessel.ap.setTargetPitch(currentPitch);
				activeVessel.throttle(
						thrControl.calcPID(activeVessel.apoastro.get() / getFinalApoapsis() * 1000, 1000));
				if (willDecoupleStages && isCurrentStageWithoutFuel()) {
					decoupleStage();
				}
				activeVessel.setCurrentStatus(
						String.format(Bundle.getString("status_liftoff_inclination") + " %.1f", currentPitch));
				timer = currentTime;
			}
		}
	}

	private void finalizeCurve() throws RPCException, StreamException, InterruptedException {
		activeVessel.setCurrentStatus(Bundle.getString("status_maintaining_until_orbit"));
		activeVessel.getNaveAtual().getControl().setRCS(true);

		while (activeVessel.parametrosDeVoo.getDynamicPressure() > 10) {
			navigation.aimAtPrograde();
			activeVessel.throttle(thrControl.calcPID(activeVessel.apoastro.get() / getFinalApoapsis() * 1000, 1000));
			Thread.sleep(100);
		}
		activeVessel.throttle(0.0f);
		if (willDeployPanelsAndRadiators) {
			deployPanelsAndRadiators();
		}
	}

	private void circularizeOrbitOnApoapsis() {
		activeVessel.setCurrentStatus(Bundle.getString("status_planning_orbit"));
		Map<String, String> commands = new HashMap<>();
		commands.put(Modulos.MODULO.get(), Modulos.MODULO_MANOBRAS.get());
		commands.put(Modulos.FUNCAO.get(), Modulos.APOASTRO.get());
		commands.put(Modulos.AJUSTE_FINO.get(), String.valueOf(false));
		activeVessel.startModule(commands);
	}

	private void decoupleStage() {
		try {
			activeVessel.setCurrentStatus(Bundle.getString("status_separating_stage"));
			activeVessel.getNaveAtual().getControl().activateNextStage();
			Thread.sleep(1000);
		} catch (RPCException | InterruptedException ignored) {
		}
	}

	private void deployPanelsAndRadiators() throws RPCException, InterruptedException {
		List<Fairing> fairings = activeVessel.getNaveAtual().getParts().getFairings();
		if (fairings.size() > 0) {
			activeVessel.setCurrentStatus(Bundle.getString("status_jettisoning_shields"));
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
		activeVessel.getNaveAtual().getControl().setSolarPanels(true);
		activeVessel.getNaveAtual().getControl().setRadiators(true);
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
		for (Engine engine : activeVessel.getNaveAtual().getParts().getEngines()) {
			if (engine.getPart().getStage() == activeVessel.getNaveAtual().getControl().getCurrentStage() &&
					!engine.getHasFuel()) {
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

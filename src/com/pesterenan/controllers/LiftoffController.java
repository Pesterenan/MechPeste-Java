package com.pesterenan.controllers;

import com.pesterenan.MechPeste;
import com.pesterenan.model.ActiveVessel;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Utilities;
import com.pesterenan.views.StatusJPanel;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Fairing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiftoffController extends ActiveVessel implements Runnable {

	private static final float PITCH_UP = 90;

	private float currentPitch;
	private float finalApoapsisAlt = 80000;
	private float heading = 90;
	private float roll = 90;
	private boolean willDecoupleStages, willDeployPanelsAndRadiators;
	private String gravityCurveModel = Modulos.CIRCULAR.get();
	private final ControlePID thrControl = new ControlePID();

	public LiftoffController(Map<String, String> commands) {
		super(getConexao());
		this.commands = commands;
		initializeParameters();
	}

	private void initializeParameters() {
		try {
			currentPitch = PITCH_UP;
			setFinalApoapsisAlt(Float.parseFloat(commands.get(Modulos.APOASTRO.get())));
			setHeading(Float.parseFloat(commands.get(Modulos.DIRECAO.get())));
			setRoll(Float.parseFloat(commands.get(Modulos.ROLAGEM.get())));
			setGravityCurveModel(commands.get(Modulos.INCLINACAO.get()));
			willDeployPanelsAndRadiators = Boolean.parseBoolean(commands.get(Modulos.ABRIR_PAINEIS.get()));
			willDecoupleStages = Boolean.parseBoolean(commands.get(Modulos.USAR_ESTAGIOS.get()));
			currentBody = naveAtual.getOrbit().getBody();
			pontoRefSuperficie = naveAtual.getSurfaceReferenceFrame();
			pontoRefOrbital = currentBody.getReferenceFrame();
			parametrosDeVoo = naveAtual.flight(pontoRefOrbital);
			altitude = getConexao().addStream(parametrosDeVoo, "getMeanAltitude");
			altitudeSup = getConexao().addStream(parametrosDeVoo, "getSurfaceAltitude");
			velVertical = getConexao().addStream(parametrosDeVoo, "getVerticalSpeed");
			velHorizontal = getConexao().addStream(parametrosDeVoo, "getHorizontalSpeed");
			apoastro = getConexao().addStream(naveAtual.getOrbit(), "getApoapsisAltitude");
			periastro = getConexao().addStream(naveAtual.getOrbit(), "getPeriapsisAltitude");
			gravityAcel = currentBody.getSurfaceGravity();
		} catch (StreamException | RPCException ignored) {
		}
	}

	@Override
	public void run() {
		try {
			liftoff();
			gravityCurve();
			finalizeCurve();
			circularizeOrbitOnApoapsis();
		} catch (RPCException | InterruptedException | StreamException e) {
			disengageAfterException(Bundle.getString("status_liftoff_abort"));
		}
	}

	private void gravityCurve() throws RPCException, StreamException, InterruptedException {
		ap.setReferenceFrame(pontoRefSuperficie);
		ap.targetPitchAndHeading(currentPitch, getHeading());
		ap.setTargetRoll(getRoll());
		ap.engage();
		throttle(1f);

		while (currentPitch > 1) {
			if (apoastro.get() > getFinalApoapsis()) {
				throttle(0);
				break;
			}
			float startCurveAlt = 100;
			double altitudeProgress =
					Utilities.remap(startCurveAlt, getFinalApoapsis(), 1, 0.01, altitude.get(), false);
			currentPitch = (float) (calculateCurrentPitch(altitudeProgress));
			ap.setTargetPitch(currentPitch);
			throttle(thrControl.calcPID(apoastro.get() / getFinalApoapsis() * 1000, 1000));

			if (willDecoupleStages && isCurrentStageWithoutFuel()) {
				decoupleStage();
			}
			StatusJPanel.setStatus(
					String.format(Bundle.getString("status_liftoff_inclination") + " %.1f", currentPitch));
			Thread.sleep(250);
		}
	}

	private void finalizeCurve() throws RPCException, StreamException, InterruptedException {
		StatusJPanel.setStatus(Bundle.getString("status_maintaining_until_orbit"));
		naveAtual.getControl().setRCS(true);
		ap.setReferenceFrame(pontoRefOrbital);
		while (parametrosDeVoo.getDynamicPressure() > 10) {
			ap.setTargetDirection(parametrosDeVoo.getPrograde());
			throttle(thrControl.calcPID(apoastro.get() / getFinalApoapsis() * 1000, 1000));
			Thread.sleep(100);
		}
		if (willDeployPanelsAndRadiators) {
			deployPanelsAndRadiators();
		}
	}

	private void circularizeOrbitOnApoapsis() {
		StatusJPanel.setStatus(Bundle.getString("status_planning_orbit"));
		Map<String, String> commands = new HashMap<>();
		commands.put(Modulos.MODULO.get(), Modulos.MODULO_MANOBRAS.get());
		commands.put(Modulos.FUNCAO.get(), Modulos.APOASTRO.get());
		commands.put(Modulos.AJUSTE_FINO.get(), String.valueOf(true));
		MechPeste.startModule(commands);
	}

	private void decoupleStage() throws InterruptedException, RPCException {
		StatusJPanel.setStatus(Bundle.getString("status_separating_stage"));
		Thread.sleep(1000);
		naveAtual.getControl().activateNextStage();
		Thread.sleep(1000);
	}

	private void deployPanelsAndRadiators() throws RPCException, InterruptedException {
		List<Fairing> fairings = naveAtual.getParts().getFairings();
		if (fairings.size() > 0) {
			StatusJPanel.setStatus(Bundle.getString("status_jettisoning_shields"));
			for (Fairing f : fairings) {
				if (!f.getJettisoned()) {
					// Overly complicated way of getting the event from the button in the fairing
					// to jettison the fairing, since the jettison method doesn't work.
					String eventName = f.getPart().getModules().get(0).getEvents().get(0);
					f.getPart().getModules().get(0).triggerEvent(eventName);
					Thread.sleep(10000);
				}
			}
		}
		naveAtual.getControl().setSolarPanels(true);
		naveAtual.getControl().setRadiators(true);
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
		for (Engine engine : naveAtual.getParts().getEngines()) {
			if (engine.getPart().getStage() == naveAtual.getControl().getCurrentStage() && !engine.getHasFuel()) {
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

	public void setRoll(float heading) {
		final int MIN_HEADING = 0;
		final int MAX_HEADING = 360;
		this.roll = (float) Utilities.clamp(heading, MIN_HEADING, MAX_HEADING);
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

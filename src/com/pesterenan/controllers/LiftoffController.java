package com.pesterenan.controllers;

import static com.pesterenan.utils.Status.STATUS_DECOLAGEM_ORBITAL;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.pesterenan.MechPeste;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Utilities;
import com.pesterenan.views.StatusJPanel;

import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;

public class LiftoffController extends FlightController implements Runnable {

	private static final float PITCH_UP = 90;

	private float currentPitch;
	private float startCurveAlt = 100;
	private float finalApoapsisAlt = 80000;
	private float heading = 90;
	private String gravityCurveModel = Modulos.CIRCULAR.get();

	public LiftoffController(Map<String, String> commands) {
		super(getConexao());
		this.currentPitch = PITCH_UP;
		setFinalApoapsisAlt(Float.parseFloat(commands.get(Modulos.APOASTRO.get())));
		setHeading(Float.parseFloat(commands.get(Modulos.DIRECAO.get())));
		setGravityCurveModel(commands.get(Modulos.INCLINACAO.get()));
		StatusJPanel.setStatus(STATUS_DECOLAGEM_ORBITAL.get());
	}

	@Override
	public void run() {
		try {
			liftoff();
			gravityCurve();
			circularizeOnAp();
		} catch (RPCException | InterruptedException | StreamException | IOException e) {
			StatusJPanel.setStatus("Decolagem abortada.");
			try {
				throttle(0f);
				naveAtual.getAutoPilot().disengage();
			} catch (RPCException e1) {
				e1.printStackTrace();
			}
			return;
		}
	}

	private void circularizeOnAp() throws RPCException, StreamException, IOException, InterruptedException {
		StatusJPanel.setStatus("Planejando Manobra de circularização...");
		naveAtual.getAutoPilot().disengage();
		naveAtual.getControl().setSAS(true);
		naveAtual.getControl().setRCS(true);

		Map<String, String> commands = new HashMap<>();
		commands.put(Modulos.MODULO.get(), Modulos.MODULO_MANOBRAS.get());
		commands.put(Modulos.FUNCAO.get(), Modulos.APOASTRO.get());
		MechPeste.iniciarModulo(commands);
	}

	private void gravityCurve() throws RPCException, StreamException, InterruptedException {
		naveAtual.getAutoPilot().targetPitchAndHeading(this.currentPitch, getHeading());
		naveAtual.getAutoPilot().setTargetRoll(270);
		
		
		naveAtual.getAutoPilot().engage();
		throttle(1f);

		while (this.currentPitch > 1) {
			double currentAltitude = Utilities.remap(startCurveAlt, getFinalApoapsis(), 1, 0.01, altitude.get());
			double inclinationCurve = calculateInclinationCurve(currentAltitude);
			this.currentPitch = (float) (inclinationCurve * PITCH_UP);
			naveAtual.getAutoPilot().targetPitchAndHeading(this.currentPitch, getHeading());

			throttle(Utilities.remap(getFinalApoapsis() * 0.95, getFinalApoapsis(), 1, 0.1, apoastro.get()));
			if (apoastro.get() > getFinalApoapsis()) {
				throttle(0.0f);
				break;
			}

			if (stageWithoutFuel()) {
				StatusJPanel.setStatus("Separando estágio...");
				Thread.sleep(1000);
				naveAtual.getControl().activateNextStage();
				Thread.sleep(1000);
			}

			StatusJPanel.setStatus(String.format("A inclinação do foguete é: %.1f", currentPitch));
			Thread.sleep(25);
		}
	}

	private double calculateInclinationCurve(double currentAltitude) {
		if (gravityCurveModel.equals(Modulos.QUADRATICA.get())) {
			return Utilities.easeInQuad(currentAltitude);
		}
		if (gravityCurveModel.equals(Modulos.CUBICA.get())) {
			return Utilities.easeInCubic(currentAltitude);
		}
		if (gravityCurveModel.equals(Modulos.SINUSOIDAL.get())) {
			return Utilities.easeInSine(currentAltitude);
		}
		if (gravityCurveModel.equals(Modulos.EXPONENCIAL.get())) {
			return Utilities.easeInExpo(currentAltitude);
		}
			return Utilities.easeInCirc(currentAltitude);
	}

	private boolean stageWithoutFuel() throws RPCException, StreamException {
		for (Engine motor : naveAtual.getParts().getEngines()) {
			if (motor.getPart().getStage() == naveAtual.getControl().getCurrentStage() && !motor.getHasFuel()) {
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

	private void setGravityCurveModel(String model) {
		this.gravityCurveModel = model;
	}

	public void setHeading(float heading) {
		final int MIN_HEADING = 0;
		final int MAX_HEADING = 360;
		this.heading = (float) Utilities.clamp(heading, MIN_HEADING, MAX_HEADING);
	}

	public void setFinalApoapsisAlt(float finalApoapsisAlt) {
		final int MIN_FINAL_APOAPSIS = 10000;
		final int MAX_FINAL_APOAPSIS = 2000000;
		this.finalApoapsisAlt = (float) Utilities.clamp(finalApoapsisAlt, MIN_FINAL_APOAPSIS, MAX_FINAL_APOAPSIS);
	}
}

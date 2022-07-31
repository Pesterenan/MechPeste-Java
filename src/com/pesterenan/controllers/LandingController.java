package com.pesterenan.controllers;

import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.*;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusJPanel;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.VesselSituation;

import java.util.Map;

public class LandingController extends FlightController implements Runnable {

private static final int ALTITUDE_POUSO_AUTOMATICO = 8000;
private static double velP = 0.05, velI = 0.001, velD = 0.01;
private static boolean landFromHovering = false;
private ControlePID altitudeCtrl = new ControlePID();
private ControlePID velocityCtrl = new ControlePID();
private Navigation navigation = new Navigation();
private double hoverAltitude = 100;
private double distanciaDaQueima = 0, velocidadeTotal = 0;
private Map<String, String> commands;
private boolean autoLandingEngaged = false;
private boolean hoveringMode = false;

public LandingController(Map<String, String> commands) {
	super(getConexao());
	this.commands = commands;
	this.altitudeCtrl.limitarSaida(0, 1);
	this.velocityCtrl.limitarSaida(0, 1);
}

public static void land() {
	landFromHovering = true;
}

@Override
public void run() {
	if (commands.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO_SOBREVOAR.get())) {
		this.hoverAltitude = Double.parseDouble(commands.get(Modulos.ALTITUDE_SOBREVOO.get()));
		hoveringMode = true;
		altitudeCtrl.limitarSaida(- 0.5, 1);
		hoverArea();
	}
	if (commands.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO.get())) {
		autoLanding();
	}
}

private void hoverArea() {
	try {
		liftoff();
		naveAtual.getAutoPilot().engage();
		while (hoveringMode) {
			try {
				if (velHorizontal.get() > 15) {
					navigation.mirarRetrogrado();
				} else {
					navigation.mirarRadialDeFora();
				}
				adjustPIDCtrls();
				double altPID = altitudeCtrl.computarPID(altitudeSup.get(), hoverAltitude);
				double velPID = velocityCtrl.computarPID(velVertical.get(), altPID * gravityAcel);
				throttle(velPID);
				if (landFromHovering) {
					naveAtual.getControl().setGear(true);
					hoverAltitude = 0;
					checkForLanding();
				}
				Thread.sleep(25);
			} catch (RPCException | StreamException e) {
				disengageAfterException(Bundle.getString("status_function_abort"));
				break;
			}
		}
	} catch (InterruptedException | RPCException e) {
		disengageAfterException(Bundle.getString("status_liftoff_abort"));
	}
}

private void autoLanding() {
	try {
		throttle(0.0f);
		naveAtual.getAutoPilot().engage();
		StatusJPanel.setStatus(Bundle.getString("status_starting_landing_at") + celestialBody);
		deOrbitShip();
		checkAltitudeForLanding();
		beginAutoLanding();
	} catch (RPCException | StreamException | InterruptedException e) {
		disengageAfterException(Bundle.getString("status_couldnt_land"));
	}
}

private void deOrbitShip() throws RPCException, InterruptedException, StreamException {
	if (naveAtual.getSituation().equals(VesselSituation.ORBITING) || naveAtual.getSituation().equals(VesselSituation.SUB_ORBITAL)) {
		StatusJPanel.setStatus(Bundle.getString("status_going_suborbital"));
		Thread.sleep(1000);
		while (naveAtual.getAutoPilot().getHeadingError() > 5) {
			StatusJPanel.setStatus(Bundle.getString("status_orienting_ship"));
			navigation.mirarRetrogrado();
			Thread.sleep(250);
		}
		while (periastro.get() > 0) {
			StatusJPanel.setStatus(Bundle.getString("status_lowering_periapsis"));
			throttle(altitudeCtrl.computarPID(- periastro.get(), 0));
			Thread.sleep(100);
		}
		throttle(0);
	}
}

private void checkAltitudeForLanding() throws RPCException, StreamException, InterruptedException {
	while (! autoLandingEngaged) {
		naveAtual.getControl().setBrakes(true);
		navigation.mirarRetrogrado();
		if (calculateCurrentVelocityMagnitude() < calculateZeroVelocityMagnitude() && velVertical.get() < - 1) {
			autoLandingEngaged = true;
		}
		Thread.sleep(50);
	}
}

private void beginAutoLanding() throws InterruptedException, RPCException, StreamException {
	StatusJPanel.setStatus(Bundle.getString("status_starting_landing"));
	while (autoLandingEngaged) {
		checkAltitude();
		checkForLanding();
		Thread.sleep(25);
	}
}

private void adjustPIDCtrls() throws RPCException, StreamException {
	altitudeCtrl.ajustarPID(velP, velI, calcularTEP() * velD);
	velocityCtrl.ajustarPID(calcularTEP() * velP, velI, velD);
}

private void checkAltitude() throws RPCException, StreamException {
	adjustPIDCtrls();
	double currentVelocityMagnitude = calculateCurrentVelocityMagnitude();
	double zeroVelocityMagnitude = calculateZeroVelocityMagnitude();
	double distanceToAutoLand = currentVelocityMagnitude - zeroVelocityMagnitude;

	// Mirar no retrogrado ou radial dependendo da velocidade
	if (velHorizontal.get() > 0.5) {
		navigation.mirarRetrogrado();
	} else {
		navigation.mirarRadialDeFora();
	}

	double landingDistanceThreshold = 0;
	landingDistanceThreshold = Utilities.clamp(landingDistanceThreshold, 100, calcularAcelMaxima() * 3);
	if (distanceToAutoLand < landingDistanceThreshold) {
		naveAtual.getControl().setGear(true);
	}

	double acelPIDValue = altitudeCtrl.computarPID(currentVelocityMagnitude, zeroVelocityMagnitude);
	double velPIDValue = velocityCtrl.computarPID(velVertical.get(), - Utilities.clamp(altitudeSup.get() * 0.1, 2, 20));
	double threshold = Utilities.clamp(((currentVelocityMagnitude + zeroVelocityMagnitude) - landingDistanceThreshold) / landingDistanceThreshold, 0, 1);
	throttle(Utilities.linearInterpolation(velPIDValue, acelPIDValue, threshold));
}

private void checkForLanding() throws RPCException {
	switch (naveAtual.getSituation()) {
		case LANDED:
		case SPLASHED:
			StatusJPanel.setStatus(Bundle.getString("status_landed"));
			autoLandingEngaged = false;
			hoveringMode = false;
			landFromHovering = false;
			throttle(0.0f);
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setRCS(true);
			naveAtual.getControl().setBrakes(false);
			naveAtual.getAutoPilot().disengage();
		default:
			break;
	}
}

private double calculateCurrentVelocityMagnitude() throws RPCException, StreamException {
	double timeToGround = altitudeSup.get() / velVertical.get();
	double horizontalDistance = velHorizontal.get() * timeToGround;
	return Math.abs(new Vetor(horizontalDistance, altitudeSup.get(), 0).Magnitude());
}

private double calculateZeroVelocityMagnitude() throws RPCException, StreamException {
	double zeroVelocityDistance = Math.abs(new Vetor(velHorizontal.get(), velVertical.get(), 0).Magnitude());
	double zeroVelocityBurnTime = zeroVelocityDistance / calcularAcelMaxima();
	MainGui.getParametros().getComponent(0).firePropertyChange("distancia", 0, zeroVelocityDistance * zeroVelocityBurnTime);
	return zeroVelocityDistance * zeroVelocityBurnTime;
}
}

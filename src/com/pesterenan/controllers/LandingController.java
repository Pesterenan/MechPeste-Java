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
private boolean executandoPousoAutomatico = false;
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
	while (! executandoPousoAutomatico) {
		distanciaDaQueima = calcularDistanciaParaZerarVelocidade();
		naveAtual.getControl().setBrakes(true);
		navigation.mirarRetrogrado();
		if (altitudeSup.get() < ALTITUDE_POUSO_AUTOMATICO) {
			if (altitudeSup.get() < distanciaDaQueima && velVertical.get() < - 1) {
				executandoPousoAutomatico = true;
			}
		}
		Thread.sleep(50);
	}
}

private void beginAutoLanding() throws InterruptedException, RPCException, StreamException {
	StatusJPanel.setStatus(Bundle.getString("status_starting_landing"));
	while (executandoPousoAutomatico) {
		checarAltitude();
		checkForLanding();
		Thread.sleep(25);
	}
}

private void adjustPIDCtrls() throws RPCException, StreamException {
	altitudeCtrl.ajustarPID(velP, velI, calcularAcelMaxima() * velD);
	velocityCtrl.ajustarPID(calcularTEP() * velP, velI, velD);
}

private void checarAltitude() throws RPCException, StreamException {
	adjustPIDCtrls();
	// Checar distancia da queima usando a magnitude da velocidade para zerar
	distanciaDaQueima = calcularDistanciaParaZerarVelocidade();
	// Checar a distancia da queda de altitude usando a velocidade e altitude do foguete
	double distVelAtual = calcularDistanciaDaVelocidadeAtual();
	MainGui.getParametros().getComponent(0).firePropertyChange("distancia", 0, distanciaDaQueima);

	// Mirar no retrogrado ou radial dependendo da velocidade
	if (velHorizontal.get() > 1) {
		navigation.mirarRetrogrado();
	} else {
		navigation.mirarRadialDeFora();
	}

	double limiarDoPouso = 0;
	limiarDoPouso = Utilities.clamp(limiarDoPouso, 100, calcularAcelMaxima() * 5);
	if (altitudeSup.get() - limiarDoPouso < limiarDoPouso) {
		naveAtual.getControl().setGear(true);
	}

	double acel = altitudeCtrl.computarPID(distVelAtual, distanciaDaQueima);
	double vel = velocityCtrl.computarPID(velVertical.get(), - 5);
	double limite = Utilities.clamp((distVelAtual - limiarDoPouso) / limiarDoPouso, 0, 1);
	throttle(Utilities.linearInterpolation(vel, acel, limite));
}

private void checkForLanding() throws RPCException {
	switch (naveAtual.getSituation()) {
		case LANDED:
		case SPLASHED:
			StatusJPanel.setStatus(Bundle.getString("status_landed"));
			executandoPousoAutomatico = false;
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

private double calcularDistanciaParaZerarVelocidade() throws RPCException, StreamException {
	velocidadeTotal = Math.abs(new Vetor(velHorizontal.get(), velVertical.get(), 0).Magnitude());
	double duracaoDaQueima = velocidadeTotal / calcularAcelMaxima();
	double distanciaDaQueima = velocidadeTotal * duracaoDaQueima;
	return distanciaDaQueima;
}

private double calcularDistanciaDaVelocidadeAtual() throws RPCException, StreamException {
	// descobrir tempo de queda usando a velocidade vertical e a altitude
	double tempoDeQueda = altitudeSup.get() / velVertical.get();
	// multiplicar a velocidade horizontal pelo tempo
	double distanciaHorizontal = velHorizontal.get() * tempoDeQueda;
	// montar um vetor com esses valores e pegar a magnitude
	// retornar esse valor para ser comparado com a distancia de queda
	return Math.abs(new Vetor(distanciaHorizontal, altitudeSup.get(), 0).Magnitude());
}
}

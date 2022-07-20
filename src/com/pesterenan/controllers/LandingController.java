package com.pesterenan.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Navegacao;
import com.pesterenan.utils.Utilities;
import com.pesterenan.utils.Vetor;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusJPanel;

import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.VesselSituation;

public class LandingController extends FlightController implements Runnable {

	private static final int ALTITUDE_POUSO_AUTOMATICO = 8000;

	private ControlePID altitudeCtrl = new ControlePID();
	private ControlePID velocityCtrl = new ControlePID();
	private Navegacao navigation = new Navegacao();

	private static double velP = 0.025, velI = 0.001, velD = 0.01;

	private double hoverAltitude = 100;
	private double distanciaDaQueima = 0, velocidadeTotal = 0;
	private Map<String, String> comandos = new HashMap<>();
	private boolean executandoPousoAutomatico = false;
	private boolean executandoSobrevoo = false;
	private static boolean descerDoSobrevoo = false;

	public LandingController(Map<String, String> comandos) {
		super(getConexao());
		this.comandos = comandos;
		this.altitudeCtrl.limitarSaida(0, 1);
		this.velocityCtrl.limitarSaida(0, 1);
	}

	@Override
	public void run() {
		if (comandos.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO_SOBREVOAR.get())) {
			this.hoverAltitude = Double.parseDouble(comandos.get(Modulos.ALTITUDE_SOBREVOO.get()));
			executandoSobrevoo = true;
			altitudeCtrl.limitarSaida(-0.5, 1);
			sobrevoarArea();
		}
		if (comandos.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO.get())) {
			pousarAutomaticamente();
		}
	}

	private void sobrevoarArea() {
		try {
			liftoff();
			naveAtual.getAutoPilot().engage();
			while (executandoSobrevoo) {
				try {
					if (velHorizontal.get() > 15) {
						navigation.mirarRetrogrado();
					} else {
						navigation.mirarRadialDeFora();
					}
					ajustarCtrlPIDs();
					double altPID = altitudeCtrl.computarPID(altitudeSup.get(), hoverAltitude);
					double velPID = velocityCtrl.computarPID(velVertical.get(), altPID * acelGravidade);
					throttle(velPID);
					if (descerDoSobrevoo == true) {
						naveAtual.getControl().setGear(true);
						hoverAltitude = 0;
						checarPouso();
					}
					Thread.sleep(25);
				} catch (RPCException | StreamException | IOException e) {
					disengageAfterException(Bundle.getString("status_function_abort"));
					break;
				}
			}
		} catch (InterruptedException | RPCException e) {
			disengageAfterException(Bundle.getString("status_liftoff_abort"));
		}
	}

	private void pousarAutomaticamente() {
		try {
			throttle(0.0f);
			naveAtual.getAutoPilot().engage();
			StatusJPanel.setStatus(Bundle.getString("status_starting_landing_at") + corpoCeleste);
			deOrbitShip();
			checarAltitudeParaPouso();
			comecarPousoAutomatico();
		} catch (RPCException | StreamException | InterruptedException | IOException e) {
			disengageAfterException(Bundle.getString("status_couldnt_land"));
		}
	}

	private void deOrbitShip() throws RPCException, InterruptedException, StreamException {
		if (naveAtual.getSituation().equals(VesselSituation.ORBITING)
				|| naveAtual.getSituation().equals(VesselSituation.SUB_ORBITAL)) {
			StatusJPanel.setStatus(Bundle.getString("status_going_suborbital"));
			Thread.sleep(1000);
			navigation.mirarRetrogrado();
			while (naveAtual.getAutoPilot().getHeadingError() > 5) {
				StatusJPanel.setStatus(Bundle.getString("status_orienting_ship"));
				Thread.sleep(250);
			}
			while (periastro.get() > 0) {
				StatusJPanel.setStatus(Bundle.getString("status_lowering_periapsis"));
				throttle(altitudeCtrl.computarPID(-periastro.get(), 0));
				Thread.sleep(100);
			}
			throttle(0);
		}
	}

	private void checarAltitudeParaPouso() throws RPCException, StreamException, InterruptedException {
		while (!executandoPousoAutomatico) {
			distanciaDaQueima = calcularDistanciaDaQueima();
			naveAtual.getControl().setBrakes(true);
			navigation.mirarRetrogrado();
			if (altitudeSup.get() < ALTITUDE_POUSO_AUTOMATICO) {
				if (altitudeSup.get() < distanciaDaQueima && velVertical.get() < -1) {
					executandoPousoAutomatico = true;
				}
			}
			Thread.sleep(50);
		}
	}

	private void comecarPousoAutomatico() throws InterruptedException, RPCException, StreamException, IOException {
		StatusJPanel.setStatus(Bundle.getString("status_starting_landing"));
		while (executandoPousoAutomatico) {
			distanciaDaQueima = calcularDistanciaDaQueima();
			checarAltitude();
			checarPouso();
			Thread.sleep(25);
		}
	}

	private void ajustarCtrlPIDs() throws RPCException, StreamException {
		double valorTEP = calcularTEP();
		double acelMaxima = calcularAcelMaxima();
		altitudeCtrl.ajustarPID(velP, velI, acelMaxima * velD);
		velocityCtrl.ajustarPID(valorTEP * velP, velI, velD);
	}

	private void checarAltitude() throws RPCException, StreamException, IOException, InterruptedException {
		distanciaDaQueima = calcularDistanciaDaQueima();
		ajustarCtrlPIDs();
		if (velHorizontal.get() > 3) {
			navigation.mirarRetrogrado();
		} else {
			navigation.mirarRadialDeFora();
		}

		double limiarDoPouso = calcularAcelMaxima() * 5;
		if (altitudeSup.get() - limiarDoPouso < limiarDoPouso) {
			naveAtual.getControl().setGear(true);
		}

		double acel = altitudeCtrl.computarPID(altitudeSup.get(), distanciaDaQueima);
		double vel = velocityCtrl.computarPID(velVertical.get(), -5);
		double limite = Utilities.clamp((altitudeSup.get() - limiarDoPouso) / limiarDoPouso, 0, 1);
		System.out.println(String.format("%.2f vel, %.2f acel, %.2f limite %.2f throttle", vel, acel, limite,
				Utilities.linearInterpolation(vel, acel, limite)));
		throttle(Utilities.linearInterpolation(vel, acel, limite));
	}

	private void checarPouso() throws RPCException, IOException, InterruptedException {
		switch (naveAtual.getSituation()) {
		case LANDED:
		case SPLASHED:
			StatusJPanel.setStatus(Bundle.getString("status_landed"));
			executandoPousoAutomatico = false;
			executandoSobrevoo = false;
			descerDoSobrevoo = false;
			throttle(0.0f);
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setRCS(true);
			naveAtual.getControl().setBrakes(false);
			naveAtual.getAutoPilot().disengage();
		default:
			break;
		}
	}

	private double calcularDistanciaDaQueima() throws RPCException, StreamException {
		velocidadeTotal = Math.abs(new Vetor(velHorizontal.get(), velVertical.get(), 0).Magnitude());
		double duracaoDaQueima = velocidadeTotal / calcularAcelMaxima();
		double distanciaDaQueima = (calcularAcelMaxima() * duracaoDaQueima) * duracaoDaQueima;
		MainGui.getParametros().getComponent(0).firePropertyChange("distancia", 0, distanciaDaQueima);
		return distanciaDaQueima;
	}

	public static void descer() {
		descerDoSobrevoo = true;

	}
}

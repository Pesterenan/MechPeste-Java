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

public class LandingController extends FlightController implements Runnable {

	private static final int ALTITUDE_POUSO_AUTOMATICO = 8000;

	private ControlePID altitudeAcelPID = new ControlePID();
	private ControlePID velocidadeAcelPID = new ControlePID();
	private Navegacao navegacao = new Navegacao();

	private static double velP = 0.025, velI = 0.001, velD = 0.01;

	private double altitudeDeSobrevoo = 100;
	private double distanciaDaQueima = 0, velocidadeTotal = 0;
	private Map<String, String> comandos = new HashMap<>();
	private boolean executandoPousoAutomatico = false;
	private boolean executandoSobrevoo = false;
	private static boolean descerDoSobrevoo = false;

	public LandingController(Map<String, String> comandos) {
		super(getConexao());
		this.comandos = comandos;
		this.altitudeAcelPID.limitarSaida(0, 1);
		this.velocidadeAcelPID.limitarSaida(0, 1);
	}

	@Override
	public void run() {
		if (comandos.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO_SOBREVOAR.get())) {
			this.altitudeDeSobrevoo = Double.parseDouble(comandos.get(Modulos.ALTITUDE_SOBREVOO.get()));
			executandoSobrevoo = true;
			altitudeAcelPID.limitarSaida(-0.5, 1);
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
						navegacao.mirarRetrogrado();
					} else {
						navegacao.mirarRadialDeFora();
					}
					ajustarCtrlPIDs();
					double altPID = altitudeAcelPID.computarPID(altitudeSup.get(), altitudeDeSobrevoo);
					double velPID = velocidadeAcelPID.computarPID(velVertical.get(), altPID * acelGravidade);
					throttle(velPID);
					if (descerDoSobrevoo == true) {
						naveAtual.getControl().setGear(true);
						altitudeDeSobrevoo = 0;
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
			liftoff();
			throttle(0.0f);
			naveAtual.getAutoPilot().engage();
			StatusJPanel.setStatus(Bundle.getString("status_starting_landing_at") + corpoCeleste);
			checarAltitudeParaPouso();
			comecarPousoAutomatico();
		} catch (RPCException | StreamException | InterruptedException | IOException e) {
			disengageAfterException(Bundle.getString("status_couldnt_land"));
		}
	}

	private void checarAltitudeParaPouso() throws RPCException, StreamException, InterruptedException {
		while (!executandoPousoAutomatico) {
			distanciaDaQueima = calcularDistanciaDaQueima();
			naveAtual.getControl().setBrakes(true);
			navegacao.mirarRetrogrado();
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
		velocidadeAcelPID.ajustarPID(valorTEP * velP, valorTEP * velI, valorTEP * velD);
	}

	private void checarAltitude() throws RPCException, StreamException, IOException, InterruptedException {
		distanciaDaQueima = calcularDistanciaDaQueima();
		ajustarCtrlPIDs();
		if (velHorizontal.get() > 3) {
			navegacao.mirarRetrogrado();
		} else {
			navegacao.mirarRadialDeFora();
		}

		double limiarDoPouso = calcularAcelMaxima() * 3;
		if (altitudeSup.get() - limiarDoPouso < limiarDoPouso) {
			naveAtual.getControl().setGear(true);
		}

		double acel = altitudeAcelPID.computarPID(altitudeSup.get(), distanciaDaQueima);
		double vel = velocidadeAcelPID.computarPID(velVertical.get(), -5);
		double limite = (altitudeSup.get() - limiarDoPouso) / limiarDoPouso;
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

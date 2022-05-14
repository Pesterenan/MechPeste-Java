package com.pesterenan.controller;

import static com.pesterenan.utils.Status.STATUS_POUSO_AUTOMATICO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pesterenan.gui.MainGui;
import com.pesterenan.gui.StatusJPanel;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Vetor;

import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.SASMode;
import krpc.client.services.SpaceCenter.VesselSituation;

public class PousoAutomaticoController extends TelemetriaController implements Runnable {

	private static final int ALTITUDE_POUSO_AUTOMATICO = 10000;
	private static final int ALTITUDE_TREM_DE_POUSO = 200;

	private static double altP = 0.01, altI = 0.01, altD = 0.01;
	private static double velP = 0.025, velI = 0.05, velD = 0.1;

	//////////////////////////////////////////////////////

	private double altitudeDeSobrevoo = 100;
	private float acelGravidade;
	private boolean executandoPousoAutomatico = false;
	private ControlePID altitudeAcelPID = new ControlePID();
	private ControlePID velocidadeAcelPID = new ControlePID();
	private Map<String, String> comandos = new HashMap<>();
	double distanciaDaQueima = 0;
	private double velocidadeTotal;

	public PousoAutomaticoController(Map<String, String> comandos) {
		super(getConexao());
		this.comandos = comandos;
		try {
			this.acelGravidade = naveAtual.getOrbit().getBody().getSurfaceGravity();
			this.altitudeAcelPID.limitarSaida(0, 1);
			this.velocidadeAcelPID.limitarSaida(0, 1);
			this.velocidadeAcelPID.setLimitePID(0);
			this.altitudeAcelPID.setLimitePID(0);
			StatusJPanel.setStatus(STATUS_POUSO_AUTOMATICO.get());
		} catch (RPCException e) {
		}
	}

	@Override
	public void run() {
		if (comandos.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO_SOBREVOAR.get())) {
			this.altitudeDeSobrevoo = Double.parseDouble(comandos.get(Modulos.ALTITUDE_SOBREVOO.get()));
			sobrevoarArea();
		}
		if (comandos.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO.get())) {
			pousarAutomaticamente();
		}
	}

	private void sobrevoarArea() {
		decolagem();
		while (true) {
			try {
				double valorTEP = calcularTEP();
				altitudeAcelPID.ajustarPID(valorTEP * altP, altI, valorTEP * altD);
				altitudeAcelPID.setEntradaPID(altitudeSup.get() * 100 / altitudeDeSobrevoo);
				velocidadeAcelPID.setEntradaPID(velVertical.get());
				acelerar((float) (altitudeAcelPID.computarPID() + velocidadeAcelPID.computarPID()));
				Thread.sleep(25);
			} catch (RPCException | InterruptedException | StreamException e) {
			}
		}
	}

	private void pousarAutomaticamente() {
		decolagem();
		try {
			acelerar(0.0f);
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setSASMode(SASMode.STABILITY_ASSIST);
			StatusJPanel.setStatus("Iniciando pouso automático em: " + naveAtual.getOrbit().getBody().getName());
			checarAltitudeParaPouso();
			comecarPousoAutomatico();
		} catch (RPCException | StreamException | InterruptedException e) {
			System.err.println("Deu erro, não tem o que fazer.");
		}
	}

	private void checarAltitudeParaPouso() throws RPCException, StreamException, InterruptedException {
		naveAtual.getControl().setBrakes(true);
		while (!executandoPousoAutomatico) {
			distanciaDaQueima = calcularDistanciaDaQueima();

			if (altitudeSup.get() < ALTITUDE_POUSO_AUTOMATICO 
					&& altitudeSup.get() < distanciaDaQueima 
					&& velVertical.get() < -1) {
				naveAtual.getControl().setSASMode(SASMode.RETROGRADE);
				executandoPousoAutomatico = true;
			}
			Thread.sleep(50);
		}
	}
	private double interpolacaoLinear (double v0, double v1, double t) {
		return (1-t) * v0 + t * v1;
	}
	private void comecarPousoAutomatico() {
		StatusJPanel.setStatus("Iniciando Pouso Automático!");
		while (executandoPousoAutomatico) {
			try {
				distanciaDaQueima = calcularDistanciaDaQueima();
				double valorTEP = calcularTEP();
				velocidadeAcelPID.ajustarPID(valorTEP * altP, valorTEP *  altI, altD);
				altitudeAcelPID.setLimitePID(distanciaDaQueima);
				altitudeAcelPID.setEntradaPID(interpolacaoLinear(distanciaDaQueima, altitudeSup.get(), 0.75));
				velocidadeAcelPID.setEntradaPID(velVertical.get());
				checarAltitude();
				checarPouso();
				MainGui.getParametros().getComponent(0).firePropertyChange("distancia", 0, distanciaDaQueima);
				Thread.sleep(25);
			} catch (RPCException | InterruptedException | StreamException | IOException e) {
			}
		}
	}

	private void checarAltitude() throws RPCException, StreamException {
		if (altitudeSup.get() > ALTITUDE_TREM_DE_POUSO) {
			velocidadeAcelPID.setLimitePID(velVertical.get());
			acelerar((float) (altitudeAcelPID.computarPID()));
		} else {
			acelerar((float) (velocidadeAcelPID.computarPID()));
			naveAtual.getControl().setGear(true);
			naveAtual.getControl().setSASMode(SASMode.RADIAL);
			velocidadeAcelPID.setLimitePID(0);
		}
	}

	private void checarPouso() throws RPCException, IOException, InterruptedException {
		switch (naveAtual.getSituation()) {
		case LANDED:
		case SPLASHED:
			StatusJPanel.setStatus("Pouso Finalizado!");
			acelerar(0.0f);
//			naveAtual.getAutoPilot().disengage();
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setRCS(true);
			naveAtual.getControl().setBrakes(false);
			executandoPousoAutomatico = false;
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

	private double calcularTEP() throws RPCException, StreamException {
		return naveAtual.getAvailableThrust() / ((massaTotal.get() * acelGravidade));
	}

	private double calcularAcelMaxima() throws RPCException, StreamException {
		return calcularTEP() * acelGravidade - acelGravidade;
	}

	public static void setAjusteAltPID(double P, double I, double D) {
		if (P > 0) {
			altP = P;
		}
		if (I >= 0) {
			altI = I;
		}
		if (D >= 0) {
			altD = D;
		}
	}

	public static void setAjusteVelPID(double P, double I, double D) {
		if (P > 0) {
			velP = P;
		}
		if (I >= 0) {
			velI = I;
		}
		if (D >= 0) {
			velD = D;
		}
	}
}

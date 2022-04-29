package com.pesterenan.controller;

import static com.pesterenan.utils.Status.STATUS_POUSO_AUTOMATICO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.pesterenan.gui.GUI;
import com.pesterenan.gui.MainGui;
import com.pesterenan.gui.StatusJPanel;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Navegacao;
import com.pesterenan.utils.Vetor;

import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.SASMode;
import krpc.client.services.SpaceCenter.VesselSituation;

public class PousoAutomaticoController extends TelemetriaController implements Runnable {

	private static final int ALTITUDE_POUSO_AUTOMATICO = 10000;
	private static final int ALTITUDE_TREM_DE_POUSO = 100;

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

	private void decolagem() {
		try {
			naveAtual.getControl().setSAS(true);
			acelerar(1f);
			if (naveAtual.getSituation().equals(VesselSituation.PRE_LAUNCH)) {
				float contagemRegressiva = 5f;
				while (contagemRegressiva > 0) {
					StatusJPanel.setStatus(String.format("Lançamento em: %.1f segundos...", contagemRegressiva));
					contagemRegressiva -= 0.1;
					Thread.sleep(100);
				}
				naveAtual.getControl().activateNextStage();
			}
			StatusJPanel.setStatus("Decolagem!");
			Thread.sleep(1000);
		} catch (RPCException | InterruptedException erro) {
			System.err.println("Não foi possivel decolar a nave. Erro: " + erro.getMessage());
		}
	}

	private void sobrevoarArea() {
		// Decolar a nave
		decolagem();
		// Sobrevoar a area
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
		double distanciaDaQueima = calcularDistanciaDaQueima();
		naveAtual.getControl().setBrakes(true);
		while (!executandoPousoAutomatico) {
			distanciaDaQueima = calcularDistanciaDaQueima();
			MainGui.getParametros().getComponent(0).firePropertyChange("distancia", 0, distanciaDaQueima);
			if (altitudeSup.get() < distanciaDaQueima && velVertical.get() < -1) {
				naveAtual.getControl().setSASMode(SASMode.RETROGRADE);
				executandoPousoAutomatico = true;
				distanciaDaQueima = calcularDistanciaDaQueima();
			}
			Thread.sleep(50);
		}
	}

	private void comecarPousoAutomatico() {
		StatusJPanel.setStatus("Iniciando Pouso Automático!");
		calcularPousoAutomatico();
		while (executandoPousoAutomatico) {
			try {
				double valorTEP = calcularTEP();
				altitudeAcelPID.ajustarPID(valorTEP * altP, altI, valorTEP * altD);
				altitudeAcelPID.setEntradaPID(altitudeSup.get() * 100 / (altitudeSup.get()+distanciaDaQueima));
				velocidadeAcelPID.setEntradaPID(velVertical.get());
				checarAltitude();
				acelerar((float) (altitudeAcelPID.computarPID() + velocidadeAcelPID.computarPID()));
				checarPouso();
				MainGui.getParametros().getComponent(0).firePropertyChange("distancia", 0, distanciaDaQueima);
				Thread.sleep(25);
			} catch (RPCException | InterruptedException | StreamException | IOException e) {
			}
		}
	}

	private void calcularPousoAutomatico() {
		// TODO Auto-generated method stub
		
	}

	private void checarAltitude() throws RPCException, StreamException {
		if (altitudeSup.get() > ALTITUDE_TREM_DE_POUSO) {
			velocidadeAcelPID.setLimitePID(-acelGravidade);
		} else {
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
		Vetor velocidade = new Vetor(velHorizontal.get(), velVertical.get(), 0);
		double acelMaxima = calcularTEP() * acelGravidade - acelGravidade;
		double duracaoDaQueima = Math.abs(velocidade.Magnitude()) / acelMaxima;
		return (Math.abs(velocidade.Magnitude()) * duracaoDaQueima)
				+ (0.5 * (acelMaxima * (duracaoDaQueima * duracaoDaQueima)));
	}

	private double calcularTEP() throws RPCException, StreamException {
		double empuxoDisponivel = naveAtual.getAvailableThrust() / 1000;
		return empuxoDisponivel / ((massaTotal.get() / 1000) * acelGravidade);
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

package com.pesterenan.controller;

import static com.pesterenan.utils.Status.STATUS_POUSO_AUTOMATICO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Vetor;
import com.pesterenan.view.MainGui;
import com.pesterenan.view.StatusJPanel;

import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.SASMode;

public class LandingController extends FlightController implements Runnable {

	private static final int ALTITUDE_POUSO_AUTOMATICO = 10000;
	private static final int ALTITUDE_TREM_DE_POUSO = 200;

	private ControlePID altitudeAcelPID = new ControlePID();
	private ControlePID velocidadeAcelPID = new ControlePID();

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
		this.velocidadeAcelPID.setLimitePID(0);
		this.altitudeAcelPID.setLimitePID(0);
		StatusJPanel.setStatus(STATUS_POUSO_AUTOMATICO.get());
	}

	@Override
	public void run() {
		if (comandos.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO_SOBREVOAR.get())) {
			this.altitudeDeSobrevoo = Double.parseDouble(comandos.get(Modulos.ALTITUDE_SOBREVOO.get()));
			executandoSobrevoo = true;
			velocidadeAcelPID.limitarSaida(-10, 10);
			sobrevoarArea();
		}
		if (comandos.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO.get())) {
			pousarAutomaticamente();
		}
	}

	private void sobrevoarArea() {
		decolar();
		
		while (executandoSobrevoo) {
			try {
				informarCtrlPIDs(altitudeDeSobrevoo);
				velocidadeAcelPID.setLimitePID(altitudeAcelPID.computarPID() * 10);
				velocidadeAcelPID.setEntradaPID(velVertical.get());
				acelerar((float) (velocidadeAcelPID.computarPID()));
				if (descerDoSobrevoo == true) {
					altitudeDeSobrevoo = 0;
					checarPouso();
				}
				Thread.sleep(25);
			} catch (RPCException | InterruptedException | StreamException | IOException e) {
			}
		}
	}

	private void pousarAutomaticamente() {
		decolar();
		try {
			acelerar(0.0f);
			StatusJPanel.setStatus("Iniciando pouso automático em: " + corpoCeleste);
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setSASMode(SASMode.STABILITY_ASSIST);

			checarAltitudeParaPouso();
			comecarPousoAutomatico();
		} catch (RPCException | StreamException | InterruptedException e) {
			System.err.println("Deu erro, não tem o que fazer.");
		}
	}

	private void checarAltitudeParaPouso() throws RPCException, StreamException, InterruptedException {
		while (!executandoPousoAutomatico) {
			distanciaDaQueima = calcularDistanciaDaQueima();
			if (altitudeSup.get() < ALTITUDE_POUSO_AUTOMATICO) {
				naveAtual.getControl().setBrakes(true);
				if (altitudeSup.get() < distanciaDaQueima && velVertical.get() < -1) {
					naveAtual.getControl().setSASMode(SASMode.RETROGRADE);
					executandoPousoAutomatico = true;
				}
			}
			Thread.sleep(50);
		}
	}

	private void comecarPousoAutomatico() {
		StatusJPanel.setStatus("Iniciando Pouso Automático!");
		while (executandoPousoAutomatico) {
			try {
				distanciaDaQueima = calcularDistanciaDaQueima();
				informarCtrlPIDs(distanciaDaQueima);
				checarAltitude();
				checarPouso();
				Thread.sleep(25);
			} catch (RPCException | InterruptedException | StreamException | IOException e) {
			}
		}
	}

	private void informarCtrlPIDs(double distanciaDaQueima) throws RPCException, StreamException {
		double valorTEP = calcularTEP();
		altitudeAcelPID.ajustarPID(valorTEP * velP, valorTEP * velI, valorTEP * velD);
		velocidadeAcelPID.ajustarPID(valorTEP * velP, valorTEP * velI, valorTEP * velD);
		altitudeAcelPID.setLimitePID(distanciaDaQueima);
		altitudeAcelPID.setEntradaPID(ControlePID.interpolacaoLinear(distanciaDaQueima, altitudeSup.get(), 1));
		velocidadeAcelPID.setEntradaPID(velVertical.get());
	}

	private void checarAltitude() throws RPCException, StreamException {
		if (altitudeSup.get() > ALTITUDE_TREM_DE_POUSO) {
			velocidadeAcelPID.setLimitePID(velVertical.get());
			acelerar(altitudeAcelPID.computarPID());
		} else {
			acelerar(velocidadeAcelPID.computarPID());
			naveAtual.getControl().setGear(true);
			naveAtual.getControl().setSASMode(SASMode.RADIAL);
			velocidadeAcelPID.setLimitePID(-acelGravidade / 2);
		}
	}

	private void checarPouso() throws RPCException, IOException, InterruptedException {
		switch (naveAtual.getSituation()) {
		case LANDED:
		case SPLASHED:
			StatusJPanel.setStatus("Pouso Finalizado!");
			executandoPousoAutomatico = false;
			executandoSobrevoo = false;
			descerDoSobrevoo = false;
			acelerar(0.0f);
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setRCS(true);
			naveAtual.getControl().setBrakes(false);
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

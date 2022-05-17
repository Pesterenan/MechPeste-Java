package com.pesterenan.controller;

import static com.pesterenan.utils.Status.STATUS_DECOLAGEM_ORBITAL;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.pesterenan.MechPeste;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.view.StatusJPanel;

import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.VesselSituation;

public class DecolagemOrbitalController extends FlightController implements Runnable {

	public static final int MIN_APOASTRO_FINAL = 10000;
	public static final int MAX_APOASTRO_FINAL = 2000000;
	public static final int MAX_DIRECAO = 360;
	public static final int MIN_DIRECAO = 0;
	private static final float INC_PARA_CIMA = 90;

	private float altInicioCurva = 100;
	private float altApoastroFinal = 80000;
	private float inclinacaoAtual = 90;
	private float direcao = 90;
	private final ControlePID aceleracaoCtrl;

	public DecolagemOrbitalController(Map<String, String> comandos) {
		super(getConexao());
		StatusJPanel.setStatus(STATUS_DECOLAGEM_ORBITAL.get());
		setAltApoastroFinal(Float.parseFloat(comandos.get(Modulos.APOASTRO.get())));
		setDirecao(Float.parseFloat(comandos.get(Modulos.DIRECAO.get())));
		aceleracaoCtrl = new ControlePID();
		aceleracaoCtrl.setAmostragem(100);
		aceleracaoCtrl.ajustarPID(0.05, 0.1, 1);
		aceleracaoCtrl.limitarSaida(0.1, 1.0);
	}

	@Override
	public void run() {
		try {
			decolar();
			curvaGravitacional();
			planejarOrbita();
		} catch (RPCException | InterruptedException | StreamException | IOException e) {
			e.printStackTrace();
		}
	}

	private void planejarOrbita() throws RPCException, StreamException, IOException, InterruptedException {
		StatusJPanel.setStatus("Planejando Manobra de circularização...");
		naveAtual.getAutoPilot().disengage();
		naveAtual.getControl().setSAS(true);
		naveAtual.getControl().setRCS(false);
		Map<String, String> comandos = new HashMap<>();
		comandos.put(Modulos.MODULO.get(), Modulos.MODULO_MANOBRAS.get());
		comandos.put(Modulos.FUNCAO.get(), Modulos.APOASTRO.get());
		MechPeste.iniciarModulo(comandos);
	}

	private void curvaGravitacional() throws RPCException, StreamException, InterruptedException {
		acelerar(1f);
		naveAtual.getAutoPilot().engage();
		naveAtual.getAutoPilot().targetPitchAndHeading(inclinacaoAtual, getDirecao());
		while (inclinacaoAtual > 1) {
			if (altitude.get() > altInicioCurva && altitude.get() < getAltApoastroFinal()) {
				double progresso = (altitude.get() - altInicioCurva) / (getAltApoastroFinal() - altInicioCurva);
				double incrementoCircular = Math.sqrt(1 - Math.pow(progresso - 1, 2));
				inclinacaoAtual = (float) (INC_PARA_CIMA - (incrementoCircular * INC_PARA_CIMA));
				naveAtual.getAutoPilot().targetPitchAndHeading((float) inclinacaoAtual, getDirecao());
				StatusJPanel.setStatus(String.format("A inclinação do foguete é: %.1f", inclinacaoAtual));
				// Informar ao Controlador PID da aceleração a porcentagem do caminho
				aceleracaoCtrl.setEntradaPID(apoastro.get() * 100 / getAltApoastroFinal());
				// Acelerar a nave de acordo com o PID, ou cortar o motor caso passe do apoastro
				if (apoastro.get() < getAltApoastroFinal()) {
					acelerar((float) aceleracaoCtrl.computarPID());
				} else {
					acelerar(0.0f);
					break;
				}
				if (oMotorTemCombustivel()) {
					StatusJPanel.setStatus("Separando estágio...");
					Thread.sleep(1000);
					naveAtual.getControl().activateNextStage();
					Thread.sleep(1000);
				}
				;
			}

			Thread.sleep(100);
		}
	}

	private boolean oMotorTemCombustivel() throws RPCException, StreamException {
		for (Engine motor : naveAtual.getParts().getEngines()) {
			if (motor.getPart().getStage() == naveAtual.getControl().getCurrentStage() && !motor.getHasFuel()) {
				return true;
			}
		}
		return false;
	}

	public float getDirecao() {
		return direcao;
	}

	public void setDirecao(float direcao) {
		if (direcao >= 0 && direcao < MAX_DIRECAO) {
			this.direcao = direcao;
		} else {
			this.direcao = getDirecao();
		}
	}

	public float getAltApoastroFinal() {
		return altApoastroFinal;
	}

	public void setAltApoastroFinal(float altApoastroFinal) {
		if (altApoastroFinal >= MIN_APOASTRO_FINAL && altApoastroFinal <= MAX_APOASTRO_FINAL) {
			this.altApoastroFinal = altApoastroFinal;
		} else {
			this.altApoastroFinal = getAltApoastroFinal();
		}
	}
}

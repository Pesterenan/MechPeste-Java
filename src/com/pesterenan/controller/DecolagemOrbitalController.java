package com.pesterenan.controller;

import java.io.IOException;

import com.pesterenan.controller.ManobrasController.Manobras;
import com.pesterenan.gui.MainGui;
import com.pesterenan.gui.StatusJPanel;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Status;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.VesselSituation;

public class DecolagemOrbitalController extends TelemetriaController implements Runnable {

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

	public DecolagemOrbitalController(Connection con) {
		super(con);
		aceleracaoCtrl = new ControlePID();
		aceleracaoCtrl.setAmostraTempo(100);
		aceleracaoCtrl.ajustarPID(0.05, 0.1, 1);
		aceleracaoCtrl.limitarSaida(0.1, 1.0);
	}

	@Override
	public void run() {
		try {
			decolagem();
			curvaGravitacional();
			planejarOrbita();
			StatusJPanel.setStatus(Status.PRONTO.get());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void planejarOrbita() throws RPCException, StreamException, IOException, InterruptedException {
		StatusJPanel.setStatus("Planejando Manobra de circularização...");
		ManobrasController manobras = new ManobrasController();
		manobras.circularizarOrbita(Manobras.APOASTRO);
		naveAtual.getAutoPilot().disengage();
		naveAtual.getControl().setSAS(true);
		naveAtual.getControl().setRCS(false);
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
				aceleracaoCtrl.setEntradaPID((apoastro.get() * 100 / getAltApoastroFinal()));
				// Acelerar a nave de acordo com o PID, ou cortar o motor caso passe do apoastro
				if (apoastro.get() < getAltApoastroFinal()) {
					acelerar((float) aceleracaoCtrl.computarPID());
				} else {
					acelerar(0.0f);
					break;
				}
				checarCombustivel();
			}
			
			Thread.sleep(100);
		}
	}

	private void checarCombustivel() throws RPCException, StreamException {
		float combustivel = naveAtual.resourcesInDecoupleStage(
				naveAtual.getControl().getCurrentStage(), false).amount("LiquidFuel");
		MainGui.getParametros().getComponent(0).firePropertyChange("estagio", -1.0, combustivel);
	}

	private void acelerar(float acel) throws RPCException {
		naveAtual.getControl().setThrottle(acel);
	}

	private void decolagem() throws RPCException, InterruptedException {
		naveAtual.getControl().setSAS(true);
		acelerar(1f);
		if (naveAtual.getSituation().equals(VesselSituation.PRE_LAUNCH)) {
			float contagemRegressiva = 5f;
			while (contagemRegressiva > 0) {
				StatusJPanel.setStatus(String.format("Lançamento em: %.1f segundos...", contagemRegressiva));
				contagemRegressiva -= 0.1;
				Thread.sleep(100);
			}
			StatusJPanel.setStatus("Decolagem!");
			naveAtual.getControl().activateNextStage();
		}
		Thread.sleep(1000);
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

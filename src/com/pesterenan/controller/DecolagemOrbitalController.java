package com.pesterenan.controller;

import static com.pesterenan.utils.Status.STATUS_DECOLAGEM_ORBITAL;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.pesterenan.MechPeste;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Utilities;
import com.pesterenan.view.StatusJPanel;

import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;

public class DecolagemOrbitalController extends FlightController implements Runnable {

	private static final float INC_PARA_CIMA = 90;

	private float inclinacaoAtual;
	private float altInicioCurva = 100;
	private float altApoastroFinal = 80000;
	private float direcao = 90;

	public DecolagemOrbitalController(Map<String, String> comandos) {
		super(getConexao());
		this.inclinacaoAtual = INC_PARA_CIMA;
		StatusJPanel.setStatus(STATUS_DECOLAGEM_ORBITAL.get());
		setAltApoastroFinal(Float.parseFloat(comandos.get(Modulos.APOASTRO.get())));
		setDirecao(Float.parseFloat(comandos.get(Modulos.DIRECAO.get())));
	}

	@Override
	public void run() {
		try {
			decolar();
			curvaGravitacional();
			planejarOrbita();
		} catch (RPCException | InterruptedException | StreamException | IOException e) {
			StatusJPanel.setStatus("Decolagem abortada.");
			try {
				acelerar(0f);
				naveAtual.getAutoPilot().disengage();
			} catch (RPCException e1) {
				e1.printStackTrace();
			}
			return;
		}
	}

	private void planejarOrbita() throws RPCException, StreamException, IOException, InterruptedException {
		StatusJPanel.setStatus("Planejando Manobra de circularização...");
		naveAtual.getAutoPilot().disengage();
		naveAtual.getControl().setSAS(true);
		naveAtual.getControl().setRCS(true);

		Map<String, String> comandos = new HashMap<>();
		comandos.put(Modulos.MODULO.get(), Modulos.MODULO_MANOBRAS.get());
		comandos.put(Modulos.FUNCAO.get(), Modulos.APOASTRO.get());
		MechPeste.iniciarModulo(comandos);
	}

	private void curvaGravitacional() throws RPCException, StreamException, InterruptedException {
		naveAtual.getAutoPilot().targetPitchAndHeading(this.inclinacaoAtual, getDirecao());
		naveAtual.getAutoPilot().setTargetRoll(90);
		naveAtual.getAutoPilot().engage();
		acelerar(1f);

		while (this.inclinacaoAtual > 1) {
			double altitudeAtual = Utilities.remap(altInicioCurva, getAltApoastroFinal(), 1, 0.1, altitude.get());
			double curvaCircular = Utilities.easeInCirc(altitudeAtual);
			this.inclinacaoAtual = (float) (curvaCircular * INC_PARA_CIMA);
			naveAtual.getAutoPilot().targetPitchAndHeading(this.inclinacaoAtual, getDirecao());

			acelerar(Utilities.remap(getAltApoastroFinal() * 0.95, getAltApoastroFinal(), 1, 0.1, apoastro.get()));
			if (apoastro.get() > getAltApoastroFinal()) {
				acelerar(0.0f);
				break;
			}

			if (estagioSemCombustivel()) {
				StatusJPanel.setStatus("Separando estágio...");
				Thread.sleep(1000);
				naveAtual.getControl().activateNextStage();
				Thread.sleep(1000);
			}

			StatusJPanel.setStatus(String.format("A inclinação do foguete é: %.1f", inclinacaoAtual));
			Thread.sleep(25);
		}
	}

	private boolean estagioSemCombustivel() throws RPCException, StreamException {
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
		final int MAX_DIRECAO = 360;
		final int MIN_DIRECAO = 0;
		if (direcao >= MIN_DIRECAO && direcao < MAX_DIRECAO) {
			this.direcao = direcao;
		} else {
			this.direcao = getDirecao();
		}
	}

	public float getAltApoastroFinal() {
		return altApoastroFinal;
	}

	public void setAltApoastroFinal(float altApoastroFinal) {
		final int MIN_APOASTRO_FINAL = 10000;
		final int MAX_APOASTRO_FINAL = 2000000;
		if (altApoastroFinal >= MIN_APOASTRO_FINAL && altApoastroFinal <= MAX_APOASTRO_FINAL) {
			this.altApoastroFinal = altApoastroFinal;
		} else {
			this.altApoastroFinal = getAltApoastroFinal();
		}
	}
}

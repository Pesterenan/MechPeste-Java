package com.pesterenan.controller;

import java.io.IOException;

import com.pesterenan.gui.GUI;
import com.pesterenan.model.Nave;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Status;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.VesselSituation;

public class DecolagemOrbitalController extends Nave implements Runnable {

	// Streams de conexao com a nave:
	double pressaoAtual;
	// Parametros de voo:
	private float altInicioCurva = 100;
	public static float altApoastroFinal = 80000;

	private static int direcao = 90;
	private int inclinacao = 90;
	private int etapaAtual = 0;
	private double anguloGiro = 0;
	private boolean executando = true;
	private static boolean abortar = false;
	private ManobrasController manobras;
	ControlePID ctrlAcel = new ControlePID();

	public DecolagemOrbitalController(Connection con) {
		super(con);
		System.out.println("Instancia Criada");
	}

	public void decolagem() throws RPCException, StreamException, IOException, InterruptedException {
		iniciarScript();
// Loop principal de subida
		while (isExecutando()) { // loop while sempre funcionando at� um break
			switch (etapaAtual) {
			case 0:
				decolar();
				break;
			case 1:
				giroGravitacional();
				break;
			case 2:
				planejarOrbita();
				break;
			case 3:
				GUI.setStatus(Status.PRONTO.get());
				etapaAtual = 0;
				setExecutando(false);
				break;
			}
			if (abortar) {
				finalizarScript();
			}

			atualizarParametros();
			Thread.sleep(50);
		}
		finalizarScript();
	}

	public boolean isExecutando() {
		return this.executando;
	}

	public void setExecutando(boolean executando) {
		this.executando = executando;
	}

	private void iniciarScript() throws RPCException, StreamException, IOException, InterruptedException {
		// Iniciar Conexão:
		naveAtual.getAutoPilot().setReferenceFrame(naveAtual.getSurfaceReferenceFrame());
		manobras = new ManobrasController(false);
		ctrlAcel.setAmostraTempo(25);
		ctrlAcel.setLimitePID(20);
		ctrlAcel.ajustarPID(0.25, 0.01, 0.025);
		ctrlAcel.limitarSaida(0.1, 1.0);
		GUI.setParametros("nome", naveAtual.getName());

	}

	private void decolar() throws RPCException, StreamException, InterruptedException {
		GUI.setStatus("Iniciando Decolagem...");
		naveAtual.getControl().setSAS(false); // desligar SAS
		naveAtual.getControl().setRCS(false); // desligar RCS
		// Ligar Piloto Automatico e Mirar a Direção:
		naveAtual.getAutoPilot().engage(); // ativa o piloto auto
		naveAtual.getAutoPilot().targetPitchAndHeading(inclinacao, getDirecao()); // direção
		GUI.setStatus("Lançamento!");
		if (naveAtual.getSituation().equals(VesselSituation.PRE_LAUNCH)) {
			aceleracao(1.0f); // acelerar ao máximo
			naveAtual.getControl().activateNextStage();
		} else {
			aceleracao(1.0f); // acelerar ao máximo
		}
		etapaAtual = 1;

	}

	private void giroGravitacional() throws RPCException, StreamException, InterruptedException {
		double altitudeAtual = altitudeSup.get();
		double apoastroAtual = apoastro.get();
		pressaoAtual = parametrosDeVoo.getDynamicPressure() / 1000;
		ctrlAcel.setEntradaPID(pressaoAtual);
		if (altitudeAtual > altInicioCurva && altitudeAtual < getAltApoastroFinal()) {
			double progresso = (altitudeAtual - altInicioCurva) / (getAltApoastroFinal() - altInicioCurva);
			double incrementoCircular = Math.sqrt(1 - Math.pow(progresso - 1, 2));
			double novoAnguloGiro = incrementoCircular * inclinacao;
			if (Math.abs(novoAnguloGiro - anguloGiro) > 0.1) {
				anguloGiro = novoAnguloGiro;
				naveAtual.getAutoPilot().targetPitchAndHeading((float) (inclinacao - anguloGiro), getDirecao());
				aceleracao((float) ctrlAcel.computarPID());
				GUI.setStatus(String.format("�ngulo de Inclina��o: %1$.1f �", anguloGiro));
			}
		}
		// Diminuir acelera��o ao chegar perto do apoastro
		if (apoastroAtual > getAltApoastroFinal() * 0.95) {
			GUI.setStatus("Se aproximando do apoastro...");
			ctrlAcel.setEntradaPID(apoastroAtual);
			ctrlAcel.setLimitePID(getAltApoastroFinal());
			aceleracao((float) ctrlAcel.computarPID());
		}
		// Sair do giro ao chegar na altitude de apoastro:
		if (apoastroAtual >= getAltApoastroFinal()) {
			naveAtual.getControl().setSAS(true);
			GUI.setStatus("Apoastro alcan�ado.");
			aceleracao(0.0f);
			Thread.sleep(25);
			etapaAtual = 2;
		}
	}

	private void planejarOrbita() throws RPCException, StreamException, InterruptedException, IOException {
		GUI.setStatus("Esperando sair da atmosfera.");
		if (altitude.get() > (getAltApoastroFinal() * 0.8)) {
			GUI.setStatus("Planejando Manobra de circulariza��o...");
			Node noDeManobra = manobras.circularizarApoastro();
			double duracaoDaQueima = manobras.calcularTempoDeQueima(noDeManobra);
			manobras.orientarNaveParaNoDeManobra(noDeManobra);
			GUI.setStatus("Executando Manobra de circulariza��o...");
			manobras.executarQueima(noDeManobra, duracaoDaQueima);
			naveAtual.getAutoPilot().disengage();
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setRCS(false);
			noDeManobra.remove();
			etapaAtual = 3;
		}
	}

	private void aceleracao(float acel) throws RPCException {
		naveAtual.getControl().setThrottle((float) acel);
	}

	private void atualizarParametros() throws RPCException, StreamException {
		GUI.setParametros("altitude", altitude.get());
		GUI.setParametros("apoastro", apoastro.get());
		GUI.setParametros("periastro", periastro.get());
	}

	private void finalizarScript() throws RPCException, IOException {
		setExecutando(false);
		setAbortar(false);
	}

	public static void setAltApoastro(float apoastroFinal) {
		altApoastroFinal = apoastroFinal;

	}

	public static void setDirecao(int direcaoOrbita) {
		direcao = direcaoOrbita;

	}

	public static void setAbortar(boolean estado) {
		abortar = estado;
		System.out.println("Voo abortado");
	}

	public static float getAltApoastroFinal() {
		return altApoastroFinal;
	}

	public static int getDirecao() {
		return direcao;
	}

	@Override
	public void run() {
		try {
			decolagem();
		} catch (RPCException | StreamException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
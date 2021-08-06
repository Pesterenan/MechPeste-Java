package com.pesterenan.funcoes;

import java.io.IOException;

import com.pesterenan.MechPeste;
import com.pesterenan.gui.GUI;
import com.pesterenan.gui.Status;
import com.pesterenan.model.Nave;
import com.pesterenan.utils.ControlePID;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.VesselSituation;

public class DecolagemOrbital extends Nave {

	public DecolagemOrbital(Connection conexao) {
		super(conexao);
	}

	// Streams de conexao com a nave:
	double pressaoAtual;
	// Parametros de voo:
	private float altInicioCurva = 100;
	public static float altApoastroFinal = 80000;
	private static int direcao = 90;
	private int inclinacao = 90;
	private int etapaAtual = 0;
	private double anguloGiro = 0;
	private static boolean executando = true;
	private static boolean abortar = false;
	private Manobras manobras;
	ControlePID ctrlAcel = new ControlePID();

	public void decolagemOrbital(Connection conexao)
			throws IOException, RPCException, InterruptedException, StreamException {
		iniciarScript(conexao);
// Loop principal de subida
		while (executando) { // loop while sempre funcionando at� um break
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
				executando = false;
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

	private void iniciarScript(Connection conexao)
			throws RPCException, StreamException, IOException, InterruptedException {
		// Iniciar Conexão:
		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = centroEspacial.getActiveVessel();
		parametrosVoo = naveAtual.flight(naveAtual.getOrbit().getBody().getReferenceFrame());
		naveAtual.getAutoPilot().setReferenceFrame(naveAtual.getSurfaceReferenceFrame());
		manobras = new Manobras(conexao, false);
		ctrlAcel.setAmostraTempo(25);
		ctrlAcel.setLimitePID(20);
		ctrlAcel.ajustarPID(0.25, 0.01, 0.025);
		ctrlAcel.limitarSaida(0.1, 1.0);
		// Iniciar Streams:
		tempoMissao = conexao.addStream(SpaceCenter.class, "getUT");
		altitudeSup = conexao.addStream(parametrosVoo, "getSurfaceAltitude");
		altitude = conexao.addStream(parametrosVoo, "getMeanAltitude");
		apoastro = conexao.addStream(naveAtual.getOrbit(), "getApoapsisAltitude");
		periastro = conexao.addStream(naveAtual.getOrbit(), "getPeriapsisAltitude");
		GUI.setParametros("nome", naveAtual.getName());

	}

	private void decolar() throws RPCException, StreamException, InterruptedException {
		GUI.setStatus("Iniciando Decolagem...");
		naveAtual.getControl().setSAS(false); // desligar SAS
		naveAtual.getControl().setRCS(false); // desligar RCS
		// Ligar Piloto Automatico e Mirar a Direção:
		naveAtual.getAutoPilot().engage(); // ativa o piloto auto
		naveAtual.getAutoPilot().targetPitchAndHeading(inclinacao, direcao); // direção
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
		pressaoAtual = parametrosVoo.getDynamicPressure() / 1000;
		ctrlAcel.setEntradaPID(pressaoAtual);
		if (altitudeAtual > altInicioCurva && altitudeAtual < altApoastroFinal) {
			double progresso = (altitudeAtual - altInicioCurva) / (altApoastroFinal - altInicioCurva);
			double incrementoCircular = Math.sqrt(1 - Math.pow(progresso - 1, 2));
			double novoAnguloGiro = incrementoCircular * inclinacao;
			if (Math.abs(novoAnguloGiro - anguloGiro) > 0.1) {
				anguloGiro = novoAnguloGiro;
				naveAtual.getAutoPilot().targetPitchAndHeading((float) (inclinacao - anguloGiro), direcao);
				aceleracao((float) ctrlAcel.computarPID());
				GUI.setStatus(String.format("�ngulo de Inclina��o: %1$.1f �", anguloGiro));
			}
		}
		// Diminuir acelera��o ao chegar perto do apoastro
		if (apoastroAtual > altApoastroFinal * 0.95) {
			GUI.setStatus("Se aproximando do apoastro...");
			ctrlAcel.setEntradaPID(apoastroAtual);
			ctrlAcel.setLimitePID(altApoastroFinal);
			aceleracao((float) ctrlAcel.computarPID());
		}
		// Sair do giro ao chegar na altitude de apoastro:
		if (apoastroAtual >= altApoastroFinal) {
			naveAtual.getControl().setSAS(true);
			GUI.setStatus("Apoastro alcan�ado.");
			aceleracao(0.0f);
			Thread.sleep(25);
			etapaAtual = 2;
		}
	}

	private void planejarOrbita() throws RPCException, StreamException, InterruptedException, IOException {
		GUI.setStatus("Esperando sair da atmosfera.");
		if (altitude.get() > (altApoastroFinal * 0.8)) {
			GUI.setStatus("Planejando Manobra de circulariza��o...");
			Node noDeManobra = manobras.circularizarApoastro();
			double duracaoDaQueima = manobras.calcularTempoDeQueima(noDeManobra);
			manobras.orientarNave(noDeManobra);
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
		tempoMissao.remove();
		altitude.remove();
		apoastro.remove();
		periastro.remove();
		executando = false;
		abortar = false;
		MechPeste.finalizarTarefa();
	}

	public static void setAltApoastro(float apoastroFinal) {
		altApoastroFinal = apoastroFinal;

	}

	public static void setDirecao(int direcaoOrbita) {
		direcao = direcaoOrbita;

	}

	public static void setExecutar(boolean estado) {
		executando = estado;
	}

	public static void setAbortar(boolean estado) {
		abortar = estado;
		System.out.println("Voo abortado");
	}
}
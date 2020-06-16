package com.pesterenan.funcoes;

import java.io.IOException;

import com.pesterenan.MechPeste;
import com.pesterenan.gui.GUI;
import com.pesterenan.gui.Status;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;

public class DecolagemOrbital {

	private static SpaceCenter centroEspacial;
	private static Vessel naveAtual;
	private Flight parametrosVoo;

	Stream<Double> tempoMissao;
	Stream<Double> altitude;
	Stream<Double> apoastro;
	Stream<Double> periastro;

	private float altInicioCurva = 250;
	private float altFimCurva = 80000;
	public static float altApoastroFinal = 80000;
	private int etapaAtual = 0;
	private int inclinacao = 90;
	private static int direcao = 90;
	private double anguloGiro;
	private static boolean executando = true;
	private Manobras manobras;

	public DecolagemOrbital(Connection conexao)
			throws IOException, RPCException, InterruptedException, StreamException {
		// Declarar Variáveis:
		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = centroEspacial.getActiveVessel();
		parametrosVoo = naveAtual.flight(naveAtual.getOrbit().getBody().getReferenceFrame());
		manobras = new Manobras(conexao, false);
		// Iniciar Streams:
		tempoMissao = conexao.addStream(SpaceCenter.class, "getUT");
		altitude = conexao.addStream(parametrosVoo, "getMeanAltitude");
		apoastro = conexao.addStream(naveAtual.getOrbit(), "getApoapsisAltitude");
		periastro = conexao.addStream(naveAtual.getOrbit(), "getPeriapsisAltitude");
		anguloGiro = 0;

		GUI.setParametros("nome", naveAtual.getName());
		// Loop principal de subida
		while (executando) { // loop while sempre funcionando até um break
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
			atualizarParametros();
			Thread.sleep(100);
		}
		tempoMissao.remove();
		altitude.remove();
		apoastro.remove();
		periastro.remove();
		MechPeste.finalizarTarefa();
	}

	private void decolar() throws RPCException, StreamException, InterruptedException {
		GUI.setStatus("Iniciando Decolagem...");
		naveAtual.getAutoPilot().setReferenceFrame(naveAtual.getOrbit().getBody().getReferenceFrame());
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
		double altitudeAtual = altitude.get();
		double apoastroAtual = apoastro.get();
		if (altitudeAtual > altInicioCurva && altitudeAtual < altFimCurva) {
			double incremento = Math.sqrt((altitudeAtual - altInicioCurva) / (altFimCurva - altInicioCurva));
			double novoAnguloGiro = incremento * inclinacao;
			if (Math.abs(novoAnguloGiro - anguloGiro) > 0.5) {
				anguloGiro = novoAnguloGiro;
				naveAtual.getAutoPilot().targetPitchAndHeading((float) (inclinacao - anguloGiro), direcao);
				GUI.setStatus(String.format("Ângulo de Inclinação: %1$.1f °", anguloGiro));
			}
		}
		// Diminuir aceleração ao chegar perto do apoastro
		if (apoastroAtual > altApoastroFinal * 0.95) {
			GUI.setStatus("Se aproximando do apoastro...");
			aceleracao(0.25f); // mudar aceleração pra 25%
		}
		// Sair do giro ao chegar na altitude de apoastro:
		if (apoastroAtual >= altApoastroFinal) {
			GUI.setStatus("Apoastro alcançado.");
			aceleracao(0.0f);
			Thread.sleep(100);
			etapaAtual = 2;
		}
	}

	private void planejarOrbita() throws RPCException, StreamException, InterruptedException, IOException {
		GUI.setStatus("Esperando sair da atmosfera.");
		if (altitude.get() > (altApoastroFinal * 0.8)) {
			GUI.setStatus("Planejando Manobra de circularização...");
			Node noDeManobra = manobras.circularizarApoastro();
			double duracaoDaQueima = manobras.calcularTempoDeQueima(noDeManobra);
			manobras.orientarNave(noDeManobra);
			GUI.setStatus("Executando Manobra de circularização...");
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

	public static void setAltApoastro(float apoastroFinal) {
		altApoastroFinal = apoastroFinal;

	}

	public static void setDirecao(int direcaoOrbita) {
		direcao = direcaoOrbita;

	}

	public static void setExecutar(boolean estado) {
		executando = estado;
	}
}
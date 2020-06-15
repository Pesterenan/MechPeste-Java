package com.pesterenan;

import java.io.IOException;
import java.util.List;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Part;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Resources;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;

public class DecolagemOrbital {

	private static Connection conexao;
	private static SpaceCenter centroEspacial;
	private static Vessel naveAtual;
	private ReferenceFrame pontoRefOrbital;
	private Flight parametrosVoo;

	Stream<Double> tempoMissao;
	Stream<Double> altitude;
	Stream<Double> apoastro;
	Stream<Double> periastro;

	List<Part> listaDePecas;
	Resources combustivelNoEstagio;
	Stream<Float> combustivel;

	private float altInicioCurva = 250;
	private float altFimCurva = 80000;
	public static float altApoastro = 80000;
	private int etapaAtual = 0;
	private int inclinacao = 90;
	private static int direcao = 90;
	private double anguloGiro;
	private static boolean executando = true;
	Manobras manobras;

	public DecolagemOrbital(Connection con) throws IOException, RPCException, InterruptedException, StreamException {
		conexao = con;
		// Declarar Variáveis:
		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = centroEspacial.getActiveVessel();
		pontoRefOrbital = naveAtual.getOrbit().getBody().getReferenceFrame();
		parametrosVoo = naveAtual.flight(pontoRefOrbital);
		manobras = new Manobras(conexao);
		// Iniciar Streams:
		tempoMissao = conexao.addStream(SpaceCenter.class, "getUT");
		altitude = conexao.addStream(parametrosVoo, "getMeanAltitude");
		apoastro = conexao.addStream(naveAtual.getOrbit(), "getApoapsisAltitude");
		periastro = conexao.addStream(naveAtual.getOrbit(), "getPeriapsisAltitude");
		combustivelNoEstagio = naveAtual.resourcesInDecoupleStage((int) naveAtual.getControl().getCurrentStage(),
				false);
		combustivel = conexao.addStream(combustivelNoEstagio, "amount", "Oxydizer");
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
				checarCombustivel();
				break;
			case 2:
				planejarOrbita();
				break;
			case 3:
				executando = false;
			}
			atualizarParametros();
			Thread.sleep(300);
		}
	}

	private void atualizarParametros() throws RPCException, StreamException {
		GUI.setParametros("altitude", altitude.get());
		GUI.setParametros("apoastro", apoastro.get());
		GUI.setParametros("periastro", periastro.get());
		GUI.setParametros("velHorz", naveAtual.getControl().getCurrentStage());
	}

	private void checarCombustivel() throws RPCException, StreamException, InterruptedException {
		GUI.setParametros("velVert", combustivel.get());
		GUI.setParametros("velHorz", naveAtual.getControl().getCurrentStage());
	}

	private void planejarOrbita() throws RPCException, StreamException, InterruptedException, IOException {
		GUI.setStatus("Esperando sair da atmosfera.");
		if (altitude.get() > (altApoastro * 0.8)) {
			// Planejar circularização usando equação vis-viva
			GUI.setStatus("Planejando queima de circularização...");

			double parGrav = naveAtual.getOrbit().getBody().getGravitationalParameter(); // parametro G do corpo
			double apo = naveAtual.getOrbit().getApoapsis(); // apoastro da orbita
			double sma = naveAtual.getOrbit().getSemiMajorAxis(); // semieixo da orbita
			double apo2 = apo; // apoastro alvo
			double v1 = Math.sqrt(parGrav * ((2.0 / apo) - (1.0 / sma))); // calculo da vel orbital atual
			double v2 = Math.sqrt(parGrav * ((2.0 / apo) - (1.0 / apo2))); // calculo da vel orbital alvo
			double deltaV = v2 - v1; // delta -v manobra
			Node noDeManobra = naveAtual.getControl()
					.addNode(tempoMissao.get() + naveAtual.getOrbit().getTimeToApoapsis(), (float) deltaV, 0, 0);
			System.out.println("Planejou Manobra");

			double duracaoDaQueima = manobras.calcularTempoDeQueima(noDeManobra);
			System.out.println("Calculou Tempo");
			manobras.orientarNave(noDeManobra);
			System.out.println("Orientou Nave");
			manobras.executarQueima(noDeManobra, duracaoDaQueima);
			System.out.println("Executou Queima");

			naveAtual.getAutoPilot().disengage();
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setRCS(false);
			noDeManobra.remove();
			GUI.setStatus(Status.PRONTO.get());
			MechPeste.finalizarTarefa();
			etapaAtual = 3;
		}
	}

	private void aceleracao(float acel) throws RPCException {
		naveAtual.getControl().setThrottle((float) acel);
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
		if (altitude.get() > altInicioCurva && altitude.get() < altFimCurva) {
			double incremento = Math.sqrt((altitude.get() - altInicioCurva) / (altFimCurva - altInicioCurva));
			double novoAnguloGiro = incremento * inclinacao;
			if (Math.abs(novoAnguloGiro - anguloGiro) > 0.5) {
				anguloGiro = novoAnguloGiro;
				naveAtual.getAutoPilot().targetPitchAndHeading((float) (inclinacao - anguloGiro), direcao);
				GUI.setStatus(String.format("Ângulo de Inclinação: %1$.1f °", anguloGiro));
			}
		}
		// Diminuir aceleração ao chegar perto do apoastro
		if (apoastro.get() > altApoastro * 0.95) {
			GUI.setStatus("Se aproximando do apoastro...");
			aceleracao(0.25f); // mudar aceleração pra 25%
		}
		// Sair do giro ao chegar na altitude de apoastro:
		if (apoastro.get() >= altApoastro) {
			GUI.setStatus("Apoastro alcançado.");
			aceleracao(0.0f);
			Thread.sleep(1000);
			etapaAtual = 2;
		}
	}

	public static void setAltApoastro(float apoastroFinal) {
		altApoastro = apoastroFinal;

	}

	public static void setDirecao(int direcaoOrbita) {
		direcao = direcaoOrbita;

	}

	public static void setExecutar(boolean estado) {
		executando = estado;
	}
}
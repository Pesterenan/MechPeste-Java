package com.pesterenan;

import java.io.IOException;
import java.util.List;

import org.javatuples.Triplet;

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

	private final float CONST_GRAV = 9.81f;
	private static Connection conexao;
	private SpaceCenter centroEspacial;
	private Vessel naveAtual;
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
	private float altFimCurva = 60000;
	public static float altApoastro = 80000;
	private int etapaAtual = 0;
	private int inclinacao = 90;
	private static int direcao = 90;
	private double anguloGiro;
	private static boolean executando = true;

	public DecolagemOrbital(Connection con) throws IOException, RPCException, InterruptedException, StreamException {
		conexao = con;
		// Declarar Variáveis:
		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = centroEspacial.getActiveVessel();
		pontoRefOrbital = naveAtual.getOrbit().getBody().getReferenceFrame();
		parametrosVoo = naveAtual.flight(pontoRefOrbital);
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
		GUI.setParametros("velVert", apoastro.get());
		GUI.setParametros("velHorz", periastro.get());
	}

	private void planejarOrbita() throws RPCException, StreamException, InterruptedException {
		GUI.setStatus("Esperando sair da atmosfera.");
		if (altitude.get() > altFimCurva) {
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

			// Calcular tempo de queima (equação de foguete)
			double empuxoTotal = naveAtual.getAvailableThrust(); // pegar empuxo disponível
			double isp = naveAtual.getSpecificImpulse() * CONST_GRAV; // pegar isp e multiplicar à constante grav
			double massaTotal = naveAtual.getMass(); // pegar massa atual
			double massaSeca = massaTotal / Math.exp(deltaV / isp); // pegar massa seca
			double taxaDeQueima = empuxoTotal / isp; // taxa de fluxo, empuxo / isp
			double duracaoDaQueima = (massaTotal - massaSeca) / taxaDeQueima;
			// Orientar nave:
			GUI.setStatus("Orientando nave para a queima de circularização...");
			naveAtual.getAutoPilot().setReferenceFrame(noDeManobra.getReferenceFrame());
			naveAtual.getAutoPilot().setTargetDirection(new Triplet<Double, Double, Double>(0.0, 1.0, 0.0));
			naveAtual.getAutoPilot().wait_();

			// Calcular inicio da queima:
			double tempoDaQueima = 10;
			GUI.setStatus("Duração da queima: " + duracaoDaQueima + " segundos.");
			while (tempoDaQueima > 0) {
				tempoDaQueima = noDeManobra.getTimeTo() - (duracaoDaQueima / 2.0) - 1;
				GUI.setStatus(String.format("Ignição em: %1$.1f segundos...", tempoDaQueima));
				atualizarParametros();
				Thread.sleep(100);
			}
			Stream<Triplet<Double, Double, Double>> queimaRestante = conexao.addStream(noDeManobra,
					"remainingBurnVector", noDeManobra.getReferenceFrame());
			GUI.setStatus("Executando manobra...");
			while (noDeManobra != null) {
				if (queimaRestante.get().getValue1() > (deltaV * 0.05)) {
					aceleracao(1.0f);
				} else if (queimaRestante.get().getValue1() > (1.0)) {
					GUI.setStatus("Ajustando...");
					aceleracao(0.25f);
				} else {
					aceleracao(0.0f);
					break;
				}
				atualizarParametros();
				Thread.sleep(100);
			}
			noDeManobra.remove();
			GUI.setStatus("Lançamento completo.");
			naveAtual.getAutoPilot().disengage();
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setRCS(false);
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
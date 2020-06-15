package com.pesterenan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Part;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Resources;
import krpc.client.services.SpaceCenter.Vessel;

public class ScriptMillenium {

	private Connection conexao;
	private SpaceCenter centroEspacial;
	private Vessel naveAtual;
	private ReferenceFrame pontoRef;
	private Flight vooNave;
	private static ScriptMillenium SM;
	Stream<Double> ut; // Tempo de Miss�o
	Stream<Double> altitude;
	Stream<Double> apoastro;
	List<Part> listaDePecas;
	Resources combustivelNoEstagio;
	Stream<Float> combustivel;

	float altInicioCurva = 600;
	float altFimCurva = 65000;
	float altFinal = 80000;
	private int estagioAtual = 0;

	public static void main(String[] args) throws RPCException, IOException, StreamException, InterruptedException {
		SM = new ScriptMillenium();
		SM.iniciar();
	}

	public void iniciar() throws RPCException, IOException, StreamException, InterruptedException {
		conexao = Connection.newInstance("Script Millenium");
		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = centroEspacial.getActiveVessel();
		pontoRef = naveAtual.getOrbit().getBody().getReferenceFrame();
		vooNave = naveAtual.flight(pontoRef);
		System.out.println(naveAtual.getName());
		ut = conexao.addStream(SpaceCenter.class, "getUT"); // Tempo de Miss�o
		altitude = conexao.addStream(vooNave, "getMeanAltitude");
		apoastro = conexao.addStream(naveAtual.getOrbit(), "getApoapsisAltitude");
		listaDePecas = new ArrayList<Part>();
		// Adicionar Pe�as com Tags � Lista:
		for (Part peca : naveAtual.getParts().getAll()) {
			if (peca.getTag() != "") {
				listaDePecas.add(peca);
				// Paineis.setStatus(("PEÇA ADICIONADA: " + peca.getName() + " TAG: " +
				// peca.getTag()).toString());
			}
		}
		// Fazer Stream de Combust�vel com o Primeiro Est�gio:
		for (Part peca : listaDePecas) {
			if (peca.getTag().equals("PRIMEIRO_ESTAGIO")) {
				naveAtual = peca.getVessel();
				combustivelNoEstagio = peca.getResources();
				combustivel = conexao.addStream(combustivelNoEstagio, "amount", "Oxidizer");
			}
		}
		if (!listaDePecas.isEmpty()) {
			decolar();
		}

		conexao.close();
	}

	private void decolar() throws RPCException, StreamException, InterruptedException {
		// Paineis.setStatus("INICIANDO DECOLAGEM");

//		//TROCAR DE NAVE
//		for (Part peca : listaDePecas) {
//			
//			naveAtual = peca.getVessel();
//		}
		naveAtual.getControl().setSAS(false); // desligar SAS
		naveAtual.getControl().setRCS(false); // desligar RCS
		naveAtual.getControl().setThrottle(1f); // acelerar ao máximo
		// Paineis.setStatus("DECOLAGEM!");
		naveAtual.getControl().activateNextStage();
		// Ligar Piloto e Mirar pra Cima:
		naveAtual.getAutoPilot().engage(); // ativa o piloto auto
		naveAtual.getAutoPilot().targetPitchAndHeading(90, 90); // direção

		giroGravitacional();
	}

	private void giroGravitacional() throws InterruptedException, RPCException, StreamException {
		double anguloGiro = 0; // angulo de giro
		while (true) { // loop while sempre funcionando até um break
			// Giro de Gravidade
			if (altitude.get() > altInicioCurva && altitude.get() < altFimCurva) {
				double incremento = Math.sqrt((altitude.get() - altInicioCurva) / (altFimCurva - altInicioCurva));
				double novoAnguloGiro = incremento * 90.0;
				if (Math.abs(novoAnguloGiro - anguloGiro) > 0.5) {
					anguloGiro = novoAnguloGiro;
					naveAtual.getAutoPilot().targetPitchAndHeading((float) (90 - anguloGiro), 90);
				}
			}
			checarCombustivel();
			checarEstagio();
			Thread.sleep(500);
			if (apoastro.get() > altFinal * 0.8) {
				// Paineis.setStatus("Aproximando-se do apoastro alvo");
				break;
			}
		}
		// Desativa motores ao chegar no apoastro
		naveAtual.getControl().setThrottle(0.25f); // mudar aceleração pra 25%
		while (apoastro.get() < altFinal) {
			// Paineis.setParametros("apoastro", apoastro.get());
			Thread.sleep(500);
		}
		naveAtual.getControl().setThrottle(0.0f);
		// Paineis.setStatus("Chegamos ao Apoastro Alvo");

	}

	private void checarCombustivel() throws StreamException, RPCException, InterruptedException {
		// Paineis.setStatus("Combust�vel Restante: " + combustivel.get());
		if (combustivel.get() < 1) {
			// Paineis.setStatus("Combust�vel Acabou!");
			Thread.sleep(1500);
			estagioAtual++;
		}
	}

	private void checarEstagio() throws StreamException, RPCException, InterruptedException {
		System.out.println("Est�gio Atual: " + estagioAtual);
		if (estagioAtual == 1) {
			naveAtual.getControl().setThrottle(0);
			naveAtual.getAutoPilot().disengage();
			naveAtual.getControl().activateNextStage();
			System.out.println("Separa��o do CHEWIE!");
			for (Part peca : listaDePecas) {
				if (peca.getTag().equals("SEGUNDO_ESTAGIO")) {
					naveAtual = peca.getVessel();
					centroEspacial.setActiveVessel(peca.getVessel());
					combustivelNoEstagio = peca.getResources();
					combustivel = conexao.addStream(combustivelNoEstagio, "amount", "Oxidizer");
					System.out.println("Combustível Restante: " + combustivel.get());

					// Ativar o motor do Chewie
					Thread.sleep(1000);
					naveAtual.getAutoPilot().engage();
					naveAtual.getControl().activateNextStage();
					Thread.sleep(2000);
					// Paineis.setStatus("Ignição do CHEWIE!");
					naveAtual.getControl().setThrottle(1f);
				}
			}
			estagioAtual++;
		}
	}
}

package com.pesterenan.utils;

import java.io.IOException;

import org.javatuples.Triplet;

import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;

public class Navegacao {

	static SpaceCenter centroEspacial;
	private Vessel naveAtual;
	private ReferenceFrame pontoRefOrbital, pontoRefSuperficie;
	private Flight parametrosDeVoo;
	private Vetor vetorDirecaoHorizontal = new Vetor(0, 0, 0);
	private Triplet<Double, Double, Double> posicaoAlvo = new Triplet<Double, Double, Double>(0.0, 0.0, 0.0);

	public Navegacao(SpaceCenter centro, Vessel nave)
			throws IOException, RPCException, InterruptedException, StreamException {
		centroEspacial = centro;
		naveAtual = nave;
		pontoRefOrbital = naveAtual.getOrbit().getBody().getReferenceFrame();
		pontoRefSuperficie = naveAtual.getSurfaceReferenceFrame();
		parametrosDeVoo = naveAtual.flight(pontoRefOrbital);
	}

	public void mirarRetrogrado() throws IOException, RPCException, InterruptedException, StreamException {
		// Buscar Direção Retrógrada:
		posicaoAlvo = centroEspacial.transformPosition(parametrosDeVoo.getRetrograde(),
				naveAtual.getSurfaceVelocityReferenceFrame(), pontoRefOrbital);

		vetorDirecaoHorizontal = Vetor.direcaoAlvoContraria(naveAtual.position(pontoRefSuperficie),
				centroEspacial.transformPosition(posicaoAlvo, pontoRefOrbital, pontoRefSuperficie));

		Vetor alinharDirecao = getElevacaoDirecaoDoVetor(vetorDirecaoHorizontal);

		naveAtual.getAutoPilot().targetPitchAndHeading((float) alinharDirecao.y, (float) alinharDirecao.x);
//		if (naveAtual.flight(pontoRefSuperficie).getHorizontalSpeed() > 10) {
//			naveAtual.getAutoPilot().setTargetRoll((float) alinharDirecao.z);
//		}
	}

	public void mirarAlvo(Vessel alvo) throws IOException, RPCException, InterruptedException, StreamException {
		// Buscar Alvo:
		vetorDirecaoHorizontal = Vetor.direcaoAlvo(naveAtual.position(pontoRefSuperficie),
				centroEspacial.transformPosition(alvo.position(pontoRefOrbital), pontoRefOrbital, pontoRefSuperficie));

		Vetor alinharDirecao = getElevacaoDirecaoDoVetor(vetorDirecaoHorizontal);

		naveAtual.getAutoPilot().targetPitchAndHeading((float) alinharDirecao.y, (float) alinharDirecao.x);
		if (naveAtual.flight(pontoRefSuperficie).getHorizontalSpeed() > 10) {
			naveAtual.getAutoPilot().setTargetRoll((float) alinharDirecao.z);
		}
	}

	private Vetor getElevacaoDirecaoDoVetor(Vetor alvo) throws RPCException, IOException, StreamException {
		Vetor velocidade = new Vetor(
				centroEspacial.transformPosition(parametrosDeVoo.getVelocity(), pontoRefOrbital, pontoRefSuperficie));
		Vetor vetorVelocidade = new Vetor(velocidade.y, velocidade.z, velocidade.x);
		alvo = alvo.subtrai(vetorVelocidade);
		return new Vetor(Vetor.anguloDirecao(alvo), Math.max(30, (int) (90 - (alvo.Magnitude()))),
				Vetor.anguloDirecao(velocidade));
	}

}

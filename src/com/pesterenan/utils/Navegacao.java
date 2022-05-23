package com.pesterenan.utils;

import java.io.IOException;

import org.javatuples.Triplet;

import com.pesterenan.controller.FlightController;

import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;

public class Navegacao extends FlightController {

	private Vetor vetorDirecaoHorizontal = new Vetor(0, 0, 0);
	private Triplet<Double, Double, Double> posicaoAlvo = new Triplet<Double, Double, Double>(0.0, 0.0, 0.0);

	public Navegacao() {
		super(getConexao());
	}

	public void mirarRetrogrado() {
		try {
			// Buscar Direção Retrógrada:
			posicaoAlvo = centroEspacial.transformPosition(parametrosDeVoo.getRetrograde(),
					naveAtual.getSurfaceVelocityReferenceFrame(), pontoRefOrbital);

			vetorDirecaoHorizontal = Vetor.direcaoAlvoContraria(naveAtual.position(pontoRefSuperficie),
					centroEspacial.transformPosition(posicaoAlvo, pontoRefOrbital, pontoRefSuperficie));

			Vetor alinharDirecao = getElevacaoDirecaoDoVetor(vetorDirecaoHorizontal);

			naveAtual.getAutoPilot().targetPitchAndHeading((float) alinharDirecao.y, (float) alinharDirecao.x);
			naveAtual.getAutoPilot().setTargetRoll((float) 90);
		} catch (RPCException | StreamException | IOException e) {
			System.err.println("Não foi possível manobrar a nave.");
		}
	}

	public void mirarRadialDeFora() {
		try {
			posicaoAlvo = centroEspacial.transformPosition(parametrosDeVoo.getRadial(),
					naveAtual.getSurfaceVelocityReferenceFrame(), pontoRefOrbital);

			vetorDirecaoHorizontal = Vetor.direcaoAlvoContraria(naveAtual.position(pontoRefSuperficie),
					centroEspacial.transformPosition(posicaoAlvo, pontoRefOrbital, pontoRefSuperficie));

			Vetor alinharDirecao = getElevacaoDirecaoDoVetor(vetorDirecaoHorizontal);

			naveAtual.getAutoPilot().targetPitchAndHeading((float) alinharDirecao.y, (float) alinharDirecao.x);
			naveAtual.getAutoPilot().setTargetRoll((float) 90);
		} catch (RPCException | StreamException | IOException e) {
			System.err.println("Não foi possível manobrar a nave.");
		}
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
		double inclinacaoGraus = Math
				.abs(ControlePID.interpolacaoLinear(90, 45, alvo.Magnitude() / 100 * 1.2));
		return new Vetor(Vetor.anguloDirecao(alvo), inclinacaoGraus, Vetor.anguloDirecao(velocidade));
	}

}

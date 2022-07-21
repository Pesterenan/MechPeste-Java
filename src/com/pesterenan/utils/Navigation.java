package com.pesterenan.utils;

import java.io.IOException;

import org.javatuples.Triplet;

import com.pesterenan.controllers.FlightController;

import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Vessel;

public class Navigation extends FlightController {

	private final Triplet<Double, Double, Double> RADIAL = new Triplet<Double, Double, Double>(1.0, 0.0, 0.0);
	private final Triplet<Double, Double, Double> ANTI_RADIAL = new Triplet<Double, Double, Double>(-1.0, 0.0, 0.0);
	private final Triplet<Double, Double, Double> PROGRADE = new Triplet<Double, Double, Double>(0.0, 1.0, 0.0);
	private final Triplet<Double, Double, Double> RETROGRADE = new Triplet<Double, Double, Double>(0.0, -1.0, 0.0);
	private final Triplet<Double, Double, Double> NORMAL = new Triplet<Double, Double, Double>(0.0, 0.0, 1.0);
	private final Triplet<Double, Double, Double> ANTI_NORMAL = new Triplet<Double, Double, Double>(0.0, 0.0, -1.0);
	private Vetor vetorDirecaoHorizontal = new Vetor(0, 0, 0);

	public Navigation() {
		super(getConexao());
	}

	public void mirarRetrogrado() throws RPCException {
		mirarNaDirecao(centroEspacial.transformDirection(RETROGRADE, naveAtual.getSurfaceVelocityReferenceFrame(),
				pontoRefOrbital));
	}

	public void mirarRadialDeFora() throws RPCException {
		mirarNaDirecao(
				centroEspacial.transformDirection(RADIAL, naveAtual.getSurfaceReferenceFrame(), pontoRefOrbital));
	}

	public void mirarNaDirecao(Triplet<Double, Double, Double> currentDirection) {
		try {
			naveAtual.getAutoPilot().setReferenceFrame(pontoRefOrbital);
			naveAtual.getAutoPilot().setTargetDirection(currentDirection);
		} catch (RPCException e) {
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
		System.out.println(alvo.Magnitude() + "alvo");
		alvo = alvo.subtrai(vetorVelocidade);
		double inclinacaoGraus = Utilities.remap(0, Math.abs(velHorizontal.get()), 90, 0, alvo.Magnitude());
		System.out.println(inclinacaoGraus);
		return new Vetor(Vetor.anguloDirecao(alvo), Utilities.clamp(inclinacaoGraus, 0, 90),
				Vetor.anguloDirecao(vetorVelocidade));
	}

}

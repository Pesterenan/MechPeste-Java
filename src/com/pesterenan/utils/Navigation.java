package com.pesterenan.utils;

import com.pesterenan.controllers.FlightController;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Vessel;
import org.javatuples.Triplet;

public class Navigation extends FlightController {

public static final Triplet<Double, Double, Double> RADIAL = new Triplet<>(1.0, 0.0, 0.0);
public static final Triplet<Double, Double, Double> ANTI_RADIAL = new Triplet<>(- 1.0, 0.0, 0.0);
public static final Triplet<Double, Double, Double> PROGRADE = new Triplet<>(0.0, 1.0, 0.0);
public static final Triplet<Double, Double, Double> RETROGRADE = new Triplet<>(0.0, - 1.0, 0.0);
public static final Triplet<Double, Double, Double> NORMAL = new Triplet<>(0.0, 0.0, 1.0);
public static final Triplet<Double, Double, Double> ANTI_NORMAL = new Triplet<>(0.0, 0.0, - 1.0);
private Vetor vetorDirecaoHorizontal = new Vetor(0, 0, 0);

public Navigation() {
	super(getConexao());
}

public void mirarNaManobra(Node maneuver) throws RPCException {
	mirarNaDirecao(centroEspacial.transformDirection(PROGRADE, maneuver.getReferenceFrame(), pontoRefOrbital));
}

public void mirarRetrogrado() throws RPCException, StreamException {
	Vetor retrograde = new Vetor(centroEspacial.transformDirection(RETROGRADE, naveAtual.getSurfaceVelocityReferenceFrame(), pontoRefOrbital));
	Vetor radial = new Vetor(centroEspacial.transformDirection(RADIAL, naveAtual.getSurfaceReferenceFrame(), pontoRefOrbital));
	double landingX = Utilities.remap(0.0, 10.0, radial.x, retrograde.x, Utilities.clamp(velHorizontal.get(), 0, 10));
	double landingY = Utilities.remap(0.0, 10.0, radial.y, retrograde.y, Utilities.clamp(velHorizontal.get(), 0, 10));
	double landingZ = Utilities.remap(0.0, 10.0, radial.z, retrograde.z, Utilities.clamp(velHorizontal.get(), 0, 10));
	Vetor landingVector = new Vetor(landingX, landingY, landingZ);
	mirarNaDirecao(landingVector.paraTriplet());
}


public void mirarRadialDeFora() throws RPCException {
	mirarNaDirecao(centroEspacial.transformDirection(RADIAL, naveAtual.getSurfaceReferenceFrame(), pontoRefOrbital));
}

public void mirarNaDirecao(Triplet<Double, Double, Double> currentDirection) {
	try {
		naveAtual.getAutoPilot().setReferenceFrame(pontoRefOrbital);
		naveAtual.getAutoPilot().setTargetDirection(currentDirection);
	} catch (RPCException e) {
		System.err.println("Não foi possível manobrar a nave.");
	}
}

public void mirarAlvo(Vessel alvo) throws RPCException, StreamException {
	// Buscar Alvo:
	vetorDirecaoHorizontal = Vetor.direcaoAlvo(naveAtual.position(pontoRefSuperficie), centroEspacial.transformPosition(alvo.position(pontoRefOrbital), pontoRefOrbital, pontoRefSuperficie));

	Vetor alinharDirecao = getElevacaoDirecaoDoVetor(vetorDirecaoHorizontal);

	naveAtual.getAutoPilot().targetPitchAndHeading((float) alinharDirecao.y, (float) alinharDirecao.x);
	if (naveAtual.flight(pontoRefSuperficie).getHorizontalSpeed() > 10) {
		naveAtual.getAutoPilot().setTargetRoll((float) alinharDirecao.z);
	}
}

private Vetor getElevacaoDirecaoDoVetor(Vetor alvo) throws RPCException, StreamException {
	Vetor velocidade = new Vetor(centroEspacial.transformPosition(parametrosDeVoo.getVelocity(), pontoRefOrbital, pontoRefSuperficie));
	Vetor vetorVelocidade = new Vetor(velocidade.y, velocidade.z, velocidade.x);
	System.out.println(alvo.Magnitude() + "alvo");
	alvo = alvo.subtrai(vetorVelocidade);
	double inclinacaoGraus = Utilities.remap(0, Math.abs(velHorizontal.get()), 90, 0, alvo.Magnitude());
	System.out.println(inclinacaoGraus);
	return new Vetor(Vetor.anguloDirecao(alvo), Utilities.clamp(inclinacaoGraus, 0, 90), Vetor.anguloDirecao(vetorVelocidade));
}

}

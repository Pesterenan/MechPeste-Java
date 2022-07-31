package com.pesterenan.utils;

import com.pesterenan.controllers.FlightController;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Node;
import org.javatuples.Triplet;

public class Navigation extends FlightController {

public static final Triplet<Double, Double, Double> RADIAL = new Triplet<>(1.0, 0.0, 0.0);
public static final Triplet<Double, Double, Double> ANTI_RADIAL = new Triplet<>(-1.0, 0.0, 0.0);
public static final Triplet<Double, Double, Double> PROGRADE = new Triplet<>(0.0, 1.0, 0.0);
public static final Triplet<Double, Double, Double> RETROGRADE = new Triplet<>(0.0, -1.0, 0.0);
public static final Triplet<Double, Double, Double> NORMAL = new Triplet<>(0.0, 0.0, 1.0);
public static final Triplet<Double, Double, Double> ANTI_NORMAL = new Triplet<>(0.0, 0.0, -1.0);

public Navigation() {
	super(getConexao());
}

public void targetManeuver(Node maneuver) throws RPCException {
	targetDirection(centroEspacial.transformDirection(PROGRADE, maneuver.getReferenceFrame(), pontoRefOrbital));
}

public void targetLanding() throws RPCException, StreamException {
	Vetor navePosition = new Vetor(naveAtual.position(pontoRefOrbital));
	Vetor retrogradeVel = new Vetor(centroEspacial.transformPosition(RETROGRADE, naveAtual.getSurfaceVelocityReferenceFrame(), pontoRefOrbital));
	Vetor retrograde = new Vetor(retrogradeVel.x - navePosition.x, retrogradeVel.y - navePosition.y, retrogradeVel.z - navePosition.z);
//	Vetor retrograde = new Vetor(centroEspacial.transformDirection(RETROGRADE, naveAtual.getSurfaceVelocityReferenceFrame(), pontoRefOrbital));
	Vetor radial = new Vetor(centroEspacial.transformDirection(RADIAL, naveAtual.getSurfaceReferenceFrame(), pontoRefOrbital));
	double landingX = Utilities.remap(0.0, 10.0, radial.x, retrograde.x, Utilities.clamp(velHorizontal.get(), 0, 10));
	double landingY = Utilities.remap(0.0, 10.0, radial.y, retrograde.y, Utilities.clamp(velHorizontal.get(), 0, 10));
	double landingZ = Utilities.remap(0.0, 10.0, radial.z, retrograde.z, Utilities.clamp(velHorizontal.get(), 0, 10));
	Vetor landingVector = new Vetor(landingX, landingY, landingZ);
	targetDirection(landingVector.paraTriplet());
}


public void targetRadialOut() throws RPCException {
	targetDirection(centroEspacial.transformDirection(RADIAL, naveAtual.getSurfaceReferenceFrame(), pontoRefOrbital));
}

public void targetRetrograde() throws RPCException {
	targetDirection(centroEspacial.transformDirection(RETROGRADE, naveAtual.getSurfaceReferenceFrame(), pontoRefOrbital));
}

public void targetDirection(Triplet<Double, Double, Double> currentDirection) {
	try {
		naveAtual.getAutoPilot().setReferenceFrame(pontoRefOrbital);
		naveAtual.getAutoPilot().setTargetDirection(currentDirection);
	} catch (RPCException e) {
		System.err.println("Não foi possível manobrar a nave.");
	}
}

}

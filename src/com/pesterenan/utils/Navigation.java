package com.pesterenan.utils;

import com.pesterenan.model.ActiveVessel;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Node;
import org.javatuples.Triplet;

public class Navigation extends ActiveVessel {

	public static final Triplet<Double, Double, Double> RADIAL = new Triplet<>(1.0, 0.0, 0.0);
	public static final Triplet<Double, Double, Double> ANTI_RADIAL = new Triplet<>(-1.0, 0.0, 0.0);
	public static final Triplet<Double, Double, Double> PROGRADE = new Triplet<>(0.0, 1.0, 0.0);
	public static final Triplet<Double, Double, Double> RETROGRADE = new Triplet<>(0.0, -1.0, 0.0);
	public static final Triplet<Double, Double, Double> NORMAL = new Triplet<>(0.0, 0.0, 1.0);
	public static final Triplet<Double, Double, Double> ANTI_NORMAL = new Triplet<>(0.0, 0.0, -1.0);

	public Navigation() {
		super(getConexao());
		initializeParameters();
	}

	private void initializeParameters() {
		try {
			parametrosDeVoo = naveAtual.flight(pontoRefOrbital);
			velHorizontal = getConexao().addStream(parametrosDeVoo, "getHorizontalSpeed");
		} catch (RPCException | StreamException ignored) {
		}
	}

	public void targetManeuver(Node maneuver) throws RPCException {
		targetDirection(centroEspacial.transformDirection(PROGRADE, maneuver.getReferenceFrame(), pontoRefOrbital));
	}

	public void targetLanding() throws RPCException, StreamException {
		Vector navePosition = new Vector(naveAtual.position(pontoRefOrbital));
		Vector retrogradeVel = new Vector(
				centroEspacial.transformPosition(RETROGRADE, naveAtual.getSurfaceVelocityReferenceFrame(),
				                                 pontoRefOrbital
				                                ));
		Vector retrograde = new Vector(retrogradeVel.x - navePosition.x, retrogradeVel.y - navePosition.y,
		                               retrogradeVel.z - navePosition.z
		);
		Vector radial = new Vector(
				centroEspacial.transformDirection(RADIAL, naveAtual.getSurfaceReferenceFrame(), pontoRefOrbital));
		double landingX =
				Utilities.remap(0.0, 5, radial.x, retrograde.x, Utilities.clamp(velHorizontal.get(), 0, 5), false);
		double landingY =
				Utilities.remap(0.0, 5, radial.y, retrograde.y, Utilities.clamp(velHorizontal.get(), 0, 5), false);
		double landingZ =
				Utilities.remap(0.0, 5, radial.z, retrograde.z, Utilities.clamp(velHorizontal.get(), 0, 5), false);
		Vector landingVector = new Vector(landingX, landingY, landingZ);
		targetDirection(landingVector.toTriplet());
	}

	public void targetPrograde() throws RPCException {
		targetDirection(centroEspacial.transformDirection(PROGRADE, naveAtual.getSurfaceVelocityReferenceFrame(),
		                                                  pontoRefOrbital
		                                                 ));
	}

	public void targetRadialOut() throws RPCException {
		targetDirection(
				centroEspacial.transformDirection(RADIAL, naveAtual.getSurfaceReferenceFrame(), pontoRefOrbital));
	}

	public void targetRetrograde() throws RPCException {
		targetDirection(centroEspacial.transformDirection(RETROGRADE, naveAtual.getSurfaceVelocityReferenceFrame(),
		                                                  pontoRefOrbital
		                                                 ));
	}

	public void targetDirection(Triplet<Double, Double, Double> currentDirection) {
		try {
			ap.setReferenceFrame(pontoRefOrbital);
			ap.setTargetDirection(currentDirection);
		} catch (RPCException e) {
			System.err.println("Não foi possível manobrar a nave.");
		}
	}

}

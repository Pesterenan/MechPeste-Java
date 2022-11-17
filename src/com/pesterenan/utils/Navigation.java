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
//	private Drawing drawing;

	public Navigation() {
		super(getConexao());
		initializeParameters();
	}

	private void initializeParameters() {
		try {
//			drawing = Drawing.newInstance(getConexao());
			parametrosDeVoo = naveAtual.flight(pontoRefOrbital);
			velHorizontal = getConexao().addStream(parametrosDeVoo, "getHorizontalSpeed");
		} catch (RPCException | StreamException ignored) {
		}
	}

	public void aimAtManeuver(Node maneuver) throws RPCException {
		aimAtDirection(centroEspacial.transformDirection(PROGRADE, maneuver.getReferenceFrame(), pontoRefOrbital));
	}

	public void aimForLanding() throws RPCException, StreamException {
		Vector currentPosition = new Vector(naveAtual.position(pontoRefOrbital));
		Vector retrograde = new Vector(
				centroEspacial.transformPosition(RETROGRADE, naveAtual.getSurfaceVelocityReferenceFrame(),
				                                 pontoRefOrbital
				                                )).subtract(currentPosition);
		Vector radial = new Vector(
				centroEspacial.transformDirection(RADIAL, naveAtual.getSurfaceReferenceFrame(), pontoRefOrbital));
		double angleLimit = Utilities.remap(0, 10, 0, 0.9, velHorizontal.get(), true);
		Vector landingVector = Utilities.linearInterpolation(radial, retrograde, angleLimit);
		aimAtDirection(landingVector.toTriplet());
	}

//	public void aimAtTarget() throws RPCException, StreamException, InterruptedException {
//		Vector currentPosition = new Vector(naveAtual.position(pontoRefSuperficie));
//		Vector targetPosition = new Vector(centroEspacial.getTargetVessel().position(pontoRefSuperficie));
//		targetPosition.x = 0.0;
//		double distanceToTarget = Vector.distance(currentPosition, targetPosition);
//
//		Vector toTargetDirection = Vector.targetDirection(currentPosition, targetPosition);
//		Vector oppositeDirection = Vector.targetOppositeDirection(currentPosition, targetPosition);
//		Vector progradeDirection = Vector.targetDirection(currentPosition, new Vector(
//				centroEspacial.transformPosition(PROGRADE, naveAtual.getSurfaceVelocityReferenceFrame(),
//				                                 pontoRefSuperficie
//				                                )));
//		Vector retrogradeDirection = Vector.targetDirection(currentPosition, new Vector(
//				centroEspacial.transformPosition(RETROGRADE, naveAtual.getSurfaceVelocityReferenceFrame(),
//				                                 pontoRefSuperficie
//				                                )));
//		progradeDirection.x = 0.0;
//		retrogradeDirection.x = 0.0;
//		drawing.addDirection(toTargetDirection.toTriplet(), pontoRefSuperficie, 10, true);
//		drawing.addDirection(oppositeDirection.toTriplet(), pontoRefSuperficie, 5, true);
//		double pointingToTargetThreshold = Utilities.remap(0, 200, 0, 1, distanceToTarget, true);
//		double speedThreshold = Utilities.remap(0, 20, 0, 1, velHorizontal.get(), true);
//
//		Vector currentDirection =
//				Utilities.linearInterpolation(oppositeDirection, toTargetDirection, pointingToTargetThreshold);
//		double angleCurrentDirection =
//				new Vector(currentDirection.z, currentDirection.y, currentDirection.x).heading();
//		double angleProgradeDirection =
//				new Vector(progradeDirection.z, progradeDirection.y, progradeDirection.x).heading();
//		double deltaAngle = angleProgradeDirection - angleCurrentDirection;
//		System.out.println(deltaAngle);
//		if (deltaAngle > 3) {
//			currentDirection.sum(progradeDirection).normalize();
//		} else if (deltaAngle < -3) {
//			currentDirection.subtract(progradeDirection).normalize();
//		}
//		drawing.addDirection(currentDirection.toTriplet(), pontoRefSuperficie, 25, true);
//
//
//		Vector currentDirectionOnOrbitalRef = new Vector(
//				centroEspacial.transformDirection(currentDirection.toTriplet(), pontoRefSuperficie, pontoRefOrbital));
//		Vector radial = new Vector(centroEspacial.transformDirection(RADIAL, pontoRefSuperficie, pontoRefOrbital));
//		Vector speedVector = Utilities.linearInterpolation(retrogradeDirection, progradeDirection, speedThreshold);
//		Vector speedVectorOnOrbitalRef = new Vector(
//				centroEspacial.transformDirection(speedVector.toTriplet(), pontoRefSuperficie, pontoRefOrbital));
//		Vector pointingVector =
//				Utilities.linearInterpolation(currentDirectionOnOrbitalRef, radial.sum(speedVectorOnOrbitalRef),
//				                              speedThreshold
//				                             );
//		Thread.sleep(50);
//		drawing.clear(false);
//		aimAtDirection(pointingVector.toTriplet());
//	}

	public void aimAtPrograde() throws RPCException {
		aimAtDirection(centroEspacial.transformDirection(PROGRADE, naveAtual.getSurfaceVelocityReferenceFrame(),
		                                                 pontoRefOrbital
		                                                ));
	}

	public void aimAtRadialOut() throws RPCException {
		aimAtDirection(
				centroEspacial.transformDirection(RADIAL, naveAtual.getSurfaceReferenceFrame(), pontoRefOrbital));
	}

	public void aimAtRetrograde() throws RPCException {
		aimAtDirection(centroEspacial.transformDirection(RETROGRADE, naveAtual.getSurfaceVelocityReferenceFrame(),
		                                                 pontoRefOrbital
		                                                ));
	}

	public void aimAtDirection(Triplet<Double, Double, Double> currentDirection) throws RPCException {
		ap.setReferenceFrame(pontoRefOrbital);
		ap.setTargetDirection(currentDirection);
	}

}

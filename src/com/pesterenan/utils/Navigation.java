package com.pesterenan.utils;

import com.pesterenan.controllers.Controller;
import com.pesterenan.model.ActiveVessel;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Node;
import org.javatuples.Triplet;

public class Navigation extends Controller {

	public static final Triplet<Double, Double, Double> RADIAL = new Triplet<>(1.0, 0.0, 0.0);
	public static final Triplet<Double, Double, Double> ANTI_RADIAL = new Triplet<>(-1.0, 0.0, 0.0);
	public static final Triplet<Double, Double, Double> PROGRADE = new Triplet<>(0.0, 1.0, 0.0);
	public static final Triplet<Double, Double, Double> RETROGRADE = new Triplet<>(0.0, -1.0, 0.0);
	public static final Triplet<Double, Double, Double> NORMAL = new Triplet<>(0.0, 0.0, 1.0);
	public static final Triplet<Double, Double, Double> ANTI_NORMAL = new Triplet<>(0.0, 0.0, -1.0);
//	private Drawing drawing;

	public Navigation(ActiveVessel activeVessel) {
		super(activeVessel);
		initializeParameters();
	}

	private void initializeParameters() {
		try {
//			drawing = Drawing.newInstance(getConexao());
			activeVessel.parametrosDeVoo = activeVessel.getNaveAtual().flight(activeVessel.pontoRefOrbital);
			activeVessel.velHorizontal =
					activeVessel.getConnection().addStream(activeVessel.parametrosDeVoo, "getHorizontalSpeed");
		} catch (RPCException | StreamException ignored) {
		}
	}

	public void aimAtManeuver(Node maneuver) throws RPCException {
		aimAtDirection(activeVessel.centroEspacial.transformDirection(PROGRADE, maneuver.getReferenceFrame(),
		                                                              activeVessel.pontoRefOrbital
		                                                             ));
	}

	public void aimForLanding() throws RPCException, StreamException {
		Vector currentPosition = new Vector(activeVessel.getNaveAtual().position(activeVessel.pontoRefOrbital));
		Vector retrograde = new Vector(activeVessel.centroEspacial.transformPosition(RETROGRADE,
		                                                                             activeVessel.getNaveAtual()
		                                                                                         .getSurfaceVelocityReferenceFrame(),
		                                                                             activeVessel.pontoRefOrbital
		                                                                            )).subtract(currentPosition);
		Vector radial = new Vector(activeVessel.centroEspacial.transformDirection(RADIAL, activeVessel.getNaveAtual()
		                                                                                              .getSurfaceReferenceFrame(),
		                                                                          activeVessel.pontoRefOrbital
		                                                                         ));
		double angleLimit = Utilities.remap(0, 10, 0, 0.9, activeVessel.velHorizontal.get(), true);
		Vector landingVector = Utilities.linearInterpolation(radial, retrograde, angleLimit);
		aimAtDirection(landingVector.toTriplet());
	}

//	public void aimAtTarget() throws RPCException, StreamException, InterruptedException {
//		Vector currentPosition = new Vector(naveAtual.position(pontoRefSuperficie));
//		Vector targetPosition = new Vector(activeVessel.centroEspacial.getTargetVessel().position(pontoRefSuperficie));
//		targetPosition.x = 0.0;
//		double distanceToTarget = Vector.distance(currentPosition, targetPosition);
//
//		Vector toTargetDirection = Vector.targetDirection(currentPosition, targetPosition);
//		Vector oppositeDirection = Vector.targetOppositeDirection(currentPosition, targetPosition);
//		Vector progradeDirection = Vector.targetDirection(currentPosition, new Vector(
//				activeVessel.centroEspacial.transformPosition(PROGRADE, naveAtual.getSurfaceVelocityReferenceFrame(),
//				                                 pontoRefSuperficie
//				                                )));
//		Vector retrogradeDirection = Vector.targetDirection(currentPosition, new Vector(
//				activeVessel.centroEspacial.transformPosition(RETROGRADE, naveAtual.getSurfaceVelocityReferenceFrame(),
//				                                 pontoRefSuperficie
//				                                )));
//		progradeDirection.x = 0.0;
//		retrogradeDirection.x = 0.0;
//		drawing.addDirection(toTargetDirection.toTriplet(), pontoRefSuperficie, 10, true);
//		drawing.addDirection(oppositeDirection.toTriplet(), pontoRefSuperficie, 5, true);
//		double pointingToTargetThreshold = Utilities.remap(0, 200, 0, 1, distanceToTarget, true);
//		double speedThreshold = Utilities.remap(0, 20, 0, 1, activeVessel.velHorizontal.get(), true);
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
//				activeVessel.centroEspacial.transformDirection(currentDirection.toTriplet(), pontoRefSuperficie,
//				activeVessel.pontoRefOrbital));
//		Vector radial = new Vector(activeVessel.centroEspacial.transformDirection(RADIAL, pontoRefSuperficie,
//		activeVessel.pontoRefOrbital));
//		Vector speedVector = Utilities.linearInterpolation(retrogradeDirection, progradeDirection, speedThreshold);
//		Vector speedVectorOnOrbitalRef = new Vector(
//				activeVessel.centroEspacial.transformDirection(speedVector.toTriplet(), pontoRefSuperficie,
//				activeVessel.pontoRefOrbital));
//		Vector pointingVector =
//				Utilities.linearInterpolation(currentDirectionOnOrbitalRef, radial.sum(speedVectorOnOrbitalRef),
//				                              speedThreshold
//				                             );
//		Thread.sleep(50);
//		drawing.clear(false);
//		aimAtDirection(pointingVector.toTriplet());
//	}

	public void aimAtPrograde() throws RPCException {
		aimAtDirection(activeVessel.centroEspacial.transformDirection(PROGRADE, activeVessel.getNaveAtual()
		                                                                                    .getSurfaceVelocityReferenceFrame(),
		                                                              activeVessel.pontoRefOrbital
		                                                             ));
	}

	public void aimAtRadialOut() throws RPCException {
		aimAtDirection(activeVessel.centroEspacial.transformDirection(RADIAL, activeVessel.getNaveAtual()
		                                                                                  .getSurfaceReferenceFrame(),
		                                                              activeVessel.pontoRefOrbital
		                                                             ));
	}

	public void aimAtRetrograde() throws RPCException {
		aimAtDirection(activeVessel.centroEspacial.transformDirection(RETROGRADE, activeVessel.getNaveAtual()
		                                                                                      .getSurfaceVelocityReferenceFrame(),
		                                                              activeVessel.pontoRefOrbital
		                                                             ));
	}

	public void aimAtDirection(Triplet<Double, Double, Double> currentDirection) throws RPCException {
		activeVessel.ap.setReferenceFrame(activeVessel.pontoRefOrbital);
		activeVessel.ap.setTargetDirection(currentDirection);
	}

}

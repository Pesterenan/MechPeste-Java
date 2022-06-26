package com.pesterenan.utils;

import java.io.IOException;

import org.javatuples.Triplet;

import com.pesterenan.controllers.FlightController;

import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Vessel;

public class Navigation extends FlightController {

	private Vector horizontalVectorDirection = new Vector(0, 0, 0);
	private Triplet<Double, Double, Double> targetPosition = new Triplet<Double, Double, Double>(0.0, 0.0, 0.0);

	public Navigation() {
		super(getConnection());
	}

	public void pointRetrograde() throws RPCException {
		pointInDirection(flightParameters.getRetrograde());
	}

	public void pointRadialOut() throws RPCException {
		pointInDirection(flightParameters.getRadial());
	}

	public void pointInDirection(Triplet<Double, Double, Double> direction) {
		try {
			targetPosition = spaceCenter.transformPosition(direction, currentShip.getSurfaceVelocityReferenceFrame(),
					orbitalRefSpot);

			horizontalVectorDirection = Vector.oppositeTargeDirection(currentShip.position(surfaceRefSpot),
					spaceCenter.transformPosition(targetPosition, orbitalRefSpot, surfaceRefSpot));

			Vector alignDirection = getRoverElevationDirection(horizontalVectorDirection);
			currentShip.getAutoPilot().targetPitchAndHeading((float) alignDirection.y, (float) alignDirection.x);
			currentShip.getAutoPilot().setTargetRoll(90);
		} catch (RPCException | StreamException | IOException e) {
			System.err.println("Não foi possível manobrar a nave.");
		}
	}

	public void aimTarget(Vessel target) throws IOException, RPCException, InterruptedException, StreamException {
		// Buscar Alvo:
		horizontalVectorDirection = Vector.targetDirection(currentShip.position(surfaceRefSpot),
				spaceCenter.transformPosition(target.position(orbitalRefSpot), orbitalRefSpot, surfaceRefSpot));

		Vector alignDirection = getRoverElevationDirection(horizontalVectorDirection);

		currentShip.getAutoPilot().targetPitchAndHeading((float) alignDirection.y, (float) alignDirection.x);
		if (currentShip.flight(surfaceRefSpot).getHorizontalSpeed() > 10) {
			currentShip.getAutoPilot().setTargetRoll((float) alignDirection.z);
		}
	}

	private Vector getRoverElevationDirection(Vector target) throws RPCException, IOException, StreamException {
		Vector speed = new Vector(
				spaceCenter.transformPosition(flightParameters.getVelocity(), orbitalRefSpot, surfaceRefSpot));
		Vector speedVector = new Vector(speed.y, speed.z, speed.x);
		target = target.subtract(speedVector);
		double inclinationDegrees = Utilities.remap(1, 100, 90, 30, horizontalSpeed.get());
		return new Vector(Vector.angleDirection(target), Utilities.clamp(inclinationDegrees, 30, 90),
				Vector.angleDirection(speedVector));
	}

}

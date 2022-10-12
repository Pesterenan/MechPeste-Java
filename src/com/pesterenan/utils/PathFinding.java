package com.pesterenan.utils;

import com.pesterenan.model.ActiveVessel;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.Drawing;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Waypoint;
import krpc.client.services.SpaceCenter.WaypointManager;
import org.javatuples.Triplet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PathFinding extends ActiveVessel {

	public static final Vector L30DEG = new Vector(Math.sin(Math.toRadians(30)), Math.cos(Math.toRadians(30)), 0.0);
	public static final Vector L60DEG = new Vector(Math.sin(Math.toRadians(60)), Math.cos(Math.toRadians(60)), 0.0);
	public static final Vector L90DEG = new Vector(Math.sin(Math.toRadians(90)), Math.cos(Math.toRadians(90)), 0.0);
	public static final Vector R30DEG = new Vector(-Math.sin(Math.toRadians(30)), Math.cos(Math.toRadians(30)), 0.0);
	public static final Vector R60DEG = new Vector(-Math.sin(Math.toRadians(60)), Math.cos(Math.toRadians(60)), 0.0);
	public static final Vector R90DEG = new Vector(-Math.sin(Math.toRadians(90)), Math.cos(Math.toRadians(90)), 0.0);
	private static final float SEARCHING_DISTANCE = 4400000;
	private WaypointManager waypointManager;
	private List<Waypoint> waypointsToReach;
	private List<Vector> pathToTarget;
	private Drawing drawing;

	public PathFinding(Connection con) {
		super(con);
		initializeParameters();
	}

	private void initializeParameters() {
		try {
			waypointManager = centroEspacial.getWaypointManager();
			waypointsToReach = new ArrayList<>();
			pathToTarget = new ArrayList<>();
			drawing = Drawing.newInstance(getConexao());

		} catch (RPCException e) {
			throw new RuntimeException(e);
		}
	}

	public void addWaypointsOnSameBody(String waypointName) throws RPCException {
		for (Waypoint waypoint : waypointManager.getWaypoints()) {
			boolean hasSameName = waypoint.getName().toLowerCase().contains(waypointName.toLowerCase());
			boolean isOnSameBody = waypoint.getBody().equals(currentBody);
			if (hasSameName && isOnSameBody) {
				waypointsToReach.add(waypoint);
			}
		}
	}

	public Vector findNearestWaypoint() throws RPCException, IOException, InterruptedException {
		double currentDistance = SEARCHING_DISTANCE;
		Waypoint currentWaypoint = null;
		for (Waypoint waypoint : waypointsToReach) {
			double waypointDistance =
					Vector.distance(new Vector(naveAtual.position(pontoRefOrbital)), waypointPosOnSurface(waypoint));
			if (currentDistance > waypointDistance) {
				currentDistance = waypointDistance;
				currentWaypoint = waypoint;
			}
		}
		return waypointPosOnSurface(currentWaypoint);
	}

	private Vector waypointPosOnSurface(Waypoint waypoint) throws RPCException {
		return new Vector(
				currentBody.surfacePosition(waypoint.getLatitude(), waypoint.getLongitude(), pontoRefOrbital));
	}

	public boolean isPathToTargetEmpty() {
		return pathToTarget.isEmpty();
	}

	public Vector getPathsFirstPoint() {
		if (isPathToTargetEmpty()) {
			return new Vector();
		}
		return pathToTarget.get(0);
	}

	public void removePathsCurrentPoint() {
		if (isPathToTargetEmpty()) {
			return;
		}
		pathToTarget.remove(0);
	}

	public void removeWaypointFromList() throws RPCException {
		if (!waypointsToReach.isEmpty()) {
			if (!waypointsToReach.get(0).getHasContract()) {
				waypointsToReach.remove(0);
			}
		}
	}

	/**
	 * Builds the path to the targetPosition, on the Celestial Body Reference ( Orbital Ref )
	 *
	 * @param targetPosition the target pos to build the path to
	 * @throws IOException
	 * @throws RPCException
	 * @throws InterruptedException
	 */
	public void buildPathToTarget(Vector targetPosition) throws IOException, RPCException, InterruptedException {
		// Get current rover Position on Orbital Ref, transform to Surf Ref and add 2 meters on height:
		Vector roverHeight = new Vector(2.0, 0.0, 0.0);
		Vector currentRoverPos =
				transformSurfToOrb(new Vector(naveAtual.position(pontoRefSuperficie)).sum(roverHeight));
		// Calculate distance from rover to target on Orbital Ref:
		double distanceToTarget = Vector.distance(currentRoverPos, targetPosition);
		// Add rover pos as first point, on Orbital Ref
		pathToTarget.add(currentRoverPos);
		// Calculate the next points positions and add to the list on Orbital Ref
		int index = 0;
		while (distanceToTarget > 100) {
			Vector previousPoint = pathToTarget.get(index);
			index++;
			Vector directionToTarget =
					transformOrbToSurf(targetPosition).subtract(transformOrbToSurf(previousPoint)).normalize();
			Vector calculatedNextPoint =
					avoidObstacles(transformOrbToSurf(previousPoint), transformDirection(directionToTarget, true));
			Vector nextPoint =
					transformSurfToOrb(transformOrbToSurf(getPosOnSurface(calculatedNextPoint)).sum(roverHeight));
			drawLineBetweenPoints(previousPoint, nextPoint);
			pathToTarget.add(nextPoint);
			double distanceBetweenPoints = Vector.distance(previousPoint, nextPoint);
			distanceToTarget -= distanceBetweenPoints;
		}
		pathToTarget.add(getPosOnSurface(targetPosition));
	}

	private void drawLineBetweenPoints(Vector pointA, Vector pointB) throws RPCException {
		Drawing.Line line = drawing.addLine(pointA.toTriplet(), pointB.toTriplet(), pontoRefOrbital, true);
		line.setThickness(0.5f);
		line.setColor(new Triplet<>(1.0, 0.5, 0.0));
	}

	private Vector avoidObstacles(Vector currentPoint, Vector targetDirection) throws RPCException, IOException {
		// PONTO REF SUPERFICIE: X = CIMA, Y = NORTE, Z = LESTE;
		// Distance of the next point in the path to the previous one:
		double stepDistance = 10.0;

		// Raycast distance in front of rover:
		double centerDistance =
				raycastDistance(currentPoint, transformDirection(targetDirection, false), pontoRefSuperficie);
		double left15degDistance =
				raycastDistance(currentPoint, transformDirection(targetDirection.sum(L30DEG).normalize(), false),
				                pontoRefSuperficie
				               );
		double left30degDistance =
				raycastDistance(currentPoint, transformDirection(targetDirection.sum(L60DEG).normalize(), false),
				                pontoRefSuperficie
				               );
		double right15degDistance =
				raycastDistance(currentPoint, transformDirection(targetDirection.sum(R30DEG).normalize(), false),
				                pontoRefSuperficie
				               );
		double right30degDistance =
				raycastDistance(currentPoint, transformDirection(targetDirection.sum(R60DEG).normalize(), false),
				                pontoRefSuperficie
				               );

		// Calculate the next point direction based on all raycast distances:
		Vector nextPointDirection = transformDirection(targetDirection, false).multiply(centerDistance)
//																																				.sum(transformDirection(targetDirection,
//																																								false).sum(L30DEG)
//																																																											 .normalize()
//																																																											 .multiply(left15degDistance))
//																																				.sum(transformDirection(targetDirection,
//																																								false).sum(L60DEG)
//																																																											 .normalize()
//																																																											 .multiply(left30degDistance))
//																																				.sum(transformDirection(targetDirection,
//																																								false).sum(R30DEG)
//																																																											 .normalize()
//																																																											 .multiply(right15degDistance))
//																																				.sum(transformDirection(targetDirection,
//																																								false).sum(R60DEG)
//																																																											 .normalize()
//																																																											 .multiply(right30degDistance))
                                                                              .normalize();
		return transformSurfToOrb(currentPoint.sum(nextPointDirection.multiply(stepDistance)));
	}

	public double raycastDistance(Vector currentPoint, Vector targetDirection, SpaceCenter.ReferenceFrame reference) throws RPCException {
		double searchDistance = 20.0;
		return Math.min(
				centroEspacial.raycastDistance(currentPoint.toTriplet(), targetDirection.toTriplet(), reference),
				searchDistance
		               );
	}

	private Vector transformDirection(Vector vector, boolean toSurf) throws RPCException {
		if (toSurf) {
			return new Vector(centroEspacial.transformDirection(vector.toTriplet(), naveAtual.getReferenceFrame(),
			                                                    pontoRefSuperficie
			                                                   ));
		}
		return new Vector(centroEspacial.transformDirection(vector.toTriplet(), pontoRefSuperficie,
		                                                    naveAtual.getReferenceFrame()
		                                                   ));
	}

	private Vector getPosOnSurface(Vector vector) throws RPCException {
		return new Vector(
				currentBody.surfacePosition(currentBody.latitudeAtPosition(vector.toTriplet(), pontoRefOrbital),
				                            currentBody.longitudeAtPosition(vector.toTriplet(), pontoRefOrbital),
				                            pontoRefOrbital
				                           ));
	}

	private Vector transformSurfToOrb(Vector vector) throws IOException, RPCException {
		return new Vector(centroEspacial.transformPosition(vector.toTriplet(), pontoRefSuperficie, pontoRefOrbital));
	}

	private Vector transformOrbToSurf(Vector vector) throws IOException, RPCException {
		return new Vector(centroEspacial.transformPosition(vector.toTriplet(), pontoRefOrbital, pontoRefSuperficie));
	}
}

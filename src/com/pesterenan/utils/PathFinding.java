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
import java.util.stream.Collectors;

public class PathFinding extends ActiveVessel {

	private static final float SEARCHING_DISTANCE = 4400000;
	private WaypointManager waypointManager;
	private String waypointName;
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
		this.waypointName = waypointName;
		waypointsToReach =
				waypointManager.getWaypoints().stream().filter(wp -> hasSameName(wp)).collect(Collectors.toList());
	}

	private boolean hasSameName(Waypoint wp) {
		try {
			return wp.getName().equals(waypointName) && wp.getBody().equals(currentBody);
		} catch (RPCException e) {
			return false;
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
		while (distanceToTarget > 10) {
			Vector currentPoint = pathToTarget.get(index);
			Vector targetDirection =
					transformOrbToSurf(targetPosition).subtract(transformOrbToSurf(currentPoint)).normalize();
			Vector nextPoint =
					transformSurfToOrb(calculateNextPoint(transformOrbToSurf(currentPoint), targetDirection));
			drawLineBetweenPoints(currentPoint, nextPoint);
			pathToTarget.add(nextPoint);
			index++;
			double distanceBetweenPoints =
					Vector.distance(transformOrbToSurf(currentPoint), transformOrbToSurf(nextPoint));
			distanceToTarget -= distanceBetweenPoints;
		}
		pathToTarget.add(getPosOnSurface(targetPosition));
	}

	private void drawLineBetweenPoints(Vector pointA, Vector pointB) throws RPCException {
		Drawing.Line line = drawing.addLine(pointA.toTriplet(), pointB.toTriplet(), pontoRefOrbital, true);
		line.setThickness(0.5f);
		line.setColor(new Triplet<>(1.0, 0.5, 0.0));
	}

	private Vector calculateNextPoint(Vector currentPoint, Vector targetDirection) throws RPCException, IOException {
		// PONTO REF SUPERFICIE: X = CIMA, Y = NORTE, Z = LESTE;
		double stepDistance = 100.0;
		// Calculate the next point position on surface:
		Vector nextPoint =
				getPosOnSurface(transformSurfToOrb(currentPoint.sum(targetDirection.multiply(stepDistance))));
		return transformOrbToSurf(nextPoint).sum(new Vector(2.0, 0.0, 0.0));
	}

	public double raycastDistance(Vector currentPoint, Vector targetDirection, SpaceCenter.ReferenceFrame reference,
	                              double searchDistance) throws RPCException {
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

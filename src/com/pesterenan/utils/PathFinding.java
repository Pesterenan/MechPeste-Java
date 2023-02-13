package com.pesterenan.utils;

import com.pesterenan.controllers.Controller;
import krpc.client.RPCException;
import krpc.client.services.Drawing;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Waypoint;
import krpc.client.services.SpaceCenter.WaypointManager;
import org.javatuples.Triplet;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import static com.pesterenan.MechPeste.getConnection;
import static com.pesterenan.MechPeste.getSpaceCenter;

public class PathFinding extends Controller {

	private WaypointManager waypointManager;
	private String waypointName;
	private List<Waypoint> waypointsToReach;
	private List<Vector> pathToTarget;
	private Drawing drawing;
	private Drawing.Polygon polygonPath;

	public PathFinding() {
		super();
		initializeParameters();
	}

	private void initializeParameters() {
		try {
			waypointManager = getSpaceCenter().getWaypointManager();
			waypointsToReach = new ArrayList<>();
			pathToTarget = new ArrayList<>();
			drawing = Drawing.newInstance(getConnection());
		} catch (RPCException ignored) {
		}
	}

	public void addWaypointsOnSameBody(String waypointName) throws RPCException {
		this.waypointName = waypointName;
		this.waypointsToReach =
				waypointManager.getWaypoints().stream().filter(this::hasSameName).collect(Collectors.toList());
	}

	private boolean hasSameName(Waypoint waypoint) {
		try {
			return waypoint.getName().toLowerCase().contains(waypointName.toLowerCase()) &&
					waypoint.getBody().equals(currentBody);
		} catch (RPCException e) {
			return false;
		}
	}

	public Vector findNearestWaypoint() throws RPCException, IOException, InterruptedException {
		waypointsToReach = waypointsToReach.stream().sorted((w1, w2) -> {
			double w1Distance = 0;
			double w2Distance = 0;
			try {
				w1Distance = Vector.distance(new Vector(getNaveAtual().position(orbitalReferenceFrame)),
				                             waypointPosOnSurface(w1)
				                            );
				w2Distance = Vector.distance(new Vector(getNaveAtual().position(orbitalReferenceFrame)),
				                             waypointPosOnSurface(w2)
				                            );
			} catch (RPCException e) {
			}
			return w1Distance > w2Distance ? 1 : -1;
		}).collect(Collectors.toList());
		waypointsToReach.forEach(System.out::println);
		Waypoint currentWaypoint = waypointsToReach.get(0);
//		for (Waypoint waypoint : waypointsToReach) {
//			double waypointDistance = Vector.distance(new Vector(getNaveAtual().position(orbitalReferenceFrame)),
//			                                          waypointPosOnSurface(waypoint)
//			                                         );
//			if (currentDistance > waypointDistance) {
//				currentDistance = waypointDistance;
//				currentWaypoint = waypoint;
//			}
//		}
		return waypointPosOnSurface(currentWaypoint);
	}

	private Vector waypointPosOnSurface(Waypoint waypoint) throws RPCException {
		return new Vector(
				currentBody.surfacePosition(waypoint.getLatitude(), waypoint.getLongitude(), orbitalReferenceFrame));
	}

	public boolean isPathToTargetEmpty() {
		return pathToTarget.isEmpty();
	}

	public boolean isWaypointsToReachEmpty() {
		boolean allFromContract = waypointsToReach.stream().allMatch(v -> {
			try {
				return v.getHasContract();
			} catch (RPCException ignored) {
			}
			return false;
		});
		return waypointsToReach.isEmpty() || allFromContract;
	}

	public Vector getPathsFirstPoint() {
		return pathToTarget.get(0);
	}

	public void removePathsCurrentPoint() {
		if (isPathToTargetEmpty()) {
			return;
		}
		pathToTarget.remove(0);
	}

	public void removeWaypointFromList() throws RPCException {
		if (waypointsToReach.isEmpty()) {
			return;
		}
		waypointsToReach.remove(0);
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
		// Get current rover Position on Orbital Ref, transform to Surf Ref and add 20 centimeters on height:
		Vector roverHeight = new Vector(0.2, 0.0, 0.0);
		Vector currentRoverPos =
				transformSurfToOrb(new Vector(getNaveAtual().position(surfaceReferenceFrame)).sum(roverHeight));
		// Calculate distance from rover to target on Orbital Ref:
		double distanceToTarget = Vector.distance(currentRoverPos, targetPosition);
		// Add rover pos as first point, on Orbital Ref
		pathToTarget.add(currentRoverPos);
		// Calculate the next points positions and add to the list on Orbital Ref
		int index = 0;
		while (distanceToTarget > 50) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			Vector currentPoint = pathToTarget.get(index);
			Vector targetDirection =
					transformOrbToSurf(targetPosition).subtract(transformOrbToSurf(currentPoint)).normalize();
			Vector nextPoint =
					transformSurfToOrb(calculateNextPoint(transformOrbToSurf(currentPoint), targetDirection));
			pathToTarget.add(nextPoint);
			index++;
			double distanceBetweenPoints =
					Vector.distance(transformOrbToSurf(currentPoint), transformOrbToSurf(nextPoint));
			distanceToTarget -= distanceBetweenPoints;
		}
		pathToTarget.add(getPosOnSurface(targetPosition));
		drawPathToTarget(pathToTarget);
	}

	private void drawPathToTarget(List<Vector> path) throws RPCException {
		Deque<Triplet<Double, Double, Double>> drawablePath =
				path.stream().map(Vector::toTriplet).collect(Collectors.toCollection(ArrayDeque::new));
		drawablePath.offerFirst(new Triplet<>(0.0, 0.0, 0.0));
		drawablePath.offerLast(new Triplet<>(0.0, 0.0, 0.0));
		polygonPath =
				drawing.addPolygon(drawablePath.stream().collect(Collectors.toList()), orbitalReferenceFrame, true);
		polygonPath.setThickness(0.5f);
		polygonPath.setColor(new Triplet<>(1.0, 0.5, 0.0));
	}

	public void removeDrawnPath() {
		try {
			polygonPath.remove();
		} catch (RPCException ignored) {
		}
	}

	private Vector calculateNextPoint(Vector currentPoint, Vector targetDirection) throws RPCException, IOException {
		// PONTO REF SUPERFICIE: X = CIMA, Y = NORTE, Z = LESTE;
		double stepDistance = 100.0;
		// Calculate the next point position on surface:
		Vector nextPoint =
				getPosOnSurface(transformSurfToOrb(currentPoint.sum(targetDirection.multiply(stepDistance))));
		return transformOrbToSurf(nextPoint).sum(new Vector(0.2, 0.0, 0.0));
	}

	public double raycastDistance(Vector currentPoint, Vector targetDirection, SpaceCenter.ReferenceFrame reference,
	                              double searchDistance) throws RPCException {
		return Math.min(
				getSpaceCenter().raycastDistance(currentPoint.toTriplet(), targetDirection.toTriplet(), reference),
				searchDistance
		               );
	}

	private Vector getPosOnSurface(Vector vector) throws RPCException {
		return new Vector(
				currentBody.surfacePosition(currentBody.latitudeAtPosition(vector.toTriplet(), orbitalReferenceFrame),
				                            currentBody.longitudeAtPosition(vector.toTriplet(), orbitalReferenceFrame),
				                            orbitalReferenceFrame
				                           ));
	}

	private Vector transformSurfToOrb(Vector vector) throws IOException, RPCException {
		return new Vector(
				getSpaceCenter().transformPosition(vector.toTriplet(), surfaceReferenceFrame, orbitalReferenceFrame));
	}

	private Vector transformOrbToSurf(Vector vector) throws IOException, RPCException {
		return new Vector(
				getSpaceCenter().transformPosition(vector.toTriplet(), orbitalReferenceFrame, surfaceReferenceFrame));
	}
}

package com.pesterenan.utils;

import com.pesterenan.model.ConnectionManager;
import com.pesterenan.model.VesselManager;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import krpc.client.RPCException;
import krpc.client.services.Drawing;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Waypoint;
import krpc.client.services.SpaceCenter.WaypointManager;
import org.javatuples.Triplet;

public class PathFinding {

  private WaypointManager waypointManager;
  private String waypointName;
  private List<Waypoint> waypointsToReach;
  private List<Vector> pathToTarget;
  private Drawing drawing;
  private Drawing.Polygon polygonPath;
  private final ConnectionManager connectionManager;
  private final VesselManager vesselManager;

  public PathFinding(ConnectionManager connectionManager, VesselManager vesselManager) {
    this.connectionManager = connectionManager;
    this.vesselManager = vesselManager;
    initializeParameters();
  }

  private void initializeParameters() {
    try {
      waypointManager = connectionManager.getSpaceCenter().getWaypointManager();
      waypointsToReach = new ArrayList<>();
      pathToTarget = new ArrayList<>();
      drawing = Drawing.newInstance(connectionManager.getConnection());
    } catch (RPCException ignored) {
    }
  }

  public void addWaypointsOnSameBody(String waypointName) throws RPCException {
    this.waypointName = waypointName;
    this.waypointsToReach =
        waypointManager.getWaypoints().stream()
            .filter(this::hasSameName)
            .collect(Collectors.toList());
  }

  private boolean hasSameName(Waypoint waypoint) {
    try {
      return waypoint.getName().toLowerCase().contains(waypointName.toLowerCase())
          && waypoint.getBody().equals(vesselManager.getCurrentVessel().currentBody);
    } catch (RPCException e) {
      return false;
    }
  }

  public Vector findNearestWaypoint() throws RPCException, IOException, InterruptedException {
    waypointsToReach =
        waypointsToReach.stream()
            .sorted(
                (w1, w2) -> {
                  double w1Distance = 0;
                  double w2Distance = 0;
                  try {
                    w1Distance =
                        Vector.distance(
                            new Vector(
                                vesselManager
                                    .getCurrentVessel()
                                    .getActiveVessel()
                                    .position(
                                        vesselManager.getCurrentVessel().orbitalReferenceFrame)),
                            waypointPosOnSurface(w1));
                    w2Distance =
                        Vector.distance(
                            new Vector(
                                vesselManager
                                    .getCurrentVessel()
                                    .getActiveVessel()
                                    .position(
                                        vesselManager.getCurrentVessel().orbitalReferenceFrame)),
                            waypointPosOnSurface(w2));
                  } catch (RPCException e) {
                  }
                  return w1Distance > w2Distance ? 1 : -1;
                })
            .collect(Collectors.toList());
    waypointsToReach.forEach(System.out::println);
    Waypoint currentWaypoint = waypointsToReach.get(0);
    return waypointPosOnSurface(currentWaypoint);
  }

  private Vector waypointPosOnSurface(Waypoint waypoint) throws RPCException {
    return new Vector(
        vesselManager
            .getCurrentVessel()
            .currentBody
            .surfacePosition(
                waypoint.getLatitude(),
                waypoint.getLongitude(),
                vesselManager.getCurrentVessel().orbitalReferenceFrame));
  }

  public boolean isPathToTargetEmpty() {
    return pathToTarget.isEmpty();
  }

  public boolean isWaypointsToReachEmpty() {
    boolean allFromContract =
        waypointsToReach.stream()
            .allMatch(
                v -> {
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

  public void buildPathToTarget(Vector targetPosition)
      throws IOException, RPCException, InterruptedException {
    Vector roverHeight = new Vector(0.2, 0.0, 0.0);
    Vector currentRoverPos =
        transformSurfToOrb(
            new Vector(
                    vesselManager
                        .getCurrentVessel()
                        .getActiveVessel()
                        .position(vesselManager.getCurrentVessel().surfaceReferenceFrame))
                .sum(roverHeight));
    double distanceToTarget = Vector.distance(currentRoverPos, targetPosition);
    pathToTarget.add(currentRoverPos);
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
        drawing.addPolygon(
            drawablePath.stream().collect(Collectors.toList()),
            vesselManager.getCurrentVessel().orbitalReferenceFrame,
            true);
    polygonPath.setThickness(0.5f);
    polygonPath.setColor(new Triplet<>(1.0, 0.5, 0.0));
  }

  public void removeDrawnPath() {
    try {
      polygonPath.remove();
    } catch (RPCException ignored) {
    }
  }

  private Vector calculateNextPoint(Vector currentPoint, Vector targetDirection)
      throws RPCException, IOException {
    double stepDistance = 100.0;
    Vector nextPoint =
        getPosOnSurface(
            transformSurfToOrb(currentPoint.sum(targetDirection.multiply(stepDistance))));
    return transformOrbToSurf(nextPoint).sum(new Vector(0.2, 0.0, 0.0));
  }

  public double raycastDistance(
      Vector currentPoint,
      Vector targetDirection,
      SpaceCenter.ReferenceFrame reference,
      double searchDistance)
      throws RPCException {
    return Math.min(
        connectionManager
            .getSpaceCenter()
            .raycastDistance(currentPoint.toTriplet(), targetDirection.toTriplet(), reference),
        searchDistance);
  }

  private Vector getPosOnSurface(Vector vector) throws RPCException {
    return new Vector(
        vesselManager
            .getCurrentVessel()
            .currentBody
            .surfacePosition(
                vesselManager
                    .getCurrentVessel()
                    .currentBody
                    .latitudeAtPosition(
                        vector.toTriplet(), vesselManager.getCurrentVessel().orbitalReferenceFrame),
                vesselManager
                    .getCurrentVessel()
                    .currentBody
                    .longitudeAtPosition(
                        vector.toTriplet(), vesselManager.getCurrentVessel().orbitalReferenceFrame),
                vesselManager.getCurrentVessel().orbitalReferenceFrame));
  }

  private Vector transformSurfToOrb(Vector vector) throws IOException, RPCException {
    return new Vector(
        connectionManager
            .getSpaceCenter()
            .transformPosition(
                vector.toTriplet(),
                vesselManager.getCurrentVessel().surfaceReferenceFrame,
                vesselManager.getCurrentVessel().orbitalReferenceFrame));
  }

  private Vector transformOrbToSurf(Vector vector) throws IOException, RPCException {
    return new Vector(
        connectionManager
            .getSpaceCenter()
            .transformPosition(
                vector.toTriplet(),
                vesselManager.getCurrentVessel().orbitalReferenceFrame,
                vesselManager.getCurrentVessel().surfaceReferenceFrame));
  }
}

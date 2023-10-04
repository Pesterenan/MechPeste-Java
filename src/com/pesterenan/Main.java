package com.pesterenan;

import java.io.IOException;

import org.javatuples.Triplet;

import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Vector;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.Drawing;
import krpc.client.services.Drawing.Line;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Control;
import krpc.client.services.SpaceCenter.DockingPort;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.SASMode;
import krpc.client.services.SpaceCenter.Vessel;

public class Main {
    private static Connection connection;
    private static SpaceCenter spaceCenter;
    private static Drawing drawing;
    private static ControlePID speedCtrl;
    private static ControlePID distCtrl;
    private static ControlePID RCSForwardsCtrl;
    private static ControlePID RCSSidewaysCtrl;
    private static ControlePID RCSUpwardsCtrl;
    private static Control control;

    private static Vessel activeVessel;
    private static Vessel targetVessel;

    private static ReferenceFrame orbitalRefVessel;
    private static ReferenceFrame vesselRefFrame;
    private static ReferenceFrame orbitalRefBody;
    private static final double SPEED_LIMIT = 5.0;
    private static final double DISTANCE_LIMIT = 50.0;
    private static Line distanceLine;
    private static Line distLineXAxis;
    private static Line distLineYAxis;
    private static Line distLineZAxis;

    private static void initializeParameters() {
        try {
            connection = Connection.newInstance();
            spaceCenter = SpaceCenter.newInstance(connection);
            drawing = Drawing.newInstance(connection);
            activeVessel = spaceCenter.getActiveVessel();
            targetVessel = spaceCenter.getTargetVessel();
            vesselRefFrame = activeVessel.getReferenceFrame();
            orbitalRefVessel = activeVessel.getOrbitalReferenceFrame();
            orbitalRefBody = activeVessel.getOrbit().getBody().getReferenceFrame();

            speedCtrl = new ControlePID(spaceCenter, 50);
            speedCtrl.setPIDValues(0.5, 0.001, 0.01);
            distCtrl = new ControlePID(spaceCenter, 50);
            distCtrl.setPIDValues(1, 0.001, 0.1);
            RCSForwardsCtrl = new ControlePID(spaceCenter, 50);
            RCSSidewaysCtrl = new ControlePID(spaceCenter, 50);
            RCSUpwardsCtrl = new ControlePID(spaceCenter, 50);

        } catch (IOException | RPCException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            // Initialize parameters for the script, connection and setup:
            initializeParameters();

            DockingPort myDockingPort = activeVessel.getParts().getDockingPorts().get(0);
            DockingPort targetDockingPort = targetVessel.getParts().getDockingPorts().get(0);

            // Fase 1: Apontar pra dockingport alvo de longe:
            Vector positionMyDockingPort = new Vector(myDockingPort.position(vesselRefFrame));
            Vector positionTargetDockingPort = new Vector(targetDockingPort.position(vesselRefFrame));

            createLines(positionMyDockingPort, positionTargetDockingPort);

            // Setting up the control
            control = activeVessel.getControl();
            control.setSAS(true);
            control.setSASMode(SASMode.STABILITY_ASSIST);

            double loopTimeInSeconds = 0;
            while (loopTimeInSeconds < 30) {
                // Calcular distancia:
                double distanceBetweenPortsInMeters = positionTargetDockingPort.subtract(positionMyDockingPort)
                        .magnitude();
                double currentRelativeVelocity = new Vector(activeVessel.velocity(
                        targetDockingPort.getReferenceFrame())).magnitude();
                positionMyDockingPort = new Vector(myDockingPort.position(vesselRefFrame));
                positionTargetDockingPort = new Vector(targetDockingPort.position(vesselRefFrame));

                double speedOutput = 0;
                double distanceOutput = 0;

                setDirection(distanceBetweenPortsInMeters);
                if (distanceBetweenPortsInMeters > DISTANCE_LIMIT) {
                    speedOutput = speedCtrl.calculate(currentRelativeVelocity, SPEED_LIMIT);
                    distanceOutput = distCtrl.calculate(-distanceBetweenPortsInMeters,
                            DISTANCE_LIMIT);
                    System.out.println("Speed: " + speedOutput + " Distance: " + distanceOutput +
                            " Calc: "
                            + (speedOutput * distanceOutput));
                    control.setForward(
                            (float) (speedOutput * distanceOutput));
                } else {
                    if (currentRelativeVelocity <= 3.0) {
                        speedOutput = speedCtrl.calculate(currentRelativeVelocity, 0.1);
                        control.setForward((float) (speedOutput));
                        if (currentRelativeVelocity < 0.3) {
                            break;
                        }
                    } else {
                        speedOutput = speedCtrl.calculate(currentRelativeVelocity, 2);
                        control.setForward((float) (speedOutput * 0.5));
                    }
                    System.out.println("Vel: " + currentRelativeVelocity + " PID: " +
                            speedOutput);
                }
                updateLines(positionMyDockingPort, positionTargetDockingPort);
                Thread.sleep(50);
                loopTimeInSeconds += 0.05;
            }

            // Triplet<Double, Double, Double> direcaoTransformada = spaceCenter
            // .transformDirection(targetDockingPort.direction(orbitalRefVessel),
            // orbitalRefVessel,
            // orbitalRefVessel);

            // SEGUNDA PARTE APONTAR PRO LADO CONTRARIO:
            Vector direcaoContrariaDockingPortAlvo = new Vector(targetDockingPort.direction(orbitalRefVessel))
                    .multiply(-1);
            control.setSAS(false);
            control.setRCS(false);
            activeVessel.getAutoPilot().engage();
            activeVessel.getAutoPilot().setReferenceFrame(orbitalRefVessel);
            activeVessel.getAutoPilot().setTargetDirection(direcaoContrariaDockingPortAlvo.toTriplet());
            loopTimeInSeconds = 0;

            while (loopTimeInSeconds < 10) {
                System.out.println("apontando");
                activeVessel.getAutoPilot().wait_();
                if (activeVessel.getAutoPilot().getError() < 1) {
                    break;
                }
                Thread.sleep(50);
                loopTimeInSeconds += 0.05;
            }
            control.setSAS(true);
            control.setSASMode(SASMode.STABILITY_ASSIST);
            activeVessel.getAutoPilot().disengage();

            Thread.sleep(1000);
            // // Close the connection when finished
            connection.close();
        } catch (IOException | RPCException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void setDirection(double distance) {
        try {
            if (distance > DISTANCE_LIMIT) {
                control.setSASMode(SASMode.TARGET);
            } else {
                control.setSASMode(SASMode.STABILITY_ASSIST);
            }
        } catch (RPCException e) {
        }
    }

    private static void createLines(Vector start, Vector end) {
        try {
            distanceLine = drawing.addLine(start.toTriplet(),
                    end.toTriplet(), vesselRefFrame, true);
            distLineXAxis = drawing.addLine(start.toTriplet(),
                    new Vector(end.x, 0.0, 0.0).toTriplet(),
                    vesselRefFrame, true);
            distLineYAxis = drawing.addLine(start.toTriplet(),
                    new Vector(end.x, end.y, 0.0).toTriplet(),
                    vesselRefFrame, true);
            distLineZAxis = drawing.addLine(start.toTriplet(),
                    end.toTriplet(),
                    vesselRefFrame, true);
            distanceLine.setThickness(0.5f);
            distLineXAxis.setThickness(0.25f);
            distLineYAxis.setThickness(0.25f);
            distLineZAxis.setThickness(0.25f);
            distLineXAxis.setColor(new Vector(1.0, 0.0, 0.0).toTriplet());
            distLineYAxis.setColor(new Vector(0.0, 1.0, 0.0).toTriplet());
            distLineZAxis.setColor(new Vector(0.0, 0.0, 1.0).toTriplet());
        } catch (RPCException e) {
        }
    }

    private static void updateLines(Vector start, Vector end) {
        // Updating drawing lines:
        try {
            distanceLine.setStart(start.toTriplet());
            distanceLine.setEnd(end.toTriplet());
            distLineXAxis.setStart(start.toTriplet());
            distLineXAxis.setEnd(new Vector(end.x, 0.0, 0.0).toTriplet());
            distLineYAxis.setStart(distLineXAxis.getEnd());
            distLineYAxis.setEnd(new Vector(end.x, end.y, 0.0).toTriplet());
            distLineZAxis.setStart(distLineYAxis.getEnd());
            distLineZAxis.setEnd(end.toTriplet());
        } catch (RPCException e) {
        }
    }
}

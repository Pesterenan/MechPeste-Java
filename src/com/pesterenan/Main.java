package com.pesterenan;

import java.io.IOException;

import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Utilities;
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
    private static DockingPort myDockingPort;
    private static DockingPort targetDockingPort;
    private static Vector positionMyDockingPort;
    private static Vector positionTargetDockingPort;

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
            distCtrl = new ControlePID(spaceCenter, 50);
            RCSForwardsCtrl = new ControlePID(spaceCenter, 50);
            RCSSidewaysCtrl = new ControlePID(spaceCenter, 50);
            RCSUpwardsCtrl = new ControlePID(spaceCenter, 50);
            distCtrl.setPIDValues(1, 0.001, 0.1);
            adjustPID(25);

            myDockingPort = activeVessel.getParts().getDockingPorts().get(0);
            targetDockingPort = targetVessel.getParts().getDockingPorts().get(0);

            positionMyDockingPort = new Vector(myDockingPort.position(orbitalRefVessel));
            positionTargetDockingPort = new Vector(targetDockingPort.position(orbitalRefVessel));

            createLines(positionMyDockingPort, positionTargetDockingPort);

        } catch (IOException | RPCException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            // Initialize parameters for the script, connection and setup:
            initializeParameters();

            // Setting up the control
            control = activeVessel.getControl();
            control.setSAS(true);
            control.setRCS(false);
            control.setSASMode(SASMode.STABILITY_ASSIST);

            double loopTimeInSeconds = 0;
            Vector targetDirection = positionMyDockingPort.subtract(positionTargetDockingPort).multiply(-1);
            activeVessel.getAutoPilot().engage();
            activeVessel.getAutoPilot().setReferenceFrame(orbitalRefVessel);
            activeVessel.getAutoPilot().setTargetDirection(targetDirection.toTriplet());
            activeVessel.getAutoPilot().setTargetRoll(90);
            activeVessel.getAutoPilot().wait_();
            control.setRCS(true);
            // PRIMEIRA PARTE DO DOCKING: APROXIMAÇÃO
            positionMyDockingPort = new Vector(myDockingPort.position(vesselRefFrame));
            positionTargetDockingPort = new Vector(targetDockingPort.position(vesselRefFrame));

            targetDirection = positionMyDockingPort.subtract(positionTargetDockingPort);

            double lastYTargetPos = targetDirection.y;
            System.out.println(lastYTargetPos + " LAST Y POS");
            long sleepTime = 25;
            while (Math.abs(lastYTargetPos) >= DISTANCE_LIMIT) {
                // Calcular distancia:
                double distanceBetweenPortsInMeters = positionTargetDockingPort.subtract(positionMyDockingPort)
                        .magnitude();
                double currentRelativeVelocity = new Vector(activeVessel.velocity(
                        targetDockingPort.getReferenceFrame())).magnitude();
                // Buscar posições atuais:
                positionMyDockingPort = new Vector(myDockingPort.position(vesselRefFrame));
                positionTargetDockingPort = new Vector(targetDockingPort.position(vesselRefFrame));

                targetDirection = positionMyDockingPort.subtract(positionTargetDockingPort);

                // Calcular velocidade de cada eixo:
                double currentYAxisSpeed = (targetDirection.y - lastYTargetPos) * sleepTime;

                // Calcular o valor PID de cada eixo:
                float forwardsError = (float) RCSForwardsCtrl.calculate(targetDirection.y,
                        Math.signum(targetDirection.y) * DISTANCE_LIMIT);
                float speedError = (float) speedCtrl.calculate(currentYAxisSpeed / 10, 0);

                control.setForward(forwardsError);

                updateLines(positionMyDockingPort, positionTargetDockingPort);
                Thread.sleep(sleepTime);
                loopTimeInSeconds += 0.05;
                lastYTargetPos = targetDirection.y;
            }

            // SEGUNDA PARTE APONTAR PRO LADO CONTRARIO:
            Vector direcaoContrariaDockingPortAlvo = new Vector(targetDockingPort.direction(orbitalRefVessel))
                    .multiply(-1);
            control.setSAS(false);
            control.setRCS(false);
            activeVessel.getAutoPilot().engage();
            activeVessel.getAutoPilot().setReferenceFrame(orbitalRefVessel);
            activeVessel.getAutoPilot().setTargetDirection(direcaoContrariaDockingPortAlvo.toTriplet());
            activeVessel.getAutoPilot().setTargetRoll(90);
            activeVessel.getAutoPilot().wait_();
            activeVessel.getAutoPilot().disengage();
            control.setSAS(true);
            control.setSASMode(SASMode.STABILITY_ASSIST);

            Thread.sleep(1000);
            control.setRCS(true);
            positionMyDockingPort = new Vector(myDockingPort.position(vesselRefFrame));
            positionTargetDockingPort = new Vector(targetDockingPort.position(vesselRefFrame));
            Vector distanceBetweenPorts = positionMyDockingPort.subtract(positionTargetDockingPort);

            loopTimeInSeconds = 0;
            double lastXTargetPos = distanceBetweenPorts.x;
            lastYTargetPos = distanceBetweenPorts.y;
            double lastZTargetPos = distanceBetweenPorts.z;
            sleepTime = 50;
            while (true) {
                // Buscar posições atuais:
                positionMyDockingPort = new Vector(myDockingPort.position(vesselRefFrame));
                positionTargetDockingPort = new Vector(targetDockingPort.position(vesselRefFrame));
                distanceBetweenPorts = positionMyDockingPort.subtract(positionTargetDockingPort);

                // Calcular velocidade de cada eixo:
                double currentXAxisSpeed = (distanceBetweenPorts.x - lastXTargetPos) * sleepTime;
                double currentYAxisSpeed = (distanceBetweenPorts.y - lastYTargetPos) * sleepTime;
                double currentZAxisSpeed = (distanceBetweenPorts.z - lastZTargetPos) * sleepTime;

                // Calcular o valor PID de cada eixo:
                float sidewaysError = (float) RCSSidewaysCtrl.calculate(distanceBetweenPorts.x, 0);
                float upwardsError = (float) RCSUpwardsCtrl.calculate(distanceBetweenPorts.z, 0);
                float forwardsError = (float) RCSForwardsCtrl.calculate(distanceBetweenPorts.y, 0);
                float speedError = (float) speedCtrl.calculate(currentYAxisSpeed / 10, 0);

                boolean shouldGetCloser = Math.abs(distanceBetweenPorts.x) < 0.3
                        && Math.abs(distanceBetweenPorts.z) < 0.3;

                if (shouldGetCloser) {
                    control.setForward(forwardsError);
                    control.setRight(0);
                    control.setUp(0);
                } else {
                    control.setForward(speedError);
                    control.setRight(sidewaysError);
                    control.setUp(-upwardsError);
                }

                // Guardando últimas posições:
                lastXTargetPos = distanceBetweenPorts.x;
                lastYTargetPos = distanceBetweenPorts.y;
                lastZTargetPos = distanceBetweenPorts.z;

                adjustPID(sleepTime);
                updateLines(positionMyDockingPort, positionTargetDockingPort);
                loopTimeInSeconds += 0.05;
                Thread.sleep(sleepTime);
            }
            // // Close the connection when finished
            // connection.close();
        } catch (RPCException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // distancia x5 , p: 0.05, i: 0.0001, d: p x2
    private static void adjustPID(double timeSample) {
        double ms = 1000 / timeSample;
        double pGain = 4 / ms;
        double iGain = pGain / 500;
        double dGain = pGain * 2;
        speedCtrl.setTimeSample(timeSample);
        speedCtrl.setPIDValues(pGain, iGain, dGain);
        RCSUpwardsCtrl.setTimeSample(timeSample);
        RCSUpwardsCtrl.setPIDValues(pGain, iGain, dGain);
        RCSSidewaysCtrl.setTimeSample(timeSample);
        RCSSidewaysCtrl.setPIDValues(pGain, iGain, dGain);
        RCSForwardsCtrl.setTimeSample(timeSample);
        RCSForwardsCtrl.setPIDValues(pGain, iGain, dGain);
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

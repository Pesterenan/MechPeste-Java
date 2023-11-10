package com.pesterenan;

import java.io.IOException;

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
    private static Control control;

    private static Vessel activeVessel;
    private static Vessel targetVessel;

    private static ReferenceFrame orbitalRefVessel;
    private static ReferenceFrame vesselRefFrame;
    private static ReferenceFrame orbitalRefBody;
    private static final double SPEED_LIMIT = 3.0;
    private static final double DISTANCE_LIMIT = 25.0;
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

            Vector targetDirection = new Vector(activeVessel.position(orbitalRefVessel))
                    .subtract(new Vector(targetVessel.position(orbitalRefVessel))).multiply(-1);
            activeVessel.getAutoPilot().setReferenceFrame(orbitalRefVessel);
            activeVessel.getAutoPilot().setTargetDirection(targetDirection.toTriplet());
            activeVessel.getAutoPilot().setTargetRoll(90);
            activeVessel.getAutoPilot().engage();
            // Fazer a nave apontar usando o piloto automático, na marra
            while (Math.abs(activeVessel.getAutoPilot().getError()) > 1) {
                activeVessel.getAutoPilot().wait_();
            }
            control.setRCS(true);
            activeVessel.getAutoPilot().disengage();
            control.setSAS(true);
            control.setSASMode(SASMode.STABILITY_ASSIST);

            // PRIMEIRA PARTE DO DOCKING: APROXIMAÇÃO

            Vector targetPosition = new Vector(targetVessel.position(vesselRefFrame));
            double lastXTargetPos = targetPosition.x;
            double lastYTargetPos = targetPosition.y;
            double lastZTargetPos = targetPosition.z;
            long sleepTime = 25;
            double currentXAxisSpeed = 0;
            double currentYAxisSpeed = 0;
            double currentZAxisSpeed = 0;
            while (Math.abs(lastYTargetPos) >= DISTANCE_LIMIT) {
                targetPosition = new Vector(targetVessel.position(vesselRefFrame));

                // Atualizar posições para linhas
                positionMyDockingPort = new Vector(myDockingPort.position(vesselRefFrame));
                positionTargetDockingPort = new Vector(targetDockingPort.position(vesselRefFrame));
                updateLines(positionMyDockingPort, positionTargetDockingPort);

                // Calcular velocidade de cada eixo:
                currentXAxisSpeed = (targetPosition.x - lastXTargetPos) * sleepTime;
                currentYAxisSpeed = (targetPosition.y - lastYTargetPos) * sleepTime;
                currentZAxisSpeed = (targetPosition.z - lastZTargetPos) * sleepTime;

                // Calcular a aceleração para cada eixo no RCS:
                float forwardsError = calculateThrottle(DISTANCE_LIMIT, DISTANCE_LIMIT * 2, currentYAxisSpeed,
                        targetPosition.y, SPEED_LIMIT);
                float sidewaysError = calculateThrottle(0, 10, currentXAxisSpeed, targetPosition.x, SPEED_LIMIT);
                float upwardsError = calculateThrottle(0, 10, currentZAxisSpeed, targetPosition.z, SPEED_LIMIT);
                control.setForward((float) forwardsError);
                control.setRight((float) sidewaysError);
                control.setUp((float) -upwardsError);

                // Guardar últimas posições:
                lastXTargetPos = targetPosition.x;
                lastYTargetPos = targetPosition.y;
                lastZTargetPos = targetPosition.z;
                Thread.sleep(sleepTime);
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
            while (Math.abs(activeVessel.getAutoPilot().getError()) > 1) {
                activeVessel.getAutoPilot().wait_();
            }
            activeVessel.getAutoPilot().disengage();
            control.setSAS(true);
            control.setSASMode(SASMode.STABILITY_ASSIST);
            Thread.sleep(1000);
            control.setRCS(true);

            while (targetVessel != null) {
                targetPosition = new Vector(targetDockingPort.position(vesselRefFrame));

                // Atualizar posições para linhas
                positionMyDockingPort = new Vector(myDockingPort.position(vesselRefFrame));
                updateLines(positionMyDockingPort, targetPosition);

                // Calcular velocidade de cada eixo:
                currentXAxisSpeed = (targetPosition.x - lastXTargetPos) * sleepTime;
                currentYAxisSpeed = (targetPosition.y - lastYTargetPos) * sleepTime;
                currentZAxisSpeed = (targetPosition.z - lastZTargetPos) * sleepTime;

                // Calcular a aceleração para cada eixo no RCS:
                float forwardsError = calculateThrottle(5, 10, currentYAxisSpeed,
                        targetPosition.y, SPEED_LIMIT);
                float sidewaysError = calculateThrottle(-1, 10, currentXAxisSpeed, targetPosition.x, SPEED_LIMIT);
                float upwardsError = calculateThrottle(-1, 10, currentZAxisSpeed, targetPosition.z, SPEED_LIMIT);
                control.setForward((float) forwardsError);
                control.setRight((float) sidewaysError);
                control.setUp((float) -upwardsError);

                // Guardar últimas posições:
                lastXTargetPos = targetPosition.x;
                lastYTargetPos = targetPosition.y;
                lastZTargetPos = targetPosition.z;
                Thread.sleep(sleepTime);
            }
            // // Close the connection when finished
            // connection.close();
        } catch (RPCException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static float calculateThrottle(double minDistance, double maxDistance, double currentSpeed,
            double currentPosition, double speedLimit) {
        double limiter = Utilities.remap(minDistance, maxDistance, 0, 1, Math.abs(currentPosition), true);
        double change = (Utilities.remap(-speedLimit, speedLimit, -1.0, 1.0,
                currentSpeed + (Math.signum(currentPosition) * (limiter * speedLimit)), true));
        return (float) change;
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

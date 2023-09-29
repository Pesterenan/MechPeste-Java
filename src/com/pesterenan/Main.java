package com.pesterenan;

import java.io.IOException;

import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Vector;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.Drawing;
import krpc.client.services.Drawing.Line;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.DockingPort;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;

public class Main {
    private static Connection connection;
    private static SpaceCenter spaceCenter;
    private static Drawing drawing;
    private static ControlePID ctrlRCS;

    public static void main(String[] args) {
        try {
            // Connect to the KSP instance
            connection = Connection.newInstance();
            spaceCenter = SpaceCenter.newInstance(connection);
            drawing = Drawing.newInstance(connection);
            ctrlRCS = new ControlePID();
            ctrlRCS.adjustOutput(-1, 1);

            Vessel minhaNave = spaceCenter.getActiveVessel();
            minhaNave.getAutoPilot().setTimeToPeak(new Vector(3, 3, 3).toTriplet());
            minhaNave.getAutoPilot().setDecelerationTime(new Vector(4, 4, 4).toTriplet());
            ReferenceFrame referenciaOrbital = minhaNave.getOrbit().getBody().getReferenceFrame();
            // Get the target vessel and its orbit
            Vessel targetVessel = spaceCenter.getTargetVessel();

            DockingPort minhaDockingPort = minhaNave.getParts().getDockingPorts().get(0);
            DockingPort dockingPortAlvo = targetVessel.getParts().getDockingPorts().get(0);

            Line linhaDistancia = drawing.addLine(minhaDockingPort.position(referenciaOrbital),
                    dockingPortAlvo.position(referenciaOrbital), referenciaOrbital, true);

            minhaNave.getAutoPilot().setReferenceFrame(referenciaOrbital);
            minhaNave.getAutoPilot().setTargetRoll(0);
            minhaNave.getAutoPilot().engage();
            double tempoDoLoop = 0;
            while (tempoDoLoop < 10) {
                // Fase 1: Apontar pra dockingport alvo de longe:
                Vector posicaoDockingPortAlvo = new Vector(dockingPortAlvo.position(referenciaOrbital));
                Vector posicaoMinhaDockingPort = new Vector(minhaDockingPort.position(referenciaOrbital));

                // Calcular distancia:
                Vector distanciaEntrePortas = posicaoDockingPortAlvo.subtract(posicaoMinhaDockingPort);
                double distanciaEntrePortasEmMetros = distanciaEntrePortas.magnitude();

                // Aproximar-se a 100 metros do alvo pra terminar a primeira fase:
                double VEL_LIMIT = 2.0; // 1m/s²
                Vector vetorVelocidadeMinhaNave = new Vector(minhaNave.velocity(
                        dockingPortAlvo.getReferenceFrame()));
                double velocidadeAtualDaNave = vetorVelocidadeMinhaNave.magnitude();

                // System.out.println(velocidadeAtualDaNave);
                Vector direcaoMinhaNave = new Vector(minhaNave.direction(referenciaOrbital));
                Vector direcaoParaOAlvo = posicaoDockingPortAlvo.subtract(posicaoMinhaDockingPort).normalize();
                if (direcaoMinhaNave.heading() - direcaoParaOAlvo.heading() < 10) {
                    if (distanciaEntrePortasEmMetros > 50) {
                        minhaNave.getAutoPilot().setTargetDirection(
                                Vector.targetDirection(posicaoMinhaDockingPort, posicaoDockingPortAlvo).toTriplet());
                        minhaNave.getControl().setForward(
                                (float) ctrlRCS.calcPID((velocidadeAtualDaNave / VEL_LIMIT) * 100, 100));
                    } else {
                        fase2docking(minhaNave, dockingPortAlvo, referenciaOrbital);
                    }
                }

                // Atualizando a linha entre as portas:
                linhaDistancia.setStart(posicaoMinhaDockingPort.toTriplet());
                linhaDistancia.setEnd(posicaoDockingPortAlvo.toTriplet());

                Thread.sleep(100);
                tempoDoLoop += 0.1;
                System.out.println(distanciaEntrePortasEmMetros + " metros");
            }
            minhaNave.getAutoPilot().disengage();
            // // Close the connection when finished
            connection.close();
        } catch (IOException | RPCException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void fase2docking(Vessel minhaNave, DockingPort dockingPortAlvo, ReferenceFrame referenciaOrbital) {
        Vector direcaoApontandoDockingAlvo;
        try {
            direcaoApontandoDockingAlvo = new Vector(dockingPortAlvo.direction(referenciaOrbital));
            direcaoApontandoDockingAlvo.x = direcaoApontandoDockingAlvo.x * -1;
            direcaoApontandoDockingAlvo.z = direcaoApontandoDockingAlvo.z * -1;
            System.out.println(direcaoApontandoDockingAlvo);

            minhaNave.getAutoPilot().setTargetDirection(
                    direcaoApontandoDockingAlvo.toTriplet());
        } catch (RPCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

package com.pesterenan.controllers;

import java.util.List;

import org.javatuples.Triplet;

import com.pesterenan.utils.Modules;
import com.pesterenan.utils.Status;
import com.pesterenan.utils.Utilities;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusJPanel;

import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.VesselSituation;

public class ManeuverController extends FlightController {

	private Node maneuverNode;
	private String function;

	public ManeuverController(String function) {
		super(getConnection());
		this.function = function;
		
	}

	@Override
	public void run() {
		calculateManeuver();
		executeNextManeuver();
	}

	public void calculateManeuver() {
		try {
			if (currentShip.getSituation() == VesselSituation.LANDED
					|| currentShip.getSituation() == VesselSituation.SPLASHED) {
				throw new InterruptedException();
			}
			if (this.function.equals(Modules.EXECUTE.get()))
				return;
			double gravitationalParameter = currentShip.getOrbit().getBody().getGravitationalParameter();
			double initialHeight = 0, timeUnltilHeight = 0;
			if (this.function.equals(Modules.APOAPSIS.get())) {
				initialHeight = currentShip.getOrbit().getApoapsis();
				timeUnltilHeight = currentShip.getOrbit().getTimeToApoapsis();
			}
			if (this.function.equals(Modules.PERIAPSIS.get())) {
				initialHeight = currentShip.getOrbit().getPeriapsis();
				timeUnltilHeight = currentShip.getOrbit().getTimeToPeriapsis();
			}

			double semiMajorAxis = currentShip.getOrbit().getSemiMajorAxis();
			double currentOrbitalSpeed = Math
					.sqrt(gravitationalParameter * ((2.0 / initialHeight) - (1.0 / semiMajorAxis)));
			double targetOrbitalSpeed = Math
					.sqrt(gravitationalParameter * ((2.0 / initialHeight) - (1.0 / initialHeight)));
			double maneuverDeltaV = targetOrbitalSpeed - currentOrbitalSpeed;
			double[] deltaV = { maneuverDeltaV, 0, 0 };
			createManeuver(timeUnltilHeight, deltaV);
		} catch (RPCException | InterruptedException e) {
			StatusJPanel.setStatus("Não foi possível calcular a manobra.");
			return;
		}
	}

	private void createManeuver(double posteriorTime, double[] deltaV) {
		try {
			currentShip.getControl().addNode(spaceCenter.getUT() + posteriorTime, (float) deltaV[0],
					(float) deltaV[1], (float) deltaV[2]);
		} catch (UnsupportedOperationException | RPCException e) {
			StatusJPanel.setStatus("Não foi possível criar a manobra.");
			return;
		}
	}

	public void executeNextManeuver() {
		try {
			StatusJPanel.setStatus("Buscando Manobras...");
			maneuverNode = currentShip.getControl().getNodes().get(0);

			double burnTime = calculateBurnTime(maneuverNode);
			orientToManeuverNode(maneuverNode);
			executeBurn(maneuverNode, burnTime);
		} catch (UnsupportedOperationException e) {
			StatusJPanel.setStatus("A criação de Manobras não foi desbloqueada ainda.");
			return;
		} catch (IndexOutOfBoundsException e) {
			StatusJPanel.setStatus("Não há Manobras disponíveis.");
			return;
		} catch (RPCException e) {
			StatusJPanel.setStatus("Não foi possivel buscar dados da nave.");
			return;
		}
	}

	public void orientToManeuverNode(Node maneuverNode) {
		try {
			float roll = currentShip.getAutoPilot().getTargetRoll();
			currentShip.getAutoPilot().setReferenceFrame(maneuverNode.getReferenceFrame());
			currentShip.getAutoPilot().setTargetDirection(new Triplet<Double, Double, Double>(0.0, 1.0, 0.0));
			currentShip.getAutoPilot().setTargetRoll((roll + 180));
			currentShip.getAutoPilot().engage();
			while (currentShip.getAutoPilot().getError() > 5) {
				StatusJPanel.setStatus("Orientando nave para o nó de Manobra...");
				Thread.sleep(250);				
			}
		} catch (InterruptedException | RPCException e) {
			try {
				currentShip.getAutoPilot().setReferenceFrame(surfaceRefSpot);
				currentShip.getAutoPilot().disengage();
			} catch (RPCException e1) {
			}
			System.err.println("Não foi possível orientar a nave para a manobra:\n\t" + e.getMessage());
		}
	}

	public double calculateBurnTime(Node maneuverNode) throws RPCException {

		List<Engine> engines = currentShip.getParts().getEngines();
		for (Engine engine : engines) {
			if (engine.getPart().getStage() == currentShip.getControl().getCurrentStage() && engine.getActive() == false) {
				engine.setActive(true);
			}
		}
		double thrust = currentShip.getAvailableThrust();
		double isp = currentShip.getSpecificImpulse() * CONST_GRAV;
		double totalMass = currentShip.getMass();
		double dryMass = totalMass / Math.exp(maneuverNode.getDeltaV() / isp);
		double burnRate = thrust / isp;
		double burnTime = (totalMass - dryMass) / burnRate;

		StatusJPanel.setStatus("Tempo de Queima da Manobra: " + burnTime + " segundos");
		return burnTime;
	}

	public void executeBurn(Node maneuverNode, double burnTime) throws RPCException {
		try {
			double burnStart = maneuverNode.getTimeTo() - (burnTime / 2.0);
			StatusJPanel.setStatus("Warp temporal para próxima manobra...");
			if (burnStart > 30) {
				spaceCenter.warpTo((spaceCenter.getUT() + burnStart - 10), 100000, 4);
			}
			// Mostrar tempo de ignição:
			StatusJPanel.setStatus("Duração da queima: " + burnTime + " segundos.");
			while (burnStart > 0) {
				burnStart = maneuverNode.getTimeTo() - (burnTime / 2.0);
				burnStart = burnStart > 0.0 ? burnStart : 0.0;
				StatusJPanel.setStatus(String.format("Ignição em: %1$.1f segundos...", burnStart));
				Thread.sleep(100);
			}
			// Executar a manobra:
			Stream<Triplet<Double, Double, Double>> remainingBurn = getConnection().addStream(maneuverNode,
					"remainingBurnVector", maneuverNode.getReferenceFrame());
			StatusJPanel.setStatus("Executando manobra!");
			double slowDownLimit = maneuverNode.getDeltaV() > 1000 ? 0.025
					: maneuverNode.getDeltaV() > 250 ? 0.10 : 0.25;

			while (!maneuverNode.equals(null)) {
				if (remainingBurn.get().getValue1() > 0.5) {
					throttle(Utilities.remap(maneuverNode.getDeltaV() * slowDownLimit, 0, 1, 0.1,
							remainingBurn.get().getValue1()));
				} else {
					remainingBurn.remove();
					break;
				}
				MainGui.getParameters().getComponent(0).firePropertyChange("distancia", 0,
						remainingBurn.get().getValue1());
				Thread.sleep(25);
			}
			throttle(0.0f);
			currentShip.getAutoPilot().setReferenceFrame(surfaceRefSpot);
			currentShip.getAutoPilot().disengage();
			currentShip.getControl().setSAS(true);
			currentShip.getControl().setRCS(false);
			maneuverNode.remove();
			StatusJPanel.setStatus(Status.READY.get());
		} catch (StreamException | RPCException e) {
			throttle(0.0f);
			currentShip.getAutoPilot().disengage();
			StatusJPanel.setStatus("Não foi possivel buscar os dados da nave.");
		} catch (InterruptedException e) {
			throttle(0.0f);
			currentShip.getAutoPilot().disengage();
			StatusJPanel.setStatus("Manobra cancelada.");
		}
	}

	public void setFunction(String function) {
		this.function = function;
	}

}

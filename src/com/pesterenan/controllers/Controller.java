package com.pesterenan.controllers;

import com.pesterenan.model.ActiveVessel;
import krpc.client.RPCException;
import krpc.client.StreamException;

public class Controller implements Runnable {
	protected ActiveVessel activeVessel;

	public Controller(ActiveVessel activeVessel) {
		this.activeVessel = activeVessel;
		System.out.println(activeVessel.commands + "COMMANDS CONTROLLER");
		iniciarStreams();
	}

	private void iniciarStreams() {
		try {
			activeVessel.pontoRefOrbital = activeVessel.getNaveAtual().getOrbit().getBody().getReferenceFrame();
			activeVessel.pontoRefSuperficie = activeVessel.getNaveAtual().getSurfaceReferenceFrame();
			activeVessel.parametrosDeVoo = activeVessel.getNaveAtual().flight(activeVessel.pontoRefOrbital);
			activeVessel.altitude =
					activeVessel.getConnection().addStream(activeVessel.parametrosDeVoo, "getMeanAltitude");
			activeVessel.altitudeSup =
					activeVessel.getConnection().addStream(activeVessel.parametrosDeVoo, "getSurfaceAltitude");
			activeVessel.apoastro = activeVessel.getConnection()
			                                    .addStream(activeVessel.getNaveAtual().getOrbit(),
			                                               "getApoapsisAltitude"
			                                              );
			activeVessel.periastro = activeVessel.getConnection()
			                                     .addStream(activeVessel.getNaveAtual().getOrbit(),
			                                                "getPeriapsisAltitude"
			                                               );
			activeVessel.velVertical =
					activeVessel.getConnection().addStream(activeVessel.parametrosDeVoo, "getVerticalSpeed");
			activeVessel.velHorizontal =
					activeVessel.getConnection().addStream(activeVessel.parametrosDeVoo, "getHorizontalSpeed");
			activeVessel.tempoMissao = activeVessel.getConnection().addStream(activeVessel.getNaveAtual(), "getMET");
			activeVessel.bateriaTotal = activeVessel.getNaveAtual().getResources().max("ElectricCharge");
			activeVessel.ap.setReferenceFrame(activeVessel.pontoRefSuperficie);
			activeVessel.gravityAcel = activeVessel.getNaveAtual().getOrbit().getBody().getSurfaceGravity();
		} catch (StreamException | RPCException | NullPointerException | IllegalArgumentException e) {
			activeVessel.checarConexao();
		}
	}

	@Override
	public void run() {
	}
}

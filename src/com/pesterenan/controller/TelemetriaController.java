package com.pesterenan.controller;

import com.pesterenan.MechPeste;
import com.pesterenan.gui.MainGui;
import com.pesterenan.model.Nave;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;

public class TelemetriaController extends Nave implements Runnable {

	public TelemetriaController(Connection con) {
		super(con);
		try {
			parametrosDeVoo = this.naveAtual.flight(this.naveAtual.getOrbit().getBody().getReferenceFrame());
			altitude = getConexao().addStream(parametrosDeVoo, "getMeanAltitude");
			altitudeSup = getConexao().addStream(parametrosDeVoo, "getSurfaceAltitude");
			apoastro = getConexao().addStream(naveAtual.getOrbit(), "getApoapsisAltitude");
			periastro = getConexao().addStream(naveAtual.getOrbit(), "getPeriapsisAltitude");
			velVertical = getConexao().addStream(parametrosDeVoo, "getVerticalSpeed");
			velHorizontal = getConexao().addStream(parametrosDeVoo, "getHorizontalSpeed");
			massaTotal = getConexao().addStream(naveAtual, "getMass");
			tempoMissao = getConexao().addStream(SpaceCenter.class, "getUT");
		} catch (StreamException | RPCException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		try {
			MainGui.getParametros().firePropertyChange("altitude", 0.0, altitude.get());
		} catch (RPCException | StreamException e) {
			e.printStackTrace();
		}
	}

}

package com.pesterenan.controllers;

import com.pesterenan.model.Nave;
import com.pesterenan.resources.Bundle;
import com.pesterenan.views.MainGui;
import com.pesterenan.views.StatusJPanel;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Vessel;

public class FlightController extends Nave implements Runnable {

protected final static float CONST_GRAV = 9.81f;

public FlightController(Connection con) {
	super(con);
	iniciarStreams(this.naveAtual);
}

private void iniciarStreams(Vessel naveAtual) {
	try {
		pontoRefOrbital = naveAtual.getOrbit().getBody().getReferenceFrame();
		pontoRefSuperficie = naveAtual.getSurfaceReferenceFrame();
		parametrosDeVoo = naveAtual.flight(pontoRefOrbital);
		altitude = getConexao().addStream(parametrosDeVoo, "getMeanAltitude");
		altitudeSup = getConexao().addStream(parametrosDeVoo, "getSurfaceAltitude");
		apoastro = getConexao().addStream(naveAtual.getOrbit(), "getApoapsisAltitude");
		periastro = getConexao().addStream(naveAtual.getOrbit(), "getPeriapsisAltitude");
		velVertical = getConexao().addStream(parametrosDeVoo, "getVerticalSpeed");
		velHorizontal = getConexao().addStream(parametrosDeVoo, "getHorizontalSpeed");
		massaTotal = getConexao().addStream(naveAtual, "getMass");
		tempoMissao = getConexao().addStream(naveAtual, "getMET");
		bateriaAtual = getConexao().addStream(naveAtual.getResources(), "amount", "ElectricCharge");
		bateriaTotal = naveAtual.getResources().max("ElectricCharge");
		gravityAcel = naveAtual.getOrbit().getBody().getSurfaceGravity();
		celestialBody = naveAtual.getOrbit().getBody().getName();
		naveAtual.getAutoPilot().setReferenceFrame(pontoRefSuperficie);
	} catch (StreamException | RPCException | NullPointerException | IllegalArgumentException e) {
		checarConexao();
	}
}

@Override
public void run() {
	while (! Thread.interrupted()) {
		try {
			trocaDeNaves();
			enviarTelemetria();
			Thread.sleep(250);
		} catch (InterruptedException | RPCException | StreamException | NullPointerException e) {
			checarConexao();
		}
	}
}

private void trocaDeNaves() {
	try {
		if (! centroEspacial.getActiveVessel().equals(this.naveAtual)) {
			this.naveAtual = centroEspacial.getActiveVessel();
			iniciarStreams(this.naveAtual);
		}
	} catch (RPCException e) {
		StatusJPanel.botConectarVisivel(true);
		StatusJPanel.setStatus(Bundle.getString("status_couldnt_switch_vessel"));
	}
}

private void enviarTelemetria() throws RPCException, StreamException {
	porcentagemCarga = (int) Math.ceil(bateriaAtual.get() * 100 / bateriaTotal);
	MainGui.getParametros().getTelemetria().firePropertyChange("altitude", 0.0, altitude.get());
	MainGui.getParametros().getTelemetria().firePropertyChange("altitudeSup", 0.0, altitudeSup.get());
	MainGui.getParametros().getTelemetria().firePropertyChange("apoastro", 0.0, apoastro.get());
	MainGui.getParametros().getTelemetria().firePropertyChange("periastro", 0.0, periastro.get());
	MainGui.getParametros().getTelemetria().firePropertyChange("velVertical", 0.0, velVertical.get());
	MainGui.getParametros().getTelemetria().firePropertyChange("velHorizontal", 0.0, velHorizontal.get());
	MainGui.getParametros().getTelemetria().firePropertyChange("bateria", 0.0, porcentagemCarga);
	MainGui.getParametros().getTelemetria().firePropertyChange("tempoMissao", 0.0, tempoMissao.get());

}

protected void disengageAfterException(String statusMessage) {
	try {
		StatusJPanel.setStatus(statusMessage);
		naveAtual.getAutoPilot().setReferenceFrame(pontoRefSuperficie);
		naveAtual.getAutoPilot().disengage();
		throttle(0);
		Thread.sleep(3000);
		StatusJPanel.setStatus(Bundle.getString("status_ready"));
	} catch (Exception ignored) {
	}
}
}

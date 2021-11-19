package com.pesterenan.controller;

import com.pesterenan.gui.MainGui;
import com.pesterenan.model.Nave;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

public class TelemetriaController extends Nave implements Runnable {

	public TelemetriaController(Connection con) {
		super(con);
		try {
			parametrosDeVoo = naveAtual.flight(naveAtual.getOrbit().getBody().getReferenceFrame());
			altitude = getConexao().addStream(parametrosDeVoo, "getMeanAltitude");
			altitudeSup = getConexao().addStream(parametrosDeVoo, "getSurfaceAltitude");
			apoastro = getConexao().addStream(naveAtual.getOrbit(), "getApoapsisAltitude");
			periastro = getConexao().addStream(naveAtual.getOrbit(), "getPeriapsisAltitude");
			velVertical = getConexao().addStream(parametrosDeVoo, "getVerticalSpeed");
			velHorizontal = getConexao().addStream(parametrosDeVoo, "getHorizontalSpeed");
			massaTotal = getConexao().addStream(naveAtual, "getMass");
			tempoMissao = getConexao().addStream(naveAtual, "getMET");
			bateriaTotal = naveAtual.getResources().max("ElectricCharge");
			bateriaAtual = naveAtual.getResources().amount("ElectricCharge");
			porcentagemCarga = (int) Math.ceil(bateriaAtual * 100 / bateriaTotal);
		} catch (StreamException | RPCException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		while (!getConexao().equals(null)) {
			try {
				if (!naveAtual.equals(null)) {
					enviarTelemetria();
					Thread.sleep(100);
				} else {
					System.out.println("Aguardando reconexão...");
					Thread.sleep(5000);
				}
			} catch (InterruptedException | RPCException | StreamException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	private void enviarTelemetria() throws RPCException, StreamException {
		MainGui.getParametros().getComponent(0).firePropertyChange("altitude", 0.0, altitude.get());
		MainGui.getParametros().getComponent(0).firePropertyChange("altitudeSup", 0.0, altitudeSup.get());
		MainGui.getParametros().getComponent(0).firePropertyChange("apoastro", 0.0, apoastro.get());
		MainGui.getParametros().getComponent(0).firePropertyChange("periastro", 0.0, periastro.get());
		MainGui.getParametros().getComponent(0).firePropertyChange("velVertical", 0.0, velVertical.get());
		MainGui.getParametros().getComponent(0).firePropertyChange("velHorizontal", 0.0, velHorizontal.get());
		MainGui.getParametros().getComponent(0).firePropertyChange("bateria", 0.0, porcentagemCarga);
		MainGui.getParametros().getComponent(0).firePropertyChange("tempoMissao", 0.0, tempoMissao.get());
	}

}

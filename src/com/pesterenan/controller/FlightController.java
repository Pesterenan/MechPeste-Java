package com.pesterenan.controller;

import com.pesterenan.model.Nave;
import com.pesterenan.view.MainGui;
import com.pesterenan.view.StatusJPanel;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.VesselSituation;

public class FlightController extends Nave implements Runnable {

	final float MAX_TEP = 5.0f;
	protected final static float CONST_GRAV = 9.81f;
	

	public FlightController(Connection con) {
		super(con);
		iniciarStreams();
	}

	private void iniciarStreams() {
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
			acelGravidade = naveAtual.getOrbit().getBody().getSurfaceGravity();
			corpoCeleste = naveAtual.getOrbit().getBody().getName();
		} catch (StreamException | RPCException | NullPointerException | IllegalArgumentException e) {
			checarConexao();
		}
	}

	@Override
	public void run() {
		while (!getConexao().equals(null)) {
			try {
				enviarTelemetria();
				Thread.sleep(100);
			} catch (InterruptedException e) {
			} catch (RPCException | StreamException | NullPointerException e) {
				checarConexao();
				iniciarStreams();
			}
		}
	}

	private void enviarTelemetria() throws RPCException, StreamException {
		porcentagemCarga = (int) Math.ceil(bateriaAtual.get() * 100 / bateriaTotal);
		MainGui.getParametros().getComponent(0).firePropertyChange("altitude", 0.0, altitude.get());
		MainGui.getParametros().getComponent(0).firePropertyChange("altitudeSup", 0.0, altitudeSup.get());
		MainGui.getParametros().getComponent(0).firePropertyChange("apoastro", 0.0, apoastro.get());
		MainGui.getParametros().getComponent(0).firePropertyChange("periastro", 0.0, periastro.get());
		MainGui.getParametros().getComponent(0).firePropertyChange("velVertical", 0.0, velVertical.get());
		MainGui.getParametros().getComponent(0).firePropertyChange("velHorizontal", 0.0, velHorizontal.get());
		MainGui.getParametros().getComponent(0).firePropertyChange("bateria", 0.0, porcentagemCarga);
		MainGui.getParametros().getComponent(0).firePropertyChange("tempoMissao", 0.0, tempoMissao.get());

	}

	protected void acelerar(float acel) throws RPCException {
		naveAtual.getControl().setThrottle(acel);
	}

	protected void acelerar(double acel) throws RPCException {
		acelerar((float) acel);
	}

	protected void decolar() throws InterruptedException {
		try {
			naveAtual.getControl().setSAS(true);
			acelerar(1f);
			if (naveAtual.getSituation().equals(VesselSituation.PRE_LAUNCH)) {
				float contagemRegressiva = 5f;
				while (contagemRegressiva > 0) {
					StatusJPanel.setStatus(String.format("Lançamento em: %.1f segundos...", contagemRegressiva));
					contagemRegressiva -= 0.1;
					Thread.sleep(100);
				}
				naveAtual.getControl().activateNextStage();
			}
			StatusJPanel.setStatus("Decolagem!");
		} catch (RPCException erro) {
			System.err.println("Não foi possivel decolar a nave. Erro: " + erro.getMessage());
		}
	}

	protected double calcularTEP() throws RPCException, StreamException {
		float valorTEP = naveAtual.getAvailableThrust() / ((massaTotal.get() * acelGravidade));
		return valorTEP > MAX_TEP ? MAX_TEP : valorTEP;
	}

	protected double calcularAcelMaxima() throws RPCException, StreamException {
		return calcularTEP() * acelGravidade - acelGravidade;
	}
}

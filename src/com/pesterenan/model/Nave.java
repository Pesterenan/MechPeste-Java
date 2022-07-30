package com.pesterenan.model;

import com.pesterenan.resources.Bundle;
import com.pesterenan.views.StatusJPanel;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.KRPC;
import krpc.client.services.KRPC.GameScene;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.*;

import static com.pesterenan.views.StatusJPanel.botConectarVisivel;
import static com.pesterenan.views.StatusJPanel.setStatus;

public class Nave {
private static Connection conexao;

protected static SpaceCenter centroEspacial;
protected Vessel naveAtual;
protected AutoPilot ap;
protected Flight parametrosDeVoo;
protected ReferenceFrame pontoRefOrbital;
protected ReferenceFrame pontoRefSuperficie;
protected Stream<Double> altitude, altitudeSup, apoastro, periastro;
protected Stream<Double> velVertical, tempoMissao, velHorizontal;
protected Stream<Float> massaTotal, bateriaAtual;
protected float bateriaTotal, gravityAcel;
protected String celestialBody;
protected int porcentagemCarga;

public Nave(Connection con) {
	setConexao(con);
	try {
		centroEspacial = SpaceCenter.newInstance(getConexao());
		this.naveAtual = centroEspacial.getActiveVessel();
		this.ap = naveAtual.getAutoPilot();
	} catch (RPCException | NullPointerException e) {
		StatusJPanel.setStatus("Erro vindo da classe Nave!");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
		}
		checarConexao();
	}
}

protected void checarConexao() {
	try {
		KRPC krpc = KRPC.newInstance(getConexao());
		if (krpc.getCurrentGameScene().equals(GameScene.FLIGHT)) {
			this.naveAtual = centroEspacial.getActiveVessel();
			setStatus(Bundle.getString("status_connected"));
			botConectarVisivel(false);
		} else {
			throw new RPCException("");
		}
	} catch (RPCException | NullPointerException e) {
		setStatus(Bundle.getString("status_error_connection"));
		StatusJPanel.setStatus("Erro vindo da método checarConexao!");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
		}
		botConectarVisivel(true);
	}
}

public static Connection getConexao() {
	return conexao;
}

private void setConexao(Connection con) {
	conexao = con;
}

protected void throttle(float acel) throws RPCException {
	naveAtual.getControl().setThrottle(acel);
}

protected void throttle(double acel) throws RPCException {
	throttle((float) acel);
}

protected void liftoff() throws InterruptedException {
	try {
		naveAtual.getControl().setSAS(true);
		throttle(1f);
		if (naveAtual.getSituation().equals(VesselSituation.PRE_LAUNCH)) {
			float launchCount = 5f;
			while (launchCount > 0) {
				StatusJPanel.setStatus(String.format(Bundle.getString("status_launching_in"), launchCount));
				launchCount -= 0.1;
				Thread.sleep(100);
			}
			naveAtual.getControl().activateNextStage();
		}
		setStatus(Bundle.getString("status_liftoff"));
	} catch (RPCException erro) {
	}
}

protected double calcularTEP() throws RPCException, StreamException {
	return naveAtual.getAvailableThrust() / ((massaTotal.get() * gravityAcel));
}

protected double calcularAcelMaxima() throws RPCException, StreamException {
	return calcularTEP() * gravityAcel - gravityAcel;
}
}

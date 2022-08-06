package com.pesterenan.model;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;
import com.pesterenan.views.StatusJPanel;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.KRPC.GameScene;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.*;

import static com.pesterenan.views.StatusJPanel.isBtnConnectVisible;
import static com.pesterenan.views.StatusJPanel.setStatus;

public class Nave {
protected static SpaceCenter centroEspacial;
private static Connection conexao;
protected Vessel naveAtual;
protected AutoPilot ap;
protected Flight parametrosDeVoo;
protected ReferenceFrame pontoRefOrbital, pontoRefSuperficie;
protected Stream<Double> altitude, altitudeSup, apoastro, periastro, velVertical, tempoMissao, velHorizontal;
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

public static Connection getConexao() {
	return conexao;
}

private void setConexao(Connection con) {
	conexao = con;
}

protected void checarConexao() {
	try {
		if (MechPeste.getCurrentGameScene().equals(GameScene.FLIGHT)) {
			this.naveAtual = centroEspacial.getActiveVessel();
			setStatus(Bundle.getString("status_connected"));
			isBtnConnectVisible(false);
		}
		else {
			setStatus(Bundle.getString("status_ready"));
		}
	} catch (RPCException | NullPointerException e) {
		setStatus(Bundle.getString("status_error_connection"));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
		}
		isBtnConnectVisible(true);
	}
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

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
import krpc.client.services.SpaceCenter.AutoPilot;
import krpc.client.services.SpaceCenter.CelestialBody;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;

import java.util.Map;

import static com.pesterenan.views.StatusJPanel.isBtnConnectVisible;
import static com.pesterenan.views.StatusJPanel.setStatus;

public class ActiveVessel {

	protected final static float CONST_GRAV = 9.81f;

	protected static SpaceCenter centroEspacial;
	private static Connection conexao;
	protected Vessel naveAtual;
	protected AutoPilot ap;
	protected Flight parametrosDeVoo;
	protected ReferenceFrame pontoRefOrbital, pontoRefSuperficie;
	protected Stream<Float> massaTotal;
	protected float bateriaTotal, gravityAcel;
	protected CelestialBody currentBody;

	protected Map<String, String> commands;
	protected Stream<Double> altitude, altitudeSup, apoastro, periastro, velVertical, tempoMissao, velHorizontal;


	public ActiveVessel(Connection con) {
		setConexao(con);
		initializeParameters();
	}

	public static Connection getConexao() {
		return conexao;
	}

	private void setConexao(Connection con) {
		conexao = con;
	}

	private void initializeParameters() {
		try {
			centroEspacial = SpaceCenter.newInstance(getConexao());
			naveAtual = centroEspacial.getActiveVessel();
			ap = naveAtual.getAutoPilot();
			currentBody = naveAtual.getOrbit().getBody();
			pontoRefOrbital = currentBody.getReferenceFrame();
			pontoRefSuperficie = naveAtual.getSurfaceReferenceFrame();
			parametrosDeVoo = naveAtual.flight(pontoRefOrbital);
			massaTotal = getConexao().addStream(naveAtual, "getMass");
		} catch (RPCException | StreamException e) {
			checarConexao();
		}
	}

	protected void checarConexao() {
		try {
			if (MechPeste.getCurrentGameScene().equals(GameScene.FLIGHT)) {
				naveAtual = centroEspacial.getActiveVessel();
				setStatus(Bundle.getString("status_connected"));
				isBtnConnectVisible(false);
			} else {
				setStatus(Bundle.getString("status_ready"));
			}
		} catch (RPCException | NullPointerException e) {
			setStatus(Bundle.getString("status_error_connection"));
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
		} catch (RPCException ignored) {
		}
	}

	protected double getTWR() throws RPCException, StreamException {
		return naveAtual.getAvailableThrust() / ((massaTotal.get() * gravityAcel));
	}

	protected double getMaxAcel() throws RPCException, StreamException {
		return getTWR() * gravityAcel - gravityAcel;
	}

	protected void disengageAfterException(String statusMessage) {
		try {
			StatusJPanel.setStatus(statusMessage);
			ap.setReferenceFrame(pontoRefSuperficie);
			ap.disengage();
			throttle(0);
			Thread.sleep(3000);
			StatusJPanel.setStatus(Bundle.getString("status_ready"));
		} catch (Exception ignored) {
		}
	}
}

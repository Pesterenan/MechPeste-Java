package com.pesterenan.model;

import static com.pesterenan.views.StatusJPanel.botConectarVisivel;
import static com.pesterenan.views.StatusJPanel.setStatus;

import com.pesterenan.resources.Bundle;
import com.pesterenan.views.StatusJPanel;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.KRPC;
import krpc.client.services.KRPC.GameScene;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;

public class Nave {
	private static Connection conexao;

	protected static SpaceCenter centroEspacial;
	protected Vessel naveAtual;
	protected Flight parametrosDeVoo;
	protected ReferenceFrame pontoRefOrbital;
	protected ReferenceFrame pontoRefSuperficie;
	protected Stream<Double> altitude, altitudeSup, apoastro, periastro;
	protected Stream<Double> velVertical, tempoMissao, velHorizontal;
	protected Stream<Float> massaTotal, bateriaAtual;
	protected float bateriaTotal, acelGravidade;
	protected String corpoCeleste;
	protected int porcentagemCarga;

	public Nave(Connection con) {
		setConexao(con);
		try {
			centroEspacial = SpaceCenter.newInstance(getConexao());
			this.naveAtual = centroEspacial.getActiveVessel();
		} catch (RPCException | NullPointerException e) {
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
		return naveAtual.getAvailableThrust() / ((massaTotal.get() * acelGravidade));
	}

	protected double calcularAcelMaxima() throws RPCException, StreamException {
		return calcularTEP() * acelGravidade - acelGravidade;
	}
}

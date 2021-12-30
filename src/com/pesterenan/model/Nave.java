package com.pesterenan.model;

import static com.pesterenan.utils.Status.ERRO_CONEXAO;

import com.pesterenan.gui.StatusJPanel;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.services.KRPC;
import krpc.client.services.SpaceCenter;
import krpc.client.services.KRPC.GameScene;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Vessel;

public class Nave {
	private static Connection conexao;

	public final static float CONST_GRAV = 9.81f;
	protected static SpaceCenter centroEspacial;
	protected Vessel naveAtual;
	protected Flight parametrosDeVoo;

	protected Stream<Double> altitude, altitudeSup, apoastro, periastro;
	protected Stream<Double> velVertical, tempoMissao, velHorizontal;
	protected Stream<Float> massaTotal, bateriaAtual;
	protected float bateriaTotal;
	protected int porcentagemCarga;

	
	public Nave(Connection con) {
		setConexao(con);
		centroEspacial = SpaceCenter.newInstance(getConexao());
		try {
			this.naveAtual = centroEspacial.getActiveVessel();
		} catch (RPCException e) {
			System.err.println("Erro ao buscar Nave Atual: \n\t" + e.getMessage());
			checarConexao();
		}
	}
	
	protected void checarConexao() {
		KRPC krpc = KRPC.newInstance(getConexao());
		try {
			if (krpc.getCurrentGameScene().equals(GameScene.FLIGHT)) {
				this.naveAtual = centroEspacial.getActiveVessel();
			} else {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
			}
		} catch (RPCException e) {
			StatusJPanel.setStatus(ERRO_CONEXAO.get());
			StatusJPanel.botConectarVisivel(true);
		}
	}

	public static Connection getConexao() {
		return conexao;
	}

	private void setConexao(Connection con) {
		conexao = con;
	}
}

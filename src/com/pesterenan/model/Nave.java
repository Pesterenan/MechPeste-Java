package com.pesterenan.model;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.services.SpaceCenter;
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

	
	public Nave(Nave nave) {
		new Nave(nave.getConexao());
	}
	public Nave(Connection con) {
		setConexao(con);
		centroEspacial = SpaceCenter.newInstance(getConexao());
		try {
			this.naveAtual = centroEspacial.getActiveVessel();
		} catch (RPCException e) {
			System.err.println("Erro ao buscar Nave Atual: \n\t" + e.getMessage());
		}
	}

	public static Connection getConexao() {
		return conexao;
	}

	private void setConexao(Connection con) {
		conexao = con;

	}
}

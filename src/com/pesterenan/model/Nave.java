package com.pesterenan.model;

import java.io.IOException;

import com.pesterenan.controller.RoverAutonomoController;
import com.pesterenan.controller.DecolagemOrbitalController;
import com.pesterenan.controller.ManobrasController;
import com.pesterenan.controller.PousoAutomaticoController;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
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
	protected Stream<Float> massaTotal;
	protected float bateriaTotal, bateriaAtual;
	protected int porcentagemCarga;

	public Nave(Connection con) {
		if (!getConexao().equals(con)) {
			setConexao(con);			
			centroEspacial = SpaceCenter.newInstance(getConexao());
		}
		try {
			this.naveAtual = centroEspacial.getActiveVessel();
		} catch (RPCException e) {
			System.err.println("Erro\n\t\t" + e.getMessage());
		}
	}

	public static Connection getConexao() {
		return conexao;
	}

	private void setConexao(Connection con) {
		conexao = con;

	}
}

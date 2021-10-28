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

	protected static SpaceCenter centroEspacial;
	protected Vessel naveAtual;
	protected Flight parametrosDeVoo;
	public final static float CONST_GRAV = 9.81f;

	protected Stream<Double> altitude, altitudeSup, apoastro, periastro;
	protected Stream<Double> velVertical, tempoMissao, velHorizontal;
	protected Stream<Float> massaTotal;

	public Nave(Connection con) {
		try {
			setConexao(con);
			Nave.centroEspacial = SpaceCenter.newInstance(getConexao());
			this.naveAtual = centroEspacial.getActiveVessel();
			iniciarTelemetria();
		} catch (RPCException | StreamException e) {
			System.err.println("Erro\n\t\t" + e.getMessage());
		}
	}

	private void iniciarTelemetria() throws RPCException, StreamException {
		parametrosDeVoo = this.naveAtual.flight(this.naveAtual.getOrbit().getBody().getReferenceFrame());
		altitude = getConexao().addStream(parametrosDeVoo, "getMeanAltitude");
		altitudeSup = getConexao().addStream(parametrosDeVoo, "getSurfaceAltitude");
		apoastro = getConexao().addStream(naveAtual.getOrbit(), "getApoapsisAltitude");
		periastro = getConexao().addStream(naveAtual.getOrbit(), "getPeriapsisAltitude");
		velVertical = getConexao().addStream(parametrosDeVoo, "getVerticalSpeed");
		velHorizontal = getConexao().addStream(parametrosDeVoo, "getHorizontalSpeed");
		massaTotal = getConexao().addStream(naveAtual, "getMass");
		tempoMissao = getConexao().addStream(SpaceCenter.class, "getUT");

	}

	public void decolagemOrbital() throws RPCException, StreamException, IOException, InterruptedException {
		new DecolagemOrbitalController(getConexao());
	}

	public void suicideBurn() throws StreamException, RPCException, IOException, InterruptedException {
		new PousoAutomaticoController(getConexao());
	}

	public void autoRover() throws IOException, RPCException, InterruptedException, StreamException {
		new RoverAutonomoController(getConexao());
	}

	public void manobras() throws RPCException, StreamException, IOException, InterruptedException {
		new ManobrasController(true);
	}

	public static Connection getConexao() {
		return conexao;
	}

	private void setConexao(Connection con) {
		conexao = con;

	}
}

package com.pesterenan.model;

import java.io.IOException;

import com.pesterenan.funcoes.AutoRover;
import com.pesterenan.funcoes.DecolagemOrbital;
import com.pesterenan.funcoes.Manobras;
import com.pesterenan.funcoes.SuicideBurn;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Vessel;

public class Nave {
	private Connection conexao;

	protected static SpaceCenter centroEspacial;
	protected Vessel naveAtual;
	protected Flight parametrosDeVoo;

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
			System.err.println("Erro" + e.getMessage());
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
		new DecolagemOrbital(getConexao());
	}

	public void suicideBurn() throws StreamException, RPCException, IOException, InterruptedException {
		new SuicideBurn(getConexao());
	}

	public void autoRover() throws IOException, RPCException, InterruptedException, StreamException {
		new AutoRover(getConexao());
	}

	public void manobras() throws RPCException, StreamException, IOException, InterruptedException {
		new Manobras(getConexao(), true);
	}

	public Connection getConexao() {
		return this.conexao;
	}

	private void setConexao(Connection con) {
		this.conexao = con;

	}
}

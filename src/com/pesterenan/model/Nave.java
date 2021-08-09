package com.pesterenan.model;

import java.io.IOException;

import com.pesterenan.funcoes.DecolagemOrbital;
import com.pesterenan.funcoes.SuicideBurn;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Vessel;

public class Nave {
	protected static Connection conexao;
	

	protected static SpaceCenter centroEspacial;
	protected Vessel naveAtual;
	protected Flight parametrosVoo;
	
	protected Stream<Double> tempoMissao, altitude, altitudeSup, apoastro, periastro;

	public Nave(Connection con) {
		try {
			Nave.conexao = con;
			Nave.centroEspacial = SpaceCenter.newInstance(conexao);
			this.naveAtual = centroEspacial.getActiveVessel();
			
		} catch (RPCException e) {
			System.err.println("Erro" + e.getMessage());
		}
	}

	public void decolagemOrbital() throws RPCException, StreamException, IOException, InterruptedException {
		new DecolagemOrbital(conexao);
	}

	public void suicideBurn() throws StreamException, RPCException, IOException, InterruptedException {
		new SuicideBurn(conexao);
	}

	public void autoRover() {
		// TODO Auto-generated method stub
		
	}

	public void manobras() {
		// TODO Auto-generated method stub
		
	}

	public static Connection getConexao() {
		return conexao;
	}
	}


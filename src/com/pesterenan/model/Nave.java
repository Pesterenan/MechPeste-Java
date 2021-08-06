package com.pesterenan.model;

import java.io.IOException;

import com.pesterenan.funcoes.DecolagemOrbital;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Vessel;

public class Nave {
	protected Connection conexao;
	protected static SpaceCenter centroEspacial;
	protected static Vessel naveAtual;
	protected Flight parametrosVoo;

	protected Stream<Double> tempoMissao, altitude, altitudeSup, apoastro, periastro;

	public Nave(Connection conexao) {
		try {
			this.conexao = conexao;
			this.centroEspacial = SpaceCenter.newInstance(conexao);
			this.naveAtual = centroEspacial.getActiveVessel();
			DecolagemOrbital dO = new DecolagemOrbital(conexao);
			dO.decolagemOrbital(conexao);
		} catch (RPCException | IOException | InterruptedException | StreamException e) {
			System.err.println("Erro" + e.getMessage());
		}
	}
}

package com.pesterenan;

import javax.swing.SwingWorker;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;

public class VooAutonomo extends SwingWorker<String, String> {

	private Connection conexao;
	private SpaceCenter centroEspacial;
	private Vessel naveAtual;
	private Flight vooNave;
	private ReferenceFrame pontoRef;

	private Stream<Double> altitude;
	private Stream<Double> velocidade;

	private VesselSituation situacao;

	public VooAutonomo(Connection con) throws RPCException, StreamException {
		conexao = con;
		naveAtual = centroEspacial.getActiveVessel();
		centroEspacial = SpaceCenter.newInstance(conexao);
		pontoRef = naveAtual.getOrbit().getBody().getReferenceFrame();
		vooNave = naveAtual.flight(pontoRef);

		iniciarStreams();
		checarSituacao();
		iniciarPilotoAutomatico();
	}

	private void iniciarPilotoAutomatico() throws RPCException, StreamException {
		firePropertyChange("altitude", 0, 50);

	}

	private void checarSituacao() throws RPCException, StreamException {
		situacao = naveAtual.getSituation();
		switch (situacao) {
		case PRE_LAUNCH:
			System.out.println("Pronto para decolagem!");
			System.out.println("Altitude atual: " + altitude.get());
			break;
		case FLYING:
			System.out.println("Estamos voando? ESTAMOS VOANDO!");
			System.out.println("Velocidade atual: " + velocidade.get());
			break;
		case LANDED:
			System.out.println("Pousado. Pronto para decolar novamente?");
			firePropertyChange("altitude", 0, altitude.get());
		default:
			break;
		}

	}

	private void iniciarStreams() throws StreamException, RPCException {
		altitude = conexao.addStream(vooNave, "getSurfaceAltitude");
		velocidade = conexao.addStream(vooNave, "getHorizontalSpeed");
		firePropertyChange("altitude", 0, altitude.get());

	}

	@Override
	protected String doInBackground() throws Exception {
		return null;
	}

}

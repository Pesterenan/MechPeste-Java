package com.pesterenan;
import java.io.IOException;

import org.javatuples.Triplet;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;
import krpc.client.services.UI;
import krpc.client.services.UI.MessagePosition;


public class SuicideBurn {

	private static final int ALTITUDE_AEROFREIOS = 10000;
	private static final int VELOCIDADE_AEROFREIOS = 400;
	private static final int ALTITUDE_RCS = 3000;
	private Connection conexao;
	private SpaceCenter centroEspacial;
	private Vessel naveAtual;
	private Stream<Double> altitude;
	private Stream<Double> velocidadeVertical;
	private Stream<Float> massaTotal;
	private float acelGravidade;
	private UI ui;

	private ControlePID ctrlAceleracao = new ControlePID();
	private ControlePID ctrlPouso = new ControlePID();

	double altitudePouso = 50.0;

	boolean executandoSuicideBurn = false;
	boolean podePousar = false;
	double valorTWR = 1.0;
	double acelMax = 0;

	double distanciaDaQueima = 0.0;
	double duracaoDaQueima = 0;
	float novaAcel = 0;

	public static void main(String[] args) throws IOException, RPCException, InterruptedException, StreamException {
		new SuicideBurn();
	}

	private void mensagem(String texto, float duracao) throws RPCException {
		ui.message(texto, duracao, MessagePosition.TOP_CENTER, new Triplet<Double,Double,Double>(0.0,1.0,0.0),16);

	}
	public SuicideBurn() throws IOException, RPCException, StreamException, InterruptedException {
		conexao = Connection.newInstance("Suicide Burn - MechPeste");
		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = centroEspacial.getActiveVessel();
		new SuicideBurn(conexao,naveAtual);
	}
	public SuicideBurn(Connection con, Vessel nave)
			throws StreamException, RPCException, IOException, InterruptedException {
		conexao = con;
		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = nave;
		ui = UI.newInstance(conexao);

		Flight parametrosDeVoo = naveAtual.flight(naveAtual.getOrbit().getBody().getReferenceFrame());
		altitude = conexao.addStream(parametrosDeVoo, "getSurfaceAltitude");
		velocidadeVertical = conexao.addStream(parametrosDeVoo, "getVerticalSpeed");
		massaTotal = conexao.addStream(naveAtual, "getMass");
		acelGravidade = naveAtual.getOrbit().getBody().getSurfaceGravity();
		Navegacao navegacao = new Navegacao(centroEspacial, naveAtual);

		calcularParametros();
		iniciarPIDs();
		decolagemDeTeste();

		mensagem("Nave Atual: " + naveAtual.getName(), 3f);
		mensagem("Situação da nave: " + naveAtual.getSituation().toString(), 3f);
		mensagem("Força da Gravidade Atual: " + acelGravidade + "Corpo Celeste: "
				+ naveAtual.getOrbit().getBody().getName(), 3f);
		mensagem("Força de TWR da Nave: " + valorTWR, 3f);
		double altitudeTWR = 100; 
		// Loop esperando para executar o Suicide Burn:
		while (!executandoSuicideBurn) {
			calcularParametros();
			podePousar = false;
			navegacao.mirarRetrogrado();

			if (!naveAtual.getControl().getBrakes() && velocidadeVertical.get() < 0) {
					if (altitude.get() < ALTITUDE_AEROFREIOS
					|| velocidadeVertical.get() < -VELOCIDADE_AEROFREIOS) {
				naveAtual.getControl().setBrakes(true);
				navegacao.anguloInclinacaoMax = 70;
			} else if (altitude.get() > ALTITUDE_AEROFREIOS) {
				naveAtual.getControl().setBrakes(false);
				navegacao.anguloInclinacaoMax = 45;
			}}
			if (altitude.get() < ALTITUDE_RCS) {
				naveAtual.getControl().setRCS(true);
			}
			// Checar altitude para o Suicide Burn:
			altitudeTWR = valorTWR * acelGravidade * 2;
			if (altitudeTWR < altitudePouso) {
				altitudePouso = 50.0;
			} else {
				altitudePouso = altitudeTWR;
			}
			if (0 > altitude.get() - distanciaDaQueima - altitudePouso && velocidadeVertical.get() < -1) {
				System.out.println(altitudeTWR);
				executandoSuicideBurn = true;
				mensagem("Iniciando o Suicide Burn!", 1f);
				// System.out.println("Iniciando o Suicide Burn!");
			}
			Thread.sleep(100);
		}

		// Loop principal de Suicide Burn:
		while (executandoSuicideBurn) {
			// Calcula os valores de aceleração e TWR do foguete:
			calcularParametros();
			// Desce o trem de pouso da nave em menos de 100 metros
			if (altitude.get() < (altitudeTWR * 3)) {
				naveAtual.getControl().setGear(true);
			}
			// Informa aos PIDs a altitude, limite e velocidade da nave
			ctrlAceleracao.setEntradaPID(altitude.get());
			ctrlAceleracao.setLimitePID(distanciaDaQueima);
			ctrlPouso.setEntradaPID(velocidadeVertical.get());
			// Aponta nave para o retrograde se a velocidade horizontal for maior que 0.2m/s
			if (parametrosDeVoo.getHorizontalSpeed() > 3) {
				navegacao.mirarRetrogrado();
				ctrlPouso.setLimitePID(0);
			} else {
				naveAtual.getAutoPilot().setTargetPitch(90);
				ctrlPouso.setLimitePID(-(altitude.get()/10));
			}
			if (altitude.get() > altitudePouso) {
				podePousar = false;
			} else {
				podePousar = true;
			}
			// Corrigir aceleração da nave:
			float correcaoAnterior = naveAtual.getControl().getThrottle();
			try {
				if (!podePousar) {
					aceleracao((float) (correcaoAnterior + ctrlAceleracao.computarPID()) / 2);
				} else {
					aceleracao((float) ctrlPouso.computarPID());
				}
			} catch (Exception erro) {
				mensagem("Erro no cálculo da aceleração. Usando valor antigo. " + erro, 1f);
				// System.out.println("Erro no cálculo da aceleração. Usando valor antigo. " +
				// erro);
				aceleracao(correcaoAnterior);
			}
			checarPouso();
			Thread.sleep(50);
		}
	}

	public void iniciarPIDs() {
		ctrlAceleracao.setAmostraTempo(50);
		ctrlAceleracao.ajustarPID(0.025, 0.001, 0.05);
		ctrlAceleracao.limitarSaida(0, 1);
		ctrlAceleracao.setLimitePID(0);
		ctrlPouso.setAmostraTempo(50);
		ctrlPouso.ajustarPID(0.5, 0.001, 0.1);
		ctrlPouso.limitarSaida(0.75 / valorTWR, 1);
		ctrlPouso.setLimitePID(-4);
	}

	private void calcularParametros() throws RPCException, StreamException, IOException {
		try {
			float empuxo = 0f;
			if (naveAtual.getAvailableThrust() == 0) {
				empuxo = naveAtual.getMaxThrust();
			} else {
				empuxo = naveAtual.getAvailableThrust();
			}
			valorTWR = empuxo / (massaTotal.get() * acelGravidade);
			acelMax = (valorTWR * acelGravidade) - acelGravidade;
			duracaoDaQueima = Math.abs(velocidadeVertical.get()) / acelMax;
			distanciaDaQueima = (Math.abs(velocidadeVertical.get()) * duracaoDaQueima
					+ 1 / 2 * acelMax * Math.pow(duracaoDaQueima, 2));
		} catch (Exception erro) {
			valorTWR = 1;
			duracaoDaQueima = 1;
			distanciaDaQueima = 1;
		}
	}

	private void decolagemDeTeste() throws StreamException, RPCException, IOException, InterruptedException {
		// Decola a nave se estiver na pista, ou pousada. Se estiver voando, apenas
		// corta a aceleração.
		naveAtual.getAutoPilot().engage();
		VesselSituation situacao = naveAtual.getSituation();
		if (situacao == VesselSituation.LANDED || situacao == VesselSituation.PRE_LAUNCH) {
			if (situacao == VesselSituation.PRE_LAUNCH) {
				naveAtual.getControl().activateNextStage();
			}
			naveAtual.getControl().setGear(false);
			aceleracao(1);
			while (altitude.get() < altitudePouso) {
				naveAtual.getAutoPilot().setTargetPitch(90);
				Thread.sleep(200);
			}
			aceleracao(0);
		} else {
			aceleracao(0);
		}
	}

	private void aceleracao(float acel) throws RPCException, IOException {
		naveAtual.getControl().setThrottle(acel);
	}

	private void checarPouso() throws RPCException, IOException, InterruptedException {
		if ((naveAtual.getSituation() == VesselSituation.LANDED)
				|| (naveAtual.getSituation() == VesselSituation.SPLASHED) && (podePousar)) {
			mensagem("Pouso finalizado.", 1f);
			// System.out.println("Pouso finalizado.");
			aceleracao(0);
			naveAtual.getAutoPilot().disengage();
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setRCS(true);
			naveAtual.getControl().setBrakes(false);
			podePousar = false;
			executandoSuicideBurn = false;
			//conexao.close();
		}
	}

}

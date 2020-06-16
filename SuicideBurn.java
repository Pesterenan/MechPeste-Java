package com.pesterenan.funcoes;

import java.io.IOException;

import com.pesterenan.MechPeste;
import com.pesterenan.gui.GUI;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Navegacao;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;

public class SuicideBurn {

	private static final int ALTITUDE_AEROFREIOS = 10000;
	private static final int VELOCIDADE_AEROFREIOS = 400;
	private static int altitudeSCR = 1000;
	private static SpaceCenter centroEspacial;
	private Vessel naveAtual;
	private Stream<Double> altitude;
	private Stream<Double> velVertical;
	private Stream<Double> velHorizontal;
	private Stream<Float> massaTotal;
	private float acelGravidade;

	private ControlePID suicidePID = new ControlePID();
	private ControlePID pousoPID = new ControlePID();

	double altitudePouso = 50.0;

	boolean executandoSuicideBurn = false;
	public static boolean podePousar = true;
	double valorTWR = 1.0;
	double acelMax = 0;

	double distanciaDaQueima = 0.0;
	double duracaoDaQueima = 0;
	float novaAcel = 0;
	double distanciaQueimaV2 = 0;

	public SuicideBurn(Connection conexao) throws StreamException, RPCException, IOException, InterruptedException {
		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = centroEspacial.getActiveVessel();
		new SuicideBurn(conexao, naveAtual);
	}

	public SuicideBurn(Connection conexao, Vessel naveAtual)
			throws StreamException, RPCException, IOException, InterruptedException {
		Flight parametrosDeVoo = naveAtual.flight(naveAtual.getOrbit().getBody().getReferenceFrame());
		altitude = conexao.addStream(parametrosDeVoo, "getSurfaceAltitude");
		velVertical = conexao.addStream(parametrosDeVoo, "getVerticalSpeed");
		velHorizontal = conexao.addStream(parametrosDeVoo, "getHorizontalSpeed");
		massaTotal = conexao.addStream(naveAtual, "getMass");
		acelGravidade = naveAtual.getOrbit().getBody().getSurfaceGravity();
		Navegacao navegacao = new Navegacao(centroEspacial, naveAtual);

		calcularParametros();
		iniciarPIDs();
		decolagemDeTeste();

		GUI.setParametros("nome", naveAtual.getName());
		GUI.setStatus(
				"Iniciando Suicide Burn em: " + naveAtual.getOrbit().getBody().getName() + ", TWR em: " + valorTWR);
		double altitudeTWR = 100;
		float anguloRoll = naveAtual.getAutoPilot().getTargetRoll();
		// Loop esperando para executar o Suicide Burn:
		while (!executandoSuicideBurn) {
			calcularParametros();
			navegacao.mirarRetrogrado();

			if (!naveAtual.getControl().getBrakes() && velVertical.get() < 0) {
				if (altitude.get() < ALTITUDE_AEROFREIOS || velVertical.get() < -VELOCIDADE_AEROFREIOS) {
					naveAtual.getControl().setBrakes(true);
					navegacao.anguloInclinacaoMax = 50;
				} else if (altitude.get() > ALTITUDE_AEROFREIOS) {
					naveAtual.getControl().setBrakes(false);
					navegacao.anguloInclinacaoMax = 30;
				}
			}
			if (altitude.get() < altitudeSCR) {
				naveAtual.getControl().setRCS(true);
			}
			// Checar altitude para o Suicide Burn:
			altitudeTWR = valorTWR * acelGravidade * 2;
			if (altitudeTWR < altitudePouso) {
				altitudePouso = 80.0;
			} else {
				altitudePouso = altitudeTWR;
			}
			if ((altitudePouso > altitude.get() - distanciaQueimaV2) && velVertical.get() < -1) {
				executandoSuicideBurn = true;
				GUI.setStatus("Iniciando o Suicide Burn!");
			}
			Thread.sleep(100);
		}

		// Loop principal de Suicide Burn:
		while (executandoSuicideBurn) {
			// Calcula os valores de aceleração e TWR do foguete:
			calcularParametros();
			// Desce o trem de pouso da nave
			if (altitude.get() < (altitudeTWR * 1.414)) {
				naveAtual.getControl().setGear(true);
			}
			// Informa aos PIDs a altitude, limite e velocidade da nave
			suicidePID.setEntradaPID(altitude.get());
			suicidePID.setLimitePID(distanciaQueimaV2);
			pousoPID.setEntradaPID(velVertical.get());
			// Aponta nave para o retrograde se a velocidade horizontal for maior que 1m/s
			if (parametrosDeVoo.getHorizontalSpeed() > 1) {
				naveAtual.getAutoPilot().setTargetRoll((float) anguloRoll);
				navegacao.mirarRetrogrado();
				pousoPID.setLimitePID(0);
			} else {
				naveAtual.getAutoPilot().setTargetPitch(90);
				pousoPID.setLimitePID(-(altitude.get() * 0.15));
			}
			// Corrigir aceleração da nave:
			float correcaoAnterior = naveAtual.getControl().getThrottle();

			try {
				if (podePousar) {
					if (altitude.get() < altitudePouso) {
						aceleracao((float) pousoPID.computarPID());
					} else {
						aceleracao((float) (suicidePID.computarPID()));
					}
				} else {
					aceleracao((float) (suicidePID.computarPID()));
				}
			} catch (Exception erro) {
				GUI.setStatus("Erro no cálculo da aceleração. Usando valor antigo. " + erro);
				aceleracao(correcaoAnterior);
			}
			checarPouso();
			Thread.sleep(25);
		}
		altitude.remove();
		velVertical.remove();
		velHorizontal.remove();
		massaTotal.remove();
		MechPeste.finalizarTarefa();
	}

	public void iniciarPIDs() {
		suicidePID.setAmostraTempo(25);
		suicidePID.ajustarPID(0.01, 0.001, 0.1);
		suicidePID.limitarSaida(0, 1);
		suicidePID.setLimitePID(0);
		pousoPID.setAmostraTempo(25);
		pousoPID.ajustarPID(0.1, 0.001, 0.2);
		pousoPID.limitarSaida(0.5 / valorTWR, 1);
		pousoPID.setLimitePID(0);
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
			duracaoDaQueima = Math.abs(velVertical.get()) / acelMax;
			distanciaDaQueima = (Math.abs(velVertical.get()) * duracaoDaQueima
					+ 1 / 2 * acelMax * Math.pow(duracaoDaQueima, 2));
			distanciaQueimaV2 = ((Math.abs(velVertical.get()) + Math.sqrt(2 * acelGravidade * altitude.get())) / 2)
					* duracaoDaQueima;
		} catch (Exception erro) {
			valorTWR = 1;
			duracaoDaQueima = 1;
			distanciaDaQueima = 1;
		}
		GUI.setParametros("altitude", altitude.get());
		GUI.setParametros("velVert", velVertical.get());
		GUI.setParametros("velHorz", velHorizontal.get());
		GUI.setParametros("periastro", distanciaQueimaV2);
	}

	private void decolagemDeTeste() throws StreamException, RPCException, IOException, InterruptedException {
		// Decola a nave se estiver na pista, ou pousada. Se estiver voando, apenas
		// corta a aceleração.
		naveAtual.getAutoPilot().engage();
		naveAtual.getAutoPilot().setReferenceFrame(naveAtual.getOrbit().getBody().getReferenceFrame());
		VesselSituation situacao = naveAtual.getSituation();
		if (situacao == VesselSituation.LANDED || situacao == VesselSituation.PRE_LAUNCH) {
			if (situacao == VesselSituation.PRE_LAUNCH) {
				naveAtual.getControl().activateNextStage();
			}
			naveAtual.getControl().setGear(false);
			aceleracao(1.0f);
			while (altitude.get() < altitudePouso) {
				naveAtual.getAutoPilot().setTargetPitch(90);
				Thread.sleep(100);
			}
			aceleracao(0.0f);
		} else {
			aceleracao(0.0f);
		}
	}

	private void aceleracao(float acel) throws RPCException, IOException {
		naveAtual.getControl().setThrottle(acel);
	}

	private void checarPouso() throws RPCException, IOException, InterruptedException {
		if ((naveAtual.getSituation() == VesselSituation.LANDED)
				|| (naveAtual.getSituation() == VesselSituation.SPLASHED) && (podePousar)) {
			GUI.setStatus("Pouso finalizado.");
			aceleracao(0);
			naveAtual.getAutoPilot().disengage();
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setRCS(true);
			naveAtual.getControl().setBrakes(false);
			podePousar = true;
			executandoSuicideBurn = false;
		}
	}

	public static void setAltSCR(int altitude) {
		altitudeSCR = altitude;
	}
}

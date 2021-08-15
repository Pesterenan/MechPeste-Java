package com.pesterenan.funcoes;

import java.io.IOException;

import com.pesterenan.MechPeste;
import com.pesterenan.gui.GUI;
import com.pesterenan.model.Nave;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Navegacao;
import com.pesterenan.utils.Vetor;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Vessel;

public class SuicideBurn extends Nave {

	private static final int ALTITUDE_SUICIDEBURN = 10000, ALTITUDE_TREM_DE_POUSO = 500;
	
	private float acelGravidade;
	private ControlePID altitudePID = new ControlePID(), velocidadePID = new ControlePID();
	boolean executandoSuicideBurn = false;
	double valorTEP = 1.0, distanciaDaQueima = 0.0, duracaoDaQueima = 0.0, acelMaxima = 0.0;
	private static double altP = 0.01, altI = 0.01, altD = 0.01;
	private static double velP = 0.025, velI = 0.05, velD = 0.1;

	public SuicideBurn(Connection con) throws StreamException, RPCException, IOException, InterruptedException {
		super(con);
		naveAtual = centroEspacial.getActiveVessel();
		new SuicideBurn(con,naveAtual);
	}

	public SuicideBurn(Connection con, Vessel nave)
			throws StreamException, RPCException, IOException, InterruptedException {
		super(con);
		naveAtual = nave;
		acelGravidade = naveAtual.getOrbit().getBody().getSurfaceGravity();

		iniciarPIDs();
		atualizarParametros();
		executarSuicideBurn();

		MechPeste.finalizarTarefa();

	}

	private void executarSuicideBurn() throws RPCException, StreamException, IOException, InterruptedException {
		naveAtual.getControl().setRCS(true);
		naveAtual.getAutoPilot().engage();
		naveAtual.getAutoPilot().setReferenceFrame(naveAtual.getSurfaceReferenceFrame());
		aceleracao(0.0f);
		GUI.setStatus(
				"Iniciando Suicide Burn em: " + naveAtual.getOrbit().getBody().getName() + ", TEP em: " + valorTEP);
		Navegacao navegacao = new Navegacao(centroEspacial, naveAtual);
		// Loop esperando para executar o Suicide Burn:
		while (!executandoSuicideBurn) {
			atualizarParametros();
			navegacao.mirarRetrogrado();
			if (!naveAtual.getControl().getBrakes() && velVertical.get() < 0) {
				naveAtual.getControl().setBrakes(true);
			}
			// Checar altitude para o Suicide Burn:
			if (altitude.get() < ALTITUDE_SUICIDEBURN) {
				if ((altitude.get() < distanciaDaQueima) && velVertical.get() < -1) {
					executandoSuicideBurn = true;
					GUI.setStatus("Iniciando o Suicide Burn!");
				}
			}
			Thread.sleep(50);
		}
		// Loop principal de Suicide Burn:
		while (executandoSuicideBurn) {
			// Calcula os valores de aceleração e TWR do foguete:
			atualizarParametros();
			// Desce o trem de pouso da nave
			if (altitude.get() < ALTITUDE_TREM_DE_POUSO) {
				naveAtual.getControl().setGear(true);
			}
			// Aponta nave para o retrograde se a velocidade horizontal for maior que 1m/s
			if (velHorizontal.get() > 3) {
				naveAtual.getControl().setRCS(true);
				navegacao.mirarRetrogrado();
			} else {
				naveAtual.getControl().setRCS(false);
				naveAtual.getAutoPilot().setTargetPitch(90);
			}
			// Corrigir aceleração da nave:
			aceleracao((float) ((altitudePID.computarPID()) + (velocidadePID.computarPID())));
			checarPouso();
			Thread.sleep(25);
		}
	}

	public void iniciarPIDs() {
		altitudePID.setAmostraTempo(25);
		altitudePID.ajustarPID(valorTEP * altP, altI, valorTEP * altD);
		altitudePID.limitarSaida(0, 1);
		altitudePID.setLimitePID(0);
		velocidadePID.setAmostraTempo(25);
		velocidadePID.ajustarPID(valorTEP * velP, velI, valorTEP * velD);
		velocidadePID.limitarSaida(0, 1);
		velocidadePID.setLimitePID(0);
	}

	private void atualizarParametros() throws RPCException, StreamException, IOException {
		try {
			distanciaDaQueima = calcularDistanciaDaQueima();
			informarPIDs(distanciaDaQueima);
		} catch (Exception erro) {
		}
		GUI.setParametros("nome", naveAtual.getName());
		GUI.setParametros("altitude", altitude.get());
		GUI.setParametros("distanciaDaQueima", distanciaDaQueima);
		GUI.setParametros("valorTEP", valorTEP);
		GUI.setParametros("velVert", velVertical.get());
		GUI.setParametros("velHorz", velHorizontal.get());
	}

	private double calcularDistanciaDaQueima() throws RPCException, StreamException {
		double distanciaDaQueima = 0;
		double empuxoDisponivel = naveAtual.getAvailableThrust() / 1000;
		Vetor velocidade = new Vetor(velHorizontal.get(), velVertical.get(), 0);
		valorTEP = empuxoDisponivel / ((massaTotal.get() / 1000) * acelGravidade);
		acelMaxima = valorTEP * acelGravidade - acelGravidade;
		double duracaoDaQueima = Math.abs(velocidade.Magnitude()) / acelMaxima;
		distanciaDaQueima = (Math.abs(velocidade.Magnitude()) * duracaoDaQueima)
				+ (0.5 * (acelMaxima * (duracaoDaQueima * duracaoDaQueima)));
		return distanciaDaQueima;
	}

	/**
	 * Informa aos PIDs de altitude e velocidade, os limites e velocidade da nave,
	 * utilizando a distancia da queima para ajustar velocidade limite.
	 * 
	 * @param distanciaDaQueima - A distância calculada para a queima
	 * @throws RPCException
	 * @throws StreamException
	 */
	private void informarPIDs(double distanciaDaQueima) throws RPCException, StreamException {
		// Informa aos PIDs de altitude e velocidade, os limites e velocidade da nave
		velocidadePID.ajustarPID(valorTEP * velP, velI, valorTEP * velD);
		altitudePID.setEntradaPID(altitude.get() - distanciaDaQueima);
		altitudePID.setLimitePID(naveAtual.boundingBox(naveAtual.getReferenceFrame()).getValue1().getValue1());
		velocidadePID.setEntradaPID(velVertical.get());
		double velFinal = (altitude.get() + (distanciaDaQueima)) / -10;
		if (velFinal <= -5) {
			velocidadePID.setLimitePID(velFinal);
		} else {
			velFinal = -5;
			velocidadePID.setLimitePID(velFinal);
		}
	}

	private void checarPouso() throws RPCException, IOException, InterruptedException {
		switch (naveAtual.getSituation()) {
		case LANDED:
		case SPLASHED:
			GUI.setStatus("Pouso finalizado.");
			aceleracao(0);
			naveAtual.getAutoPilot().disengage();
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setRCS(true);
			naveAtual.getControl().setBrakes(false);
			executandoSuicideBurn = false;
		default:
			break;
		}
	}

	private void aceleracao(double acel) throws RPCException, IOException {
		naveAtual.getControl().setThrottle((float) acel);
	}

	public static void setAjusteAltPID(double P, double I, double D) {
		if (P > 0) {
			altP = P;
		}
		if (I >= 0) {
			altI = I;
		}
		if (D >= 0) {
			altD = D;
		}
	}

	public static void setAjusteVelPID(double P, double I, double D) {
		if (P > 0) {
			velP = P;
		}
		if (I >= 0) {
			velI = I;
		}
		if (D >= 0) {
			velD = D;
		}
	}
}

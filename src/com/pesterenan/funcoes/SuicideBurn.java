package com.pesterenan.funcoes;

import java.io.IOException;

import com.pesterenan.MechPeste;
import com.pesterenan.gui.GUI;
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

public class SuicideBurn {

	private static final int ALTITUDE_AEROFREIOS = 10000;
	private static int altitudeSCR = 1000;
	private static SpaceCenter centroEspacial;
	private Vessel naveAtual;
	private Stream<Double> altitude;
	private Stream<Double> velVertical;
	private Stream<Double> velHorizontal;
	private Stream<Float> massaTotal;
	private float acelGravidade;

	private ControlePID altitudePID = new ControlePID();
	private ControlePID velocidadePID = new ControlePID();

	double altVooTeste = 100.0;

	boolean executandoSuicideBurn = false;
	public static boolean podePousar = true;
	double valorTEP = 1.0;

	double distanciaDaQueima = 0.0;
	double duracaoDaQueima = 0;
	float novaAcel = 0;
	private double acelMaxima;

	private static double altP = 0.001, altI = 0.01, altD = 0.01;

	private static double velP = 0.025, velI = 0.05, velD = 0.05;

	public SuicideBurn(Connection conexao) throws StreamException, RPCException, IOException, InterruptedException {

		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = centroEspacial.getActiveVessel();
		new SuicideBurn(conexao, naveAtual);
	}

	public SuicideBurn(Connection conexao, Vessel nave)
			throws StreamException, RPCException, IOException, InterruptedException {
		naveAtual = nave;
		Flight parametrosDeVoo = naveAtual.flight(naveAtual.getOrbit().getBody().getReferenceFrame());
		altitude = conexao.addStream(parametrosDeVoo, "getSurfaceAltitude");
		velVertical = conexao.addStream(parametrosDeVoo, "getVerticalSpeed");
		velHorizontal = conexao.addStream(parametrosDeVoo, "getHorizontalSpeed");
		massaTotal = conexao.addStream(naveAtual, "getMass");
		acelGravidade = naveAtual.getOrbit().getBody().getSurfaceGravity();
		Navegacao navegacao = new Navegacao(centroEspacial, naveAtual);

		atualizarParametros();
		iniciarPIDs();
		decolagemDeTeste();

		GUI.setStatus(
				"Iniciando Suicide Burn em: " + naveAtual.getOrbit().getBody().getName() + ", TEP em: " + valorTEP);
		// Loop esperando para executar o Suicide Burn:
		while (!executandoSuicideBurn) {
			atualizarParametros();
			navegacao.mirarRetrogrado();
			if (!naveAtual.getControl().getBrakes() && velVertical.get() < 0) {
				naveAtual.getControl().setBrakes(true);
				if (altitude.get() > ALTITUDE_AEROFREIOS) {
					navegacao.anguloInclinacaoMax = 30;
				} else if (altitude.get() < ALTITUDE_AEROFREIOS) {
					navegacao.anguloInclinacaoMax = 10;
				}
			}
			// Checar altitude para o Suicide Burn:
			if (altitude.get() < ALTITUDE_AEROFREIOS) {
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
			if (altitude.get() < altitudeSCR) {
				naveAtual.getControl().setGear(true);
			}

			// Aponta nave para o retrograde se a velocidade horizontal for maior que 1m/s
			if (velHorizontal.get() > 1) {
				navegacao.mirarRetrogrado();
			} else {
				naveAtual.getAutoPilot().setTargetPitch(90);
			}

			// Corrigir aceleração da nave:
			aceleracao((float) ((altitudePID.computarPID()) + (velocidadePID.computarPID())));

			checarPouso();
			Thread.sleep(25);
		}
		MechPeste.finalizarTarefa();

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
		} catch (Exception erro) {
			System.out.println("Erro ao calcular os dados para Suicide Burn.");
		}
		GUI.setParametros("nome", naveAtual.getName());
		GUI.setParametros("altitude", altitude.get());
		GUI.setParametros("apoastro", distanciaDaQueima);
		GUI.setParametros("periastro", altitudePID.computarPID());
		GUI.setParametros("velVert", acelMaxima);
		GUI.setParametros("velHorz", velocidadePID.computarPID());
	}

	private double calcularDistanciaDaQueima() throws RPCException, StreamException {
		double distanciaDaQueima = 0;
		double empuxoDisponivel = naveAtual.getAvailableThrust() / 1000;
		Vetor velocidade = new Vetor(velHorizontal.get(), velVertical.get(), 0);
		valorTEP = empuxoDisponivel / ((massaTotal.get() / 1000) * acelGravidade);
		acelMaxima = valorTEP * acelGravidade - acelGravidade;
		double duracaoDaQueima = Math.abs(velocidade.Magnitude()) / acelMaxima;
		distanciaDaQueima = (Math.abs(velocidade.Magnitude()) * duracaoDaQueima)
				+ (0.5 * (acelMaxima * Math.pow(duracaoDaQueima, 2)));

		velocidadePID.ajustarPID(valorTEP * velP, velI, valorTEP * velD);
		// Informa aos PIDs a altitude, limite e velocidade da nave
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
		return distanciaDaQueima;
	}

	private void decolagemDeTeste() throws StreamException, RPCException, IOException, InterruptedException {
		naveAtual.getControl().setRCS(false);
		naveAtual.getAutoPilot().engage();
		naveAtual.getAutoPilot().setReferenceFrame(naveAtual.getSurfaceReferenceFrame());
		// Decola a nave se estiver na pista, ou pousada. Se estiver voando, apenas
		// corta a aceleração.
		switch (naveAtual.getSituation()) {
		case PRE_LAUNCH:
			naveAtual.getControl().activateNextStage();
		case LANDED:
			naveAtual.getControl().setGear(false);
			aceleracao(1.0f);
			while (altitude.get() < altVooTeste) {
				naveAtual.getAutoPilot().setTargetPitch(90);
				Thread.sleep(100);
			}
		default:
			aceleracao(0.0f);
			break;
		}
	}

	private void checarPouso() throws RPCException, IOException, InterruptedException {
		switch (naveAtual.getSituation()) {
		case LANDED:
		case SPLASHED:
			if (podePousar) {
				GUI.setStatus("Pouso finalizado.");
				aceleracao(0);
				naveAtual.getAutoPilot().disengage();
				naveAtual.getControl().setSAS(true);
				naveAtual.getControl().setRCS(true);
				naveAtual.getControl().setBrakes(false);
				podePousar = true;
				executandoSuicideBurn = false;
				altitude.remove();
				velVertical.remove();
				velHorizontal.remove();
				massaTotal.remove();
			}
		default:
			break;
		}
	}

	private void aceleracao(double acel) throws RPCException, IOException {
		naveAtual.getControl().setThrottle((float) acel);
	}

	public static void setAltSCR(int altitude) {
		altitudeSCR = altitude;
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
		System.out.println("Valores AltPID novos: " + altP + " " + altI + " " + altD);
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
		System.out.println("Valores VelPID novos: " + velP + " " + velI + " " + velD);
	}
}

package com.pesterenan.funcoes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pesterenan.MechPeste;
import com.pesterenan.gui.GUI;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Vetor;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.SASMode;
import krpc.client.services.SpaceCenter.SolarPanel;
import krpc.client.services.SpaceCenter.SolarPanelState;
import krpc.client.services.SpaceCenter.SpeedMode;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.Waypoint;
import krpc.client.services.SpaceCenter.WaypointManager;

// Módulo de Piloto automático de Rovers
// Autor: Renan Torres <pesterenan@gmail.com>
// Data: 14/02/2019

public class AutoRover {
	private static final int DISTANCIA_DE_PROCURA = 4400000;
	// Declaração de variáveis:
	static private SpaceCenter centroEspacial;
	WaypointManager gerenciadorMarcadores;
	List<Waypoint> listaDeMarcadoresASeguir = new ArrayList<Waypoint>();
//	private List<Triplet<Double, Double, Double>> pontosASeguir = new ArrayList<Triplet<Double, Double, Double>>();
	private List<Vetor> pontosASeguir = new ArrayList<Vetor>();
	private static Vessel rover, naveAlvo;
	Waypoint alvoMarcador;
	private ReferenceFrame pontoRefRover, pontoRefOrbital, pontoRefSuperficie;
	Flight parametrosRover;
	Vetor posicaoRover, posicaoAnguloRover, posicaoAlvo, direcaoRover, direcaoTrajeto;
	double anguloAlvo = 0, anguloRover = 0;
	float limiteDistanciaAlvo = 100;
	static float velocidadeMaxima = 6;
	float velocidadeCurva = 3;
	ControlePID ctrlDirecao = new ControlePID(), ctrlAceleracao = new ControlePID();
	private static String nomeMarcador = "ALVO";
	public static boolean buscandoMarcadores = true;
	private boolean executandoAutoRover = true;
	Stream<Double> velocidadeRover;
	private int nivelMinBateria = 10;
	double cargaTotal = 100;
	double cargaAtual = 10;

	Vetor distParaAlvo;
	int pontos;
	private boolean carregando;
	private double tempoRestante;
	private double tempoAnterior;
	private float kmsPercorridos;
	private Stream<Double> tempoDoJogo;

	public AutoRover(Connection conexao) throws IOException, RPCException, InterruptedException, StreamException {
		iniciarParametros(conexao);
		definirAlvo();
		controlarRover();
	}

	private void iniciarParametros(Connection conexao) throws RPCException, StreamException {
		centroEspacial = SpaceCenter.newInstance(conexao);
		gerenciadorMarcadores = centroEspacial.getWaypointManager();
		rover = centroEspacial.getActiveVessel();
		// REFERENCIA PARA BUSCAR ANGULO DE DIREÇÃO DO ROVER:
		pontoRefRover = rover.getReferenceFrame();
		// REFERENCIA PARA VELOCIDADE DO ROVER:
		pontoRefOrbital = rover.getOrbit().getBody().getReferenceFrame();
		// REFERENCIA PARA BUSCAR POSICOES DE ALVO:
		pontoRefSuperficie = rover.getSurfaceReferenceFrame();
		parametrosRover = rover.flight(pontoRefOrbital);
		velocidadeRover = conexao.addStream(parametrosRover, "getHorizontalSpeed");
		tempoDoJogo = conexao.addStream(centroEspacial.getClass(), "getUT");
		// AJUSTAR CONTROLES PID:
		ctrlAceleracao.ajustarPID(0.5, 0.1, 0.01);
		ctrlAceleracao.limitarSaida(0, 1);
		ctrlDirecao.ajustarPID(0.03, 0.05, 0.3);
		ctrlDirecao.limitarSaida(-1, 1);
		tempoAnterior = tempoDoJogo.get();
		tempoRestante = 0;
		kmsPercorridos = 0;

	}

	private void definirAlvo() throws IOException, RPCException {
		if (buscandoMarcadores) {
			for (Waypoint marcador : gerenciadorMarcadores.getWaypoints()) {
				if (marcador.getName().contains(nomeMarcador)) {
					listaDeMarcadoresASeguir.add(marcador);
				}
			}
			if (listaDeMarcadoresASeguir.isEmpty()) {
				executandoAutoRover = false;
				GUI.setStatus("Sem alvos disponíveis");
			} else {
				checarDistancia();
			}
		} else {
			try {
				naveAlvo = centroEspacial.getTargetVessel();
				GUI.setStatus("Está indo na direção de: " + naveAlvo.getName());
				GUI.setParametros("nome", naveAlvo.getName());
				distParaAlvo = new Vetor(naveAlvo.position(pontoRefSuperficie));
				fazerListaDoCaminho();
			} catch (NullPointerException e) {
				executandoAutoRover = false;
				GUI.setStatus("Sem alvos disponíveis");
			}
		}
	}

	private void carregarBaterias() throws RPCException, IOException, StreamException, InterruptedException {
		cargaTotal = rover.getResources().max("ElectricCharge");
		cargaAtual = rover.getResources().amount("ElectricCharge");
		int porcentagemCarga = (int) Math.ceil(cargaAtual * 100 / cargaTotal);
		if (porcentagemCarga > nivelMinBateria) {
			carregando = false;
		} else {
			carregando = true;
			acelerarRover(0);
			rover.getControl().setLights(false);
			rover.getControl().setBrakes(true);
			rover.getControl().setWheelSteering(0.0f);
			if (velocidadeRover.get() < 1 && rover.getControl().getBrakes()) {
				Thread.sleep(1000);
				GUI.setStatus("Carregando Baterias...");
				double segCarga = 0;
				List<SolarPanel> paineis = new ArrayList<SolarPanel>();
				paineis = rover.getParts().getSolarPanels();
				for (SolarPanel painel : paineis) {
					segCarga += painel.getEnergyFlow();
					if (painel.getState() == SolarPanelState.BROKEN) {
						paineis.remove(painel);
					}
				}
				if (paineis.isEmpty()) {
					GUI.setStatus("Não há painéis solares para carregar as baterias.");
					executandoAutoRover = false;
				}
				segCarga = ((cargaTotal - cargaAtual) / segCarga);
				System.out.println("Segundos de Carga: " + segCarga);
				if (segCarga < 1 || segCarga > 21600) {
					segCarga = 3600;
				}
				centroEspacial.warpTo((centroEspacial.getUT() + segCarga), 10000, 4);
				rover.getControl().setLights(true);
			}
		}
		GUI.setParametros("carga", porcentagemCarga);
	}

	private void checarDistancia() throws RPCException, IOException {
		double distanciaProcura = DISTANCIA_DE_PROCURA;
		for (Waypoint marcador : listaDeMarcadoresASeguir) {
			double distanciaMarcador = posicionarMarcador(marcador).Magnitude3d();
			if (distanciaProcura > distanciaMarcador) {
				distanciaProcura = distanciaMarcador;
				alvoMarcador = marcador;
			}
		}
		distParaAlvo = (posicionarMarcador(alvoMarcador));
		fazerListaDoCaminho();
		GUI.setStatus("Localizado marcador mais próximo: " + alvoMarcador.getName());
		GUI.setParametros("nome", alvoMarcador.getName());

	}

	private void fazerListaDoCaminho() throws IOException, RPCException {

		System.out.println("distParaAlvo" + distParaAlvo);

		pontos = (int) distParaAlvo.Magnitude3d() / 1000;
		System.out.println("pontos" + pontos);
		Vetor ponto = distParaAlvo.divide((double) pontos);
		System.out.println("ponto" + ponto);
		System.out.println();
		pontosASeguir.add(posicionarVetor(ponto));
		System.out.println(pontosASeguir.get(0));
		for (int i = 1; i < pontos; i++) {
			Vetor pontoSeguinte = (posicionarVetor(ponto.multiplica(i)));
			pontosASeguir.add(pontoSeguinte);
		}
	}

	private void controlarRover() throws IOException, RPCException, InterruptedException, StreamException {
		while (executandoAutoRover) {
			try {
				definirVetorDirecao();
				ctrlAceleracao.setEntradaPID(velocidadeRover.get());
				checarTerreno();
				logarDados();
			} catch (Exception erro) {
				GUI.setStatus("Sem alvo selecionado");
				executandoAutoRover = false;
			}
			carregarBaterias();
			if (!carregando) {
				if (posicaoAlvo.Magnitude3d() > limiteDistanciaAlvo) {
					if (rover.getControl().getBrakes()) {
						rover.getControl().setBrakes(false);
					}
					acelerarRover(ctrlAceleracao.computarPID());
					pilotarRover();
				} else {
					rover.getControl().setBrakes(true);
					if (!pontosASeguir.isEmpty()) {
						pontosASeguir.remove(0);
					} else {
						if (!listaDeMarcadoresASeguir.isEmpty()) {
							if (!alvoMarcador.getHasContract()) {
								alvoMarcador.remove();
							}
							listaDeMarcadoresASeguir.remove(alvoMarcador);
							checarDistancia();
						} else {
							executandoAutoRover = false;
						}
					}
				}
			}
			Thread.sleep(250);
		}
		rover.getAutoPilot().disengage();
		velocidadeRover.remove();
		Thread.sleep(1000);
		MechPeste.finalizarTarefa();
	}

	private void acelerarRover(double arg) throws IOException, RPCException, StreamException {
		if (velocidadeRover.get() < (velocidadeMaxima * 1.01)) {
			rover.getControl().setBrakes(false);
			rover.getControl().setWheelThrottle((float) arg);
		} else {
			rover.getControl().setBrakes(true);
		}
	}

	private void pilotarRover() throws IOException, RPCException, StreamException {
		// Calcular diferença de angulo entre o alvo e o rover
		double diferencaAngulo = Math.abs(anguloAlvo - anguloRover);
		GUI.setParametros("diferenca", diferencaAngulo);
		if (velocidadeRover.get() > velocidadeCurva && diferencaAngulo < 20) {
			try {
				if (rover.getControl().getSpeedMode() == SpeedMode.TARGET) {
					rover.getControl().setSpeedMode(SpeedMode.SURFACE);
				}
				rover.getControl().setSAS(true);
				rover.getControl().setSASMode(SASMode.PROGRADE);
			} catch (Exception e) {
			}
		} else {
			rover.getControl().setSAS(false);
		}

		// Controlar a velocidade para fazer curvas
		if (diferencaAngulo > 20) {
			ctrlAceleracao.setLimitePID(velocidadeCurva);
		} else {
			ctrlAceleracao.setLimitePID(velocidadeMaxima);
		}
		if (diferencaAngulo > 3) {
			// Dirigir o Rover ao Alvo
			rover.getControl().setWheelSteering((float) ctrlDirecao.computarPID());
		} else {
			rover.getControl().setWheelSteering(0f);
		}
	}

	private void definirVetorDirecao() throws IOException, RPCException {
		// Definir posicao do Alvo, sendo ele um Waypoint, ou um Vessel
		if (!pontosASeguir.isEmpty()) {
			posicaoAlvo = posParaRover(posicionarPonto(pontosASeguir.get(0)));
		} else {
			if (buscandoMarcadores) {
				posicaoAlvo = posParaRover(posicionarMarcador(alvoMarcador));
			} else {
				posicaoAlvo = posParaRover(new Vetor(naveAlvo.position(pontoRefSuperficie)));
			}
		}

		// Definir a direcao do Rover e do Trajeto
		direcaoRover = new Vetor(rover.direction(pontoRefRover));
		direcaoTrajeto = posicaoAlvo.Normalizar();
		// Definir o angulo entre os dois
		anguloAlvo = (Vetor.anguloDirecao(direcaoTrajeto));
		anguloRover = (Vetor.anguloDirecao(direcaoRover));
		ctrlDirecao.setEntradaPID(anguloRover * 0.5);
		ctrlDirecao.setLimitePID(anguloAlvo * 0.5);
	}

	private void checarTerreno() throws RPCException {
		Vetor dirEsq = new Vetor(rover.getParts().getRoot().direction(pontoRefRover)).soma(new Vetor(-0.2, 0.0, -1));
		Vetor dirDir = new Vetor(rover.getParts().getRoot().direction(pontoRefRover)).soma(new Vetor(0.2, 0.0, -1));
		double distEsq = centroEspacial.raycastDistance(rover.position(pontoRefRover), dirEsq.paraTriplet(),
				pontoRefSuperficie);
		double distDir = centroEspacial.raycastDistance(rover.position(pontoRefRover), dirDir.paraTriplet(),
				pontoRefSuperficie);

//		rover.getControl().setRoll((float) -(distEsq - distDir));
//		System.out.println("distEsq " + distEsq);
//		System.out.println("distDir " + distDir);
//		System.out.println("difDeE  " + (distEsq - distDir));
	}

	private void logarDados() throws IOException, RPCException, StreamException {
		if (buscandoMarcadores) {
			distParaAlvo = posParaRover(posicionarMarcador(alvoMarcador));
		} else {
			distParaAlvo = posParaRover(new Vetor(naveAlvo.position(pontoRefSuperficie)));
		}
		double distanciaRestante = distParaAlvo.Magnitude3d();
		double mudancaDeTempo = tempoDoJogo.get() - tempoAnterior;
		if (mudancaDeTempo > 1) {
			kmsPercorridos += (float) (mudancaDeTempo * velocidadeRover.get());
			tempoRestante = distanciaRestante / velocidadeMaxima;
			tempoAnterior = tempoDoJogo.get();
			GUI.setParametros("distancia", distanciaRestante);
			GUI.setParametros("distPercorrida", kmsPercorridos);
			GUI.setParametros("tempoRestante", tempoRestante);
//			double teste = centroEspacial
//					.raycastDistance(
//							new Triplet<Double, Double, Double>(0.0,
//									rover.boundingBox(pontoRefRover).getValue1().getValue1(), 0.0),
//							new Triplet<Double, Double, Double>(0.0, 1.0, 0.0), pontoRefRover);
//
//			System.out.println(teste);

		}
	}

	private Vetor posicionarMarcador(Waypoint marcador) throws RPCException {
		return new Vetor(rover.getOrbit().getBody().surfacePosition(marcador.getLatitude(), marcador.getLongitude(),
				pontoRefSuperficie));
	}

	private Vetor posicionarPonto(Vetor vetor) throws RPCException {
		return new Vetor(rover.getOrbit().getBody().surfacePosition(
				rover.getOrbit().getBody().latitudeAtPosition(vetor.paraTriplet(), pontoRefOrbital),
				rover.getOrbit().getBody().longitudeAtPosition(vetor.paraTriplet(), pontoRefOrbital),
				pontoRefSuperficie));
	}

	private Vetor posParaRover(Vetor vetor) throws IOException, RPCException {
		return new Vetor(centroEspacial.transformPosition(vetor.paraTriplet(), pontoRefSuperficie, pontoRefRover));
	}

	private Vetor posicionarVetor(Vetor vetor) throws IOException, RPCException {
		return new Vetor(centroEspacial.transformPosition(vetor.paraTriplet(), pontoRefSuperficie, pontoRefOrbital));
	}

	public static void setAlvo(String alvo) {
		nomeMarcador = alvo;
	}

	public static void setVelMaxima(float velMax) {
		velocidadeMaxima = velMax;
	}
}
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.javatuples.Triplet;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.Waypoint;
import krpc.client.services.SpaceCenter.WaypointManager;
import krpc.client.services.Drawing;
import krpc.client.services.Drawing.Line;
import krpc.client.services.Drawing.Text;

// Módulo de Piloto automático de Rovers
// Autor: Renan Torres <pesterenan@gmail.com>
// Data: 14/02/2019

public class RoverPST {
	private static final int DISTANCIA_DE_PROCURA = 10000;
	// Declaração de variáveis:
	static Connection conexao;
	static SpaceCenter centroEspacial;
	Drawing desenhos;
	Line linhaAngulo;
	Line linhaDirecao;
	Text textoAngulo;
	WaypointManager gerenciadorMarcadores;
	List<Waypoint> listaMarcadoresASeguir = new ArrayList<Waypoint>();
	Vessel rover;
	Waypoint alvo;
	ReferenceFrame pontoRefOrbita;
	ReferenceFrame pontoRefSuperficie;
	Flight parametrosRover;
	Vetor posicaoRover;
	Vetor posicaoAlvo;
	Vetor direcaoRover;
	Vetor distanciaEntrePontos;
	float anguloAlvo = 0;
	float anguloRover = 0;
	float limiteDistanciaAlvo = 50;
	float velocidadeMaxima = 20;
	float velocidadeCurva = 5;
	ControlePID ctrlDirecao = new ControlePID();
	ControlePID ctrlAceleracao = new ControlePID();
	ControlePID ctrlDistancia = new ControlePID();

	public static void main(String[] args) throws IOException, RPCException, InterruptedException {
		conexao = Connection.newInstance("RoverPST");
		new RoverPST(conexao);
	}

	public RoverPST(Connection conexao) throws IOException, RPCException, InterruptedException {
		centroEspacial = SpaceCenter.newInstance(conexao);
		gerenciadorMarcadores = centroEspacial.getWaypointManager();
		desenhos = Drawing.newInstance(conexao);
		rover = centroEspacial.getActiveVessel();
		// REFERENCIA PARA BUSCAR ANGULO DE DIREÇÃO DO ROVER:
		pontoRefOrbita = rover.getOrbit().getBody().getReferenceFrame();
		// REFERENCIA PARA BUSCAR POSICOES DE ALVO:
		pontoRefSuperficie = rover.getSurfaceReferenceFrame();
		parametrosRover = rover.flight(pontoRefOrbita);
		iniciarPIDs();
		definirAlvo();
		desenharVetor();

		controlarRover();
	}

	private void iniciarPIDs() {
		ctrlAceleracao.ajustarPID(2.0, 0.001, 0.1);
		ctrlDirecao.ajustarPID(0.01, 0.001, 0.001);
		ctrlDirecao.limitarSaida(-1, 1);
		ctrlDistancia.ajustarPID(1.0, 0.001, 0.1);
		ctrlDistancia.limitarSaida(-1, 1);
	}

	private void definirAlvo() throws IOException, RPCException {
		posicaoRover = new Vetor(posicionarTupla(rover.position(pontoRefSuperficie)));
		listarMarcadores("ALVO");
		checarDistancia();
		
	}

	private void listarMarcadores(String nome) throws RPCException, IOException {
		for (Waypoint marcador : gerenciadorMarcadores.getWaypoints()) {
			if (marcador.getName().startsWith(nome)) {
				listaMarcadoresASeguir.add(marcador);
			}
		}
	}

	private void checarDistancia() throws RPCException, IOException {
		double distanciaProcura = DISTANCIA_DE_PROCURA;
		double distanciaMarcadorAlvo = 0;
		for (Waypoint marcador : listaMarcadoresASeguir) {
			Vetor marcadorPos = new Vetor(posicionarTupla(posicionarMarcador(marcador)));
			double distanciaMarcador = marcadorPos.subtrai(posicaoRover).Magnitude();
			if (distanciaProcura > distanciaMarcador) {
				distanciaProcura = distanciaMarcador;
				alvo = marcador;
				distanciaMarcadorAlvo = distanciaMarcador;
			}
		}
		System.out.println("Est� indo em dire��o a: " + alvo.getName());
		System.out.println("Marcador �: " + String.format("%s metros ", String.valueOf(distanciaMarcadorAlvo).substring(0, String.valueOf(distanciaMarcadorAlvo).indexOf('.') + 3))+ "de dist�ncia.");
		
	}

	private void logarDados() throws IOException, RPCException {
		atualizaVetor();
	}

	private void controlarRover() throws IOException, RPCException, InterruptedException {
		while (true) {
			try {
				definirVetorDirecao(alvo);
				informarCtrlPID();
				if (distanciaEntrePontos.Magnitude3d() > limiteDistanciaAlvo) {
					float distancia = 1;
					if (rover.getControl().getBrakes()) {
						rover.getControl().setBrakes(false);
					}
					if (distanciaEntrePontos.Magnitude3d() < limiteDistanciaAlvo * 3) {
						distancia = (float) ctrlDistancia.computarPID();
					}
					acelerarRover(ctrlAceleracao.computarPID() * distancia);
					pilotarRover();
				} else {
					rover.getControl().setBrakes(true);
					if (parametrosRover.getHorizontalSpeed() < 2) {
						listaMarcadoresASeguir.remove(alvo);
						checarDistancia();
					}
					if (listaMarcadoresASeguir.isEmpty()) {
						break;
					}
				}
			} catch (Exception erro) {
				System.out.println("Sem alvo selecionado");
			}
			logarDados();
			Thread.sleep(100);
		}
		System.out.println("MISS�O CUMPRIDA!");
		rover.getAutoPilot().disengage();
		conexao.close();
	}

	private void acelerarRover(double arg) throws IOException, RPCException {
		rover.getControl().setWheelThrottle((float) arg);
	}

	private void pilotarRover() throws IOException, RPCException {
		ctrlDirecao.setEntradaPID(anguloRover);
		ctrlDirecao.setLimitePID(anguloAlvo);
		float diferencaAngulo = Math.abs(anguloRover - anguloAlvo);
		if (diferencaAngulo > 30) {
			ctrlAceleracao.setLimitePID(velocidadeCurva);
		} else {
			ctrlAceleracao.setLimitePID(velocidadeMaxima);
		}
		if (diferencaAngulo > 1) {
			if (diferencaAngulo > 180) {
				rover.getControl().setWheelSteering((float) -ctrlDirecao.computarPID());
			} else {
				rover.getControl().setWheelSteering((float) ctrlDirecao.computarPID());
			}
		} else {
			rover.getControl().setWheelSteering(0f);
		}
	}

	private void informarCtrlPID() throws IOException, RPCException {
		ctrlDistancia.setEntradaPID(-distanciaEntrePontos.Magnitude3d() + limiteDistanciaAlvo);
		ctrlDistancia.setLimitePID(0);
		ctrlAceleracao.setEntradaPID(parametrosRover.getHorizontalSpeed());
	}

	private void definirVetorDirecao(Waypoint marcador) throws IOException, RPCException {
		posicaoRover = new Vetor(posicionarTupla(rover.position(pontoRefSuperficie)));
		posicaoAlvo = new Vetor(posicionarTupla(posicionarMarcador(marcador)));

		direcaoRover = new Vetor(rover.direction(pontoRefOrbita));
		distanciaEntrePontos = posicaoAlvo.subtrai(posicaoRover);

		anguloAlvo = Vetor.anguloDirecao(distanciaEntrePontos);
		anguloRover = Vetor.anguloDirecao(direcaoRover);
	}

	private Triplet<Double, Double, Double> posicionarMarcador(Waypoint marcador) throws RPCException {
		Triplet<Double, Double, Double> tuplaRetorno;
		tuplaRetorno = rover.getOrbit().getBody().surfacePosition(marcador.getLatitude(), marcador.getLongitude(),
				pontoRefSuperficie);
		return tuplaRetorno;
	}

	private Triplet<Double, Double, Double> posicionarTupla(Triplet<Double, Double, Double> tupla)
			throws IOException, RPCException {
		Triplet<Double, Double, Double> tuplaPosicionada = centroEspacial.transformPosition(tupla, pontoRefSuperficie,
				pontoRefOrbita);
		return tuplaPosicionada;
	}

	private void desenharVetor() throws IOException, RPCException {
		linhaDirecao = desenhos.addDirection(new Triplet<Double, Double, Double>(0.0, 1.0, 0.0),
				rover.getReferenceFrame(), 100, true);
		linhaAngulo = desenhos.addLine(new Triplet<Double, Double, Double>(0.0, 0.0, 0.0),
				new Triplet<Double, Double, Double>(1.0, 1.0, 0.0), pontoRefOrbita, true);
		textoAngulo = desenhos.addText("ANGULO", pontoRefOrbita, rover.position(pontoRefOrbita),
				rover.rotation(pontoRefOrbita), true);
	}

	private void atualizaVetor() throws IOException, RPCException {
		if (alvo != null) {
			linhaAngulo.setColor(new Triplet<Double, Double, Double>(0.5, 0.0, 0.0));
			linhaAngulo.setStart(rover.position(pontoRefOrbita));
			linhaAngulo.setEnd(posicionarTupla(posicionarMarcador(alvo)));
			linhaDirecao.setReferenceFrame(rover.getReferenceFrame());
			textoAngulo.setPosition(rover.position(pontoRefOrbita));
			textoAngulo.setRotation(rover.rotation(pontoRefOrbita));
			textoAngulo.setContent(String.valueOf(distanciaEntrePontos.Magnitude()));
		}
	}
}
package com.pesterenan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.javatuples.Triplet;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.Drawing;
import krpc.client.services.Drawing.Line;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.Waypoint;
import krpc.client.services.SpaceCenter.WaypointManager;

public class StarHopper extends JFrame implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int ALTITUDE_HOVER = 500;
	private static final double LIMITE_TWR = 1.1;
	Connection conexao;
	SpaceCenter centroEspacial;
	private Vessel naveAtual;
	private Flight parametrosVoo;
	private ControlePID ctrlAceleracao = new ControlePID();
	private ControlePID ctrlTWR = new ControlePID();
	private ControlePID ctrlDistancia = new ControlePID();
	private Stream<Double> altitude;
	private Stream<Double> velocidadeHorizontal;
	private Stream<Float> massaTotal;
	private float acelGravidade;
	double valorTWR = 1.0;
	double acelMax = 0;

	private JLabel labelPosZ;
	private JLabel labelPosX;
	private JLabel labelPosY;
	private JLabel labelInclinacao;
	private JLabel labelVelHoriz;
	private JLabel labelAcel;
	private JLabel labelTWR;

	private ReferenceFrame pontoRefOrbital;
	private ReferenceFrame pontoRefSuperficie;

	Vetor direcaoAlvo = new Vetor(1.0, 0, 0);

	WaypointManager gerenciadorMarcadores;
	List<Waypoint> listaMarcadoresASeguir = new ArrayList<Waypoint>();
	private Vessel alvo;

	Triplet<Double, Double, Double> teste;

	Drawing desenhos;
	Line linhaDirecao;
	double anguloAlvo = 0;

	private double distanciaAlvo;

	private double distanciaVelocidade;

	private Vetor direcaoAlvoContraria;

	private float anguloAlvoContraria;

	public static void main(String[] args) throws RPCException, IOException, StreamException, InterruptedException {
		new StarHopper();
	}

	private StarHopper() throws RPCException, IOException, StreamException, InterruptedException {
		super("Star Hopper");
//		setSize(200, 100);
//		setLayout(new BorderLayout());
//		setDefaultCloseOperation(EXIT_ON_CLOSE);
//		setLocation(500, 500);
//		setVisible(true);
//		JPanel painel = new Paineis().PainelParametros();
//		add(painel);
		// Conexão e Parâmetros:
		conexao = Connection.newInstance("StarHopper - MechPeste");
		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = centroEspacial.getActiveVessel();
		pontoRefOrbital = naveAtual.getOrbit().getBody().getReferenceFrame();
		pontoRefSuperficie = naveAtual.getSurfaceReferenceFrame();
		parametrosVoo = naveAtual.flight(pontoRefOrbital);

		gerenciadorMarcadores = centroEspacial.getWaypointManager();
		desenhos = Drawing.newInstance(conexao);
		// Streams e Variáveis:
		altitude = conexao.addStream(parametrosVoo, "getSurfaceAltitude");
		velocidadeHorizontal = conexao.addStream(parametrosVoo, "getHorizontalSpeed");
		massaTotal = conexao.addStream(naveAtual, "getMass");
		acelGravidade = naveAtual.getOrbit().getBody().getSurfaceGravity();

		// Executar Métodos:
		naveAtual.getAutoPilot().engage();
		naveAtual.getAutoPilot().setTargetPitch(90);
		criarJanelaInfo();
		acertarCtrlPID();
		desenharVetor();

		while (true) {
			// Calcular parametros antes de tudo:
			calcularParametros();

			// Acelerar a Nave de acordo com a altitude e o TWR limitado:
			naveAtual.getControl().setThrottle((float) ((ctrlAceleracao.computarPID()) * ctrlTWR.computarPID()));

			// Inclinar a Nave na direção do alvo:
			if (altitude.get() > (ALTITUDE_HOVER * 0.5)) {
				if (distanciaVelocidade * 2 > distanciaAlvo) {
					naveAtual.getAutoPilot().setTargetHeading((float) anguloAlvoContraria);
				} else {
					naveAtual.getAutoPilot().setTargetHeading((float) anguloAlvo);
				}
				naveAtual.getAutoPilot().setTargetPitch((float) (90 - ctrlDistancia.computarPID()));
			}
			Thread.sleep(150);
		}

		// conexao.close();

	}

	private void acertarCtrlPID() throws RPCException, StreamException, IOException {
		ctrlAceleracao.ajustarPID(0.05, 0.001, 2);
		ctrlAceleracao.limitarSaida(0, 1);
		ctrlAceleracao.setLimitePID(ALTITUDE_HOVER);
		ctrlTWR.ajustarPID(0.01, 0.001, 0.1);
		ctrlTWR.limitarSaida(0.9, 1.1);
		ctrlTWR.setLimitePID(LIMITE_TWR);
		ctrlDistancia.ajustarPID(1, 0.001, 10);
		ctrlDistancia.limitarSaida(5, 15);

	}

	private void calcularParametros() throws RPCException, StreamException, IOException {
		// Buscar Alvo:
		try {
			alvo = centroEspacial.getTargetVessel();
		} catch (Exception erro) {
			System.out.println("Sem nave como alvo");
		}
		try {
			float empuxo = 0f;
			if (naveAtual.getThrust() == 0) {
				empuxo = naveAtual.getMaxThrust();
			} else {
				empuxo = naveAtual.getAvailableThrust();
			}
			empuxo = naveAtual.getThrust();
			valorTWR = empuxo / (massaTotal.get() * acelGravidade);
			acelMax = (valorTWR * acelGravidade) - acelGravidade;
		} catch (Exception erro) {
			valorTWR = 1;
		}
		// Calcular Altitude da Nave e TWR:
		ctrlAceleracao.setEntradaPID(altitude.get());
		ctrlTWR.setEntradaPID(valorTWR);
		ctrlDistancia.setEntradaPID(distanciaVelocidade);
		ctrlDistancia.setLimitePID(distanciaAlvo);

		labelAcel.firePropertyChange("Acel", 0, naveAtual.getControl().getThrottle());
		labelVelHoriz.firePropertyChange("VH", 0, velocidadeHorizontal.get());
		labelInclinacao.firePropertyChange("Inc", 0, naveAtual.getAutoPilot().getTargetPitch());
		labelTWR.firePropertyChange("TWR", 0, ctrlTWR.computarPID());
		labelPosX.firePropertyChange("PosX", 0, naveAtual.position(pontoRefOrbital).getValue0());
		checarDistancia();
		atualizaVetor();
	}

	private void checarDistancia() throws RPCException, IOException {
		// Distancia do Alvo
		direcaoAlvo = Vetor.direcaoAlvo(naveAtual.position(pontoRefSuperficie),
				centroEspacial.transformPosition(alvo.position(pontoRefOrbital), pontoRefOrbital, pontoRefSuperficie));
		direcaoAlvoContraria = Vetor.direcaoAlvoContraria(naveAtual.position(pontoRefSuperficie),
				centroEspacial.transformPosition(alvo.position(pontoRefOrbital), pontoRefOrbital, pontoRefSuperficie));
		distanciaAlvo = direcaoAlvo.Magnitude();

		// Distancia da Velocidade
		Vetor vetorVelocidade = new Vetor(parametrosVoo.getVelocity());
		distanciaVelocidade = vetorVelocidade.Magnitude3d() * (parametrosVoo.getHorizontalSpeed() * 0.5);

		// Angulo de Heading
		anguloAlvo = Vetor.anguloDirecao(direcaoAlvo);
		anguloAlvoContraria = Vetor.anguloDirecao(direcaoAlvoContraria);

		labelPosX.firePropertyChange("PosX", 0, naveAtual.getAutoPilot().getTargetHeading());
		labelPosY.firePropertyChange("PosY", 0, distanciaVelocidade);
		labelPosY.firePropertyChange("PosZ", 0, distanciaAlvo);

		// System.out.println("Est� indo em dire��o a: " + alvo.getName());
	}

	private void criarJanelaInfo() {
		JFrame jv = new JFrame();
		JPanel p = new JPanel();
		jv.setBounds(0, 0, 200, 200);
		labelAcel = new JLabel();
		labelAcel.addPropertyChangeListener(this);
		labelTWR = new JLabel();
		labelTWR.addPropertyChangeListener(this);
		labelInclinacao = new JLabel();
		labelInclinacao.addPropertyChangeListener(this);
		labelVelHoriz = new JLabel();
		labelVelHoriz.addPropertyChangeListener(this);
		labelPosX = new JLabel("Pos X: ", SwingConstants.LEFT);
		labelPosX.addPropertyChangeListener(this);
		labelPosY = new JLabel("Angulo Alvo: ", SwingConstants.LEFT);
		labelPosY.addPropertyChangeListener(this);
		labelPosZ = new JLabel("Distancia Alvo: ", SwingConstants.LEFT);
		labelPosZ.addPropertyChangeListener(this);
		p.add(labelAcel);
		p.add(labelTWR);
		p.add(labelInclinacao);
		p.add(labelVelHoriz);
		p.add(labelPosX);
		p.add(labelPosY);
		p.add(labelPosZ);
		jv.add(p);
		jv.setVisible(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if ("Acel".equals(arg0.getPropertyName())) {
			labelAcel.setText("Acelera��o: " + String.valueOf(arg0.getNewValue()));
			labelAcel.updateUI();
		}
		if ("TWR".equals(arg0.getPropertyName())) {
			labelTWR.setText("TWR: " + String.valueOf(arg0.getNewValue()));
			labelTWR.updateUI();
		}
		if ("Inc".equals(arg0.getPropertyName())) {
			labelInclinacao.setText("Inclina��o: " + String.valueOf(arg0.getNewValue()));
			labelInclinacao.updateUI();
		}
		if ("VH".equals(arg0.getPropertyName())) {
			labelVelHoriz.setText("Vel Horiz.: " + String.valueOf(arg0.getNewValue()));
			labelVelHoriz.updateUI();
		}
		if ("PosX".equals(arg0.getPropertyName())) {
			labelPosX.setText("Direção: " + String.valueOf(arg0.getNewValue()));
			labelPosX.updateUI();
		}
		if ("PosY".equals(arg0.getPropertyName())) {
			labelPosY.setText("Distancia Vel: " + String.valueOf(arg0.getNewValue()));
			labelPosY.updateUI();
		}
		if ("PosZ".equals(arg0.getPropertyName())) {
			labelPosZ.setText("Distancia: " + String.valueOf(arg0.getNewValue()));
			labelPosZ.updateUI();
		}

	}

	private void desenharVetor() throws IOException, RPCException {
		linhaDirecao = desenhos.addDirection(Vetor.paraTriplet(direcaoAlvo), naveAtual.getSurfaceReferenceFrame(), 10,
				true);
	}

	private void atualizaVetor() throws IOException, RPCException {
		linhaDirecao.setStart((naveAtual.position(pontoRefSuperficie)));
		linhaDirecao.setEnd(
				centroEspacial.transformPosition(alvo.position(pontoRefOrbital), pontoRefOrbital, pontoRefSuperficie));

//		if (alvo != null) {
//			linhaDirecao.setColor(new Triplet<Double, Double, Double>(0.5, 0.0, 0.0));
//			linhaDirecao.setStart(naveAtual.position(pontoRefOrbital));
//			// linhaDirecao.setEnd(Vetor.paraTriplet(posicaoNave));
//		}
	}
}

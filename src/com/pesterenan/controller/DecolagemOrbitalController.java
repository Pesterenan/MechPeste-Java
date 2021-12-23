package com.pesterenan.controller;

import com.pesterenan.gui.StatusJPanel;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Status;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

public class DecolagemOrbitalController extends TelemetriaController implements Runnable {

	private final float INC_PARA_CIMA = 90;

	private float altInicioCurva = 100;
	private float altApoastroFinal = 80000;
	private float inclinacaoAtual = 90;
	private float direcao = 90;
	private ControlePID aceleracaoCtrl = new ControlePID();

	public DecolagemOrbitalController(Connection con) {
		super(con);
		aceleracaoCtrl.setAmostraTempo(100);
		aceleracaoCtrl.ajustarPID(0.05, 0.1, 1);
	}

	@Override
	public void run() {
		try {
			decolagem();
			curvaGravitacional();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void curvaGravitacional() throws RPCException, StreamException, InterruptedException {
		naveAtual.getControl().setThrottle(1f);
		naveAtual.getAutoPilot().engage();
		naveAtual.getAutoPilot().targetPitchAndHeading(inclinacaoAtual, getDirecao());
		// Limitar a aceleração no apoastro final
		aceleracaoCtrl.limitarSaida(0.1, 1.0);
		
		while (inclinacaoAtual > 1) {
			if (altitude.get() > altInicioCurva && altitude.get() < getAltApoastroFinal()) {
				double progresso = (altitude.get() - altInicioCurva) / (getAltApoastroFinal() - altInicioCurva);
				double incrementoCircular = Math.sqrt(1 - Math.pow(progresso - 1, 2));
				inclinacaoAtual = (float) (INC_PARA_CIMA - (incrementoCircular * INC_PARA_CIMA));
				naveAtual.getAutoPilot().targetPitchAndHeading((float) inclinacaoAtual, getDirecao());
				StatusJPanel.setStatus(String.format("A inclinação do foguete é: %.1f", inclinacaoAtual));
				// Informar ao Controlador PID da aceleração a porcentagem do caminho
				aceleracaoCtrl.setEntradaPID((apoastro.get() * 100 / getAltApoastroFinal()));
				// Acelerar a nave de acordo com o PID, ou cortar o motor caso passe do apoastro
				if (apoastro.get() < getAltApoastroFinal()) {
					naveAtual.getControl().setThrottle((float) aceleracaoCtrl.computarPID());					
				} else {
					naveAtual.getControl().setThrottle(0.0f);
				}
			}
			Thread.sleep(100);
		}
		StatusJPanel.setStatus(Status.PRONTO.get());
		naveAtual.getAutoPilot().disengage();
	}

	private void decolagem() throws RPCException, InterruptedException {
		naveAtual.getControl().setSAS(true);
		naveAtual.getControl().setThrottle(1f);
		float contagemRegressiva = 5f;
		for (; contagemRegressiva > 0;) {
			StatusJPanel.setStatus(String.format("Lançamento em: %.1f segundos...", contagemRegressiva));
			contagemRegressiva -= 0.1;
			Thread.sleep(100);
		}
		StatusJPanel.setStatus("Decolagem!");
		naveAtual.getControl().activateNextStage();
		Thread.sleep(1000);
		StatusJPanel.setStatus(Status.PRONTO.get());
	}

	public float getDirecao() {
		return direcao;
	}

	public void setDirecao(float direcao) {
		if (direcao > 360) {
			this.direcao = 360;
		} else if (direcao < 0) {
			this.direcao = 0;
		} else {
		this.direcao = direcao;
		}
	}

	public float getAltApoastroFinal() {
		return altApoastroFinal;
	}

	public void setAltApoastroFinal(float altApoastroFinal) {
		if (altApoastroFinal < 10000) {
			this.altApoastroFinal = 10000;
		} else if (altApoastroFinal > 2000000) {
			this.altApoastroFinal = 2000000;
		} else {
		this.altApoastroFinal = altApoastroFinal;
		}
	}

//	// Streams de conexao com a nave:
//	double pressaoAtual;
//	// Parametros de voo:
//	private float altInicioCurva = 100;
//	public static float altApoastroFinal = 80000;
//
//	private static int direcao = 90;
//	private int inclinacao = 90;
//	private int etapaAtual = 0;
//	private double anguloGiro = 0;
//	private boolean executando = true;
//	private static boolean abortar = false;
//	private ManobrasController manobras;
//	ControlePID ctrlAcel = new ControlePID();

//	public void decolagem() throws RPCException, StreamException, IOException, InterruptedException {
//		iniciarScript();
//// Loop principal de subida
//		while (isExecutando()) { // loop while sempre funcionando at� um break
//			switch (etapaAtual) {
//			case 0:
//				decolar();
//				break;
//			case 1:
//				giroGravitacional();
//				break;
//			case 2:
//				planejarOrbita();
//				break;
//			case 3:
//				GUI.setStatus(Status.PRONTO.get());
//				etapaAtual = 0;
//				setExecutando(false);
//				break;
//			}
//			if (abortar) {
//				finalizarScript();
//			}
//
//			atualizarParametros();
//			Thread.sleep(50);
//		}
//		finalizarScript();
//	}
//
//	public boolean isExecutando() {
//		return this.executando;
//	}
//
//	public void setExecutando(boolean executando) {
//		this.executando = executando;
//	}
//
//	private void iniciarScript() throws RPCException, StreamException, IOException, InterruptedException {
//		// Iniciar Conexão:
//		naveAtual.getAutoPilot().setReferenceFrame(naveAtual.getSurfaceReferenceFrame());
//		manobras = new ManobrasController(false);
//		ctrlAcel.setAmostraTempo(25);
//		ctrlAcel.setLimitePID(20);
//		ctrlAcel.ajustarPID(0.25, 0.01, 0.025);
//		ctrlAcel.limitarSaida(0.1, 1.0);
//		GUI.setParametros("nome", naveAtual.getName());
//
//	}
//
//	private void decolar() throws RPCException, StreamException, InterruptedException {
//		GUI.setStatus("Iniciando Decolagem...");
//		naveAtual.getControl().setSAS(false); // desligar SAS
//		naveAtual.getControl().setRCS(false); // desligar RCS
//		// Ligar Piloto Automatico e Mirar a Direção:
//		naveAtual.getAutoPilot().engage(); // ativa o piloto auto
//		naveAtual.getAutoPilot().targetPitchAndHeading(inclinacao, getDirecao()); // direção
//		GUI.setStatus("Lançamento!");
//		if (naveAtual.getSituation().equals(VesselSituation.PRE_LAUNCH)) {
//			aceleracao(1.0f); // acelerar ao máximo
//			naveAtual.getControl().activateNextStage();
//		} else {
//			aceleracao(1.0f); // acelerar ao máximo
//		}
//		etapaAtual = 1;
//
//	}
//
//	private void giroGravitacional() throws RPCException, StreamException, InterruptedException {
//		double altitudeAtual = altitudeSup.get();
//		double apoastroAtual = apoastro.get();
//		pressaoAtual = parametrosDeVoo.getDynamicPressure() / 1000;
//		ctrlAcel.setEntradaPID(pressaoAtual);
//		if (altitudeAtual > altInicioCurva && altitudeAtual < getAltApoastroFinal()) {
//			double progresso = (altitudeAtual - altInicioCurva) / (getAltApoastroFinal() - altInicioCurva);
//			double incrementoCircular = Math.sqrt(1 - Math.pow(progresso - 1, 2));
//			double novoAnguloGiro = incrementoCircular * inclinacao;
//			if (Math.abs(novoAnguloGiro - anguloGiro) > 0.1) {
//				anguloGiro = novoAnguloGiro;
//				naveAtual.getAutoPilot().targetPitchAndHeading((float) (inclinacao - anguloGiro), getDirecao());
//				aceleracao((float) ctrlAcel.computarPID());
//				GUI.setStatus(String.format("�ngulo de Inclina��o: %1$.1f �", anguloGiro));
//			}
//		}
//		// Diminuir acelera��o ao chegar perto do apoastro
//		if (apoastroAtual > getAltApoastroFinal() * 0.95) {
//			GUI.setStatus("Se aproximando do apoastro...");
//			ctrlAcel.setEntradaPID(apoastroAtual);
//			ctrlAcel.setLimitePID(getAltApoastroFinal());
//			aceleracao((float) ctrlAcel.computarPID());
//		}
//		// Sair do giro ao chegar na altitude de apoastro:
//		if (apoastroAtual >= getAltApoastroFinal()) {
//			naveAtual.getControl().setSAS(true);
//			GUI.setStatus("Apoastro alcan�ado.");
//			aceleracao(0.0f);
//			Thread.sleep(25);
//			etapaAtual = 2;
//		}
//	}
//
//	private void planejarOrbita() throws RPCException, StreamException, InterruptedException, IOException {
//		GUI.setStatus("Esperando sair da atmosfera.");
//		if (altitude.get() > (getAltApoastroFinal() * 0.8)) {
//			GUI.setStatus("Planejando Manobra de circulariza��o...");
//			Node noDeManobra = manobras.circularizarApoastro();
//			double duracaoDaQueima = manobras.calcularTempoDeQueima(noDeManobra);
//			manobras.orientarNaveParaNoDeManobra(noDeManobra);
//			GUI.setStatus("Executando Manobra de circulariza��o...");
//			manobras.executarQueima(noDeManobra, duracaoDaQueima);
//			naveAtual.getAutoPilot().disengage();
//			naveAtual.getControl().setSAS(true);
//			naveAtual.getControl().setRCS(false);
//			noDeManobra.remove();
//			etapaAtual = 3;
//		}
//	}
//
//	private void aceleracao(float acel) throws RPCException {
//		naveAtual.getControl().setThrottle((float) acel);
//	}
//
//	private void atualizarParametros() throws RPCException, StreamException {
//		GUI.setParametros("altitude", altitude.get());
//		GUI.setParametros("apoastro", apoastro.get());
//		GUI.setParametros("periastro", periastro.get());
//	}
//
//	private void finalizarScript() throws RPCException, IOException {
//		setExecutando(false);
//		setAbortar(false);
//	}
//
//	public static void setAltApoastro(float apoastroFinal) {
//		altApoastroFinal = apoastroFinal;
//
//	}
//
//	public static void setDirecao(int direcaoOrbita) {
//		direcao = direcaoOrbita;
//
//	}
//
//	public static void setAbortar(boolean estado) {
//		abortar = estado;
//		System.out.println("Voo abortado");
//	}
//
//	public static float getAltApoastroFinal() {
//		return altApoastroFinal;
//	}
//
//	public static int getDirecao() {
//		return direcao;
//	}

}
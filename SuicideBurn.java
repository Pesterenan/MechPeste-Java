import java.io.IOException;
import java.util.List;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;

public class SuicideBurn extends MechPeste{

	// DECLARAÇÃO DE VARIAVEIS GLOBAIS
	static Connection conexao;
	static SpaceCenter centroEspacial;
	private Vessel naveAtual;
	private ReferenceFrame pontoRef;
	private Flight vooNave;
	private Stream<Double> altitudeNave;
	private Stream<Float> massaTotalNave;
	private Stream<Double> velVertNave;
	private ControlePID controleAcel = new ControlePID();
	private ControlePID controlePouso = new ControlePID();
	private Navegacao nav;
	double distanciaPouso = 50.0; // Altura pra começar o Hover
	float forcaGravidade;
	boolean executandoSuicideBurn = false;
	boolean podePousar = false;
	double naveTWRMax = 1.0;
	double acelMax = 0;
	double empuxoTotal = 0;
	double distanciaDaQueima = 0.0;
	double tempoDaQueima = 0;
	float novaAcel = 0;

	
	public static void main(String[] args) throws IOException, RPCException, InterruptedException, StreamException {
		new SuicideBurn();
	}

	public SuicideBurn()  throws StreamException, RPCException, IOException, InterruptedException {
		// Inicialização das variáveis:
		conexao = Connection.newInstance("Suicide Burn - MechPeste");
		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = centroEspacial.getActiveVessel(); // objeto da nave
		pontoRef = naveAtual.getOrbit().getBody().getReferenceFrame();
		vooNave = naveAtual.flight(pontoRef);
		altitudeNave = conexao.addStream(vooNave, "getSurfaceAltitude");
		velVertNave = conexao.addStream(vooNave, "getVerticalSpeed");
		massaTotalNave = conexao.addStream(naveAtual, "getMass");
		forcaGravidade = naveAtual.getOrbit().getBody().getSurfaceGravity();

		System.out.println("Nave Atual: " +  naveAtual.getName());
		System.out.println("Situação da nave: " + naveAtual.getSituation().toString());
		System.out.println("Força da Gravidade Atual: " + forcaGravidade + "Corpo Celeste: " + naveAtual.getOrbit().getBody().getName());
		calcularParametros();
		System.out.println("Força de TWR da Nave: " + naveTWRMax);
		controleAcel.setAmostraTempo(40);
		controlePouso.setAmostraTempo(40);
		controleAcel.ajustarPID(0.025, 0.001, 0.025);
		controlePouso.ajustarPID(0.1, 0.001, 0.1);
		// Limitar aceleração da nave
		naveAtual.getAutoPilot().engage(); // LIGAR O PILOTO
		decolagemDeTeste();
		calcularParametros();
		controleAcel.limitarSaida(0, 1);
		controlePouso.limitarSaida(0.75 / naveTWRMax, 1);
		controlePouso.setLimitePID(0);
		nav = new Navegacao(centroEspacial, naveAtual);
		
		// Loop esperando para executar o Suicide Burn:
		while (!executandoSuicideBurn) {
			calcularParametros();
			podePousar = false;
			nav.MirarNave();
			if (altitudeNave.get() > 100 && altitudeNave.get() < 10000) {
				naveAtual.getControl().setBrakes(true);
			} else {
				naveAtual.getControl().setBrakes(false);
			}
			if (altitudeNave.get() < 4000) {
				naveAtual.getControl().setRCS(true);
			}
			// Checar altitude para o Suicide Burn:
			if (0 > altitudeNave.get() - distanciaDaQueima - distanciaPouso && velVertNave.get() < -1) {
				executandoSuicideBurn = true;
				System.out.println("Iniciando o Suicide Burn!");
			}
			Thread.sleep(100);
		}
		
		// Loop principal de Suicide Burn:
		while (executandoSuicideBurn) {
			// Calcula os valores de aceleração e TWR do foguete:
			calcularParametros();
			// Desce o trem de pouso da nave em menos de 100 metros
			if (altitudeNave.get() < 100) {
				naveAtual.getControl().setGear(true);
			}
			// Informa aos PIDs a altitude, limite e velocidade da nave
			controleAcel.setEntradaPID(altitudeNave.get());
			controleAcel.setLimitePID(distanciaDaQueima);
			controlePouso.setEntradaPID(velVertNave.get());
			// Aponta nave para o retrograde se a velocidade horizontal for maior que 1m/s
			if (vooNave.getHorizontalSpeed() > 0.2) {
				nav.MirarNave();
			} else {
				naveAtual.getAutoPilot().setTargetPitch(90);
			}
			if (altitudeNave.get() > distanciaPouso) {
				podePousar = false;
			} else {
				podePousar = true;
			}
			
			float correcaoAnterior = naveAtual.getControl().getThrottle();
			try{
				if (!podePousar) {
					aceleracao((float) (correcaoAnterior + controleAcel.computarPID() + 1 / naveTWRMax) / 3);
//					System.out.println("Valor Saída ACEL: " + controleAcel.computarPID());
				} else {
					aceleracao((float) controlePouso.computarPID());
//					System.out.println("Valor Saída POUSO: " + controlePouso.computarPID());
				}
			} catch (Exception erro) {
				System.out.println("Erro no cálculo da aceleração. Usando valor antigo. " + erro);
				aceleracao(correcaoAnterior);
			}
			// Verificar se o foguete pousou:
			checarPouso();
			Thread.sleep(50);
		} // Fim loop-while
	} // Fim código principal

	

	private void calcularParametros() throws RPCException, StreamException, IOException {
		try {
			if (naveAtual.getAvailableThrust() == 0) {
				empuxoTotal = naveAtual.getMaxThrust();	
			} else {
				empuxoTotal = 0.0;
				List<Engine> motores = naveAtual.getParts().getEngines();
				for (Engine motor : motores) {
					empuxoTotal += motor.getAvailableThrust();
				}
			}
			empuxoTotal = naveAtual.getMaxThrust();	// TESTAR
			naveTWRMax = empuxoTotal / (massaTotalNave.get() * forcaGravidade);
			acelMax = (naveTWRMax * forcaGravidade) - forcaGravidade;
			tempoDaQueima = Math.abs(velVertNave.get()) / acelMax;
			distanciaDaQueima = (Math.abs(velVertNave.get()) * tempoDaQueima + 1 / 2 * acelMax * Math.pow(tempoDaQueima, 2));

			// imprimir valor no console:
//			System.out.println("TWR         : " + naveTWRMax);
//			System.out.println("Dist. Queima: " + distanciaDaQueima);
//			System.out.println("Altitude Voo: " + altitudeNave.get());
//			System.out.println("Correção    : " + controleAcel.computarPID());
//			System.out.println("Vel Vert    : " + velVertNave.get());
		} catch (Exception erro) {
			naveTWRMax = 1;
			tempoDaQueima = 0;
			distanciaDaQueima = 0;
		}
	}
	

	private void decolagemDeTeste() throws StreamException, RPCException, IOException, InterruptedException{
		// Decola a nave se estiver na pista, ou pousada. Se estiver voando, apenas corta a aceleração.
		if (naveAtual.getSituation() == VesselSituation.LANDED
		 || naveAtual.getSituation() == VesselSituation.PRE_LAUNCH) {
			if (naveAtual.getSituation() == VesselSituation.PRE_LAUNCH) {
				naveAtual.getControl().activateNextStage();
			}
			naveAtual.getControl().setGear(false);
			aceleracao(1);
			while (altitudeNave.get() <= distanciaPouso) {
				naveAtual.getAutoPilot().setTargetPitch(90);
				Thread.sleep(100);
			}
			aceleracao(0);
		} else {
			aceleracao(0);
		}
	}

	private void aceleracao(float acel) throws RPCException, IOException {
		naveAtual.getControl().setThrottle((float) acel);
	}
	
	private void checarPouso() throws RPCException, IOException, InterruptedException {
		if ((naveAtual.getSituation() == VesselSituation.LANDED)
		 || (naveAtual.getSituation() == VesselSituation.SPLASHED) && (podePousar)) {
			while (naveAtual.getControl().getThrottle() > 0.1) {
				float acel = naveAtual.getControl().getThrottle() - 0.1f;
				aceleracao(acel);
				Thread.sleep(10);
			}
			System.out.println("Pouso finalizado.");
			aceleracao(0);
			naveAtual.getAutoPilot().disengage();
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setRCS(true);
			naveAtual.getControl().setBrakes(false);
			podePousar = false;
			executandoSuicideBurn = false;
			conexao.close();
		}
	}

}

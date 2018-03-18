import java.io.IOException;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;

public class SuicideBurn extends MechPeste{

	// DECLARAÇÃO DE VARIAVEIS GLOBAIS
	private static SpaceCenter centroEspacial;
	private static Vessel naveAtual;
	private static Flight vooNave;
	private static ReferenceFrame pontoRef;
	static Stream<Double> tempoUniversal;

	private static double naveTWRMax;
	private static double distanciaDaQueima;
	private static double tempoDaQueima;
	private static double acelMax;
	private static double alturaPouso = 20.0; // Altura pra começar o Hover

	private static Stream<Double> altitudeNave;
	private static Stream<Float> massaTotalNave;
	private static Stream<Double> velNave;
	private static Stream<Double> velVertNave;

	private static ControlePID controleAcel = new ControlePID();
	private static float forcaGravidade;
	boolean direcaoPouso = false;
	boolean executandoSuicideBurn = false;
	static boolean podePousar = false;
	float novaAcel = 0;

	public SuicideBurn(Connection conexaoOK)  throws StreamException, RPCException, IOException, InterruptedException {

		// DECLARAÇÃO DE VARIÁVEIS
		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = centroEspacial.getActiveVessel(); // objeto da nave
		pontoRef = naveAtual.getOrbit().getBody().getReferenceFrame();
		vooNave = naveAtual.flight(pontoRef);
		altitudeNave = conexao.addStream(vooNave, "getSurfaceAltitude");
		velNave = conexao.addStream(vooNave, "getSpeed");
		velVertNave = conexao.addStream(vooNave, "getVerticalSpeed");

		massaTotalNave = conexao.addStream(naveAtual, "getMass");
		forcaGravidade = naveAtual.getOrbit().getBody().getSurfaceGravity();
		tempoUniversal = conexao.addStream(SpaceCenter.class, "getUT");
		MechPeste.setTextoPainel2("");
		MechPeste.setTextoPainel("Em " + naveAtual.getOrbit().getBody().getName() + " atualmente.");
		Thread.sleep(1000);
		double distanciaPouso = alturaPouso;
		controleAcel.setAmostraTempo(25);
		controleAcel.setAjustes(0.02, 0.001, 1); // <== AJUSTES PID
		controleAcel.setLimiteSaida(-1, 1);
		atualizarVariaveis();
		naveAtual.getAutoPilot().engage(); // LIGAR O PILOTO
		new Navegacao(conexao);

		if (naveAtual.getSituation() == VesselSituation.LANDED
				|| naveAtual.getSituation() == VesselSituation.PRE_LAUNCH) {
			MechPeste.setTextoPainel("Subindo...");
			naveAtual.getControl().setGear(false);
			naveAtual.getControl().setThrottle((float) (1 / naveTWRMax * 1.5));
			while (altitudeNave.get() < distanciaPouso) {
				if (altitudeNave.get() > distanciaPouso) {
					naveAtual.getControl().setThrottle((float) (0f));
					break;
				}
				Thread.sleep(100);
				Navegacao.MirarNave();
			}
			
		}
		naveAtual.getControl().setThrottle((float) (0f));

		MechPeste.setTextoPainel("Alt:");
		naveAtual.getControl().setBrakes(true);
		naveAtual.getControl().setRCS(true);
		while (!executandoSuicideBurn) {
			atualizarVariaveis();
			//if (velNave.get() > 20) {
				Navegacao.MirarNave();
			//} else {
			//	naveAtual.getAutoPilot().setTargetPitch(90);
			//}
			Thread.sleep(25);
			if (0 >= altitudeNave.get() - distanciaDaQueima - distanciaPouso) {
				executandoSuicideBurn = true;
			}

		}

		while (executandoSuicideBurn) { // LOOP PRINCIPAL DE SUICIDE BURN
			if (MechPeste.botPousoClick.get()) {
				podePousar = true;
				MechPeste.setTextoPainel("Pousando Nave...");
				MechPeste.textoPainel2.setVisible(false);
				MechPeste.botaoPousar.setClicked(false);
				MechPeste.botaoDecolar.setVisible(false);
				MechPeste.botaoFlutuar.setVisible(false);

			}
			if (vooNave.getHorizontalSpeed() < 1) {
				naveAtual.getAutoPilot().setTargetPitch(90);
			} else {
				Navegacao.MirarNave();
			}

			atualizarVariaveis(); // atualiza valores

			// -=- Informa ao PID a altitude da nave e o limite -=-

			controleAcel.setValorEntrada(altitudeNave.get());
			controleAcel.setValorLimite(distanciaPouso + distanciaDaQueima);

			if (altitudeNave.get() < 300.0) { // altitude para as perninhas
				naveAtual.getControl().setGear(true);

			}

			if ((altitudeNave.get() < distanciaPouso) && (naveAtual.getControl().getGear() == true)
					&& (podePousar == true)) {
				controleAcel.setAjustes(0.025, 0.01, 0.5); // <== AJUSTES PID

				distanciaPouso -= alturaPouso / 5;
			}

			// -=- Corrigir a aceleração -=-
			novaAcel = (float) (1 / naveTWRMax + controleAcel.computarPID());
			naveAtual.getControl().setThrottle(novaAcel);
			if (terminarPouso()) {
				executandoSuicideBurn = false;
				MechPeste.resetarPainel();
			}

			Thread.sleep(25);

		} // Fim loop-while

	} // Fim código principal

	private static boolean terminarPouso() throws RPCException, IOException, InterruptedException {
		if ((naveAtual.getSituation() == VesselSituation.LANDED)
				|| (naveAtual.getSituation() == VesselSituation.SPLASHED) && (podePousar == true)) {
			float acel = naveAtual.getControl().getThrottle() - 0.1f;
			naveAtual.getControl().setThrottle((float) (acel));
			Thread.sleep(50);
			if (naveAtual.getControl().getThrottle() == 0.0f) {
				System.out.println("POUSO TERMINADO COM SUCESSO, ESPERAMOS");
				naveAtual.getControl().setThrottle(0.0f);
				naveAtual.getAutoPilot().disengage();
				naveAtual.getControl().setBrakes(false);

				podePousar = false;
				return true;
			}

		}
		return false;
	}

	private static void atualizarVariaveis() throws RPCException, StreamException, IOException {
		naveTWRMax = naveAtual.getMaxThrust() / (massaTotalNave.get() * forcaGravidade);
		acelMax = (naveTWRMax * forcaGravidade) - forcaGravidade;
		tempoDaQueima = velNave.get() / acelMax;
		distanciaDaQueima = (velNave.get() * tempoDaQueima + 1 / 2 * acelMax * Math.pow(tempoDaQueima, 2));

		// imprimir valor no console:
		System.out.println("TWR         : " + naveTWRMax);
		System.out.println("Dist. Queima: " + distanciaDaQueima);
		System.out.println("Altitude Voo: " + altitudeNave.get());
		System.out.println("Correção    : " + controleAcel.computarPID());
		System.out.println("Vel Vert    : " + velNave.get());

		MechPeste.setTextoPainel2(String.valueOf(altitudeNave.get()));

	}

}

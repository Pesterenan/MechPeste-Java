import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.CelestialBody;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Resources;

import org.javatuples.Triplet;

import java.io.IOException;
import java.lang.Math;
import java.util.Map;

public class SuicideBurn {

	private static double tempoAteImpacto;
	private static float naveTWR;
	private static float maxAcel;
	private static double tempoQueima;
	private static double velVertical;

	public static void main(String[] args) throws IOException, RPCException, InterruptedException, StreamException {

		Connection conexao = Connection.newInstance("Suicide Burn - Teste"); // nova conex√£o
		SpaceCenter centroEspacial = SpaceCenter.newInstance(conexao); // nova instancia centro controle
		SpaceCenter.Vessel naveAtual = centroEspacial.getActiveVessel(); // nova instancia nave

		// Streams de telemetria - pegam dados do jogo
		centroEspacial.getUT(); // pegar tempo universal
		Stream<Double> ut = conexao.addStream(SpaceCenter.class, "getUT"); // stream do tempo
		ReferenceFrame pontoReferencia = naveAtual.getOrbitalReferenceFrame();

		ReferenceFrame refVelocidade = ReferenceFrame.createHybrid(conexao,
				naveAtual.getOrbit().getBody().getReferenceFrame(), naveAtual.getSurfaceReferenceFrame(),
				naveAtual.getOrbit().getBody().getReferenceFrame(), naveAtual.getOrbit().getBody().getReferenceFrame());

		Flight voo = naveAtual.flight(pontoReferencia);
		Stream<Double> altitude = conexao.addStream(voo, "getMeanAltitude"); // altitude acima do mar
		Stream<Double> altitudeSuperficie = conexao.addStream(voo, "getSurfaceAltitude"); // altitude acima do mar
		Stream<Double> apoastro = conexao.addStream(naveAtual.getOrbit(), "getApoapsisAltitude");
		Stream<Float> massaTotalNave = conexao.addStream(naveAtual, "getMass");
		float aceleracaoGravidade = naveAtual.getOrbit().getBody().getSurfaceGravity();
		Triplet<Double, Double, Double> velocidade = naveAtual.flight(refVelocidade).getVelocity();
		boolean suicideInicio = false;
		while (true) {
			velVertical = velocidade.getValue0();
			tempoAteImpacto = Math.sqrt(2 * altitudeSuperficie.get() * 1 / aceleracaoGravidade);
			naveTWR = naveAtual.getAvailableThrust() / (massaTotalNave.get() * aceleracaoGravidade);
			maxAcel = (naveTWR * aceleracaoGravidade);
			tempoQueima = velVertical / maxAcel; // importante

			velocidade = naveAtual.flight(refVelocidade).getVelocity();
			System.out.println(altitudeSuperficie.get());
			while (0 >= (altitudeSuperficie.get() - velVertical * tempoQueima
					+ 1 / 2 * maxAcel * (tempoQueima * tempoQueima))) {
				suicideInicio = true;
				break;
			}
			if (suicideInicio) {
				naveAtual.getControl().setThrottle(1.0f);
				while (velVertical < 0.0) {
					System.out.println(altitudeSuperficie.get());
					Thread.sleep(10);
				}
				naveAtual.getControl().setThrottle(0.0f);
				
			}

		}
	}

	public static void escreverDados() throws InterruptedException {
		/*
		 * System.out.println(String.format("Tempo atÈ suicide: " + "%.1f",
		 * tempoAteImpacto)); System.out.println(String.format("TWR atual: " +
		 * "%.1f", naveTWR)); System.out.println(String.format("MaxAcel: " + "%.2f",
		 * maxAcel) + "m/2"); System.out.println(String.format("Velocidade Vertical: " +
		 * velVertical + "m/2")); Thread.sleep(1000);
		 */
	}
}

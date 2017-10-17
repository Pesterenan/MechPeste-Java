import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import javax.swing.text.ChangedCharSetException;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Leg;
import krpc.client.services.SpaceCenter.Part;
import krpc.client.services.SpaceCenter.Parts;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.VesselSituation;
import krpc.client.services.UI;
import krpc.client.services.UI.Canvas;
import krpc.client.services.UI.Panel;
import krpc.client.services.UI.RectTransform;
import krpc.client.services.UI.Text;

public class SuicideBurn {

	// Streams de conexão - Declaração:

	private static Connection conexao;
	private static SpaceCenter centroEspacial;
	private static SpaceCenter.Vessel naveAtual;
	private static Stream<Double> ut;
	private static ReferenceFrame pontoReferencia;
	private static ReferenceFrame refVelocidade;
	private static Flight parametrosVoo;
	private static Stream<Double> altitudeSuperficie;
	private static Stream<Float> massaTotalNave;
	private static Stream<Float> massaSecaNave;
	private static Triplet<Double, Double, Double> velocidade;
	private static PrintStream gravadorDeDados;

	// Variáveis de controles:
	private static double tempoAteImpacto;
	private static double velocidadeNoImpacto;
	private static double acelMax;
	private static float aceleracaoGravidade;
	private static float naveTWR;
	private static double tempoDeQueima;
	private static double taxaDeQueima;
	private static double distanciaDaQueima;
	private static double naveDeltaV;
	private static float massaMedia;
	private static float aceleracao = 1f;
	private static double correcaoAceleracao = 1.0;
	private static boolean suicideInicio = false;
	private static String dadosColetados = null;

	static double[] tamAmostraVel = new double[25];
	static double[] tamAmostraAlt = new double[25];
	static int posicao = 0;
	static double gravarDados = 0;

	public static void main(String[] args) throws IOException, RPCException, InterruptedException, StreamException {

		gravadorDeDados = new PrintStream("valores.txt");
		// Inicialização das variáveis estáticas:
		conexao = Connection.newInstance("Suicide Burn - Teste"); // indica uma nova conexão com o kRPC
		centroEspacial = SpaceCenter.newInstance(conexao); // cria uma nova instância do centro de espacial com a
															// conexão
		naveAtual = centroEspacial.getActiveVessel(); // cria uma nova instância nave à partir do centro
		centroEspacial.getUT(); // Pega o tempo universal pelo centro espacial
		ut = conexao.addStream(SpaceCenter.class, "getUT"); // Adiciona o "Tempo Universal" do jogo à Stream de tempo.
		pontoReferencia = naveAtual.getOrbitalReferenceFrame(); // Ponto de referência orbital da nave
		refVelocidade = ReferenceFrame.createHybrid(conexao, // Ponto de referência da velocidade de superfície em
																// relação ao planeta
				naveAtual.getOrbit().getBody().getReferenceFrame(), naveAtual.getSurfaceReferenceFrame(),
				naveAtual.getOrbit().getBody().getReferenceFrame(), naveAtual.getOrbit().getBody().getReferenceFrame());

		parametrosVoo = naveAtual.flight(pontoReferencia); // Adiciona o ponto de referência da nave à Stream de
															// parametros de voo
		altitudeSuperficie = conexao.addStream(parametrosVoo, "getSurfaceAltitude"); // altitude acima da superfície
		massaTotalNave = conexao.addStream(naveAtual, "getMass"); // Adiciona a Stream de massa total da nave
		massaSecaNave = conexao.addStream(naveAtual, "getDryMass"); // Adiciona a Stream de massa seca da nave
		velocidade = naveAtual.flight(refVelocidade).getVelocity(); // Adiciona a Stream de velocidade da nave
		aceleracaoGravidade = naveAtual.getOrbit().getBody().getSurfaceGravity(); // aceleração da gravidade do corpo
																					// celeste orbitado ao nível do mar

		// ---------- CÓDIGO DE EXECUÇÃO E CHECAGEM DO SUICIDE BURN! ---------------

		while (true) {

			atualizarVariaveis();

			if (checarSuicide()) {

				atualizarVariaveis();
				System.out.println("Ínicio do Suicide Burn!!!");
				suicideInicio = true;
				naveAtual.getControl().setThrottle(1.0f);
				break;
			}
		}
		while (suicideInicio) {
			atualizarVariaveis();
			System.out.println("Aceleracao: " + aceleracao);
			System.out.println("Delta V : " + naveDeltaV);
			System.out.println("Nave TWR: " + naveTWR);
			System.out.println("Correção Acel: " + correcaoAceleracao);
			System.out.println("Tempo Queima: " + tempoDeQueima);
			System.out.println("Taxa De Queima: " + taxaDeQueima);
			System.out.println("Tempo ate impacto: " + Math.pow((naveDeltaV/taxaDeQueima),2));
			System.out.println(" ");
			
			// gravar log:
			dadosColetados = coletarDados(velocidade.getValue0(), altitudeSuperficie.get());
			if (dadosColetados != null) {

				gravadorDeDados.println(dadosColetados);
				gravarDados = 0;
			}

			if (checarSuicide()) {
				atualizarVariaveis();
				correcaoAceleracao = Math.abs(Math.log((Math.sqrt(aceleracao / Math.pow((naveDeltaV/taxaDeQueima),2) * naveTWR))));
				
				//correcaoAceleracao = Math.abs(Math.log(Math.sqrt((aceleracao / naveDeltaV) * naveTWR)));
				
				aceleracao = (float) (correcaoAceleracao);

				System.out.println("Após correção: " + aceleracao);
				naveAtual.getControl().setThrottle(aceleracao);

				
			}

			/*
			 * if (altitudeSuperficie.get() < 300) { List<Leg> pernas =
			 * naveAtual.getParts().getLegs(); for (Leg perna : pernas) { if
			 * (perna.getDeployable() && !perna.getDeployed()) { perna.setDeployed(true); }
			 * } }
			 */
			// CHECK-UP DE VELOCIDADE PARA 95% DE TWR
			if (velocidade.getValue0() > -4.0) {
				aceleracao = (float) (correcaoAceleracao * 0.90 / naveTWR);
				naveAtual.getControl().setThrottle(aceleracao);

			}

			VesselSituation pouso = naveAtual.getSituation();
			if (pouso.toString() == "LANDED") {
				System.out.println("Pouso finalizado!");
				suicideInicio = false;
				naveAtual.getControl().setThrottle(0);
				gravadorDeDados.close();
				conexao.close();

			}
		}
	}

	public static void atualizarVariaveis() throws InterruptedException {
		try {
			tempoAteImpacto = Math.sqrt(2 * altitudeSuperficie.get() * 1 / aceleracaoGravidade);
			velocidade = naveAtual.flight(refVelocidade).getVelocity();
			velocidadeNoImpacto = velocidade.getValue0() + aceleracaoGravidade * (tempoAteImpacto * tempoAteImpacto);
			distanciaDaQueima = -velocidade.getValue0() * tempoDeQueima + 1 / 2 * acelMax * Math.pow(tempoDeQueima, 2);
			taxaDeQueima = naveAtual.getAvailableThrust() / (naveAtual.getSpecificImpulse() * aceleracaoGravidade);
			naveDeltaV = Math.log(massaTotalNave.get() / massaSecaNave.get()) * aceleracaoGravidade *naveAtual.getSpecificImpulse();
			massaMedia = (float) ((massaTotalNave.get() + (massaTotalNave.get()
					/ Math.pow(Math.E, (naveDeltaV / (naveAtual.getSpecificImpulse() * aceleracaoGravidade))))) / 2);
			
			naveTWR = naveAtual.getAvailableThrust() / (massaMedia * aceleracaoGravidade);
			acelMax = (naveTWR * aceleracaoGravidade) - aceleracaoGravidade;
			tempoDeQueima = velocidade.getValue0() / acelMax;

			
			// gravar log:
			dadosColetados = coletarDados(velocidade.getValue0(), altitudeSuperficie.get());
			if (dadosColetados != null) {

				gravadorDeDados.println(dadosColetados);
				gravarDados = 0;
			}
			Thread.sleep(25);

		} catch (IOException e) {
			System.out.println("Erro de gravação de dados: " + e.getMessage());
		} catch (RPCException e) {
			System.out.println("Erro de conexão com o Mod: " + e.getMessage());
		} catch (StreamException e) {
			System.out.println("Streams não iniciadas");
			e.printStackTrace();
		}
	}

	public static boolean checarSuicide() {
		try {
			if (0 >= altitudeSuperficie.get() - velocidade.getValue0() * tempoDeQueima
					+ 1 / 2 * acelMax * (tempoDeQueima * tempoDeQueima)) {
				return true;
			}
		} catch (StreamException | RPCException | IOException e) {
			System.out.println("Erro ao checar hora correta do Suicide Burn");
			e.printStackTrace();
		}
		return false;
	}

	public static String coletarDados(double velocidade, double altitude) {

		if (posicao < tamAmostraVel.length) {
			tamAmostraVel[posicao] = velocidade;
		}
		if (posicao < tamAmostraAlt.length) {
			tamAmostraVel[posicao] = altitude;
		}
		String resultado = null;
		posicao++;
		if (posicao >= tamAmostraVel.length) {
			for (int i = 0; i < tamAmostraVel.length; i++) {
				gravarDados += tamAmostraVel[i];
			}
			gravarDados /= tamAmostraVel.length;
			resultado = (String.valueOf(gravarDados) + ",");
			for (int i = 0; i < tamAmostraAlt.length; i++) {
				gravarDados += tamAmostraAlt[i];
			}
			gravarDados /= tamAmostraAlt.length;
			resultado += (String.valueOf(gravarDados) + ",");
			posicao = 0;

		}
		return resultado;

	}

}


import krpc.client.RPCException;
import krpc.client.Connection;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Resources;
import java.io.IOException;

import org.javatuples.Triplet;

import krpc.client.Stream;

import krpc.client.StreamException;

public class Navegacao {

	private static Connection conexao;
	private static SpaceCenter centroEspacial;
	private static ReferenceFrame pontoRef;
	private static Vessel naveAtual;
	private static Flight vooNave;

	private static Vetor alinharDistanciaHorizontal;
	private static Triplet<Double, Double, Double> posicaoPousoAlvo = new Triplet<Double, Double, Double>(0.0, 0.0,
			0.0);

	private static Vetor vetorDaVelocidade = new Vetor(0, 0);
	private static ControlePID controleMag = new ControlePID();
	private static boolean alvoOuNao = false;

	public Navegacao(Connection conexaoOK) throws IOException, RPCException, InterruptedException, StreamException {
		conexao = conexaoOK;
		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = centroEspacial.getActiveVessel(); // objeto da nave
		pontoRef = naveAtual.getOrbit().getBody().getReferenceFrame();
		vooNave = naveAtual.flight(pontoRef);
		controleMag.setAmostraTempo(25);
		controleMag.setAjustes(0.5, 0.001, 10); // <== AJUSTES PID
		controleMag.setLimiteSaida(-1, 1);

		MirarNave();
	}

	public static void MirarNave() throws IOException, RPCException, InterruptedException, StreamException {
		
		// DECLARAÇÃO DE VARIÁVEIS
		posicaoPousoAlvo = centroEspacial.transformPosition(vooNave.getRetrograde(), // RETROCESSO
				naveAtual.getSurfaceVelocityReferenceFrame(), pontoRef);
		
		try {
		  posicaoPousoAlvo = centroEspacial.getTargetVessel().position(pontoRef); //ALVO
			alvoOuNao = true;
		} catch (Exception e) {
			alvoOuNao = false;
		} 
		alinharDistanciaHorizontal = Vetor.vetorDistancia(
				centroEspacial.transformPosition(posicaoPousoAlvo, pontoRef, naveAtual.getSurfaceReferenceFrame()),
				naveAtual.position(naveAtual.getSurfaceReferenceFrame()));

		DirElev alinharDirecao = getElevacaoDirecaoDoVetor(alinharDistanciaHorizontal);

		naveAtual.getAutoPilot().targetPitchAndHeading(alinharDirecao.elevacao, alinharDirecao.direcao);
		naveAtual.getAutoPilot().setTargetRoll(alinharDirecao.direcao);

		System.out.println(alinharDirecao.toString());

	} // FIM DO METODO MAIN

	static DirElev getElevacaoDirecaoDoVetor(Vetor vec) throws RPCException, IOException, StreamException {
		DirElev toRet = new DirElev(0, 0);
		Vetor vecInvertido;
		Triplet<Double, Double, Double> velRelativa = centroEspacial.transformPosition(vooNave.getVelocity(), pontoRef,
				naveAtual.getSurfaceReferenceFrame());

		vetorDaVelocidade.x = ((Double) velRelativa.getValue1()).doubleValue();
		vetorDaVelocidade.y = ((Double) velRelativa.getValue2()).doubleValue();
		vetorDaVelocidade = vetorDaVelocidade.inverte();

		vec = vec.subtrai(vetorDaVelocidade);
		if (alvoOuNao) {
			vecInvertido = vec.multiplica(-1); //ALVO
		} else {
		vecInvertido = vec.multiplica(1); //RETROCESSO
		}
		toRet.direcao = (float) ((Math.atan2(vecInvertido.x, vecInvertido.y) / Math.PI) * 180d);
		if (toRet.direcao < 0)
			toRet.direcao = 360f + toRet.direcao;

		double comprimento = Math.pow(1 + vec.Magnitude(), 1) - 1;
		controleMag.setValorEntrada(comprimento);
		controleMag.setValorLimite(vetorDaVelocidade.Magnitude());
		toRet.elevacao = Math.max(60, (float) (90d - comprimento * 2));

		System.out.println("Comprimento: " + comprimento);
		System.out.println("Comprimento Vel: " + vetorDaVelocidade.Magnitude());
		System.out.println("Inclinação: " + toRet.elevacao);
		System.out.println("PID: " + controleMag.computarPID());

		return toRet;
	}

}// FIM DA CLASSE

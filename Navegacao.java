import java.io.IOException;

import org.javatuples.Triplet;

import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;

public class Navegacao {

	static SpaceCenter centroEspacial;
	private Vessel naveAtual;
	private ReferenceFrame pontoRef;
	private Flight vooNave;

	private Vetor alinharDistanciaHorizontal = new Vetor(0, 0);
	private Triplet<Double, Double, Double> posicaoPousoAlvo = new Triplet<Double, Double, Double>(0.0, 0.0,	0.0);
	private Vetor vetorDaVelocidade = new Vetor(0, 0);
	private Vetor retornarVetor = new Vetor(0, 0);
	private ControlePID controleMag = new ControlePID();
		
	public Navegacao(SpaceCenter CentroEspacial, Vessel NaveAtual) throws IOException, RPCException, InterruptedException, StreamException {
		centroEspacial = CentroEspacial;
		naveAtual = NaveAtual; 
		pontoRef = naveAtual.getOrbit().getBody().getReferenceFrame();
		vooNave = naveAtual.flight(pontoRef);

		controleMag.setAmostraTempo(40);
		controleMag.ajustarPID(0.5, 0.001, 10); // <== AJUSTES PID
		controleMag.limitarSaida(-1, 1);
		MirarNave();
	}

	public void MirarNave() throws IOException, RPCException, InterruptedException, StreamException {
		// Buscar Nó Retrógrado:
		posicaoPousoAlvo = centroEspacial.transformPosition(vooNave.getRetrograde(),
				naveAtual.getSurfaceVelocityReferenceFrame(), pontoRef);
				
		alinharDistanciaHorizontal = Vetor.vetorDistancia(
				centroEspacial.transformPosition(posicaoPousoAlvo, pontoRef, naveAtual.getSurfaceReferenceFrame()),
				naveAtual.position(naveAtual.getSurfaceReferenceFrame()));

		Vetor alinharDirecao = getElevacaoDirecaoDoVetor(alinharDistanciaHorizontal);

		naveAtual.getAutoPilot().targetPitchAndHeading((float) alinharDirecao.y, (float) alinharDirecao.x);
		naveAtual.getAutoPilot().setTargetRoll((float) alinharDirecao.x);
	}

	Vetor getElevacaoDirecaoDoVetor(Vetor vetor) throws RPCException, IOException, StreamException {
		Triplet<Double, Double, Double> velRelativa = 
			centroEspacial.transformPosition(vooNave.getVelocity(), pontoRef, naveAtual.getSurfaceReferenceFrame());

		vetorDaVelocidade.x = ((Double) velRelativa.getValue1()).doubleValue();
		vetorDaVelocidade.y = ((Double) velRelativa.getValue2()).doubleValue();
		
		vetor = vetor.subtrai(vetorDaVelocidade);
		
		retornarVetor.x = Vetor.anguloDirecao(vetor); //(float) ((Math.atan2(vecInvertido.x, vecInvertido.y) / Math.PI) * 180d);
		
		double comprimento = Math.pow(1 + vetor.Magnitude(), 1) - 1;
		controleMag.setEntradaPID(comprimento);
		controleMag.setLimitePID(vetorDaVelocidade.Magnitude());
		retornarVetor.y = Math.max(60, (int) (90 - comprimento * 2));

//		System.out.println("Comprimento: " + comprimento);
//		System.out.println("Comprimento Vel: " + vetorDaVelocidade.Magnitude());
//		System.out.println("Inclinação: " + retornarVetor.y);
//		System.out.println("PID: " + controleMag.computarPID());

		return retornarVetor;
	}

}// FIM DA CLASSE

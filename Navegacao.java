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

	private Vetor alinharDistanciaHorizontal = new Vetor(0, 0, 0);
	private Triplet<Double, Double, Double> posicaoPousoAlvo = new Triplet<Double, Double, Double>(0.0, 0.0, 0.0);
	private Vetor vetorDaVelocidade = new Vetor(0, 0, 0);
	private Vetor retornarVetor = new Vetor(0, 0, 0);
	public int anguloInclinacaoMax = 60;
	
	public Navegacao(SpaceCenter CentroEspacial, Vessel NaveAtual)
			throws IOException, RPCException, InterruptedException, StreamException {
		centroEspacial = CentroEspacial;
		naveAtual = NaveAtual;
		pontoRef = naveAtual.getOrbit().getBody().getReferenceFrame();
		vooNave = naveAtual.flight(pontoRef);
		MirarNave();
	}

	public void MirarNave() throws IOException, RPCException, InterruptedException, StreamException {
		// Buscar N� Retr�grado:
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
		Triplet<Double, Double, Double> velRelativa = centroEspacial.transformPosition(vooNave.getVelocity(), pontoRef,
				naveAtual.getSurfaceReferenceFrame());

		vetorDaVelocidade.x = ((Double) velRelativa.getValue1()).doubleValue();
		vetorDaVelocidade.y = ((Double) velRelativa.getValue2()).doubleValue();

		vetor = vetor.subtrai(vetorDaVelocidade);

		retornarVetor.x = Vetor.anguloDirecao(vetor);
		retornarVetor.y = Math.max(anguloInclinacaoMax, (int) (90 - vetor.Magnitude() * 1.5));
		return retornarVetor;
	}
}

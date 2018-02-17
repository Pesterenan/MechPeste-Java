import java.io.IOException;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Resources;
import krpc.client.services.SpaceCenter.Vessel;

public class SuicideBurn {

	// DECLARAÇÃO DE VARIAVEIS GLOBAIS
	private static Connection conexao;
	private static SpaceCenter centroEspacial;
	private static Vessel naveAtual;
	private static Flight vooNave;
	private static ReferenceFrame pontoRef;
	private static InterfaceKRPC UI;
	static Stream<Double> UT;
	
	
	private static double naveTWRMax;
	private static double distanciaDaQueima;
	private static double tempoDaQueima;
	private static double acelMax;
	private static double alturaPouso = 5.0;

	private static Stream<Double> altitudeNave;
	private static Stream<Double> elevacaoTerreno;
	private static Stream<Float> massaTotalNave;
	private static Stream<Double> velVertNave;

	private static ControlePID controleAcel = new ControlePID();
	

	public static void main(String[] args) throws StreamException, RPCException, IOException, InterruptedException {

		// DECLARAÇÃO DE VARIÁVEIS
		conexao = Connection.newInstance("Suicide Burn - Teste");
		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = centroEspacial.getActiveVessel(); // objeto da nave
		pontoRef = naveAtual.getOrbit().getBody().getReferenceFrame();
		vooNave = naveAtual.flight(pontoRef);
		altitudeNave = conexao.addStream(vooNave, "getSurfaceAltitude");
		elevacaoTerreno = conexao.addStream(vooNave, "getElevation");
		velVertNave = conexao.addStream(vooNave, "getVerticalSpeed");
		massaTotalNave = conexao.addStream(naveAtual, "getMass");
		UT = conexao.addStream(SpaceCenter.class, "getUT");
		UI = new InterfaceKRPC(conexao);
		atualizarVariaveis();
		
		naveAtual.getAutoPilot().engage(); // LIGAR O PILOTO
		naveAtual.getAutoPilot().targetPitchAndHeading(90, 90); // MIRAR PRA CIMA
		//System.out.println("Mirado para cima");
		UI.setTextoPainel("Mirado para cima");
		Thread.sleep(1000);
		//naveAtual.getControl().activateNextStage(); // USAR O ESTÁGIO
		
		
		//System.out.println("Ajustando PID...");
		UI.setTextoPainel("Ajustando PID...");
		Thread.sleep(1000);
		controleAcel.setAmostraTempo(25);
		controleAcel.setValorLimite(distanciaDaQueima);
		controleAcel.setAjustes(0.1, 0.001, 0.5); // <== AJUSTES
		controleAcel.setLimiteSaida(-1,1);
		UI.setTextoPainel("Altitude:");
		naveAtual.getControl().setBrakes(true);
		while (true) { // LOOP PRINCIPAL DE SUICIDE BURN
			
			atualizarVariaveis(); //atualiza valores
			
			//-=- Informa ao PID a altitude da nave e o limite -=-
			controleAcel.setValorEntrada(altitudeNave.get());
			controleAcel.setValorLimite(alturaPouso+distanciaDaQueima);

			if (altitudeNave.get() < 50) { //altitude para as perninhas
				naveAtual.getControl().setGear(true);
			}
			/*if (altitudeNave.get() < alturaPouso) {
				alturaPouso -= 0.5;
				if (alturaPouso < 1) {
					naveAtual.getControl().setThrottle(0.0f);
					break;
				}
			}
			*///-=- Corrigir a aceleração -=-
			float novaAcel = (float) (1 / naveTWRMax + controleAcel.computarPID());
			naveAtual.getControl().setThrottle(novaAcel);
			Thread.sleep(10);

		} // Fim loop-while
		//System.out.println("POUSO TERMINADO COM SUCESSO, ESPERAMOS");
		//conexao.close();
		
	} // Fim código principal

	private static void atualizarVariaveis() throws RPCException, StreamException, IOException {
		float forcaGravidade = naveAtual.getOrbit().getBody().getSurfaceGravity();
		naveTWRMax = naveAtual.getMaxThrust() / (massaTotalNave.get() * forcaGravidade);
		acelMax = (naveTWRMax * forcaGravidade) - forcaGravidade;
		tempoDaQueima = velVertNave.get() / acelMax;
		distanciaDaQueima = (velVertNave.get() * tempoDaQueima + 1/2 * acelMax * Math.pow(tempoDaQueima, 2));
		// imprimir valor no console:
		System.out.println("TWR         : " + naveTWRMax);
		System.out.println("Dist. Queima: " + distanciaDaQueima);
		System.out.println("Altitude Voo: " + altitudeNave.get());
		System.out.println("Alt. Terreno: " + elevacaoTerreno.get());
		System.out.println("Correção    : " + controleAcel.computarPID());
		UI.setTextoPainel2(String.valueOf(altitudeNave.get()));
		
	}

}

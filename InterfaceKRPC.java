import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Leg;
import krpc.client.services.SpaceCenter.LegState;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.VesselSituation;
import krpc.client.services.UI;
import krpc.client.services.UI.Canvas;
import krpc.client.services.UI.Panel;
import krpc.client.services.UI.RectTransform;
import krpc.client.services.UI.Text;



public class InterfaceKRPC {
	private Connection conexao;
	private UI telaUsuario;
	private Canvas telaItens;
	private Pair<Double,Double> tamanhoTela;
	private Panel painelInfo;
	private double margemUI = 20.0;
	private Text textoPainel;
	private Text textoPainel2;

	public InterfaceKRPC(Connection conexaoEntrada) throws IOException, RPCException, InterruptedException, StreamException {
		this.conexao = conexaoEntrada;
		this.telaUsuario = UI.newInstance(this.conexao);
		this.telaItens = telaUsuario.getStockCanvas();
		// Tamanho da tela de jogo em pixels
		this.tamanhoTela = telaItens.getRectTransform().getSize();
		this.painelInfo = adicionarPainel(this.telaItens);
	}
	
	
	public Panel adicionarPainel(Canvas telaItens) throws IOException, RPCException, InterruptedException, StreamException {
		// Adicionar um painel para conter os elementos de UI
		Panel painelInfo = telaItens.addPanel(true);
				// Posicionar o painel à esquerda da tela
		RectTransform retangulo = painelInfo.getRectTransform();
		retangulo.setSize(new Pair<Double, Double>(200.0, 100.0));
		retangulo.setPosition(new Pair<Double, Double>((110 - (tamanhoTela.getValue0()) / 3), 150.0));

		// Adicionar texto mostrando infos

		textoPainel = painelInfo.addText("Altitude:", true);
		textoPainel.getRectTransform().setPosition(new Pair<Double, Double>(margemUI, margemUI));
		textoPainel.getRectTransform().setSize(new Pair<Double, Double>(retangulo.getSize().getValue0(), margemUI));
		textoPainel.setColor(new Triplet<Double, Double, Double>(1.0, 1.0, 1.0));
		textoPainel.setSize(18);
		
		textoPainel2 = painelInfo.addText("0", true);
		textoPainel2.getRectTransform().setPosition(new Pair<Double, Double>(margemUI, -margemUI));
		textoPainel2.getRectTransform().setSize(new Pair<Double, Double>(retangulo.getSize().getValue0(), margemUI));
		textoPainel2.setColor(new Triplet<Double, Double, Double>(1.0, 1.0, 1.0));
		textoPainel2.setSize(18);
	return painelInfo;
	}

	public void setTextoPainel(String texto) throws RPCException, IOException {
		textoPainel.setContent(texto);
	}
	public void setTextoPainel2(String texto) throws RPCException, IOException {
		textoPainel2.setContent(texto);
	}


}
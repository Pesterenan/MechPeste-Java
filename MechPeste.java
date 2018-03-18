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
import krpc.client.services.UI.Button;
import krpc.client.services.UI.Canvas;
import krpc.client.services.UI.Panel;
import krpc.client.services.UI.RectTransform;
import krpc.client.services.UI.Text;

public class MechPeste {
	protected static Connection conexao;
	private static UI telaUsuario;
	private static Canvas telaItens;
	private static Pair<Double, Double> tamanhoTela;
	static Panel painelBotoes;
	private static double margemUI = 20.0;
	public static Text textoPainel;
	public static Text textoPainel2;
	public static Button botaoPousar;
	public static Button botaoFlutuar;
	public static Button botaoDecolar;
	public static Button botaoMirar;
	public static Stream<Boolean> botPousoClick;

	public static void main(String[] args) throws StreamException, RPCException, IOException, InterruptedException {
		conexao = Connection.newInstance("MechPeste");
		telaUsuario = UI.newInstance(conexao);
		telaItens = telaUsuario.getStockCanvas();
		tamanhoTela = telaItens.getRectTransform().getSize();
		painelBotoes = adicionarPainel(telaItens);
		botPousoClick = conexao.addStream(botaoPousar, "getClicked");
		Stream<Boolean> botSBClick = conexao.addStream(botaoFlutuar, "getClicked");
		Stream<Boolean> botDecolarClick = conexao.addStream(botaoDecolar, "getClicked");
		Stream<Boolean> botMirarClick = conexao.addStream(botaoMirar, "getClicked");

		while (true) {
			if (botSBClick.get()) {
				botaoFlutuar.setClicked(false);
				botaoDecolar.setVisible(false);
				botaoFlutuar.setVisible(false);
				textoPainel.setVisible(true);
				textoPainel2.setVisible(true);
				new SuicideBurn(conexao);

			}

			if (botDecolarClick.get()) {
				botaoDecolar.setClicked(false);
				botaoDecolar.setVisible(false);
				textoPainel.setVisible(true);
				textoPainel2.setVisible(true);
				new DecolagemOrbital(conexao);

			}
			if (botMirarClick.get()) {
				botaoMirar.setClicked(false);
				botaoMirar.setVisible(false);
				textoPainel.setVisible(true);
				textoPainel2.setVisible(true);
				new Navegacao(conexao);
			}
			Thread.sleep(1000);
		}
	}

	public static Panel adicionarPainel(Canvas telaItens)
			throws IOException, RPCException, InterruptedException, StreamException {
		// Adicionar um painel para conter os elementos de UI
		Panel painelInfo = telaItens.addPanel(true);
		// Posicionar o painel à esquerda da tela
		RectTransform retangulo = painelInfo.getRectTransform();
		retangulo.setSize(new Pair<Double, Double>(450.0, 40.0));
		retangulo.setPosition(new Pair<Double, Double>(0.0, tamanhoTela.getValue1() / 2 - 85.0));

		botaoDecolar = painelInfo.addButton("Decolar", true);
		botaoDecolar.getRectTransform().setSize(new Pair<Double, Double>(125.0, 30.0));
		botaoDecolar.getRectTransform().setPosition(new Pair<Double, Double>(-150.0, 0.0));

		botaoFlutuar = painelInfo.addButton("Flutuar", true);
		botaoFlutuar.getRectTransform().setSize(new Pair<Double, Double>(125.0, 30.0));
		botaoFlutuar.getRectTransform().setPosition(new Pair<Double, Double>(0.0, 0.0));

		botaoMirar = painelInfo.addButton("Mirar", true);
		botaoMirar.getRectTransform().setSize(new Pair<Double, Double>(125.0, 30.0));
		botaoMirar.getRectTransform().setPosition(new Pair<Double, Double>(0.0, -50.0));

		botaoPousar = painelInfo.addButton("Pousar", true);
		botaoPousar.getRectTransform().setSize(new Pair<Double, Double>(125.0, 30.0));
		botaoPousar.getRectTransform().setPosition(new Pair<Double, Double>(150.0, 0.0));

		// Adicionar texto mostrando infos

		textoPainel = painelInfo.addText("Alt:", false);
		textoPainel.getRectTransform().setPosition(new Pair<Double, Double>(10.0, 0.0));
		textoPainel.getRectTransform().setSize(new Pair<Double, Double>(retangulo.getSize().getValue0(), margemUI));
		textoPainel.setColor(new Triplet<Double, Double, Double>(1.0, 1.0, 1.0));
		textoPainel.setSize(18);

		textoPainel2 = painelInfo.addText("0", false);
		textoPainel2.getRectTransform().setPosition(new Pair<Double, Double>(50.0, 0.0));
		textoPainel2.getRectTransform().setSize(new Pair<Double, Double>(retangulo.getSize().getValue0(), margemUI));
		textoPainel2.setColor(new Triplet<Double, Double, Double>(1.0, 1.0, 1.0));
		textoPainel2.setSize(18);
		return painelInfo;
	}

	public static void setTextoPainel(String texto) throws RPCException, IOException {
		textoPainel.setContent(texto);
	}

	public static void setTextoPainel2(String texto) throws RPCException, IOException {
		textoPainel2.setContent(texto);
	}
	public static void resetarPainel() throws RPCException,IOException {
		botaoFlutuar.setClicked(false);
		botaoDecolar.setClicked(false);
		botaoMirar.setClicked(false);
		botaoPousar.setClicked(false);
		
		botaoPousar.setVisible(true);
		botaoDecolar.setVisible(true);
		botaoFlutuar.setVisible(true);
		botaoMirar.setVisible(true);
		
		textoPainel.setVisible(false);
		textoPainel2.setVisible(false);
		textoPainel.setContent("");
		textoPainel2.setContent("");
		
	}
}
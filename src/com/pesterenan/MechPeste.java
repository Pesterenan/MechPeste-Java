package com.pesterenan;

import static com.pesterenan.utils.Dicionario.CONECTAR;
import static com.pesterenan.utils.Dicionario.ERRO_AO_CONECTAR;
import static com.pesterenan.utils.Modulos.EXECUTAR_DECOLAGEM;
import static com.pesterenan.utils.Modulos.EXECUTAR_MANOBRA;
import static com.pesterenan.utils.Dicionario.MECHPESTE;
import static com.pesterenan.utils.Dicionario.TELEMETRIA;
import static com.pesterenan.utils.Modulos.APOASTRO;
import static com.pesterenan.utils.Modulos.DIRECAO;
import static com.pesterenan.utils.Status.CONECTADO;
import static com.pesterenan.utils.Status.CONECTANDO;
import static com.pesterenan.utils.Status.ERRO_CONEXAO;
import static com.pesterenan.utils.Status.STATUS_DECOLAGEM_ORBITAL;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.pesterenan.controller.DecolagemOrbitalController;
import com.pesterenan.controller.ManobrasController;
import com.pesterenan.controller.TelemetriaController;
import com.pesterenan.gui.Arquivos;
import com.pesterenan.gui.MainGui;
import com.pesterenan.gui.StatusJPanel;
import com.pesterenan.utils.Dicionario;
import com.pesterenan.utils.Modulos;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

public class MechPeste implements PropertyChangeListener {

	private static MechPeste mechPeste = null;

	private static Connection conexao;
	private static Thread threadModulos;
	private static Thread threadTelemetria;
	private static TelemetriaController telemetriaCtrl;
	private static DecolagemOrbitalController decolagemOrbitalCtrl;
	private static ManobrasController manobrasCtrl;

	public static void main(String[] args) throws StreamException, RPCException, IOException, InterruptedException {
		MechPeste.getInstance();
	}

	private MechPeste() {
		MainGui.getInstance();
		MainGui.getStatus().addPropertyChangeListener(this);
		MainGui.getFuncoes().addPropertyChangeListener(this);
		iniciarConexao();
//		new Arquivos();
	}

	public static MechPeste getInstance() {
		if (mechPeste == null) {
			mechPeste = new MechPeste();
		}
		return mechPeste;
	}

	public void iniciarConexao() {
		if (getConexao() == null) {
			try {
				StatusJPanel.setStatus(CONECTANDO.get());
				MechPeste.conexao = Connection.newInstance(MECHPESTE.get());
				StatusJPanel.setStatus(CONECTADO.get());
				StatusJPanel.botConectarVisivel(false);
				iniciarTelemetria();
			} catch (IOException e) {
				System.err.println(ERRO_AO_CONECTAR.get() + e.getMessage());
				try {
					Arquivos.criarLogDeErros(e.getStackTrace());
				} catch (IOException e1) {
					System.err.println("Erro ao criar log de Erros:\n\t" + e1.getMessage());
				}
				StatusJPanel.setStatus(ERRO_CONEXAO.get());
				StatusJPanel.botConectarVisivel(true);
			}
		}
	}

	private void iniciarTelemetria() {
		telemetriaCtrl = new TelemetriaController(getConexao());
		setThreadTelemetria(new Thread(telemetriaCtrl));
		getThreadTelemetria().start();
	}

	public static void iniciarModulo(Modulos modulo) {
		Map<Modulos, String> valores = new HashMap<>();
		iniciarModulo(modulo, valores);
	}

	public static void iniciarModulo(Modulos modulo, Map<Modulos, String> valores) {
		if (modulo.equals(EXECUTAR_DECOLAGEM)) {
			if (validarDecolagem(valores)) {
				StatusJPanel.setStatus(STATUS_DECOLAGEM_ORBITAL.get());
				MainGui.getParametros().firePropertyChange(TELEMETRIA.get(), 0, 1);
				decolagemOrbitalCtrl = new DecolagemOrbitalController(getConexao());
				decolagemOrbitalCtrl.setAltApoastroFinal(Float.parseFloat(valores.get(APOASTRO)));
				decolagemOrbitalCtrl.setDirecao(Float.parseFloat(valores.get(DIRECAO)));
				setThreadModulos(new Thread(decolagemOrbitalCtrl));
				getThreadModulos().start();
			}
		}
		if (modulo.equals(EXECUTAR_MANOBRA)) {
			try {
				MainGui.getParametros().firePropertyChange(TELEMETRIA.get(), 0, 1);
				manobrasCtrl = new ManobrasController();
				manobrasCtrl.setFuncao(valores.get(EXECUTAR_MANOBRA));
				setThreadModulos(new Thread(manobrasCtrl));
				getThreadModulos().start();
			} catch (RPCException e) {
			}
		}
	}

	private static boolean validarDecolagem(Map<Modulos, String> valores) {
		try {
			Float.parseFloat(valores.get(APOASTRO));
			Float.parseFloat(valores.get(DIRECAO));
		} catch (NumberFormatException nfe) {
			StatusJPanel.setStatus("Os campos só aceitam números");
			return false;
		}
		return true;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String evtNomeProp = evt.getPropertyName();
		if (evtNomeProp.equals(CONECTAR.get())) {
			iniciarConexao();
		}
	}

	public static void finalizarTarefa() throws IOException {
		if (getThreadModulos().isAlive()) {
			getThreadModulos().interrupt();
			setThreadModulos(null);
		}
	}

	public static Connection getConexao() {
		return conexao;
	}

	private static Thread getThreadModulos() {
		return threadModulos;
	}

	private static void setThreadModulos(Thread threadModulos) {
		MechPeste.threadModulos = threadModulos;
	}

	private static Thread getThreadTelemetria() {
		return threadTelemetria;
	}

	private static void setThreadTelemetria(Thread threadTelemetria) {
		MechPeste.threadTelemetria = threadTelemetria;
	}
}

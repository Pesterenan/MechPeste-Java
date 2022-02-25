package com.pesterenan;

import static com.pesterenan.utils.Dicionario.CONECTAR;
import static com.pesterenan.utils.Dicionario.ERRO_AO_CONECTAR;
import static com.pesterenan.utils.Dicionario.MECHPESTE;
import static com.pesterenan.utils.Dicionario.TELEMETRIA;
import static com.pesterenan.utils.Status.CONECTADO;
import static com.pesterenan.utils.Status.CONECTANDO;
import static com.pesterenan.utils.Status.ERRO_CONEXAO;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Map;

import com.pesterenan.controller.DecolagemOrbitalController;
import com.pesterenan.controller.ManobrasController;
import com.pesterenan.controller.TelemetriaController;
import com.pesterenan.gui.MainGui;
import com.pesterenan.gui.StatusJPanel;
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

	public static void main(String[] args) throws StreamException, RPCException, IOException, InterruptedException {
		MechPeste.getInstance();
	}

	private MechPeste() {
		MainGui.getInstance();
		MainGui.getStatus().addPropertyChangeListener(this);
		MainGui.getFuncoes().addPropertyChangeListener(this);
		iniciarConexao();
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

	public static void iniciarModulo(Map<String, String> comandos) {
		TelemetriaController modulo = null;
		String executarModulo = comandos.get(Modulos.MODULO.get());

		if (executarModulo.equals(Modulos.MODULO_MANOBRAS.get())) {
			modulo = new ManobrasController(comandos.get(Modulos.FUNCAO.get()));
		}
		if (executarModulo.equals(Modulos.MODULO_DECOLAGEM.get())) {
			modulo = new DecolagemOrbitalController(comandos);
		}
		setThreadModulos(new Thread(modulo));
		getThreadModulos().start();
		MainGui.getParametros().firePropertyChange(TELEMETRIA.get(), 0, 1);
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

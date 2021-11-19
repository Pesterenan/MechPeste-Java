package com.pesterenan;

import static com.pesterenan.utils.Dicionario.*;
import static com.pesterenan.utils.Status.*;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.JComponent;

import com.pesterenan.controller.DecolagemOrbitalController;
import com.pesterenan.controller.TelemetriaController;
import com.pesterenan.gui.Arquivos;
import com.pesterenan.gui.MainGui;
import com.pesterenan.gui.StatusJPanel;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

public class MechPeste implements PropertyChangeListener {

	private static Connection conexao;
	private static Thread threadModulos;
	private static Thread threadTelemetria;
	private static TelemetriaController telemetriaCtrl;
	private static DecolagemOrbitalController decolagemOrbitalCtrl;

	public static void main(String[] args) throws StreamException, RPCException, IOException, InterruptedException {
		new MechPeste();
	}

	private MechPeste() {
		new MainGui();
		iniciarConexao();
		MainGui.getStatus().addPropertyChangeListener(this);
		MainGui.getFuncoes().addPropertyChangeListener(this);
//		GUI gui = new GUI();
//		gui.addPropertyChangeListener(this);
//		new Arquivos();
	}

	public void iniciarConexao() {
		StatusJPanel.setStatus(CONECTANDO.get());
		if (getConexao() == null) {
			try {
				setConexao(Connection.newInstance(MECHPESTE.get()));
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

	public static void iniciarThreadModulos(String modulo) {
		decolagemOrbitalCtrl = new DecolagemOrbitalController(getConexao());
		setThreadModulos(new Thread(decolagemOrbitalCtrl));
		getThreadModulos().start();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String evtNomeProp = evt.getPropertyName();
		if (evtNomeProp.equals(CONECTAR.get())) {
			iniciarConexao();
		}	
		if(evtNomeProp.equals(EXECUTAR_DECOLAGEM.get())){
			StatusJPanel.setStatus(STATUS_DECOLAGEM_ORBITAL.get());
			iniciarThreadModulos(evtNomeProp);
		}			

//		if (getThreadModulos() == null) {
//			iniciarConexao();
//			setThreadModulos(new Thread(new Runnable() {
//				public void run() {
//					naveAtual = new Nave(getConexao());
//					try {
//						switch (Dicionario.valueOf(evt.getPropertyName())) {
//						case DECOLAGEM_ORBITAL:
//							naveAtual.decolagemOrbital();
//							break;
//						case POUSO_AUTOMATICO:
//							StatusJPanel.setStatus(EXEC_POUSO_AUTO.get());
//							naveAtual.suicideBurn();
//							break;
//						case ROVER_AUTONOMO:
//							StatusJPanel.setStatus(EXEC_ROVER.get());
//							new RoverAutonomoController(getConexao());
//							naveAtual.autoRover();
//							break;
//						case MANOBRAS:
//							StatusJPanel.setStatus(EXEC_MANOBRAS.get());
//							new ManobrasController(true);
//							naveAtual.manobras();
//							break;
//						default:
//							break;
//						}
//					} catch (Exception e) {
//						try {
//							Arquivos.criarLogDeErros(e.getStackTrace());
//						} catch (IOException e1) {
//						}
//						e.printStackTrace();
//						StatusJPanel.setStatus(ERRO_DECOLAGEM_ORBITAL.get());
//						GUI.botConectarVisivel(true);
//						setThreadModulos(null);
//					} finally {
//						StatusJPanel.setStatus(PRONTO.get());
//						try {
//							finalizarTarefa();
//						} catch (IOException e) {
//							System.err.println("Deu erro! :D " + e.getMessage());
//						}
//					}
//				}
//			}));
//			getThreadModulos().start();
//
//		}
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

	private static void setConexao(Connection conexao) {
		MechPeste.conexao = conexao;
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

	public MechPeste get() {
		return this;
	}

}

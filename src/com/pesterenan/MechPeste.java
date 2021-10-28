package com.pesterenan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import com.pesterenan.controller.RoverAutonomoController;
import com.pesterenan.controller.ManobrasController;
import com.pesterenan.gui.Arquivos;
import com.pesterenan.gui.FuncoesJPanel;
import com.pesterenan.gui.GUI;
import com.pesterenan.gui.MainGui;
import com.pesterenan.gui.Status;
import com.pesterenan.gui.StatusJPanel;
import com.pesterenan.model.Nave;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

public class MechPeste implements PropertyChangeListener {
	
	private static Connection conexao;
	private static Thread threadModulos;
	private Nave naveAtual;

	public static void main(String[] args) throws StreamException, RPCException, IOException, InterruptedException {
		new MechPeste();
		
	}

	private MechPeste() {
		new MainGui();
		iniciarConexao();
		MainGui.getStatus().addPropertyChangeListener(this);
//		GUI gui = new GUI();
//		gui.addPropertyChangeListener(this);
//		new Arquivos();
	}

	public static void iniciarConexao() {
		StatusJPanel.setStatus(Status.CONECTANDO);
		if (getConexao() == null) {
			try {
				setConexao(Connection.newInstance("MechPeste"));
				StatusJPanel.setStatus(Status.CONECTADO);
				StatusJPanel.botConectarVisivel(false);
			} catch (IOException e) {
				System.err.println("Erro ao se conectar ao jogo:\n\t" + e.getMessage());
				try {
					Arquivos.criarLogDeErros(e.getStackTrace());
				} catch (IOException e1) {
					System.err.println("Erro ao criar log de Erros:\n\t" + e1.getMessage());
				}
				StatusJPanel.setStatus(Status.ERRO_CONEXAO);
				StatusJPanel.botConectarVisivel(true);
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(StatusJPanel.conectar)) {
			iniciarConexao();
		}
		if (getThreadModulos() == null) {
			iniciarConexao();
			setThreadModulos(new Thread(new Runnable() {
				public void run() {
					naveAtual = new Nave(getConexao());
					try {
						switch (evt.getPropertyName()) {
						case FuncoesJPanel.decolagemOrbital:
							StatusJPanel.setStatus(Status.EXEC_DECOLAGEM_ORBITAL);
							naveAtual.decolagemOrbital();
							break;
						case GUI.suicideBurn:
							StatusJPanel.setStatus(Status.EXEC_POUSO_AUTO);
							naveAtual.suicideBurn();
							break;
						case GUI.autoRover:
							StatusJPanel.setStatus(Status.EXEC_ROVER);
							new RoverAutonomoController(getConexao());
							naveAtual.autoRover();
							break;
						case GUI.manobras:
							StatusJPanel.setStatus(Status.EXEC_MANOBRAS);
							new ManobrasController(true);
							naveAtual.manobras();
							break;
						}
					} catch (Exception e) {
						try {
							Arquivos.criarLogDeErros(e.getStackTrace());
						} catch (IOException e1) {
						}
						e.printStackTrace();
						StatusJPanel.setStatus(Status.ERRO_DECOLAGEM_ORBITAL);
						GUI.botConectarVisivel(true);
						setThreadModulos(null);
					} finally {
						StatusJPanel.setStatus(Status.PRONTO);
						try {
							finalizarTarefa();
						} catch (IOException e) {
							System.err.println("Deu erro! :D " + e.getMessage());
						}
					}
				}
			}));
			getThreadModulos().start();

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

	public static void setConexao(Connection conexao) {
		MechPeste.conexao = conexao;
	}

	public static Thread getThreadModulos() {
		return threadModulos;
	}

	public static void setThreadModulos(Thread threadModulos) {
		MechPeste.threadModulos = threadModulos;
	}

}

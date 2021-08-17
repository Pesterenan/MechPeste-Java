package com.pesterenan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import com.pesterenan.funcoes.AutoRover;
import com.pesterenan.funcoes.Manobras;
import com.pesterenan.gui.Arquivos;
import com.pesterenan.gui.GUI;
import com.pesterenan.gui.Status;
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
		GUI gui = new GUI();
		gui.addPropertyChangeListener(this);
		new Arquivos();
		iniciarConexao();
	}

	public static void iniciarConexao() {
		if (getConexao() == null) {
			try {
				GUI.setStatus(Status.CONECTANDO.get());
				setConexao(Connection.newInstance("MechPeste"));
				GUI.setStatus(Status.CONECTADO.get());
				GUI.botConectarVisivel(false);
			} catch (IOException e) {
				System.err.println("Erro ao se conectar ao jogo:\n\t" + e.getMessage());
				try {
					Arquivos.criarLogDeErros(e.getStackTrace());
				} catch (IOException e1) {
					System.err.println("Erro ao criar log de Erros:\n\t" + e1.getMessage());
				}
				GUI.setStatus(Status.ERROCONEXAO.get());
				GUI.botConectarVisivel(true);
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(GUI.conectar)) {
			iniciarConexao();
		}
		if (getThreadModulos() == null) {
			iniciarConexao();
			System.out.println("chamou");
			setThreadModulos(new Thread(new Runnable() {
				public void run() {
					naveAtual = new Nave(getConexao());
					try {
						switch (evt.getPropertyName()) {
						case GUI.decolagemOrbital:
							GUI.setStatus(Status.EXECDECOLAGEM.get());
							naveAtual.decolagemOrbital();
							break;
						case GUI.suicideBurn:
							GUI.setStatus(Status.EXECSUICIDE.get());
							naveAtual.suicideBurn();
							break;
						case GUI.autoRover:
							GUI.setStatus(Status.EXECROVER.get());
							new AutoRover(getConexao());
							naveAtual.autoRover();
							break;
						case GUI.manobras:
							GUI.setStatus(Status.EXECMANOBRAS.get());
							new Manobras(true);
							naveAtual.manobras();
							break;
						}
					} catch (Exception e) {
						try {
							Arquivos.criarLogDeErros(e.getStackTrace());
						} catch (IOException e1) {
						}
						e.printStackTrace();
						GUI.setStatus(Status.ERRODECOLAGEM.get());
						GUI.botConectarVisivel(true);
						setThreadModulos(null);
					} finally {
						GUI.setStatus(Status.PRONTO.get());
						setThreadModulos(null);
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

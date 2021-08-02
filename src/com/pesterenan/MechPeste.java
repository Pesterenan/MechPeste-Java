package com.pesterenan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.JFrame;

import com.pesterenan.funcoes.AutoRover;
import com.pesterenan.funcoes.DecolagemOrbital;
import com.pesterenan.funcoes.Manobras;
import com.pesterenan.funcoes.SuicideBurn;
import com.pesterenan.funcoes.VooAutonomo;
import com.pesterenan.gui.Arquivos;
import com.pesterenan.gui.GUI;
import com.pesterenan.gui.Status;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

public class MechPeste implements PropertyChangeListener {
	public static Connection conexao;
	public static Thread threadModulos;

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
		if (conexao == null) {
			try {
				GUI.setStatus(Status.CONECTANDO.get());
				conexao = Connection.newInstance("MechPeste");
				GUI.setStatus(Status.CONECTADO.get());
				GUI.botConectarVisivel(false);
			} catch (IOException e) {
				try {
					Arquivos.criarLogDeErros(e.getStackTrace());
				} catch (IOException e1) {
				}
				GUI.setStatus(Status.ERROCONEXAO.get());
				GUI.botConectarVisivel(true);
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(GUI.conectar)) {
			GUI.botConectarVisivel(false);
			iniciarConexao();
		}
		if (threadModulos == null) {
			if (conexao == null) {
				iniciarConexao();
			} else {
				threadModulos = new Thread(new Runnable() {
					public void run() {
						try {
							switch (evt.getPropertyName()) {
							case GUI.decolagemOrbital:
								GUI.setStatus(Status.EXECDECOLAGEM.get());
								new DecolagemOrbital(conexao);
								break;
							case GUI.suicideBurn:
								GUI.setStatus(Status.EXECSUICIDE.get());
								new SuicideBurn(conexao);
								break;
							case GUI.autoRover:
								GUI.setStatus(Status.EXECROVER.get());
								new AutoRover(conexao);
								break;
							case GUI.manobras:
								GUI.setStatus(Status.EXECMANOBRAS.get());
								new Manobras(conexao, true);
								break;
							default:
								GUI.setStatus(Status.PRONTO.get());
								threadModulos = null;
							}
						} catch (Exception e) {
							try {
								Arquivos.criarLogDeErros(e.getStackTrace());
							} catch (IOException e1) {
							}
							e.printStackTrace();
							GUI.setStatus(Status.ERRODECOLAGEM.get());
							GUI.botConectarVisivel(true);
							threadModulos = null;
						}
					}
				});
			}
		}
	}

	public static void finalizarTarefa() throws IOException {
		if (threadModulos.isAlive()) {
			threadModulos.interrupt();
			threadModulos = null;
			conexao.close();
			conexao = null;
		}
	}
}

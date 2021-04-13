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
	private static JFrame gui;

	public static void main(String[] args) throws StreamException, RPCException, IOException, InterruptedException {
		new MechPeste();
	}

	private MechPeste() {
		gui = new GUI();
		gui.addPropertyChangeListener(this);
		new Arquivos();
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
			iniciarConexao();
			switch (evt.getPropertyName()) {
			case GUI.decolagemOrbital:
				rodarDecolagemOrbital();
				break;
			case GUI.suicideBurn:
				rodarSuicideBurn();
				break;
			case GUI.autoRover:
				rodarAutoRover();
				break;
			case GUI.manobras:
				rodarManobras();
				break;
			case GUI.dev:
				rodarDev();
				break;
			}
		} else {
			GUI.setStatus(Status.JAEXEC.get());
		}

	}

	private void rodarDecolagemOrbital() {
		threadModulos = new Thread(new Runnable() {
			public void run() {
				try {
					GUI.setStatus(Status.EXECDECOLAGEM.get());
					new DecolagemOrbital(conexao);
					GUI.setStatus(Status.PRONTO.get());
					threadModulos = null;
				} catch (InterruptedException sleep) {
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
		threadModulos.start();
	}

	private void rodarSuicideBurn() {
		threadModulos = new Thread(new Runnable() {
			public void run() {
				try {
					GUI.setStatus(Status.EXECSUICIDE.get());
					new SuicideBurn(conexao);
					GUI.setStatus(Status.PRONTO.get());
					threadModulos = null;
				} catch (InterruptedException sleep) {
				} catch (Exception e) {
					try {
						Arquivos.criarLogDeErros(e.getStackTrace());
					} catch (IOException e1) {
					}
					e.printStackTrace();
					GUI.setStatus(Status.ERROSUICIDE.get());
					GUI.botConectarVisivel(true);
					threadModulos = null;
				}
			}
		});
		threadModulos.start();
	}

	private void rodarAutoRover() {
		threadModulos = new Thread(new Runnable() {
			public void run() {
				try {
					GUI.setStatus(Status.EXECROVER.get());
					new AutoRover(conexao);
					GUI.setStatus(Status.PRONTO.get());
					threadModulos = null;

				} catch (InterruptedException sleep) {
				} catch (Exception e) {
					try {
						Arquivos.criarLogDeErros(e.getStackTrace());
					} catch (IOException e1) {
					}
					e.printStackTrace();
					GUI.setStatus(Status.ERROROVER.get());
					GUI.botConectarVisivel(true);
					threadModulos = null;
				}
			}
		});
		threadModulos.start();
	}

	private void rodarManobras() {
		threadModulos = new Thread(new Runnable() {
			public void run() {
				try {
					GUI.setStatus(Status.EXECMANOBRAS.get());
					new Manobras(conexao, true);
					GUI.setStatus(Status.PRONTO.get());
					threadModulos = null;
				} catch (InterruptedException sleep) {
				} catch (Exception e) {
					try {
						Arquivos.criarLogDeErros(e.getStackTrace());
					} catch (IOException e1) {
					}
					e.printStackTrace();
					GUI.setStatus(Status.ERROMANOBRAS.get());
					GUI.botConectarVisivel(true);
					threadModulos = null;
				}
			}
		});
		threadModulos.start();
	}

	private void rodarDev() {
		threadModulos = new Thread(new Runnable() {
			public void run() {
				try {
					new VooAutonomo(conexao);
					GUI.setStatus(Status.PRONTO.get());
					threadModulos = null;
				} catch (Exception e) {
					try {
						Arquivos.criarLogDeErros(e.getStackTrace());
					} catch (IOException e1) {
					}
					GUI.setStatus(Status.ERROMANOBRAS.get());
					GUI.botConectarVisivel(true);
					threadModulos = null;
				}
			}
		});
		threadModulos.start();
	}

//		case "botSuicideMulti":
//			Vessel naveAtual = null;
//			try {
//				naveAtual = SpaceCenter.newInstance(conexao).getActiveVessel();
//			} catch (RPCException e2) {
//				GUI.setStatus(Status.ERROCONEXAO.get());
//			}
//			try {
//				for (Vessel nave : centroEspacial.getVessels()) {
//					if (nave.getName().contains(naveAtual.getName())) {
//						new Thread(new Runnable() {
//							@Override
//							public void run() {
//								try {
//									System.out.println("Executando Suicide Burn para: " + nave.getName());
//									new SuicideBurn(conexao, nave);
//								} catch (StreamException | RPCException | IOException | InterruptedException e) {
//									GUI.setStatus(Status.ERROCONEXAO.get());
//								}
//							}
//						}).start();
//					}
//				}
//			} catch (RPCException e1) {
//				GUI.setStatus(Status.ERROCONEXAO.get());
//			}
//			break;

	public static void finalizarTarefa() throws IOException {
		if (threadModulos.isAlive()) {
			threadModulos.interrupt();
			threadModulos = null;
			conexao.close();
			conexao = null;
		}
	}
}

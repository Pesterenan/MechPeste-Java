package com.pesterenan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.JFrame;

import com.pesterenan.funcoes.AutoRover;
import com.pesterenan.funcoes.DecolagemOrbital;
import com.pesterenan.funcoes.Manobras;
import com.pesterenan.funcoes.SuicideBurn;
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
	}

	public static void iniciarConexao() {
		try {
			GUI.setStatus(Status.CONECTANDO.get());
			conexao = Connection.newInstance("MechPeste");
			GUI.setStatus(Status.CONECTADO.get());
			GUI.botConectarVisivel(false);
		} catch (IOException e) {
			GUI.setStatus(Status.ERROCONEXAO.get());
			GUI.botConectarVisivel(true);
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
				} catch (Exception e) {
					GUI.setStatus(Status.ERROCONEXAO.get());
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
				} catch (Exception e) {
					GUI.setStatus(Status.ERROCONEXAO.get());
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
				} catch (Exception e) {
					GUI.setStatus(Status.ERROCONEXAO.get());
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
				} catch (Exception e) {
					GUI.setStatus(Status.ERROCONEXAO.get());
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

//		case "botVooAutonomo":
//			GUI.setStatus(Status.EXECSUICIDE.get());
//			if (t_VooAutonomo == null) {
//				t_VooAutonomo = new Thread(new Runnable() {
//					@Override
//					public void run() {
//						try {
//							new VooAutonomo(conexao);
//						} catch (StreamException | RPCException e) {
//							GUI.setStatus(Status.ERROCONEXAO.get());
//						}
//					}
//				});
//				t_VooAutonomo.start();
//			} else {
//				GUI.setStatus(Status.JAEXEC.get());
//			}
//			break;
//		}
	public static void finalizarTarefa() throws IOException {
		if (threadModulos.isAlive()) {
			threadModulos.interrupt();
			threadModulos = null;
			conexao.close();
		}
	}
}

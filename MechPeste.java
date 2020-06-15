package com.pesterenan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.JFrame;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;

public class MechPeste implements PropertyChangeListener {
	protected static Connection conexao;
	protected static SpaceCenter centroEspacial;
	protected static MechPeste mp;
	public static Thread threadModulos;
	private JFrame gui;

	public static void main(String[] args) throws StreamException, RPCException, IOException, InterruptedException {
		mp = new MechPeste();
	}

	public MechPeste() throws InterruptedException, RPCException {
		gui = new GUI();
		gui.addPropertyChangeListener(this);

		iniciarConexao();
	}

	public static void iniciarConexao() {
		try {
			GUI.setStatus(Status.CONECTANDO.get());
			conexao = Connection.newInstance("MechPeste");
			centroEspacial = SpaceCenter.newInstance(conexao);
			GUI.setStatus(Status.CONECTADO.get());
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
		GUI.setStatus(Status.EXECDECOLAGEM.get());
		threadModulos = new Thread(new Runnable() {
			public void run() {
				try {
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
		GUI.setStatus(Status.EXECSUICIDE.get());
		threadModulos = new Thread(new Runnable() {
			public void run() {
				try {
					new SuicideBurn(conexao);
					GUI.setStatus(Status.PRONTO.get());
					threadModulos = null;
				} catch (StreamException | RPCException | IOException | InterruptedException | NullPointerException e) {
					GUI.setStatus(Status.ERROCONEXAO.get());
					GUI.botConectarVisivel(true);
					threadModulos = null;
				}
			}
		});
		threadModulos.start();
	}

	private void rodarAutoRover() {
		GUI.setStatus(Status.EXECROVER.get());
		threadModulos = new Thread(new Runnable() {
			public void run() {
				try {
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
		GUI.setStatus(Status.EXECMANOBRAS.get());
		threadModulos = new Thread(new Runnable() {
			public void run() {
				try {
					new Manobras(centroEspacial);

				} catch (Exception e) {
					e.printStackTrace();
					GUI.setStatus(Status.ERROCONEXAO.get());
					GUI.botConectarVisivel(true);
					System.out.println("Erro");
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
	static void finalizarTarefa() throws IOException {
		if (threadModulos.isAlive()) {
			threadModulos.interrupt();
			threadModulos = null;
		}
	}
}

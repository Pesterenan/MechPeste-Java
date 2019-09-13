package com.pesterenan;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;

public class MechPeste extends JFrame implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static Connection conexao;
	protected static SpaceCenter centroEspacial;
	protected static ReferenceFrame pontoRef;
	protected static Vessel naveAtual;
	protected static Flight vooNave;
	protected static MechPeste mp;

	private static int numConexao = 0;

	private Thread t_SuicideBurn;
	private Thread t_DecolagemOrbital;
	private Thread t_VooAutonomo;
	private JPanel painelFuncoes;
	private JPanel painelParametros;
	private JPanel painelStatus;

	public static void main(String[] args) throws StreamException, RPCException, IOException, InterruptedException {
		mp = new MechPeste();
	}

	public MechPeste() throws InterruptedException, RPCException {
		super("MechPeste - por Pesterenan");
		iniciarGUI();

//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				iniciarGUI();
//			}
//		});
		while (numConexao < 3) {
			try {
				painelStatus.firePropertyChange("status", 0, 1);
				iniciarConexao();
				numConexao = 3;
			} catch (Exception e) {
				numConexao++;
				painelStatus.firePropertyChange("status", 0, 2);
				Thread.sleep(5000);
			}
		}
	}

	private void iniciarGUI() {
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(400, 240));
		setLocation(500, 200);
		painelFuncoes = new Paineis().PainelFuncoes();
		painelParametros = new Paineis().PainelParametros();
		painelStatus = new Paineis().PainelStatus();

		add(painelFuncoes, BorderLayout.WEST);
		add(painelParametros, BorderLayout.CENTER);
		add(painelStatus, BorderLayout.SOUTH);
		painelFuncoes.addPropertyChangeListener(this);

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);

		setVisible(true);

	}

	public static void iniciarConexao() throws StreamException, RPCException, IOException, InterruptedException {
		conexao = Connection.newInstance("MechPeste");
		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = centroEspacial.getActiveVessel();
		pontoRef = naveAtual.getOrbit().getBody().getReferenceFrame();
		vooNave = naveAtual.flight(pontoRef);

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String evento = evt.getPropertyName();
		switch (evento) {
		case ("botSuicideBurn"): {
			painelStatus.firePropertyChange("status", 0, 4);
			if (t_SuicideBurn == null) {
				t_SuicideBurn = new Thread(new Runnable() {
					@Override
					public void run() {
						boolean SuicideBurnRodando = true;
						while (SuicideBurnRodando) {
							try {
								new SuicideBurn(conexao, naveAtual);
								SuicideBurnRodando = false;
								t_SuicideBurn = null;

							} catch (StreamException | RPCException | IOException | InterruptedException
									| NullPointerException e) {
								e.printStackTrace();
								SuicideBurnRodando = false;
								t_SuicideBurn = null;
							}
						}
					}
				});
				t_SuicideBurn.start();
			} else {
				painelStatus.firePropertyChange("status", 0, 5);
			}
			break;
		}
		case ("botDecolagem"): {
			painelStatus.firePropertyChange("status", 0, 3);
			if (t_DecolagemOrbital == null) {
				t_DecolagemOrbital = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							new DecolagemOrbital(conexao, naveAtual);
							t_DecolagemOrbital = null;
						} catch (StreamException | RPCException | IOException | InterruptedException e) {
							t_DecolagemOrbital = null;
						}
					}
				});
				t_DecolagemOrbital.start();
			} else {
				painelStatus.firePropertyChange("status", 0, 5);
			}
			break;
		}
		case ("botSuicideMulti"): {
			try {
				for (Vessel nave : centroEspacial.getVessels()) {
					if (nave.getName().contains(naveAtual.getName())) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									System.out.println("Executando Suicide Burn para: " + nave.getName());
									new SuicideBurn(conexao, nave);
								} catch (StreamException | RPCException | IOException | InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}).start();
					}
				}
			} catch (RPCException e1) {
				e1.printStackTrace();
			}
			break;
		}
		case ("botAutoRover"): {
			System.out.println("OLOCO3");
			break;
		}
		case ("botVooAutonomo"): {
			painelStatus.firePropertyChange("status", 0, 4);
			if (t_VooAutonomo == null) {
				t_VooAutonomo = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							new VooAutonomo(conexao, naveAtual);
						} catch (StreamException | RPCException e) {
							e.printStackTrace();
						}
					}
				});
				t_VooAutonomo.start();
			} else {
				painelStatus.firePropertyChange("status", 0, 5);
			}
			break;
		}
		}
	}
}
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.WindowConstants;

import org.javatuples.Triplet;

import krpc.client.Connection;
import krpc.client.RPCException;

import krpc.client.StreamException;
import krpc.client.services.Drawing;
import krpc.client.services.Drawing.Line;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MechPeste extends JFrame implements ActionListener, PropertyChangeListener {
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

	private JPanel painelBotoes;
	private JButton botSuicideBurn;
	private JButton botDecolagem;
	private JButton botAutoRover;
	private JButton botSuicideMulti;
	private JButton botVooAutonomo;
	private Thread t_SuicideBurn;
	private Thread t_DecolagemOrbital;
	private Thread t_VooAutonomo;
	private JLabel labelAltitude;
	

	public static void main(String[] args) throws StreamException, RPCException, IOException, InterruptedException {
		mp = new MechPeste();
	}

	private MechPeste() throws InterruptedException {
		super("MechPeste - por Pesterenan");
		try {
			iniciarConexao();
			criarJanela();
		} catch (Exception e) {
			numConexao++;
			System.out.println("Erro de conexÃ£o: " + e.getMessage());
			Thread.sleep(5000);
			if (numConexao < 3) {
				new MechPeste();
			}
		}
	}

	private void criarJanela() throws RPCException {
		painelBotoes = new JPanel();

		painelBotoes.setLayout(new GridLayout(0,1,5,5));
		botSuicideBurn = new JButton("Suicide Burn");
		botDecolagem = new JButton("Decolagem Orbital");
		botAutoRover = new JButton("Auto-Rover");
		botSuicideMulti = new JButton("Multi SuicideBurn");
		botVooAutonomo = new JButton("Voo Autônomo");
		
		JPanel p = new JPanel();
		p.add(botSuicideBurn);
		painelBotoes.add(p);
		p = new JPanel();
		p.add(botSuicideMulti);
		painelBotoes.add(p);
		p = new JPanel();
		p.add(botDecolagem);
		painelBotoes.add(p);
		p = new JPanel();
		p.add(botAutoRover);
		painelBotoes.add(p);
		p = new JPanel();
		p.add(botVooAutonomo);
		painelBotoes.add(p);
		botSuicideBurn.addActionListener(this);
		botSuicideMulti.addActionListener(this);
		botDecolagem.addActionListener(this);
		botAutoRover.addActionListener(this);
		botVooAutonomo.addActionListener(this);

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);
		setSize(250, 250);
		setLocation(500, 200);
		p = new JPanel();
		p.add(painelBotoes);
		add(p);
		setVisible(true);
	}

	public static void iniciarConexao() throws StreamException, RPCException, IOException, InterruptedException {
		conexao = Connection.newInstance("MechPeste");
		centroEspacial = SpaceCenter.newInstance(conexao);
		naveAtual = centroEspacial.getActiveVessel();
		pontoRef = naveAtual.getOrbit().getBody().getReferenceFrame();
		vooNave = naveAtual.flight(pontoRef);
	}

	public void actionPerformed(ActionEvent e) {
		Object fonte = e.getSource();
		if (fonte.equals(botSuicideMulti)) {
			try {
				List<Vessel> naves = new ArrayList<Vessel>();
				for (Vessel nave : centroEspacial.getVessels()) {
					if (nave.getName().contains(naveAtual.getName())) {
						naves.add(nave);
					}
				}
				for (Vessel nave : naves) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								System.out.println(nave);
								new SuicideBurn(conexao, nave);
							} catch (StreamException | RPCException | IOException | InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}).start();
					System.out.println(nave.getName());
				}

			} catch (RPCException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if (fonte.equals(botSuicideBurn)) {
			System.out.println("Iniciando módulo de Suicide Burn");
			if (t_SuicideBurn == null) {
				t_SuicideBurn = new Thread(new Runnable() {
					@Override
					public void run() {
						System.out.println("Começou a Rodar");
						boolean SuicideBurnRodando = true;
						while (SuicideBurnRodando) {
							try {
								new SuicideBurn(conexao, naveAtual);
								SuicideBurnRodando = false;
								t_SuicideBurn = null;
								
							} catch (StreamException | RPCException | IOException | InterruptedException e) {
								e.printStackTrace();
								
							}
						}
					}
				});
				t_SuicideBurn.start();
				
			} else {
				System.out.println("Já Rodando");
			}
		}
		if (fonte.equals(botDecolagem)) {
			System.out.println("Iniciando módulo de Decolagem Orbital");
			if (t_DecolagemOrbital == null) {
				t_DecolagemOrbital = new Thread(new Runnable() {
					@Override
					public void run() {
						System.out.println("Começou a Rodar");
						try {
							new DecolagemOrbital(conexao, naveAtual);
						} catch (StreamException | RPCException | IOException | InterruptedException e) {
							e.printStackTrace();
						}
					}
				});
				t_DecolagemOrbital.start();
				
			} else {
				System.out.println("Já Rodando");
			}
		}
		if (fonte.equals(botVooAutonomo)) {
			System.out.println("Iniciando Piloto Automático de Aviões");
			try {
				novaJanelaDeVoo();
				VooAutonomo va = new VooAutonomo(conexao, naveAtual);
				va.addPropertyChangeListener(this);
				va.execute();
				
			} catch (RPCException | StreamException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
	}

	private void novaJanelaDeVoo() {
		JFrame jv = new JFrame();
		jv.setBounds(mp.getLocation().x + mp.getWidth(), mp.getLocation().y , 200, 200);
		labelAltitude = new JLabel("Altitude: ");
		labelAltitude.addPropertyChangeListener(this);
		jv.add(labelAltitude);
		jv.setVisible(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("altitude".equals(evt.getPropertyName())) {
			labelAltitude.setText("Altitude: AE" + String.valueOf(evt.getNewValue()));
			labelAltitude.updateUI();
		}
		
	}

}
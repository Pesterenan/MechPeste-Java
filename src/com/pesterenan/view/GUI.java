package com.pesterenan.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.pesterenan.controller.RoverAutonomoController;

public class GUI extends JFrame implements ActionListener, ChangeListener {
	private static final long serialVersionUID = 6999337104582004411L;

	// Bot�es dos m�dulos:
	private JButton botSuicideBurn, botDecolagem, botAutoRover, botManobras, botVooAutonomo, botIniciar, botCancelar,
			botVoltar, botMApoastro, botMPeriastro;
	private static JButton botConectar;
	private ButtonGroup grupoRBAutoRover = new ButtonGroup();
	private JRadioButton alvoRB = new JRadioButton("Alvo");
	private JRadioButton marcadorRB = new JRadioButton("Marcador", true);

	private ButtonGroup grupoRBDev = new ButtonGroup();
	private JRadioButton roverRB = new JRadioButton("Rover");
	private JRadioButton superficieRB = new JRadioButton("Superf�cie", true);

	// Barra de Status:
	protected static JLabel statusLabel;

	// Labels de par�metros:
	private static JLabel primeiraLinha, segundaLinha, terceiraLinha, quartaLinha, quintaLinha, sextaLinha;
	private JLabel iniciarFuncaoLabel;

	// Propriedades da GUI:
	private TitledBorder bordaTitulo = new TitledBorder("");
	private EmptyBorder bordaVazia = new EmptyBorder(5, 5, 5, 5);
	private Dimension tamanhoMenu = new Dimension(150, 240);

	// Pain�is da GUI:
	private JPanel pnlFuncoes, pnlStatus, pnlParametros, pnlIniciarFuncao, pnlConfigDecolagem, pnlConfigSuicideBurn,
			pnlConfigAutoRover, pnlManobras, pnlDev, painelMenu, painelPrincipal;

	// Strings de identifica��o de eventos:
	private final String funcoes = "Fun��es";
	private final String iniciarFuncao = "Iniciar Fun��o";
	public static final String parametros = "Par�metros", decolagemOrbital = "Decolagem Orbital",
			suicideBurn = "Suicide Burn", autoRover = "Auto Rover", manobras = "Manobras", vooAutonomo = "Voo Autonomo",
			conectar = "Conectar", iniciar = "Iniciar", cancelar = "Cancelar", voltar = "Voltar",
			marcadorOuAlvo = "Marcador ou Alvo", dev = "Dev", roverOuSuperficie = "Rover ou Superficie";

	// Entrada de Usu�rio
	// Decolagem Orbital:
	public static JTextField apoastroFinalTextField, direcaoOrbitaTextField;
	// Suicide Burn:
	public static JTextField altP, altI, altD, velP, velI, velD;
	// Auto Rover:
	public static JTextField nomeMarcadorTextField, velMaxTextField;

	private String executarModulo = "";

	private String circApoastro;

	private String circPeriastro;

	private JSlider sliderX = new JSlider(SwingConstants.HORIZONTAL, 0, 10, 1);

	public GUI() {
		super("MechPeste - Pesterenan");
		// super.setIconImage(image);
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(400, 240));
		setLocation(50, 50);

		pnlFuncoes = painelFuncoes();
		pnlIniciarFuncao = painelIniciarFuncao();
		pnlParametros = painelParametros();
		pnlConfigDecolagem = painelDecolagem();
		pnlConfigSuicideBurn = painelSuicide();
		pnlConfigAutoRover = painelAutoRover();
		pnlManobras = painelManobras();
		pnlStatus = painelStatus();
		pnlDev = painelDev();

		painelMenu = new JPanel();
		painelMenu.setLayout(new CardLayout());
		painelMenu.add(pnlFuncoes, funcoes);
		painelMenu.add(pnlIniciarFuncao, iniciarFuncao);

		painelPrincipal = new JPanel();
		painelPrincipal.setLayout(new CardLayout());
		painelPrincipal.add(pnlParametros, parametros);
		painelPrincipal.add(pnlConfigDecolagem, decolagemOrbital);
		painelPrincipal.add(pnlConfigSuicideBurn, suicideBurn);
		painelPrincipal.add(pnlConfigAutoRover, autoRover);
		painelPrincipal.add(pnlManobras, manobras);
		painelPrincipal.add(pnlDev, dev);

		add(painelMenu, BorderLayout.WEST);
		add(painelPrincipal, BorderLayout.CENTER);
		add(pnlStatus, BorderLayout.SOUTH);

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
	}

	private JPanel painelManobras() {
		pnlManobras = new JPanel();
		JLabel circularizar = new JLabel("Circularizar �rbita no:");
		botMApoastro = new JButton("Apoastro");
		botMApoastro.addActionListener(this);
		botMApoastro.setActionCommand(circApoastro);
		botMPeriastro = new JButton("Periastro");
		botMPeriastro.addActionListener(this);
		botMPeriastro.setActionCommand(circPeriastro);

		pnlManobras.setBorder(BorderFactory.createCompoundBorder(bordaVazia,
				BorderFactory.createTitledBorder("Par�metros da Miss�o:")));

		pnlManobras.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 1;
		gc.weighty = 1;
		gc.gridx = 0;
		gc.gridy = GridBagConstraints.RELATIVE;
		pnlManobras.add(circularizar, gc);
		JPanel botAP = new JPanel();
		gc.gridx = 0;
		botAP.add(botMApoastro, gc);
		gc.gridx = 0;
		botAP.add(botMPeriastro, gc);
		gc.gridx = 0;
		gc.gridy = GridBagConstraints.RELATIVE;

		pnlManobras.add(botAP, gc);

		pnlManobras.setVisible(true);
		return pnlManobras;
	}

	private JPanel painelDev() {
		pnlDev = new JPanel();
		JLabel pontoRef = new JLabel("Ponto de Refer�ncia");
		sliderX.addChangeListener(this);
		roverRB.addActionListener(this);
		roverRB.setActionCommand(roverOuSuperficie);
		superficieRB.addActionListener(this);
		superficieRB.setActionCommand(roverOuSuperficie);
		grupoRBDev.add(roverRB);
		grupoRBDev.add(superficieRB);
		JPanel grupoRBPanel = new JPanel();
		grupoRBPanel.setBorder(BorderFactory.createEtchedBorder());
		grupoRBPanel.add(roverRB);
		grupoRBPanel.add(superficieRB);

		pnlDev.setBorder(BorderFactory.createCompoundBorder(bordaVazia,
				BorderFactory.createTitledBorder("Par�metros da Miss�o:")));

		pnlDev.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 1;
		gc.weighty = 1;
		gc.gridx = 0;
		gc.gridy = GridBagConstraints.RELATIVE;

		gc.anchor = GridBagConstraints.LINE_START;
		pnlDev.add(pontoRef, gc);

		gc.anchor = GridBagConstraints.LINE_START;
		pnlDev.add(grupoRBPanel, gc);

		gc.anchor = GridBagConstraints.LINE_START;
		pnlDev.add(sliderX, gc);

		pnlDev.setVisible(true);
		return pnlDev;
	}

	public JPanel painelIniciarFuncao() {
		pnlIniciarFuncao = new JPanel();
		bordaTitulo.setTitle("Iniciar");
		iniciarFuncaoLabel = new JLabel("");
		pnlIniciarFuncao.setBorder(BorderFactory.createCompoundBorder(bordaVazia, bordaTitulo));
		pnlIniciarFuncao.setMinimumSize(tamanhoMenu);
		pnlIniciarFuncao.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 1;
		gc.weighty = 1;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridx = 0;
		gc.gridy = GridBagConstraints.RELATIVE;
		pnlIniciarFuncao.add(iniciarFuncaoLabel, gc);

		gc.weighty = 0.1;
		gc.anchor = GridBagConstraints.LINE_END;
		botIniciar = new JButton(iniciar);
		botIniciar.addActionListener(this);
		botIniciar.setActionCommand(iniciar);
		botCancelar = new JButton(cancelar);
		botCancelar.setVisible(false);
		botCancelar.addActionListener(this);
		botCancelar.setActionCommand(cancelar);

		gc.anchor = GridBagConstraints.LINE_END;
		botVoltar = new JButton(voltar);
		botVoltar.addActionListener(this);
		botVoltar.setActionCommand(voltar);

		pnlIniciarFuncao.add(botIniciar, gc);
		pnlIniciarFuncao.add(botCancelar, gc);
		pnlIniciarFuncao.add(botVoltar, gc);
		pnlIniciarFuncao.setVisible(true);
		return pnlIniciarFuncao;
	}

	private JPanel painelFuncoes() {
		pnlFuncoes = new JPanel();
		// Criar Bot�es:
		botDecolagem = new JButton("Decolagem Orbital");
		botDecolagem.setMnemonic(KeyEvent.VK_D);
		botSuicideBurn = new JButton("Suicide Burn");
		botSuicideBurn.setMnemonic(KeyEvent.VK_S);
		botAutoRover = new JButton("Rover Aut�nomo");
		botAutoRover.setMnemonic(KeyEvent.VK_R);
		botManobras = new JButton("Manobras");
		botManobras.setMnemonic(KeyEvent.VK_M);
		botVooAutonomo = new JButton("Voo Aut�nomo");
		botVooAutonomo.setMnemonic(KeyEvent.VK_V);
		bordaTitulo.setTitle("Fun��es");
		pnlFuncoes.setBorder(BorderFactory.createCompoundBorder(bordaVazia, bordaTitulo));

		pnlFuncoes.setLayout(new GridBagLayout());

		pnlFuncoes.setMinimumSize(tamanhoMenu);
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 0.1;
		gc.weighty = 0.01;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridx = 0;
		gc.gridy = GridBagConstraints.RELATIVE;

		gc.anchor = GridBagConstraints.LINE_START;
		pnlFuncoes.add(botDecolagem, gc);
		botDecolagem.addActionListener(this);
		botDecolagem.setActionCommand(decolagemOrbital);

		gc.anchor = GridBagConstraints.LINE_START;
		pnlFuncoes.add(botSuicideBurn, gc);
		botSuicideBurn.addActionListener(this);
		botSuicideBurn.setActionCommand(suicideBurn);

		gc.anchor = GridBagConstraints.LINE_START;
		pnlFuncoes.add(botAutoRover, gc);
		botAutoRover.addActionListener(this);
		botAutoRover.setActionCommand(autoRover);

		gc.anchor = GridBagConstraints.LINE_START;
		pnlFuncoes.add(botManobras, gc);
		botManobras.addActionListener(this);
		botManobras.setActionCommand(manobras);

		gc.anchor = GridBagConstraints.LINE_START;
		pnlFuncoes.add(botVooAutonomo, gc);
		botVooAutonomo.addActionListener(this);
		botVooAutonomo.setActionCommand(dev);

		pnlFuncoes.setVisible(true);
		return pnlFuncoes;
	}

	private JPanel painelParametros() {
		pnlParametros = new JPanel();
		primeiraLinha = new JLabel("Nome: ");
		segundaLinha = new JLabel("Altitude: ");
		terceiraLinha = new JLabel("Apoastro: ");
		quartaLinha = new JLabel("Periastro: ");
		quintaLinha = new JLabel("Vel. Vertical: ");
		sextaLinha = new JLabel("Vel. Horizontal:");
		pnlParametros.setBorder(BorderFactory.createCompoundBorder(bordaVazia,
				BorderFactory.createTitledBorder("Par�metros da Miss�o:")));

		pnlParametros.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 1;
		gc.weighty = 1;
		gc.gridx = 0;
		gc.gridy = GridBagConstraints.RELATIVE;

		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(primeiraLinha, gc);

		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(segundaLinha, gc);

		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(terceiraLinha, gc);

		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(quartaLinha, gc);

		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(quintaLinha, gc);

		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(sextaLinha, gc);

		pnlParametros.setVisible(true);
		return pnlParametros;
	}

	private JPanel painelDecolagem() {
		pnlConfigDecolagem = new JPanel();
		JLabel apoastroFinalLabel = new JLabel("Altitude do Apoastro Alvo: ");
		apoastroFinalTextField = new JTextField("80000", 5);
		JLabel direcaoDeOrbitaLabel = new JLabel("Dire��o de Inclina��o de �rbita: ");
		direcaoOrbitaTextField = new JTextField("90", 5);
		JLabel dicasDirecao = new JLabel("0: Norte, 90: Leste, 180: Sul, 270: Oeste");

		pnlConfigDecolagem.setBorder(
				BorderFactory.createCompoundBorder(bordaVazia, BorderFactory.createTitledBorder("Configura��es:")));

		pnlConfigDecolagem.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 1;
		gc.weighty = 1;
		gc.gridx = 0;
		gc.gridy = 0;
		pnlConfigDecolagem.add(new JLabel(), gc);

		gc.weighty = 0;
		gc.gridy++;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlConfigDecolagem.add(apoastroFinalLabel, gc);

		gc.gridy++;
		gc.fill = GridBagConstraints.HORIZONTAL;
		pnlConfigDecolagem.add(apoastroFinalTextField, gc);

		gc.weighty = 1;
		gc.gridy++;
		pnlConfigDecolagem.add(new JLabel(), gc);

		gc.weighty = 0;
		gc.gridy++;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlConfigDecolagem.add(direcaoDeOrbitaLabel, gc);

		gc.gridy++;
		gc.fill = GridBagConstraints.HORIZONTAL;
		pnlConfigDecolagem.add(direcaoOrbitaTextField, gc);

		gc.weighty = 1;
		gc.gridy++;
		pnlConfigDecolagem.add(new JLabel(), gc);

		gc.weighty = 0;
		gc.gridy++;
		gc.fill = GridBagConstraints.LINE_START;
		gc.gridheight = GridBagConstraints.REMAINDER;
		pnlConfigDecolagem.add(dicasDirecao, gc);

		pnlConfigDecolagem.setVisible(true);
		return pnlConfigDecolagem;
	}

	private JPanel painelSuicide() {
		pnlConfigSuicideBurn = new JPanel();
		altP = new JTextField("0.01");
		altI = new JTextField("0.01");
		altD = new JTextField("0.01");
		velP = new JTextField("0.025");
		velI = new JTextField("0.05");
		velD = new JTextField("0.05");

		pnlConfigSuicideBurn.setBorder(
				BorderFactory.createCompoundBorder(bordaVazia, BorderFactory.createTitledBorder("Configura��es:")));

		pnlConfigSuicideBurn.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 1;
		gc.weighty = 1;
		gc.gridx = 0;
		gc.gridy = 0;
		pnlConfigSuicideBurn.add(new JLabel(), gc);

		gc.weighty = 0;
		gc.gridy++;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlConfigSuicideBurn.add(new JLabel("Ajuste do PID de Altitude:"), gc);
		JPanel painelAltPID = new JPanel();
		gc.fill = GridBagConstraints.HORIZONTAL;

		gc.gridy++;
		gc.gridx = 0;
		painelAltPID.add(altP, gc);
		gc.gridx = 1;
		painelAltPID.add(altI, gc);
		gc.gridx = 2;
		painelAltPID.add(altD, gc);

		gc.weightx = 1;
		gc.weighty = 1;
		gc.gridx = 0;
		gc.gridy++;
		pnlConfigSuicideBurn.add(painelAltPID, gc);
		pnlConfigSuicideBurn.add(new JLabel(), gc);

		gc.gridy++;
		gc.weightx = 1;
		gc.weighty = 0;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlConfigSuicideBurn.add(new JLabel("Ajuste do PID de Velocidade:"), gc);
		JPanel painelVelPID = new JPanel();
		gc.fill = GridBagConstraints.HORIZONTAL;

		gc.gridy++;
		gc.gridx = 0;
		painelVelPID.add(velP, gc);
		gc.gridx = 1;
		painelVelPID.add(velI, gc);
		gc.gridx = 2;
		painelVelPID.add(velD, gc);
		gc.gridy++;
		gc.gridx = 0;
		pnlConfigSuicideBurn.add(painelVelPID, gc);
		gc.weighty = 1;
		gc.gridy++;
		pnlConfigSuicideBurn.add(new JLabel(), gc);

		gc.weighty = 1;
		gc.gridx = 0;
		gc.gridy++;
		pnlConfigSuicideBurn.add(new JLabel(), gc);

		pnlConfigSuicideBurn.setVisible(true);
		return pnlConfigSuicideBurn;
	}

	private JPanel painelAutoRover() {
		pnlConfigAutoRover = new JPanel();
		JLabel nomeAlvoLabel = new JLabel("Nome do Marcador: ");
		nomeMarcadorTextField = new JTextField("ALVO", 10);
		JLabel velMaxLabel = new JLabel("Velocidade M�xima: ");
		velMaxTextField = new JTextField("10", 5);
		// Painel com bot�es de r�dio para o alvo:
		JLabel alvoLabel = new JLabel("Dirigir at� o: ");

		marcadorRB.addActionListener(this);
		marcadorRB.setActionCommand(marcadorOuAlvo);
		alvoRB.addActionListener(this);
		alvoRB.setActionCommand(marcadorOuAlvo);
		grupoRBAutoRover.add(alvoRB);
		grupoRBAutoRover.add(marcadorRB);
		JPanel grupoRBPanel = new JPanel();
		grupoRBPanel.setBorder(BorderFactory.createEtchedBorder());
		grupoRBPanel.add(marcadorRB);
		grupoRBPanel.add(alvoRB);

		pnlConfigAutoRover.setBorder(
				BorderFactory.createCompoundBorder(bordaVazia, BorderFactory.createTitledBorder("Configura��es:")));

		pnlConfigAutoRover.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 1;
		gc.weighty = 1;
		gc.gridx = 0;
		gc.gridy = 0;
		pnlConfigAutoRover.add(new JLabel(), gc);

		gc.weighty = 0;
		gc.gridy++;
		gc.anchor = GridBagConstraints.CENTER;
		pnlConfigAutoRover.add(alvoLabel, gc);

		gc.weighty = 0;
		gc.gridy++;
		gc.anchor = GridBagConstraints.CENTER;
		pnlConfigAutoRover.add(grupoRBPanel, gc);

		gc.weighty = 1;
		gc.gridy++;
		pnlConfigAutoRover.add(new JLabel(), gc);

		gc.weighty = 0;
		gc.gridy++;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlConfigAutoRover.add(nomeAlvoLabel, gc);

		gc.gridy++;
		gc.fill = GridBagConstraints.HORIZONTAL;
		pnlConfigAutoRover.add(nomeMarcadorTextField, gc);

		gc.weighty = 1;
		gc.gridy++;
		pnlConfigAutoRover.add(new JLabel(), gc);

		gc.weighty = 0;
		gc.gridy++;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlConfigAutoRover.add(velMaxLabel, gc);

		gc.gridy++;
		gc.fill = GridBagConstraints.HORIZONTAL;
		pnlConfigAutoRover.add(velMaxTextField, gc);

		gc.weighty = 1;
		gc.gridy++;
		pnlConfigAutoRover.add(new JLabel(), gc);

		pnlConfigAutoRover.setVisible(true);
		return pnlConfigAutoRover;
	}

	private JPanel painelStatus() {
		pnlStatus = new JPanel();
		return pnlStatus;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(sliderX)) {
			System.out.println(sliderX.getValue() * 0.1);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Mudar paineis e validar dados de acordo com os cliques nos menus:
		switch (e.getActionCommand()) {
		case conectar:
			firePropertyChange(conectar, 0, 1);
			break;
		case decolagemOrbital:
			iniciarFuncao(decolagemOrbital);
			break;
		case suicideBurn:
			iniciarFuncao(suicideBurn);
			break;
		case autoRover:
			iniciarFuncao(autoRover);
			break;
		case manobras:
			iniciarFuncao(manobras);
			break;
		case dev:
			iniciarFuncao(dev);
			break;
		case voltar:
			CardLayout pp = (CardLayout) (painelPrincipal.getLayout());
			pp.show(painelPrincipal, parametros);
			CardLayout pm = (CardLayout) (painelMenu.getLayout());
			pm.show(painelMenu, funcoes);
			bordaTitulo.setTitle("Fun��es");
			break;
		case marcadorOuAlvo:
			if (marcadorRB.isSelected()) {
				System.out.println("MARCADOR");
				nomeMarcadorTextField.setEnabled(true);
			} else {
				System.out.println("ALVO");
				nomeMarcadorTextField.setEnabled(false);
			}
			break;

		case roverOuSuperficie:
			if (roverRB.isSelected()) {

			} else {

			}
			break;
		}

		if (e.getSource().equals(botIniciar)) {
			System.out.println("Clicou em Iniciar");
			if (validarDados(executarModulo)) {
				CardLayout pp = (CardLayout) (painelPrincipal.getLayout());
				firePropertyChange(executarModulo, 0, 1);
				pp.show(painelPrincipal, parametros);
				botCancelar.setVisible(true);
				botIniciar.setVisible(false);

			}
		}
		if (e.getSource().equals(botCancelar)) {
			System.out.println("Clicou em Cancelar");
			botCancelar.setVisible(false);
			botIniciar.setVisible(true);
//			switch (executarModulo) {
//			case decolagemOrbital:
//				DecolagemOrbitalController.setAbortar(true);
//			case suicideBurn:
//				SuicideBurn.setAbortar(true);
//			}
			CardLayout pp = (CardLayout) (painelPrincipal.getLayout());
			pp.show(painelPrincipal, executarModulo);
		}

	}

	private void iniciarFuncao(String modulo) {
		executarModulo = modulo;
		CardLayout pm = (CardLayout) (painelMenu.getLayout());
		CardLayout pp = (CardLayout) (painelPrincipal.getLayout());
		bordaTitulo.setTitle("Iniciar");
		iniciarFuncaoLabel.setText(modulo);
		iniciarFuncaoLabel.setHorizontalAlignment(MAXIMIZED_HORIZ);
		iniciarFuncaoLabel.setVisible(true);
		pm.show(painelMenu, iniciarFuncao);
		pp.show(painelPrincipal, modulo);
	}

	private boolean validarDados(String modulo) {
		switch (modulo) {
		case decolagemOrbital:
			float apoastro;
			int direcao;
			try {
				apoastro = Float.parseFloat(apoastroFinalTextField.getText());
				direcao = Integer.parseInt(direcaoOrbitaTextField.getText());
			} catch (NumberFormatException erro) {
				GUI.setStatus("Os campos s� aceitam n�meros inteiros.");
				return false;
			}
//			if (apoastro >= 1000) {
//				DecolagemOrbitalController.setAltApoastro(apoastro);
//			} else {
//				GUI.setStatus("O apoastro tem que ser maior ou igual a 1000 metros.");
//				return false;
//			}
//			if (direcao >= 0 && direcao < 360) {
//				DecolagemOrbitalController.setDirecao(direcao);
//			} else {
//				GUI.setStatus("A direcao tem que ser um n�mero entre 0 e 359 graus.");
//				return false;
//			}
			return true;
		case suicideBurn:
			try {
				double altPd = Double.parseDouble(altP.getText());
				double altId = Double.parseDouble(altI.getText());
				double altDd = Double.parseDouble(altD.getText());
				double velPd = Double.parseDouble(velP.getText());
				double velId = Double.parseDouble(velI.getText());
				double velDd = Double.parseDouble(velD.getText());
			} catch (NullPointerException | NumberFormatException npe) {
				GUI.setStatus("Valores incorretos para o PID.");
				return false;
			}
			return true;
		case autoRover:
			String nomeMarcador = nomeMarcadorTextField.getText();
			float velMaxima = 10;
			if ((nomeMarcadorTextField.isEnabled()) && (nomeMarcador != null)) {
				RoverAutonomoController.buscandoMarcadores = true;
				RoverAutonomoController.setAlvo(nomeMarcador);
			} else {
				RoverAutonomoController.buscandoMarcadores = false;
			}
			try {
				velMaxima = Float.parseFloat(velMaxTextField.getText());
				RoverAutonomoController.setVelMaxima(velMaxima);
			} catch (NullPointerException | NumberFormatException npe) {
				GUI.setStatus("Valor inv�lido para velocidade. Utilizando 10m/s.");
				RoverAutonomoController.setVelMaxima(10);
			}
			return true;
		case manobras:
			try {

			} catch (NullPointerException | NumberFormatException npe) {
				GUI.setStatus("Erro");
				return false;
			}
			return true;
		case vooAutonomo: {
			return true;
		}
		case dev: {
			return true;
		}
		}

		return false;
	}

	public static void setStatus(String texto) {
		statusLabel.setText(texto);
	}

	public static void setParametros(String par, double val) {
		String valor = String.format("%1$.1f", val);
		switch (par) {
		case "altitude":
			segundaLinha.setText("Altitude: " + valor + "m");
			break;
		case "carga":
			segundaLinha.setText("Carga El�trica: " + String.format("%1$.0f", val) + "%");
			break;
		case "distanciaDaQueima":
			terceiraLinha.setText("Dist�ncia da Queima: " + valor + "m");
			break;
		case "apoastro":
			terceiraLinha.setText("Apoastro: " + valor + "m");
			break;
		case "distPercorrida":
			terceiraLinha.setText("Dist�ncia Percorrida: " + valor + "m");
			break;
		case "distancia":
			quartaLinha.setText("Dist�ncia Restante: " + valor + "m");
			break;
		case "periastro":
			quartaLinha.setText("Periastro: " + valor + "m");
			break;
		case "valorTEP":
			quartaLinha.setText("Valor TEP: " + valor);
			break;
		case "tempoDeMissao":
			int segTotaisTdm = (int) val;
			int horasTdm = segTotaisTdm / 3600;
			int minutosTdm = (segTotaisTdm % 3600) / 60;
			int segundosTdm = segTotaisTdm % 60;
			quintaLinha
					.setText("Tempo de Miss�o: " + String.format("%02d:%02d:%02d", horasTdm, minutosTdm, segundosTdm));
			break;
		case "velVert":
			quintaLinha.setText("Vel Vert.: " + valor + "m/s");
			break;
		case "velHorz":
			sextaLinha.setText("Vel Horz.: " + valor + "m/s");
			break;
		case "tempoRestante":
			int segTotaisTr = (int) val;
			int horasTr = segTotaisTr / 3600;
			int minutosTr = (segTotaisTr % 3600) / 60;
			int segundosTr = segTotaisTr % 60;
			sextaLinha.setText("Tempo Restante: " + String.format("%02d:%02d:%02d", horasTr, minutosTr, segundosTr));
			break;

		}
	}

	public static void setParametros(String par, String val) {
		switch (par) {
		case "nome":
			primeiraLinha.setText("Alvo: " + val);
			break;
		}
	}

	public static void botConectarVisivel(boolean visivel) {
		botConectar.setVisible(visivel);
	}

}

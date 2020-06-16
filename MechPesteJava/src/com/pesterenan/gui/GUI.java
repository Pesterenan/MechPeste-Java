package com.pesterenan.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.pesterenan.MechPeste;
import com.pesterenan.funcoes.AutoRover;
import com.pesterenan.funcoes.DecolagemOrbital;
import com.pesterenan.funcoes.SuicideBurn;
import com.pesterenan.utils.Navegacao;

public class GUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 6999337104582004411L;

	// Botões dos módulos:
	private JButton botSuicideBurn;
	private JButton botDecolagem;
	private JButton botAutoRover;
	private JButton botManobras;
	private JButton botVooAutonomo;
	private JButton botIniciarCancelar;
	private JButton botVoltar;
	private static JButton botConectar;
	private ButtonGroup grupoRB = new ButtonGroup();
	private JRadioButton alvoRB = new JRadioButton("Alvo");
	private JRadioButton marcadorRB = new JRadioButton("Marcador", true);

	// Barra de Status:
	protected static JLabel statusLabel;

	// Labels de parâmetros:
	private static JLabel nomeLabel;
	private static JLabel altitudeLabel;
	private static JLabel apoastroLabel;
	private static JLabel periastroLabel;
	private static JLabel velVertLabel;
	private static JLabel velHorzLabel;
	private JLabel iniciarFuncaoLabel;

	// Propriedades da GUI:
	private TitledBorder bordaTitulo = new TitledBorder("");
	private EmptyBorder bordaVazia = new EmptyBorder(5, 5, 5, 5);
	private Dimension tamanhoMenu = new Dimension(150, 240);

	// Painéis da GUI:
	private JPanel pnlFuncoes;
	private JPanel pnlStatus;
	private JPanel pnlParametros;
	private JPanel pnlIniciarFuncao;
	private JPanel pnlConfigDecolagem;
	private JPanel pnlConfigSuicideBurn;
	private JPanel pnlConfigAutoRover;
	private JPanel painelMenu;
	private JPanel painelPrincipal;

	// Strings de identificação de eventos:
	private final String funcoes = "Funções";
	private final String iniciarFuncao = "Iniciar Função";
	public static final String parametros = "Parâmetros";
	public static final String decolagemOrbital = "Decolagem Orbital";
	public static final String suicideBurn = "Suicide Burn";
	public static final String autoRover = "Auto Rover";
	public static final String manobras = "Manobras";
	public static final String conectar = "Conectar";
	public static final String iniciar = "Iniciar";
	public static final String voltar = "Voltar";
	public static final String marcadorOuAlvo = "Marcador ou Alvo";

	// Entrada de Usuário
	// Decolagem Orbital:
	private JTextField apoastroFinalTextField;
	private JTextField direcaoOrbitaTextField;
	// Suicide Burn:
	private JTextField ajusteDePousoTextField;
	private JTextField altitudeSCRTextField;
	private JCheckBox checkPouso;
	// Auto Rover:
	private JTextField nomeMarcadorTextField;
	private JTextField velMaxTextField;
	private String executarModulo = "";

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
		pnlStatus = painelStatus();

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

		add(painelMenu, BorderLayout.WEST);
		add(painelPrincipal, BorderLayout.CENTER);
		add(pnlStatus, BorderLayout.SOUTH);

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
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
		botIniciarCancelar = new JButton(iniciar);
		botIniciarCancelar.addActionListener(this);
		botIniciarCancelar.setActionCommand(iniciar);

		gc.anchor = GridBagConstraints.LINE_END;
		botVoltar = new JButton(voltar);
		botVoltar.addActionListener(this);
		botVoltar.setActionCommand(voltar);

		pnlIniciarFuncao.add(botIniciarCancelar, gc);
		pnlIniciarFuncao.add(botVoltar, gc);
		pnlIniciarFuncao.setVisible(true);
		return pnlIniciarFuncao;
	}

	private JPanel painelFuncoes() {
		pnlFuncoes = new JPanel();
		// Criar Botões:
		botDecolagem = new JButton("Decolagem Orbital");
		botDecolagem.setMnemonic(KeyEvent.VK_D);
		botSuicideBurn = new JButton("Suicide Burn");
		botSuicideBurn.setMnemonic(KeyEvent.VK_S);
		botAutoRover = new JButton("Rover Autônomo");
		botAutoRover.setMnemonic(KeyEvent.VK_R);
		botManobras = new JButton("Manobras");
		botManobras.setMnemonic(KeyEvent.VK_M);
		botVooAutonomo = new JButton("Voo Autônomo");
		botVooAutonomo.setMnemonic(KeyEvent.VK_V);
		bordaTitulo.setTitle("Funções");
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

		pnlFuncoes.setVisible(true);
		return pnlFuncoes;
	}

	private JPanel painelParametros() {
		pnlParametros = new JPanel();
		nomeLabel = new JLabel();
		altitudeLabel = new JLabel();
		apoastroLabel = new JLabel();
		periastroLabel = new JLabel();
		velVertLabel = new JLabel();
		velHorzLabel = new JLabel();
		pnlParametros.setBorder(
				BorderFactory.createCompoundBorder(bordaVazia, BorderFactory.createTitledBorder("Parâmetros de Voo:")));

		pnlParametros.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 1;
		gc.weighty = 1;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(new JLabel("Nome: "), gc);

		gc.gridx = 1;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(nomeLabel, gc);

		gc.gridx = 0;
		gc.gridy++;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(new JLabel("Altitude: "), gc);

		gc.gridx = 1;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(altitudeLabel, gc);

		gc.gridx = 0;
		gc.gridy++;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(new JLabel("Apoastro: "), gc);

		gc.gridx = 1;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(apoastroLabel, gc);

		gc.gridx = 0;
		gc.gridy++;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(new JLabel("Periastro: "), gc);

		gc.gridx = 1;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(periastroLabel, gc);

		gc.gridx = 0;
		gc.gridy++;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(new JLabel("Vel. Vert.: "), gc);

		gc.gridx = 1;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(velVertLabel, gc);

		gc.gridx = 0;
		gc.gridy++;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(new JLabel("Vel. Horz.: "), gc);

		gc.gridx = 1;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlParametros.add(velHorzLabel, gc);

		pnlParametros.setVisible(true);
		return pnlParametros;
	}

	private JPanel painelDecolagem() {
		pnlConfigDecolagem = new JPanel();
		JLabel apoastroFinalLabel = new JLabel("Altitude do Apoastro Alvo: ");
		apoastroFinalTextField = new JTextField("80000", 5);
		JLabel direcaoDeOrbitaLabel = new JLabel("Direção de Inclinação de Órbita: ");
		direcaoOrbitaTextField = new JTextField("90", 5);

		pnlConfigDecolagem.setBorder(
				BorderFactory.createCompoundBorder(bordaVazia, BorderFactory.createTitledBorder("Configurações:")));

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

		pnlConfigDecolagem.setVisible(true);
		return pnlConfigDecolagem;
	}

	private JPanel painelSuicide() {
		pnlConfigSuicideBurn = new JPanel();
		ajusteDePousoTextField = new JTextField("1.2");
		altitudeSCRTextField = new JTextField("3000");
		checkPouso = new JCheckBox();
		checkPouso.setSelected(true);
		checkPouso.addActionListener(this);

		pnlConfigSuicideBurn.setBorder(
				BorderFactory.createCompoundBorder(bordaVazia, BorderFactory.createTitledBorder("Configurações:")));

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
		pnlConfigSuicideBurn.add(new JLabel("Ajuste de Pouso: "), gc);

		gc.gridy++;
		gc.fill = GridBagConstraints.HORIZONTAL;
		pnlConfigSuicideBurn.add(ajusteDePousoTextField, gc);

		gc.weighty = 1;
		gc.gridy++;
		pnlConfigSuicideBurn.add(new JLabel(), gc);

		gc.gridy++;
		gc.weighty = 0;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlConfigSuicideBurn.add(new JLabel("Altitude para usar SCR: "), gc);

		gc.gridy++;
		pnlConfigSuicideBurn.add(altitudeSCRTextField, gc);

		gc.weighty = 1;
		gc.gridy++;
		pnlConfigSuicideBurn.add(new JLabel(), gc);

		gc.gridy++;
		gc.weighty = 0;
		JPanel pouso = new JPanel();
		pouso.add(new JLabel("Pousar depois do Burn? "), gc);
		pouso.add(checkPouso);
		pnlConfigSuicideBurn.add(pouso, gc);

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
		JLabel velMaxLabel = new JLabel("Velocidade Máxima: ");
		velMaxTextField = new JTextField("10", 5);
		// Painel com botões de rádio para o alvo:
		JLabel alvoLabel = new JLabel("Dirigir até o: ");

		marcadorRB.addActionListener(this);
		marcadorRB.setActionCommand(marcadorOuAlvo);
		alvoRB.addActionListener(this);
		alvoRB.setActionCommand(marcadorOuAlvo);
		grupoRB.add(alvoRB);
		grupoRB.add(marcadorRB);
		JPanel grupoRBPanel = new JPanel();
		grupoRBPanel.setBorder(BorderFactory.createEtchedBorder());
		grupoRBPanel.add(marcadorRB);
		grupoRBPanel.add(alvoRB);

		pnlConfigAutoRover.setBorder(
				BorderFactory.createCompoundBorder(bordaVazia, BorderFactory.createTitledBorder("Configurações:")));

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
		statusLabel = new JLabel(" ");
		botConectar = new JButton("Conectar");
		botConectar.setPreferredSize(new Dimension(90, 18));
		botConectar.setVisible(false);
		botConectar.addActionListener(this);
		botConectar.setActionCommand(conectar);
		pnlStatus.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2),
				BorderFactory.createBevelBorder(BevelBorder.LOWERED)));

		pnlStatus.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 1;
		gc.weighty = 0;
		gc.insets = new Insets(1, 1, 1, 1);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.LINE_START;
		pnlStatus.add(statusLabel, gc);

		gc.weightx = 0.2;
		gc.weighty = 0;
		gc.gridx = 1;
		gc.insets = new Insets(0, 1, 0, 1);
		gc.anchor = GridBagConstraints.LINE_END;

		pnlStatus.add(botConectar, gc);

		return pnlStatus;
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
		case voltar:
			CardLayout pp = (CardLayout) (painelPrincipal.getLayout());
			pp.show(painelPrincipal, parametros);
			CardLayout pm = (CardLayout) (painelMenu.getLayout());
			pm.show(painelMenu, funcoes);
			bordaTitulo.setTitle("Funções");
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
		}

		if (e.getSource().equals(botIniciarCancelar) && botIniciarCancelar.getText().equals("Iniciar")) {
			if (validarDados(executarModulo)) {
				CardLayout pp = (CardLayout) (painelPrincipal.getLayout());
				firePropertyChange(executarModulo, 0, 1);
				pp.show(painelPrincipal, parametros);
				botIniciarCancelar.setText("Cancelar");
			}
		} else {
			if (MechPeste.threadModulos != null) {
				try {
					MechPeste.finalizarTarefa();
					MechPeste.iniciarConexao();
				} catch (IOException e1) {
					botConectarVisivel(true);
				}
			}
			CardLayout pp = (CardLayout) (painelPrincipal.getLayout());
			pp.show(painelPrincipal, executarModulo);
			botIniciarCancelar.setText("Iniciar");
		}

		if (e.getSource().equals(checkPouso)) {
			SuicideBurn.podePousar = checkPouso.isSelected();
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
				GUI.setStatus("Os campos só aceitam números inteiros.");
				return false;
			}
			if (apoastro >= 72000) {
				DecolagemOrbital.setAltApoastro(apoastro);
			} else {
				GUI.setStatus("O apoastro tem que ser maior ou igual a 72000 metros.");
				return false;
			}
			if (direcao >= 0 && direcao < 360) {
				DecolagemOrbital.setDirecao(direcao);
			} else {
				GUI.setStatus("A direcao tem que ser um número entre 0 e 359 graus.");
				return false;
			}
			return true;
		case suicideBurn:
			float ajuste = 1.0f;
			int altitude = 3000;
			try {
				ajuste = Float.parseFloat(ajusteDePousoTextField.getText());
				Navegacao.setAjuste(ajuste);
				altitude = Integer.parseInt(altitudeSCRTextField.getText());
				SuicideBurn.setAltSCR(altitude);
			} catch (NullPointerException | NumberFormatException npe) {
				GUI.setStatus("Valor incorreto para ajuste. Recomendado 1.25");
				return false;
			}
			return true;
		case autoRover:
			String nomeMarcador = nomeMarcadorTextField.getText().trim().toUpperCase();
			if ((nomeMarcadorTextField.isEnabled()) && (nomeMarcador != null)) {
				AutoRover.alvoOuMarcador = true;
				AutoRover.setAlvo(nomeMarcador);
			} else {
				AutoRover.alvoOuMarcador = false;
			}
			try {
				AutoRover.setVelMaxima(Float.parseFloat(velMaxTextField.getText()));
			} catch (NullPointerException | NumberFormatException npe) {
				GUI.setStatus("Valor inválido para velocidade. Utilizando 10m/s.");
				AutoRover.setVelMaxima(10);
			}
			return true;
		case manobras:
			try {

			} catch (NullPointerException | NumberFormatException npe) {
				GUI.setStatus("Érro");
				return false;
			}
			return true;
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
			altitudeLabel.setText(valor + "m");
			break;
		case "apoastro":
			apoastroLabel.setText(valor + "m");
			break;
		case "periastro":
			periastroLabel.setText(valor + "m");
			break;
		case "velVert":
			velVertLabel.setText(valor + "m/s");
			break;
		case "velHorz":
			velHorzLabel.setText(valor + "m/s");
			break;

		}
	}

	public static void setParametros(String par, String val) {
		switch (par) {
		case "nome":
			nomeLabel.setText(val);
			break;
		}
	}

	public static void botConectarVisivel(boolean visivel) {
		botConectar.setVisible(visivel);
	}

}

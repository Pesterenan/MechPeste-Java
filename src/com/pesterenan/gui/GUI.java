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

public class GUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 6999337104582004411L;

	// Botões dos módulos:
	private JButton botSuicideBurn, botDecolagem, botAutoRover, botManobras, botVooAutonomo, botIniciarCancelar,
			botVoltar;
	private static JButton botConectar;
	private ButtonGroup grupoRB = new ButtonGroup();
	private JRadioButton alvoRB = new JRadioButton("Alvo");
	private JRadioButton marcadorRB = new JRadioButton("Marcador", true);

	// Barra de Status:
	protected static JLabel statusLabel;

	// Labels de parâmetros:
	private static JLabel primeiraLinha, segundaLinha, terceiraLinha, quartaLinha, quintaLinha, sextaLinha;
	private JLabel iniciarFuncaoLabel;

	// Propriedades da GUI:
	private TitledBorder bordaTitulo = new TitledBorder("");
	private EmptyBorder bordaVazia = new EmptyBorder(5, 5, 5, 5);
	private Dimension tamanhoMenu = new Dimension(150, 240);

	// Painéis da GUI:
	private JPanel pnlFuncoes, pnlStatus, pnlParametros, pnlIniciarFuncao, pnlConfigDecolagem, pnlConfigSuicideBurn,
			pnlConfigAutoRover, painelMenu, painelPrincipal;

	// Strings de identificação de eventos:
	private final String funcoes = "Funções";
	private final String iniciarFuncao = "Iniciar Função";
	public static final String parametros = "Parâmetros", decolagemOrbital = "Decolagem Orbital",
			suicideBurn = "Suicide Burn", autoRover = "Auto Rover", manobras = "Manobras", vooAutonomo = "Voo Autonomo",
			conectar = "Conectar", iniciar = "Iniciar", voltar = "Voltar", marcadorOuAlvo = "Marcador ou Alvo";

	// Entrada de Usuário
	// Decolagem Orbital:
	public static JTextField apoastroFinalTextField, direcaoOrbitaTextField;
	// Suicide Burn:
	public static JTextField altP, altI, altD, velP, velI, velD;
	// Auto Rover:
	public static JTextField nomeMarcadorTextField, velMaxTextField;

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
		primeiraLinha = new JLabel("Nome: ");
		segundaLinha = new JLabel("Altitude: ");
		terceiraLinha = new JLabel("Apoastro: ");
		quartaLinha = new JLabel("Periastro: ");
		quintaLinha = new JLabel("Vel. Vertical: ");
		sextaLinha = new JLabel("Vel. Horizontal:");
		pnlParametros.setBorder(BorderFactory.createCompoundBorder(bordaVazia,
				BorderFactory.createTitledBorder("Parâmetros da Missão:")));

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
		altP = new JTextField("0.025");
		altI = new JTextField("0.05");
		altD = new JTextField("0.05");
		velP = new JTextField("0.025");
		velI = new JTextField("0.05");
		velD = new JTextField("0.05");

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
		case vooAutonomo:

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
			if (apoastro >= 10000) {
				DecolagemOrbital.setAltApoastro(apoastro);
			} else {
				GUI.setStatus("O apoastro tem que ser maior ou igual a 10000 metros.");
				return false;
			}
			if (direcao >= 0 && direcao < 360) {
				DecolagemOrbital.setDirecao(direcao);
			} else {
				GUI.setStatus("A direcao tem que ser um número entre 0 e 359 graus.");
				return false;
			}
			try {
				String[] dados = { Arquivos.DO, String.valueOf(apoastro), String.valueOf(direcao) };
				Arquivos.gravarDadosConfig(dados);
			} catch (IOException e1) {
				System.out.println("Erro ao gravar dados da Decolagem Orbital");
			}
			return true;
		case suicideBurn:
			try {
				double altPd = Double.parseDouble(altP.getText());
				double altId = Double.parseDouble(altI.getText());
				double altDd = Double.parseDouble(altD.getText());
				double velPd = Double.parseDouble(velP.getText());
				double velId = Double.parseDouble(velI.getText());
				double velDd = Double.parseDouble(velD.getText());
				SuicideBurn.setAjusteAltPID(altPd, altId, altDd);
				SuicideBurn.setAjusteVelPID(velPd, velId, velDd);
				try {
					String[] dados = { Arquivos.SB, String.valueOf(altPd), String.valueOf(altId), String.valueOf(altDd),
							String.valueOf(velPd), String.valueOf(velId), String.valueOf(velDd), };
					Arquivos.gravarDadosConfig(dados);
				} catch (IOException e) {
					System.out.println("Erro ao gravar dados do Suicide Burn");
				}
			} catch (NullPointerException | NumberFormatException npe) {
				GUI.setStatus("Valores incorretos para o PID.");
				return false;
			}
			return true;
		case autoRover:
			String nomeMarcador = nomeMarcadorTextField.getText();
			float velMaxima = 10;
			if ((nomeMarcadorTextField.isEnabled()) && (nomeMarcador != null)) {
				AutoRover.buscandoMarcadores = true;
				AutoRover.setAlvo(nomeMarcador);
			} else {
				AutoRover.buscandoMarcadores = false;
			}
			try {
				velMaxima = Float.parseFloat(velMaxTextField.getText());
				AutoRover.setVelMaxima(velMaxima);
			} catch (NullPointerException | NumberFormatException npe) {
				GUI.setStatus("Valor inválido para velocidade. Utilizando 10m/s.");
				AutoRover.setVelMaxima(10);
			}
			try {
				String[] dados = { Arquivos.AR, nomeMarcador, String.valueOf(velMaxima) };
				Arquivos.gravarDadosConfig(dados);
			} catch (IOException e) {
				System.out.println("Erro ao gravar dados do Auto-Rover");
			}
			return true;
		case manobras:
			try {

			} catch (NullPointerException | NumberFormatException npe) {
				GUI.setStatus("Érro");
				return false;
			}
			return true;
		case vooAutonomo: {
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
			segundaLinha.setText("Carga Elétrica: " + String.format("%1$.0f", val) + "%");
			break;
		case "distanciaDaQueima":
			terceiraLinha.setText("Distância da Queima: " + valor + "m");
			break;
		case "apoastro":
			terceiraLinha.setText("Apoastro: " + valor + "m");
			break;
		case "distPercorrida":
			terceiraLinha.setText("Distância Percorrida: " + valor + "m");
			break;
		case "distancia":
			quartaLinha.setText("Distância Restante: " + valor + "m");
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
					.setText("Tempo de Missão: " + String.format("%02d:%02d:%02d", horasTdm, minutosTdm, segundosTdm));
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

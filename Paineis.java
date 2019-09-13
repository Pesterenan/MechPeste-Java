package com.pesterenan;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class Paineis extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 6999337104582004411L;
	private JButton botSuicideBurn;
	private JButton botDecolagem;
	private JButton botAutoRover;
	private JButton botSuicideMulti;
	private JButton botVooAutonomo;
	private JLabel statusLabel;

	public Paineis() {

	}

	public JPanel PainelFuncoes() {
		// Criar Botões:
		botSuicideBurn = new JButton("Suicide Burn");
		botSuicideBurn.setMnemonic(KeyEvent.VK_S);
		botDecolagem = new JButton("Decolagem Orbital");
		botDecolagem.setMnemonic(KeyEvent.VK_D);
		botSuicideMulti = new JButton("Multi SuicideBurn");
		botSuicideMulti.setMnemonic(KeyEvent.VK_M);
		botAutoRover = new JButton("Rover Autônomo");
		botAutoRover.setMnemonic(KeyEvent.VK_R);
		botVooAutonomo = new JButton("Voo Autônomo");
		botVooAutonomo.setMnemonic(KeyEvent.VK_V);

		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder("Funções")));

		setLayout(new GridBagLayout());
		Dimension tamanhoMenu = new Dimension(160, 240);
		this.setMinimumSize(tamanhoMenu);
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 0.1;
		gc.weighty = 0.01;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridx = 0;
		gc.gridy = GridBagConstraints.RELATIVE;

		gc.anchor = GridBagConstraints.LINE_START;
		add(botDecolagem, gc);
		botDecolagem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				firePropertyChange("botDecolagem", 0, 1);
			}
		});

		gc.anchor = GridBagConstraints.LINE_START;
		add(botSuicideBurn, gc);
		botSuicideBurn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				firePropertyChange("botSuicideBurn", 0, 1);
			}
		});

		gc.anchor = GridBagConstraints.LINE_START;
		add(botAutoRover, gc);
		botAutoRover.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				firePropertyChange("botAutoRover", 0, 1);
			}
		});

		gc.anchor = GridBagConstraints.LINE_START;
		add(botSuicideMulti, gc);
		botSuicideMulti.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				firePropertyChange("botSuicideMulti", 0, 1);
			}
		});

		gc.anchor = GridBagConstraints.LINE_START;
		add(botVooAutonomo, gc);
		botVooAutonomo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				firePropertyChange("botVooAutonomo", 0, 1);
			}
		});
		setVisible(true);
		return this;
	}

	public JPanel PainelParametros() {
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder("Parâmetros de Voo:")));

		setLayout(new GridBagLayout());
		Dimension tamanhoMenu = new Dimension(160, 240);
		this.setMinimumSize(tamanhoMenu);
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 1;
		gc.weighty = 0.1;

		gc.gridx = 0;
		gc.gridy = 0;

		gc.anchor = GridBagConstraints.LINE_END;
		add(new JLabel("Altitude: "), gc);

		gc.gridx = 1;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.LINE_START;
		add(new JLabel("20.0m"), gc);

		gc.gridx = 0;
		gc.gridy = 1;
		gc.anchor = GridBagConstraints.LINE_END;
		add(new JLabel("Apoastro: "), gc);

		gc.gridx = 1;
		gc.gridy = 1;
		gc.anchor = GridBagConstraints.LINE_START;
		add(new JLabel("20.0m"), gc);

		setVisible(true);
		return this;
	}

	public JPanel PainelStatus() {
		statusLabel = new JLabel("");
		addPropertyChangeListener(this);
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2),
				BorderFactory.createBevelBorder(BevelBorder.LOWERED)));

		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 1;
		gc.weighty = 0.1;
		gc.insets = new Insets(1, 1, 1, 1);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.LINE_START;
		add(statusLabel, gc);
		;

		return this;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String evento = evt.getPropertyName();
		String status = "";
		if ("status".equals(evento)) {
			switch ((int) evt.getNewValue()) {
			case 0:
				status = "";
				break;
			case 1:
				status = "Conectando...";
				break;
			case 2:
				status = "Erro de Conexão!";
				break;
			case 3:
				status = "Iniciando o Módulo de Decolagem Orbital";
				break;
			case 4:
				status = "Iniciando o Módulo de Suicide Burn";
				break;
			case 5:
				status = "Já está em execução.";
				break;
			}
			statusLabel.setText(status);
		}
	}
}

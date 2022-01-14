package com.pesterenan.gui;

import com.pesterenan.MechPeste;
import com.pesterenan.utils.Modulos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import static com.pesterenan.utils.Modulos.APOASTRO;
import static com.pesterenan.utils.Modulos.DIRECAO;
import static com.pesterenan.utils.Modulos.EXECUTAR_DECOLAGEM;
import static com.pesterenan.utils.Dicionario.TELEMETRIA;

public class DecolagemJPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JButton botVoltar = new JButton("Voltar");
	private JButton botIniciar = new JButton("Iniciar");
	private JLabel apoastroLabel = new JLabel("Apoastro final: ");
	private JLabel direcaoLabel = new JLabel("Direção: ");
	private JTextField apoastroTextField = new JTextField("80000");
	private JTextField direcaoTextField = new JTextField("90");

	public DecolagemJPanel() {

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{170, 0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0};
		setLayout(gridBagLayout);
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(0, 0, 5, 5);
		gc.gridx = 0;

		gc.gridy = 0;
		add(apoastroLabel, gc);
		gc.fill = GridBagConstraints.HORIZONTAL;
		GridBagConstraints gbc_apoastroTextField = new GridBagConstraints();
		gbc_apoastroTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_apoastroTextField.insets = new Insets(0, 0, 5, 0);
		gbc_apoastroTextField.gridx = 1;
		gbc_apoastroTextField.gridy = 0;
		add(apoastroTextField, gbc_apoastroTextField);

		gc.gridy = 1;
		gc.fill = GridBagConstraints.HORIZONTAL;

		gc.gridy = 2;
		GridBagConstraints gbc_direcaoLabel = new GridBagConstraints();
		gbc_direcaoLabel.insets = new Insets(0, 0, 5, 5);
		gbc_direcaoLabel.gridx = 0;
		gbc_direcaoLabel.gridy = 1;
		add(direcaoLabel, gbc_direcaoLabel);
		GridBagConstraints gbc_direcaoTextField = new GridBagConstraints();
		gbc_direcaoTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_direcaoTextField.insets = new Insets(0, 0, 5, 0);
		gbc_direcaoTextField.gridx = 1;
		gbc_direcaoTextField.gridy = 1;
		add(direcaoTextField, gbc_direcaoTextField);
		botIniciar.addActionListener(this);
		botIniciar.setActionCommand("Iniciar");
		GridBagConstraints gbc_botIniciar = new GridBagConstraints();
		gbc_botIniciar.fill = GridBagConstraints.HORIZONTAL;
		gbc_botIniciar.anchor = GridBagConstraints.SOUTH;
		gbc_botIniciar.insets = new Insets(0, 0, 0, 5);
		gbc_botIniciar.gridx = 0;
		gbc_botIniciar.gridy = 2;
		add(botIniciar, gbc_botIniciar);
		botVoltar.addActionListener(this);
		botVoltar.setActionCommand("Voltar");
		GridBagConstraints gbc_botVoltar = new GridBagConstraints();
		gbc_botVoltar.anchor = GridBagConstraints.SOUTH;
		gbc_botVoltar.fill = GridBagConstraints.HORIZONTAL;
		gbc_botVoltar.gridx = 1;
		gbc_botVoltar.gridy = 2;
		add(botVoltar, gbc_botVoltar);

		return;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Iniciar")) {
			Map<Modulos, String> valores = new HashMap<>();
			valores.put(APOASTRO, apoastroTextField.getText());
			valores.put(DIRECAO, direcaoTextField.getText());
			MechPeste.iniciarModulo(EXECUTAR_DECOLAGEM, valores);
		}
		if (e.getActionCommand().equals("Voltar")) {
			MainGui.getParametros().firePropertyChange(TELEMETRIA.get(), 0, 1);
		}
	}

}

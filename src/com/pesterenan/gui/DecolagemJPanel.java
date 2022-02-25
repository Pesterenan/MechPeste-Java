package com.pesterenan.gui;

import static com.pesterenan.utils.Dicionario.TELEMETRIA;
import static com.pesterenan.utils.Modulos.APOASTRO;
import static com.pesterenan.utils.Modulos.DIRECAO;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.pesterenan.MechPeste;
import com.pesterenan.utils.Modulos;

public class DecolagemJPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JButton botVoltar = new JButton("Voltar");
	private JButton botIniciar = new JButton("Iniciar");
	private JLabel apoastroLabel = new JLabel("Apoastro final: ");
	private JLabel direcaoLabel = new JLabel("Direção: ");
	private JTextField apoastroTextField = new JTextField("80000");
	private JTextField direcaoTextField = new JTextField("90");
	private final JPanel panel = new JPanel();

	public DecolagemJPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{136, 100, 0};
		gridBagLayout.rowHeights = new int[]{23, 23, 30, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		GridBagConstraints gbc_apoastroLabel = new GridBagConstraints();
		gbc_apoastroLabel.anchor = GridBagConstraints.WEST;
		gbc_apoastroLabel.insets = new Insets(0, 0, 5, 5);
		gbc_apoastroLabel.gridx = 0;
		gbc_apoastroLabel.gridy = 0;
		add(apoastroLabel, gbc_apoastroLabel);
		GridBagConstraints gbc_apoastroTextField = new GridBagConstraints();
		gbc_apoastroTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_apoastroTextField.insets = new Insets(0, 0, 5, 0);
		gbc_apoastroTextField.gridx = 1;
		gbc_apoastroTextField.gridy = 0;
		add(apoastroTextField, gbc_apoastroTextField);
		GridBagConstraints gbc_direcaoLabel = new GridBagConstraints();
		gbc_direcaoLabel.anchor = GridBagConstraints.WEST;
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
		
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 4;
		add(panel, gbc_panel);
		panel.add(botIniciar);
		botIniciar.addActionListener(this);
		botIniciar.setActionCommand("Iniciar");
		panel.add(botVoltar);
		botVoltar.addActionListener(this);
		botVoltar.setActionCommand("Voltar");

		return;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Iniciar")) {
			Map<String, String> comandos = new HashMap<>();
			comandos.put(Modulos.MODULO.get(), Modulos.MODULO_DECOLAGEM.get());
			comandos.put(Modulos.APOASTRO.get(), apoastroTextField.getText());
			comandos.put(Modulos.DIRECAO.get(), direcaoTextField.getText());
			MechPeste.iniciarModulo(comandos);
		}
		if (e.getActionCommand().equals("Voltar")) {
			MainGui.getParametros().firePropertyChange(TELEMETRIA.get(), 0, 1);
		}
	}

}

package com.pesterenan.view;

import static com.pesterenan.utils.Dicionario.TELEMETRIA;

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
import com.pesterenan.controller.LandingController;
import com.pesterenan.utils.Modulos;

public class PousoAutomaticoJPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JTextField sobrevoarTextField;
	private JButton sobrevoarButton;

	public PousoAutomaticoJPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 100, 86, 0 };
		gridBagLayout.rowHeights = new int[] { 23, 0, 20, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JButton voltarButton = new JButton("Voltar");
		voltarButton.setActionCommand("Voltar");
		voltarButton.addActionListener(this);

		JLabel pousoAutomaticoLabel = new JLabel("Executar Pouso Automático:");
		GridBagConstraints gbc_pousoAutomaticoLabel = new GridBagConstraints();
		gbc_pousoAutomaticoLabel.insets = new Insets(0, 0, 5, 5);
		gbc_pousoAutomaticoLabel.gridx = 0;
		gbc_pousoAutomaticoLabel.gridy = 0;
		add(pousoAutomaticoLabel, gbc_pousoAutomaticoLabel);

		JButton pousoAutomaticoButton = new JButton("Pousar");
		GridBagConstraints gbc_pousoAutomaticoButton = new GridBagConstraints();
		pousoAutomaticoButton.setActionCommand("Pousar");
		pousoAutomaticoButton.addActionListener(this);
		pousoAutomaticoButton.setMnemonic('p');
		gbc_pousoAutomaticoButton.insets = new Insets(0, 0, 5, 0);
		gbc_pousoAutomaticoButton.gridx = 1;
		gbc_pousoAutomaticoButton.gridy = 0;
		add(pousoAutomaticoButton, gbc_pousoAutomaticoButton);

		JLabel sobrevoarLabel = new JLabel("Sobrevoar a área na altitude de:");
		GridBagConstraints gbc_sobrevoarLabel = new GridBagConstraints();
		gbc_sobrevoarLabel.anchor = GridBagConstraints.WEST;
		gbc_sobrevoarLabel.insets = new Insets(0, 0, 5, 5);
		gbc_sobrevoarLabel.gridx = 0;
		gbc_sobrevoarLabel.gridy = 2;
		add(sobrevoarLabel, gbc_sobrevoarLabel);

		sobrevoarTextField = new JTextField();
		GridBagConstraints gbc_sobrevoarTextField = new GridBagConstraints();
		gbc_sobrevoarTextField.anchor = GridBagConstraints.WEST;
		gbc_sobrevoarTextField.insets = new Insets(0, 0, 5, 0);
		gbc_sobrevoarTextField.gridx = 1;
		gbc_sobrevoarTextField.gridy = 2;
		add(sobrevoarTextField, gbc_sobrevoarTextField);
		sobrevoarTextField.setColumns(10);
		sobrevoarTextField.setText("100");
		GridBagConstraints gbc_voltarButton = new GridBagConstraints();
		gbc_voltarButton.anchor = GridBagConstraints.EAST;
		gbc_voltarButton.insets = new Insets(0, 0, 0, 5);
		gbc_voltarButton.gridx = 0;
		gbc_voltarButton.gridy = 3;
		add(voltarButton, gbc_voltarButton);

		sobrevoarButton = new JButton("Sobrevoar");
		sobrevoarButton.setActionCommand("Sobrevoar");
		sobrevoarButton.addActionListener(this);
		sobrevoarButton.setMnemonic('s');
		GridBagConstraints gbc_sobrevoarButton = new GridBagConstraints();
		gbc_sobrevoarButton.anchor = GridBagConstraints.NORTHWEST;
		gbc_sobrevoarButton.gridx = 1;
		gbc_sobrevoarButton.gridy = 3;
		add(sobrevoarButton, gbc_sobrevoarButton);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Sobrevoar")) {
			if (sobrevoarButton.getText().equals("Descer")) {
				LandingController.descer();
				sobrevoarButton.setText("Sobrevoar");
				return;
			}
			if (sobrevoarTextField.getText().equals("")) {
				StatusJPanel.setStatus("A altitude para sobrevoo tem que ser um número.");
				return;
			}
			Map<String, String> comandos = new HashMap<>();
			comandos.put(Modulos.MODULO.get(), Modulos.MODULO_POUSO_SOBREVOAR.get());
			comandos.put(Modulos.ALTITUDE_SOBREVOO.get(), sobrevoarTextField.getText());
			MechPeste.iniciarModulo(comandos);
			sobrevoarButton.setText("Descer");
		}
		if (e.getActionCommand().equals("Pousar")) {
			Map<String, String> comandos = new HashMap<>();
			comandos.put(Modulos.MODULO.get(), Modulos.MODULO_POUSO.get());
			MechPeste.iniciarModulo(comandos);
		}
		if (e.getActionCommand().equals("Voltar")) {
			MainGui.getParametros().firePropertyChange(TELEMETRIA.get(), 0, 1);
		}
	}

}

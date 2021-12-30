package com.pesterenan.gui;

import static com.pesterenan.utils.Dicionario.TELEMETRIA;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.pesterenan.controller.ManobrasController;
import com.pesterenan.controller.ManobrasController.Altitude;

import krpc.client.RPCException;
import krpc.client.StreamException;

public class ManobrasJPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JLabel circularizarLabel = new JLabel("Circularizar órbita no:");
	private JButton botApoastro = new JButton("Apoastro");
	private JButton botPeriastro = new JButton("Periastro");

	public ManobrasJPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 261, 0 };
		gridBagLayout.rowHeights = new int[] { 23, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		GridBagConstraints gbc_circularizarLabel = new GridBagConstraints();
		gbc_circularizarLabel.anchor = GridBagConstraints.WEST;
		gbc_circularizarLabel.insets = new Insets(0, 0, 5, 0);
		gbc_circularizarLabel.gridx = 0;
		gbc_circularizarLabel.gridy = 0;
		add(circularizarLabel, gbc_circularizarLabel);

		GridBagConstraints gbc_botApoastro = new GridBagConstraints();
		gbc_botApoastro.anchor = GridBagConstraints.WEST;
		gbc_botApoastro.gridx = 0;
		gbc_botApoastro.gridy = 1;
		add(botApoastro, gbc_botApoastro);
		botApoastro.addActionListener(this);

		GridBagConstraints gbc_botPeriastro = new GridBagConstraints();
		gbc_botPeriastro.anchor = GridBagConstraints.EAST;
		gbc_botPeriastro.gridx = 0;
		gbc_botPeriastro.gridy = 1;
		add(botPeriastro, gbc_botPeriastro);
		botPeriastro.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(botApoastro) || e.getSource().equals(botPeriastro)) {
			ManobrasController manobras;
			Altitude direcao = Altitude.APOASTRO;
			if (e.getSource().equals(botApoastro)) {
				direcao = Altitude.APOASTRO;
			} else {
				direcao = Altitude.PERIASTRO;
			}
			try {
				manobras = new ManobrasController();
				MainGui.getParametros().firePropertyChange(TELEMETRIA.get(), 0, 1);
				manobras.circularizarOrbita(direcao);
			} catch (RPCException | StreamException | IOException | InterruptedException e1) {
				StatusJPanel.setStatus("Erro ao tentar executar manobra.");
			}
		}

	}

}

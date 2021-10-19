package com.pesterenan.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class TelemetriaJPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JLabel nomeValorLabel = new JLabel("");
	private JLabel altitudeValorLabel = new JLabel("");
	private JLabel apoastroValorLabel = new JLabel("");
	private JLabel periastroValorLabel = new JLabel("");
	private JLabel velVValorLabel = new JLabel("");
	private JLabel velHValorLabel = new JLabel("");

	public TelemetriaJPanel() {

		JLabel nomeLabel = new JLabel("Nome: ");
		JLabel altitudeLabel = new JLabel("Altitude: ");
		JLabel apoastroLabel = new JLabel("Apoastro: ");
		JLabel periastroLabel = new JLabel("Periastro: ");
		JLabel velVLabel = new JLabel("Vel. Vertical: ");
		JLabel velHLabel = new JLabel("Vel. Horizontal:");
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 0.1;
		gc.weighty = 0.05;
		gc.gridx = 0;
		gc.anchor = GridBagConstraints.LINE_START;
		add(nomeLabel, gc);
		add(altitudeLabel, gc);
		add(apoastroLabel, gc);
		add(periastroLabel, gc);
		add(velVLabel, gc);
		add(velHLabel, gc);
		gc.weightx = 1;
		gc.gridx = 1;
		gc.anchor = GridBagConstraints.EAST;
		add(nomeValorLabel, gc);
		add(altitudeValorLabel, gc);
		add(apoastroValorLabel, gc);
		add(periastroValorLabel, gc);
		add(velVValorLabel, gc);
		add(velHValorLabel, gc);
		gc.weighty = 0.8;
		add(new JPanel(), gc);
		return;

	}

}

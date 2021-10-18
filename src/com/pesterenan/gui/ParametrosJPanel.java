package com.pesterenan.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class ParametrosJPanel extends JPanel  {

	private static final long serialVersionUID = 1L;

	private JLabel nomeValorLabel = new JLabel("0");
	private JLabel altitudeValorLabel = new JLabel("0");
	private JLabel apoastroValorLabel = new JLabel("0");
	private JLabel periastroValorLabel = new JLabel("0");
	private JLabel velVValorLabel = new JLabel("0");
	private JLabel velHValorLabel = new JLabel("0");
	private EmptyBorder bordaVazia = new EmptyBorder(5, 5, 5, 5);
	

	public ParametrosJPanel() {
		JLabel nomeLabel = new JLabel("Nome: ");
		JLabel altitudeLabel = new JLabel("Altitude: ");
		JLabel apoastroLabel = new JLabel("Apoastro: ");
		JLabel periastroLabel = new JLabel("Periastro: ");
		JLabel velVLabel = new JLabel("Vel. Vertical: ");
		JLabel velHLabel = new JLabel("Vel. Horizontal:");
		setBorder(BorderFactory.createCompoundBorder(
				((Border)BorderFactory.createCompoundBorder(bordaVazia,
				BorderFactory.createTitledBorder("Parâmetros da Nave:")))
				,bordaVazia));

		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 0.1;
		gc.weighty = 0.5;
		gc.gridx = 0;
		gc.anchor = GridBagConstraints.LINE_START;
		add(nomeLabel, gc);
		add(altitudeLabel, gc);
		add(apoastroLabel, gc);
		add(periastroLabel, gc);
		add(velVLabel, gc);
		add(velHLabel, gc);
		gc.weightx = 1;
		gc.weighty = 0.01;
		gc.gridx = 1;
		gc.anchor = GridBagConstraints.EAST;
		add(nomeValorLabel, gc);
		add(altitudeValorLabel, gc);
		add(apoastroValorLabel, gc);
		add(periastroValorLabel, gc);
		add(velVValorLabel, gc);
		add(velHValorLabel, gc);
		gc.weighty = 0.8;
		add(new JPanel(),gc);
		return;

	}

}

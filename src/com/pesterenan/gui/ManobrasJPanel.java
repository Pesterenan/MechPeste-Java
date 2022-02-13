package com.pesterenan.gui;

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

import com.pesterenan.MechPeste;
import com.pesterenan.utils.Modulos;

public class ManobrasJPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private final JLabel lblCircularizar = new JLabel("Circularizar órbita no:");
	private final JLabel lblExecutar = new JLabel("Executar próxima manobra: ");
	private final JLabel lblAjustar = new JLabel("Ajustar inclinação:");
	private final JButton btnApoastro = new JButton("Apoastro");
	private final JButton btnPeriastro = new JButton("Periastro");
	private final JButton btnExecutar = new JButton("Executar");
	private final JButton btnAjustar = new JButton("Ajustar");

	public ManobrasJPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{136, 100, 0};
		gridBagLayout.rowHeights = new int[]{23, 23, 30, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		GridBagConstraints gbc_lblExecutar = new GridBagConstraints();
		gbc_lblExecutar.anchor = GridBagConstraints.WEST;
		gbc_lblExecutar.insets = new Insets(0, 0, 5, 5);
		gbc_lblExecutar.gridx = 0;
		gbc_lblExecutar.gridy = 0;
		add(lblExecutar, gbc_lblExecutar);
		GridBagConstraints gbc_btnExecutar = new GridBagConstraints();
		gbc_btnExecutar.insets = new Insets(0, 0, 5, 0);
		gbc_btnExecutar.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExecutar.gridx = 1;
		gbc_btnExecutar.gridy = 0;
		add(btnExecutar, gbc_btnExecutar);
		btnExecutar.addActionListener(this);
		btnExecutar.setActionCommand("Executar");
		
		GridBagConstraints gbc_lblCircularizar = new GridBagConstraints();
		gbc_lblCircularizar.anchor = GridBagConstraints.WEST;
		gbc_lblCircularizar.insets = new Insets(0, 0, 5, 5);
		gbc_lblCircularizar.gridx = 0;
		gbc_lblCircularizar.gridy = 1;
		add(lblCircularizar, gbc_lblCircularizar);
		GridBagConstraints gbc_btnApoastro = new GridBagConstraints();
		gbc_btnApoastro.insets = new Insets(0, 0, 5, 0);
		gbc_btnApoastro.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnApoastro.gridx = 1;
		gbc_btnApoastro.gridy = 1;
		add(btnApoastro, gbc_btnApoastro);
		btnApoastro.addActionListener(this);
		btnApoastro.setActionCommand("Apoastro");
		
		GridBagConstraints gbc_btnPeriastro = new GridBagConstraints();
		gbc_btnPeriastro.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnPeriastro.insets = new Insets(0, 0, 5, 0);
		gbc_btnPeriastro.gridx = 1;
		gbc_btnPeriastro.gridy = 2;
		add(btnPeriastro, gbc_btnPeriastro);
		btnPeriastro.addActionListener(this);
		btnPeriastro.setActionCommand("Periastro");
		
		GridBagConstraints gbc_lblAjustar = new GridBagConstraints();
		gbc_lblAjustar.anchor = GridBagConstraints.WEST;
		gbc_lblAjustar.insets = new Insets(0, 0, 0, 5);
		gbc_lblAjustar.gridx = 0;
		gbc_lblAjustar.gridy = 3;
		add(lblAjustar, gbc_lblAjustar);
		
		GridBagConstraints gbc_btnAjustar = new GridBagConstraints();
		gbc_btnAjustar.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAjustar.gridx = 1;
		gbc_btnAjustar.gridy = 3;
		add(btnAjustar, gbc_btnAjustar);
		btnAjustar.addActionListener(this);
		btnAjustar.setActionCommand("Ajustar");
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String evtComando = e.getActionCommand();
		Map<Modulos, String> valores = new HashMap<>();
		valores.put(Modulos.EXECUTAR_MANOBRA, evtComando);
		MechPeste.iniciarModulo(Modulos.EXECUTAR_MANOBRA, valores);
	}

}

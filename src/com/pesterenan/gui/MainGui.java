package com.pesterenan.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class MainGui extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private final Dimension tamanhoApp = new Dimension(450, 250);
	private JPanel parametros = new ParametrosJPanel();
	private JPanel funcoes = new FuncoesJPanel();

	public MainGui() {
		setLocation(100, 100);
		setMinimumSize(tamanhoApp);
		add(funcoes, BorderLayout.WEST);
		add(parametros);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
	}

}

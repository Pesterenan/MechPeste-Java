package com.pesterenan.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class MainGui extends JFrame {

	private static final long serialVersionUID = 1L;

	private final Dimension tamanhoApp = new Dimension(450, 250);
	private static JPanel parametros = new ParametrosJPanel();
	private JPanel funcoes = new FuncoesJPanel();

	public MainGui() {
		setLocation(100, 100);
		setMinimumSize(tamanhoApp);
		add(funcoes, BorderLayout.WEST);
		add(parametros, BorderLayout.CENTER);
		
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);
		pack();
		setVisible(true);
	}

	public static JPanel getParametros() {
		return parametros;
	}

}

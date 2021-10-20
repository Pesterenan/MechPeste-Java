package com.pesterenan.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public class DecolagemJPanel extends JPanel implements ActionListener{

	private static final long serialVersionUID = 1L;
	JButton botVoltar = new JButton("Voltar");

	public DecolagemJPanel() {
		botVoltar.addActionListener(this);
		botVoltar.setActionCommand("Voltar");
		add(botVoltar);
		
		return;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Voltar")) {
			MainGui.getParametros().firePropertyChange(FuncoesJPanel.telemetria, 0, 1);
		}
	}

	
}

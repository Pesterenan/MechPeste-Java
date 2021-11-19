package com.pesterenan.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.pesterenan.MechPeste;

import static com.pesterenan.utils.Dicionario.*;

public class DecolagemJPanel extends JPanel implements ActionListener{

	private static final long serialVersionUID = 1L;
	JButton botVoltar = new JButton("Voltar");
	JButton botIniciar = new JButton("Iniciar");

	public DecolagemJPanel() {
		botIniciar.addActionListener(this);
		botVoltar.addActionListener(this);
		botIniciar.setActionCommand("Iniciar");
		botVoltar.setActionCommand("Voltar");
		add(botIniciar);
		add(botVoltar);
		
		return;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Iniciar")) {
			MechPeste.iniciarThreadModulos(EXECUTAR_DECOLAGEM.get());
		}
		if (e.getActionCommand().equals("Voltar")) {
			MainGui.getParametros().firePropertyChange(TELEMETRIA.get(), 0, 1);
		}
	}

	
}

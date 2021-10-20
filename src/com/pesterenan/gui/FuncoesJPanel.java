package com.pesterenan.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class FuncoesJPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	public static final String 	decolagemOrbital = "Decolagem Orbital",
							pousoAutomatico = "Pouso Automático",
							roverAutonomo = "Rover Autônomo",
							execManobras = "Exec. Manobras",
							telemetria = "Telemetria",
							sair = "Sair";

	
	public FuncoesJPanel() {
		TitledBorder bordaTitulo = new TitledBorder("Funções");
		EmptyBorder bordaVazia = new EmptyBorder(5, 5, 5, 5);
		Dimension tamanhoMenu = new Dimension(100, ParametrosJPanel.HEIGHT);

		JButton botDecolagem = new JButton(decolagemOrbital);
		JButton botPousoAutomatico = new JButton(pousoAutomatico);
		JButton botRoverAutonomo = new JButton(roverAutonomo);
		JButton botExecManobras = new JButton(execManobras);
		JButton botSair = new JButton(sair);
		
		botDecolagem.setMnemonic(KeyEvent.VK_D);
		botPousoAutomatico.setMnemonic(KeyEvent.VK_S);
		botRoverAutonomo.setMnemonic(KeyEvent.VK_R);
		botExecManobras.setMnemonic(KeyEvent.VK_M);
		botExecManobras.setMnemonic(KeyEvent.VK_X);

		botPousoAutomatico.setEnabled(false);
		botRoverAutonomo.setEnabled(false);
		botExecManobras.setEnabled(false);
		
		botDecolagem.addActionListener(this);
		botPousoAutomatico.addActionListener(this);
		botRoverAutonomo.addActionListener(this);
		botExecManobras.addActionListener(this);
		botSair.addActionListener(this);
		
		setMinimumSize(tamanhoMenu);
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 0.1;
		gc.weighty = 0.05;
		gc.gridx = 0;
		gc.gridy = GridBagConstraints.RELATIVE;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.HORIZONTAL;

		add(botDecolagem, gc);
		add(botPousoAutomatico, gc);
		add(botRoverAutonomo, gc);
		add(botExecManobras, gc);
		gc.weighty = 1;
		add(new JPanel(), gc);
		gc.weighty = 0;
		add(botSair, gc);
		setBorder(BorderFactory.createCompoundBorder(bordaVazia, bordaTitulo));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(sair)){ 
			System.exit(0);
		}
		MainGui.getParametros().firePropertyChange(e.getActionCommand(), 0, 1);
	
	}

}

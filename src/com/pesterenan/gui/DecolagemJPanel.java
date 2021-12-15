package com.pesterenan.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.pesterenan.MechPeste;

import static com.pesterenan.utils.Dicionario.*;

public class DecolagemJPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JButton botVoltar = new JButton("Voltar");
	private JButton botIniciar = new JButton("Iniciar");
	private JLabel apoastroLabel = new JLabel("Apoastro final: ");
	private JLabel direcaoLabel = new JLabel("Direção: ");
	private JTextField apoastroTextField = new JTextField("80.000");
	private JTextField direcaoTextField = new JTextField("90");

	public DecolagemJPanel() {
		botIniciar.addActionListener(this);
		botVoltar.addActionListener(this);
		botIniciar.setActionCommand("Iniciar");
		botVoltar.setActionCommand("Voltar");

		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.LINE_START;
		add(apoastroLabel, gc);
		gc.anchor = GridBagConstraints.EAST;
		gc.fill = GridBagConstraints.HORIZONTAL;
		add(apoastroTextField, gc);
		gc.gridy = 1;
		gc.anchor = GridBagConstraints.LINE_START;
		add(direcaoLabel, gc);
		gc.anchor = GridBagConstraints.EAST;
		gc.fill = GridBagConstraints.HORIZONTAL;
		add(direcaoTextField, gc);

		gc.gridy = 2;
		gc.anchor = GridBagConstraints.LINE_START;
		add(botIniciar, gc);
		gc.anchor = GridBagConstraints.EAST;
		add(botVoltar, gc);


		return;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Iniciar")) {
			MainGui.getParametros().firePropertyChange(TELEMETRIA.get(), 0, 1);
			MechPeste.iniciarThreadModulos(EXECUTAR_DECOLAGEM.get());
		}
		if (e.getActionCommand().equals("Voltar")) {
			MainGui.getParametros().firePropertyChange(TELEMETRIA.get(), 0, 1);
		}
	}

}

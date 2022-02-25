package com.pesterenan.gui;

import static com.pesterenan.utils.Dicionario.DECOLAGEM_ORBITAL;
import static com.pesterenan.utils.Dicionario.MANOBRAS;
import static com.pesterenan.utils.Dicionario.POUSO_AUTOMATICO;
import static com.pesterenan.utils.Dicionario.ROVER_AUTONOMO;
import static com.pesterenan.utils.Dicionario.SAIR;

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

import com.pesterenan.utils.Dicionario;

import java.awt.Insets;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import java.awt.Color;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import javax.swing.SwingConstants;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Component;

public class FuncoesJPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	public FuncoesJPanel() {
		Dimension tamanhoMenu = new Dimension(100, ParametrosJPanel.HEIGHT);
		FlowLayout flowLayout = (FlowLayout) getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		setAlignmentY(Component.TOP_ALIGNMENT);
		setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel pnlFuncoes = new JPanel();
		pnlFuncoes.setSize(tamanhoMenu);
		pnlFuncoes.setBorder(new TitledBorder("Funções"));
		add(pnlFuncoes);
		pnlFuncoes.setLayout(new BorderLayout(0, 30));

		JPanel pnlSair = new JPanel();
		pnlFuncoes.add(pnlSair, BorderLayout.SOUTH);
		GridBagLayout gbl_pnlSair = new GridBagLayout();
		gbl_pnlSair.columnWidths = new int[] { 120, 0 };
		gbl_pnlSair.rowHeights = new int[] { 23, 0 };
		gbl_pnlSair.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_pnlSair.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		pnlSair.setLayout(gbl_pnlSair);

		JButton btnSair = new JButton(SAIR.get());
		btnSair.setActionCommand(SAIR.get());
		btnSair.addActionListener(this);
		GridBagConstraints gbc_btnSair = new GridBagConstraints();
		gbc_btnSair.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSair.anchor = GridBagConstraints.NORTH;
		gbc_btnSair.gridx = 0;
		gbc_btnSair.gridy = 0;
		pnlSair.add(btnSair, gbc_btnSair);

		JPanel pnlBotoes = new JPanel();
		pnlFuncoes.add(pnlBotoes, BorderLayout.CENTER);
		GridBagLayout gbl_pnlBotoes = new GridBagLayout();
		pnlBotoes.setLayout(gbl_pnlBotoes);

		JButton btnDecolagem = new JButton(DECOLAGEM_ORBITAL.get());
		btnDecolagem.addActionListener(this);
		btnDecolagem.setActionCommand(DECOLAGEM_ORBITAL.get());		
		GridBagConstraints gbc_btnDecolagem = new GridBagConstraints();
		gbc_btnDecolagem.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnDecolagem.anchor = GridBagConstraints.NORTH;
		gbc_btnDecolagem.insets = new Insets(0, 0, 2, 0);
		gbc_btnDecolagem.gridx = 0;
		gbc_btnDecolagem.gridy = 0;
		pnlBotoes.add(btnDecolagem, gbc_btnDecolagem);

		JButton btnPousoAutomatico = new JButton(POUSO_AUTOMATICO.get());
		btnPousoAutomatico.setEnabled(false);
		btnPousoAutomatico.addActionListener(this);
		btnPousoAutomatico.setActionCommand(POUSO_AUTOMATICO.get());
		GridBagConstraints gbc_btnPousoAutomatico = new GridBagConstraints();
		gbc_btnPousoAutomatico.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnPousoAutomatico.anchor = GridBagConstraints.NORTH;
		gbc_btnPousoAutomatico.insets = new Insets(0, 0, 2, 0);
		gbc_btnPousoAutomatico.gridx = 0;
		gbc_btnPousoAutomatico.gridy = 1;
		pnlBotoes.add(btnPousoAutomatico, gbc_btnPousoAutomatico);

		JButton btnExecManobras = new JButton(MANOBRAS.get());
		btnExecManobras.addActionListener(this);
		btnExecManobras.setActionCommand(MANOBRAS.get());
		GridBagConstraints gbc_btnExecManobras = new GridBagConstraints();
		gbc_btnExecManobras.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExecManobras.anchor = GridBagConstraints.NORTH;
		gbc_btnExecManobras.insets = new Insets(0, 0, 2, 0);
		gbc_btnExecManobras.gridx = 0;
		gbc_btnExecManobras.gridy = 2;
		pnlBotoes.add(btnExecManobras, gbc_btnExecManobras);

		JButton btnAutoRover = new JButton(ROVER_AUTONOMO.get());
		btnAutoRover.setEnabled(false);
		btnAutoRover.addActionListener(this);
		btnAutoRover.setActionCommand(ROVER_AUTONOMO.get());
		GridBagConstraints gbc_btnAutoRover = new GridBagConstraints();
		gbc_btnAutoRover.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAutoRover.anchor = GridBagConstraints.NORTH;
		gbc_btnAutoRover.gridx = 0;
		gbc_btnAutoRover.gridy = 3;
		pnlBotoes.add(btnAutoRover, gbc_btnAutoRover);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(SAIR.get())) {
			System.exit(0);
		}
		MainGui.getParametros().firePropertyChange(e.getActionCommand(), 0, 1);
	}
}

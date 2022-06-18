package com.pesterenan.views;

import static com.pesterenan.utils.Dicionario.DECOLAGEM_ORBITAL;
import static com.pesterenan.utils.Dicionario.MANOBRAS;
import static com.pesterenan.utils.Dicionario.POUSO_AUTOMATICO;
import static com.pesterenan.utils.Dicionario.ROVER_AUTONOMO;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;

public class FuncoesJPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	public static final int BUTTON_WIDTH = 135;

	private JButton btnDecolagemOrbital;
	private JButton btnPousoAutomtico;
	private JButton btnManobras;
	private JButton btnPilotarRover;

	public FuncoesJPanel() {
		initComponents();
	}

	private void initComponents() {
		setMinimumSize(new Dimension(0, 0));
		setPreferredSize(new Dimension(148, 216));
		setBorder(new TitledBorder(null, "Fun\u00E7\u00F5es", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		btnDecolagemOrbital = new JButton(DECOLAGEM_ORBITAL.get());
		btnDecolagemOrbital.addActionListener(this);

		btnPousoAutomtico = new JButton(POUSO_AUTOMATICO.get());
		btnPousoAutomtico.addActionListener(this);

		btnManobras = new JButton(MANOBRAS.get());
		btnManobras.addActionListener(this);

		btnPilotarRover = new JButton(ROVER_AUTONOMO.get());
		btnPilotarRover.addActionListener(this);
		btnPilotarRover.setEnabled(false);

		GroupLayout gl_pnlFuncoes = new GroupLayout(this);
		gl_pnlFuncoes.setHorizontalGroup(gl_pnlFuncoes.createParallelGroup(Alignment.LEADING).addGroup(gl_pnlFuncoes
				.createSequentialGroup()
				.addGroup(gl_pnlFuncoes.createParallelGroup(Alignment.LEADING)
						.addComponent(btnDecolagemOrbital, GroupLayout.PREFERRED_SIZE, BUTTON_WIDTH,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(btnPousoAutomtico, GroupLayout.PREFERRED_SIZE, BUTTON_WIDTH,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(btnManobras, GroupLayout.PREFERRED_SIZE, BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnPilotarRover, GroupLayout.PREFERRED_SIZE, BUTTON_WIDTH,
								GroupLayout.PREFERRED_SIZE))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		gl_pnlFuncoes.setVerticalGroup(gl_pnlFuncoes.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlFuncoes.createSequentialGroup().addComponent(btnDecolagemOrbital)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnPousoAutomtico)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnManobras)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnPilotarRover)
						.addContainerGap(100, Short.MAX_VALUE)));
		setLayout(gl_pnlFuncoes);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnPilotarRover) {
			handleBtnPilotarRoverActionPerformed(e);
		}
		if (e.getSource() == btnManobras) {
			handleBtnManobrasActionPerformed(e);
		}
		if (e.getSource() == btnPousoAutomtico) {
			handleBtnPousoAutomticoActionPerformed(e);
		}
		if (e.getSource() == btnDecolagemOrbital) {
			handleBtnDecolagemOrbitalActionPerformed(e);
		}
	}

	protected void handleBtnDecolagemOrbitalActionPerformed(ActionEvent e) {
		MainGui.getParametros().firePropertyChange(DECOLAGEM_ORBITAL.get(), false, true);
	}

	protected void handleBtnPousoAutomticoActionPerformed(ActionEvent e) {
		MainGui.getParametros().firePropertyChange(POUSO_AUTOMATICO.get(), false, true);
	}

	protected void handleBtnManobrasActionPerformed(ActionEvent e) {
		MainGui.getParametros().firePropertyChange(MANOBRAS.get(), false, true);
	}

	protected void handleBtnPilotarRoverActionPerformed(ActionEvent e) {
		MainGui.getParametros().firePropertyChange(ROVER_AUTONOMO.get(), false, true);
	}
}

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

public class FunctionsJPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	public static final int BUTTON_WIDTH = 135;

	private JButton btnLiftoff;
	private JButton btnLanding;
	private JButton btnManeuver;
	private JButton btnRover;

	public FunctionsJPanel() {
		initComponents();
	}

	private void initComponents() {
		setPreferredSize(new Dimension(148, 216));
		setBorder(new TitledBorder(null, "Fun\u00E7\u00F5es", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		btnLiftoff = new JButton(DECOLAGEM_ORBITAL.get());
		btnLiftoff.addActionListener(this);

		btnLanding = new JButton(POUSO_AUTOMATICO.get());
		btnLanding.addActionListener(this);

		btnManeuver = new JButton(MANOBRAS.get());
		btnManeuver.addActionListener(this);

		btnRover = new JButton(ROVER_AUTONOMO.get());
		btnRover.addActionListener(this);
		btnRover.setEnabled(false);

		GroupLayout gl_pnlFunctions = new GroupLayout(this);
		gl_pnlFunctions.setHorizontalGroup(gl_pnlFunctions.createParallelGroup(Alignment.LEADING).addGroup(gl_pnlFunctions
				.createSequentialGroup()
				.addGroup(gl_pnlFunctions.createParallelGroup(Alignment.LEADING)
						.addComponent(btnLiftoff, GroupLayout.PREFERRED_SIZE, BUTTON_WIDTH,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(btnLanding, GroupLayout.PREFERRED_SIZE, BUTTON_WIDTH,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(btnManeuver, GroupLayout.PREFERRED_SIZE, BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnRover, GroupLayout.PREFERRED_SIZE, BUTTON_WIDTH,
								GroupLayout.PREFERRED_SIZE))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		gl_pnlFunctions.setVerticalGroup(gl_pnlFunctions.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlFunctions.createSequentialGroup().addComponent(btnLiftoff)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnLanding)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnManeuver)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnRover)
						.addContainerGap(100, Short.MAX_VALUE)));
		setLayout(gl_pnlFunctions);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnRover) {
			handleBtnPilotarRoverActionPerformed(e);
		}
		if (e.getSource() == btnManeuver) {
			handleBtnManobrasActionPerformed(e);
		}
		if (e.getSource() == btnLanding) {
			handleBtnPousoAutomticoActionPerformed(e);
		}
		if (e.getSource() == btnLiftoff) {
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

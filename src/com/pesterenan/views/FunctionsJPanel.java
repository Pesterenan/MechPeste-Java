package com.pesterenan.views;

import static com.pesterenan.utils.Dictionary.ORBITAL_LIFTOFF;
import static com.pesterenan.utils.Dictionary.MANEUVERS;
import static com.pesterenan.utils.Dictionary.AUTOMATIC_LAND;
import static com.pesterenan.utils.Dictionary.AUTOMATIC_ROVER;

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

		btnLiftoff = new JButton(ORBITAL_LIFTOFF.get());
		btnLiftoff.addActionListener(this);

		btnLanding = new JButton(AUTOMATIC_LAND.get());
		btnLanding.addActionListener(this);

		btnManeuver = new JButton(MANEUVERS.get());
		btnManeuver.addActionListener(this);

		btnRover = new JButton(AUTOMATIC_ROVER.get());
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
		MainGui.getParameters().firePropertyChange(ORBITAL_LIFTOFF.get(), false, true);
	}

	protected void handleBtnPousoAutomticoActionPerformed(ActionEvent e) {
		MainGui.getParameters().firePropertyChange(AUTOMATIC_LAND.get(), false, true);
	}

	protected void handleBtnManobrasActionPerformed(ActionEvent e) {
		MainGui.getParameters().firePropertyChange(MANEUVERS.get(), false, true);
	}

	protected void handleBtnPilotarRoverActionPerformed(ActionEvent e) {
		MainGui.getParameters().firePropertyChange(AUTOMATIC_ROVER.get(), false, true);
	}
}

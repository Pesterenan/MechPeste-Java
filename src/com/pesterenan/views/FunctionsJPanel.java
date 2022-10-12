package com.pesterenan.views;

import com.pesterenan.resources.Bundle;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FunctionsJPanel extends JPanel implements ActionListener {
	public static final int BUTTON_WIDTH = 135;
	private static final long serialVersionUID = 1L;
	private JButton btnLiftoff;
	private JButton btnLanding;
	private JButton btnManeuver;
	private JButton btnRover;

	public FunctionsJPanel() {

		initComponents();
	}

	private void initComponents() {
		setPreferredSize(new Dimension(148, 216));
		setBorder(
				new TitledBorder(null, Bundle.getString("pnl_func_title"), TitledBorder.LEADING, TitledBorder.TOP,
				                 null,
				                 null
				));

		btnLiftoff = new JButton(Bundle.getString("btn_func_liftoff")); //$NON-NLS-1$ //$NON-NLS-2$
		btnLiftoff.addActionListener(this);

		btnLanding = new JButton(Bundle.getString("btn_func_landing")); //$NON-NLS-1$ //$NON-NLS-2$
		btnLanding.addActionListener(this);

		btnManeuver = new JButton(Bundle.getString("btn_func_maneuvers")); //$NON-NLS-1$ //$NON-NLS-2$
		btnManeuver.addActionListener(this);

		btnRover = new JButton(Bundle.getString("btn_func_rover")); //$NON-NLS-1$ //$NON-NLS-2$
		btnRover.addActionListener(this);

		GroupLayout gl_pnlFunctions = new GroupLayout(this);
		gl_pnlFunctions.setHorizontalGroup(gl_pnlFunctions.createParallelGroup(Alignment.LEADING)
		                                                  .addGroup(gl_pnlFunctions.createSequentialGroup()
		                                                                           .addGroup(
				                                                                           gl_pnlFunctions.createParallelGroup(
						                                                                                          Alignment.LEADING)
				                                                                                          .addComponent(
						                                                                                          btnLiftoff,
						                                                                                          GroupLayout.PREFERRED_SIZE,
						                                                                                          BUTTON_WIDTH,
						                                                                                          GroupLayout.PREFERRED_SIZE
				                                                                                                       )
				                                                                                          .addComponent(
						                                                                                          btnLanding,
						                                                                                          GroupLayout.PREFERRED_SIZE,
						                                                                                          BUTTON_WIDTH,
						                                                                                          GroupLayout.PREFERRED_SIZE
				                                                                                                       )
				                                                                                          .addComponent(
						                                                                                          btnManeuver,
						                                                                                          GroupLayout.PREFERRED_SIZE,
						                                                                                          BUTTON_WIDTH,
						                                                                                          GroupLayout.PREFERRED_SIZE
				                                                                                                       )
				                                                                                          .addComponent(
						                                                                                          btnRover,
						                                                                                          GroupLayout.PREFERRED_SIZE,
						                                                                                          BUTTON_WIDTH,
						                                                                                          GroupLayout.PREFERRED_SIZE
				                                                                                                       ))
		                                                                           .addContainerGap(
				                                                                           GroupLayout.DEFAULT_SIZE,
				                                                                           Short.MAX_VALUE
		                                                                                           )));
		gl_pnlFunctions.setVerticalGroup(gl_pnlFunctions.createParallelGroup(Alignment.LEADING)
		                                                .addGroup(gl_pnlFunctions.createSequentialGroup()
		                                                                         .addComponent(btnLiftoff)
		                                                                         .addPreferredGap(
				                                                                         ComponentPlacement.RELATED)
		                                                                         .addComponent(btnLanding)
		                                                                         .addPreferredGap(
				                                                                         ComponentPlacement.RELATED)
		                                                                         .addComponent(btnManeuver)
		                                                                         .addPreferredGap(
				                                                                         ComponentPlacement.RELATED)
		                                                                         .addComponent(btnRover)
		                                                                         .addContainerGap(100,
		                                                                                          Short.MAX_VALUE
		                                                                                         )));
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
			handleBtnPousoAutomaticoActionPerformed(e);
		}
		if (e.getSource() == btnLiftoff) {
			handleBtnDecolagemOrbitalActionPerformed(e);
		}
	}

	protected void handleBtnDecolagemOrbitalActionPerformed(ActionEvent e) {
		MainGui.getParametros().firePropertyChange("Decolagem Orbital", false, true);
	}

	protected void handleBtnPousoAutomaticoActionPerformed(ActionEvent e) {
		MainGui.getParametros().firePropertyChange("Pouso Automático", false, true);
	}

	protected void handleBtnManobrasActionPerformed(ActionEvent e) {
		MainGui.getParametros().firePropertyChange("Manobras", false, true);
	}

	protected void handleBtnPilotarRoverActionPerformed(ActionEvent e) {
		MainGui.getParametros().firePropertyChange("Rover Autônomo", false, true);
	}
}

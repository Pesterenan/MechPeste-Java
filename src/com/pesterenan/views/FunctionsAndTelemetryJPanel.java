package com.pesterenan.views;

import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Telemetry;
import com.pesterenan.utils.Utilities;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import static com.pesterenan.views.MainGui.dmsPanels;

public class FunctionsAndTelemetryJPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 0L;
	public static final int BUTTON_WIDTH = 135;
	private JButton btnLiftoff;
	private JButton btnLanding;
	private JButton btnManeuver;
	private JButton btnRover;
	private JPanel pnlTelemetry;

	private static JLabel altitudeValorLabel = new JLabel("");
	private static JLabel altitudeSupValorLabel = new JLabel("");
	private static JLabel apoastroValorLabel = new JLabel("");
	private static JLabel periastroValorLabel = new JLabel("");
	private static JLabel velVValorLabel = new JLabel("");
	private static JLabel velHValorLabel = new JLabel("");

	public FunctionsAndTelemetryJPanel() {
		initComponents();
	}

	private void initComponents() {
		setPreferredSize(dmsPanels);
		setBorder(
				new TitledBorder(null, Bundle.getString("pnl_func_title"), TitledBorder.LEADING, TitledBorder.TOP,
				                 null,
				                 null
				));
		pnlTelemetry = new JPanel();
		pnlTelemetry.setBorder(
				new TitledBorder(null, Bundle.getString("pnl_tel_border"), TitledBorder.LEADING, TitledBorder.TOP,
				                 null,
				                 null
				));

		btnLiftoff = new JButton(Bundle.getString("btn_func_liftoff"));
		btnLiftoff.addActionListener(this);

		btnLanding = new JButton(Bundle.getString("btn_func_landing"));
		btnLanding.addActionListener(this);

		btnManeuver = new JButton(Bundle.getString("btn_func_maneuvers"));
		btnManeuver.addActionListener(this);

		btnRover = new JButton(Bundle.getString("btn_func_rover"));
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
						                                                                                          135,
						                                                                                          GroupLayout.PREFERRED_SIZE
				                                                                                                       )
				                                                                                          .addComponent(
						                                                                                          btnLanding,
						                                                                                          GroupLayout.PREFERRED_SIZE,
						                                                                                          135,
						                                                                                          GroupLayout.PREFERRED_SIZE
				                                                                                                       )
				                                                                                          .addComponent(
						                                                                                          btnManeuver,
						                                                                                          GroupLayout.PREFERRED_SIZE,
						                                                                                          135,
						                                                                                          GroupLayout.PREFERRED_SIZE
				                                                                                                       )
				                                                                                          .addComponent(
						                                                                                          btnRover,
						                                                                                          GroupLayout.PREFERRED_SIZE,
						                                                                                          135,
						                                                                                          GroupLayout.PREFERRED_SIZE
				                                                                                                       ))
		                                                                           .addPreferredGap(
				                                                                           ComponentPlacement.RELATED)
		                                                                           .addComponent(pnlTelemetry,
		                                                                                         GroupLayout.DEFAULT_SIZE,
		                                                                                         301, Short.MAX_VALUE
		                                                                                        )
		                                                                           .addGap(6)));
		gl_pnlFunctions.setVerticalGroup(gl_pnlFunctions.createParallelGroup(Alignment.LEADING)
		                                                .addGroup(gl_pnlFunctions.createSequentialGroup()
		                                                                         .addGroup(
				                                                                         gl_pnlFunctions.createParallelGroup(
						                                                                                        Alignment.LEADING)
				                                                                                        .addGroup(
						                                                                                        gl_pnlFunctions.createSequentialGroup()
						                                                                                                       .addComponent(
								                                                                                                       btnLiftoff)
						                                                                                                       .addPreferredGap(
								                                                                                                       ComponentPlacement.RELATED)
						                                                                                                       .addComponent(
								                                                                                                       btnLanding)
						                                                                                                       .addPreferredGap(
								                                                                                                       ComponentPlacement.RELATED)
						                                                                                                       .addComponent(
								                                                                                                       btnManeuver)
						                                                                                                       .addPreferredGap(
								                                                                                                       ComponentPlacement.RELATED)
						                                                                                                       .addComponent(
								                                                                                                       btnRover))
				                                                                                        .addComponent(
						                                                                                        pnlTelemetry,
						                                                                                        GroupLayout.DEFAULT_SIZE,
						                                                                                        188,
						                                                                                        Short.MAX_VALUE
				                                                                                                     ))
		                                                                         .addGap(6)));
		pnlTelemetry.setLayout(new GridLayout(6, 2, 0, 0));
		JLabel altitudeLabel = new JLabel(Bundle.getString("pnl_tel_lbl_alt"));
		pnlTelemetry.add(altitudeLabel);
		pnlTelemetry.add(altitudeValorLabel);

		JLabel altitudeSupLabel = new JLabel(Bundle.getString("pnl_tel_lbl_alt_sur"));
		pnlTelemetry.add(altitudeSupLabel);
		pnlTelemetry.add(altitudeSupValorLabel);

		JLabel apoastroLabel = new JLabel(Bundle.getString("pnl_tel_lbl_apoapsis"));
		pnlTelemetry.add(apoastroLabel);
		pnlTelemetry.add(apoastroValorLabel);

		JLabel periastroLabel = new JLabel(Bundle.getString("pnl_tel_lbl_periapsis"));
		pnlTelemetry.add(periastroLabel);
		pnlTelemetry.add(periastroValorLabel);

		JLabel velVLabel = new JLabel(Bundle.getString("pnl_tel_lbl_vert_spd"));
		pnlTelemetry.add(velVLabel);
		pnlTelemetry.add(velVValorLabel);

		JLabel velHLabel = new JLabel(Bundle.getString("pnl_tel_lbl_horz_spd"));
		pnlTelemetry.add(velHLabel);
		pnlTelemetry.add(velHValorLabel);

		setLayout(gl_pnlFunctions);
	}

	@Override
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

	public static void updateTelemetry(Map<Telemetry, Double> telemetryData) {
		synchronized (telemetryData) {
			for (Telemetry key : telemetryData.keySet()) {
				switch (key) {
					case ALTITUDE:
						altitudeValorLabel.setText(Utilities.convertToMetersMagnitudes(telemetryData.get(key)));
						break;
					case ALT_SURF:
						altitudeSupValorLabel.setText(Utilities.convertToMetersMagnitudes(telemetryData.get(key)));
						break;
					case APOAPSIS:
						apoastroValorLabel.setText(Utilities.convertToMetersMagnitudes(telemetryData.get(key)));
						break;
					case PERIAPSIS:
						periastroValorLabel.setText(Utilities.convertToMetersMagnitudes(telemetryData.get(key)));
						break;
					case VERT_SPEED:
						velVValorLabel.setText(Utilities.convertToMetersMagnitudes(telemetryData.get(key)) + "/s");
						break;
					case HORZ_SPEED:
						velHValorLabel.setText(Utilities.convertToMetersMagnitudes(telemetryData.get(key)) + "/s");
						break;
				}
			}
		}
	}

	protected void handleBtnDecolagemOrbitalActionPerformed(ActionEvent e) {
		MainGui.getCardJPanels().firePropertyChange(Modulos.MODULO_DECOLAGEM.get(), false, true);
	}

	protected void handleBtnPousoAutomaticoActionPerformed(ActionEvent e) {
		MainGui.getCardJPanels().firePropertyChange(Modulos.MODULO_POUSO.get(), false, true);
	}

	protected void handleBtnManobrasActionPerformed(ActionEvent e) {
		MainGui.getCardJPanels().firePropertyChange(Modulos.MODULO_MANOBRAS.get(), false, true);
	}

	protected void handleBtnPilotarRoverActionPerformed(ActionEvent e) {
		MainGui.getCardJPanels().firePropertyChange(Modulos.MODULO_ROVER.get(), false, true);
	}

}

package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Modulos;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import static com.pesterenan.views.MainGui.BTN_DIMENSION;
import static com.pesterenan.views.MainGui.MARGIN_BORDER_10_PX_LR;
import static com.pesterenan.views.MainGui.PNL_DIMENSION;

public class RoverJPanel extends JPanel implements UIMethods {

	private static final long serialVersionUID = 0L;

	private JLabel lblWaypointName, lblMaxSpeed;
	private JTextField txfWaypointName, txfMaxSpeed;
	private JButton btnBack, btnDrive;
	private ButtonGroup bgTargetChoice;
	private JRadioButton rbTargetVessel, rbWaypointOnMap;

	public RoverJPanel() {
		initComponents();
		setupComponents();
		layoutComponents();
	}

	@Override
	public void initComponents() {
		// Labels:
		lblMaxSpeed = new JLabel(Bundle.getString("pnl_rover_lbl_max_speed"));
		lblWaypointName = new JLabel(Bundle.getString("pnl_rover_waypoint_name"));

		// Textfields:
		txfMaxSpeed = new JTextField("10");
		txfWaypointName = new JTextField(Bundle.getString("pnl_rover_default_name"));

		// Buttons:
		btnBack = new JButton(Bundle.getString("pnl_rover_btn_back"));
		btnDrive = new JButton(Bundle.getString("pnl_rover_btn_drive"));

		// Misc:
		rbTargetVessel = new JRadioButton(Bundle.getString("pnl_rover_target_vessel"));
		rbWaypointOnMap = new JRadioButton(Bundle.getString("pnl_rover_waypoint_on_map"));
	}

	@Override
	public void setupComponents() {
		// Main Panel setup:
		setBorder(new TitledBorder(null, Bundle.getString("pnl_rover_border")));

		// Setting-up components:
		txfWaypointName.setEnabled(false);
		txfWaypointName.setHorizontalAlignment(SwingConstants.CENTER);
		txfWaypointName.setMaximumSize(BTN_DIMENSION);
		txfWaypointName.setPreferredSize(BTN_DIMENSION);
		txfMaxSpeed.setHorizontalAlignment(SwingConstants.RIGHT);
		txfMaxSpeed.setMaximumSize(BTN_DIMENSION);
		txfMaxSpeed.setPreferredSize(BTN_DIMENSION);

		rbTargetVessel.setSelected(true);
		rbTargetVessel.setActionCommand(Modulos.NAVE_ALVO.get());
		rbTargetVessel.addActionListener(this::handleTargetSelection);
		rbWaypointOnMap.setActionCommand(Modulos.MARCADOR_MAPA.get());
		rbWaypointOnMap.addActionListener(this::handleTargetSelection);

		bgTargetChoice = new ButtonGroup();
		bgTargetChoice.add(rbTargetVessel);
		bgTargetChoice.add(rbWaypointOnMap);

		btnBack.addActionListener(MainGui::backToTelemetry);
		btnBack.setMaximumSize(BTN_DIMENSION);
		btnBack.setPreferredSize(BTN_DIMENSION);
		btnDrive.addActionListener(this::handleDriveTo);
		btnDrive.setMaximumSize(BTN_DIMENSION);
		btnDrive.setPreferredSize(BTN_DIMENSION);
	}

	@Override
	public void layoutComponents() {
		// Main Panel layout:
		setPreferredSize(PNL_DIMENSION);
		setSize(PNL_DIMENSION);
		setLayout(new BorderLayout());

		// Layout components:
		JPanel pnlWaypointName = new JPanel();
		pnlWaypointName.setLayout(new BoxLayout(pnlWaypointName, BoxLayout.X_AXIS));
		pnlWaypointName.add(lblWaypointName);
		pnlWaypointName.add(Box.createHorizontalGlue());
		pnlWaypointName.add(txfWaypointName);

		JPanel pnlMaxSpeed = new JPanel();
		pnlMaxSpeed.setLayout(new BoxLayout(pnlMaxSpeed, BoxLayout.X_AXIS));
		pnlMaxSpeed.add(lblMaxSpeed);
		pnlMaxSpeed.add(Box.createHorizontalGlue());
		pnlMaxSpeed.add(txfMaxSpeed);

		JPanel pnlRoverControls = new JPanel();
		pnlRoverControls.setLayout(new BoxLayout(pnlRoverControls, BoxLayout.Y_AXIS));
		pnlRoverControls.setBorder(MARGIN_BORDER_10_PX_LR);
		pnlRoverControls.add(MainGui.createMarginComponent(0, 6));
		pnlRoverControls.add(pnlWaypointName);
		pnlRoverControls.add(pnlMaxSpeed);
		pnlRoverControls.add(Box.createVerticalGlue());

		JPanel pnlTargetChoice = new JPanel();
		pnlTargetChoice.setBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), Bundle.getString("pnl_rover_target_choice")));
		pnlTargetChoice.setLayout(new BoxLayout(pnlTargetChoice, BoxLayout.Y_AXIS));
		pnlTargetChoice.add(rbTargetVessel);
		pnlTargetChoice.add(rbWaypointOnMap);
		pnlTargetChoice.add(Box.createHorizontalGlue());

		JPanel pnlButtons = new JPanel();
		pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
		pnlButtons.add(btnDrive);
		pnlButtons.add(Box.createHorizontalGlue());
		pnlButtons.add(btnBack);

		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.X_AXIS));
		pnlRoverControls.setAlignmentY(TOP_ALIGNMENT);
		pnlTargetChoice.setAlignmentY(TOP_ALIGNMENT);
		pnlMain.add(pnlRoverControls);
		pnlMain.add(pnlTargetChoice);

		setLayout(new BorderLayout());
		add(pnlMain, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);
	}

	private void handleTargetSelection(ActionEvent e) {
		txfWaypointName.setEnabled(e.getSource().equals(rbWaypointOnMap));
	}

	private void handleDriveTo(ActionEvent e) {
		if (validateTextFields()) {
			Map<String, String> commands = new HashMap<>();
			commands.put(Modulos.MODULO.get(), Modulos.MODULO_ROVER.get());
			commands.put(Modulos.TIPO_ALVO_ROVER.get(), bgTargetChoice.getSelection().getActionCommand());
			commands.put(Modulos.NOME_MARCADOR.get(), txfWaypointName.getText());
			commands.put(Modulos.VELOCIDADE_MAX.get(), txfMaxSpeed.getText());
			MechPeste.newInstance().startModule(commands);
		}
	}

	private boolean validateTextFields() {
		try {
			if (Float.parseFloat(txfMaxSpeed.getText()) < 3) {
				throw new NumberFormatException();
			}
			if (txfWaypointName.getText().equals("")) {
				throw new IllegalArgumentException();
			}
		} catch (NumberFormatException e) {
			StatusJPanel.setStatusMessage(Bundle.getString("pnl_rover_max_speed_above_3"));
			return false;
		} catch (IllegalArgumentException e) {
			StatusJPanel.setStatusMessage(Bundle.getString("pnl_rover_waypoint_name_not_empty"));
			return false;
		}
		return true;
	}
}

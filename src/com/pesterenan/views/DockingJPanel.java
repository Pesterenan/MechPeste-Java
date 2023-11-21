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

public class DockingJPanel extends JPanel implements UIMethods {

	private static final long serialVersionUID = 0L;

	private JLabel lblMaxSpeed;
	private JTextField txfMaxSpeed;
	private JButton btnBack, btnStartDocking;

	public DockingJPanel() {
		initComponents();
		setupComponents();
		layoutComponents();
	}

	@Override
	public void initComponents() {
		// Labels:
		lblMaxSpeed = new JLabel(Bundle.getString("pnl_rover_lbl_max_speed"));

		// Textfields:
		txfMaxSpeed = new JTextField("3");

		// Buttons:
		btnBack = new JButton(Bundle.getString("pnl_rover_btn_back"));
		btnStartDocking = new JButton(Bundle.getString("pnl_rover_btn_docking"));
	}

	@Override
	public void setupComponents() {
		// Main Panel setup:
		setBorder(new TitledBorder(null, Bundle.getString("btn_func_docking")));

		// Setting-up components:
		txfMaxSpeed.setHorizontalAlignment(SwingConstants.RIGHT);
		txfMaxSpeed.setMaximumSize(BTN_DIMENSION);
		txfMaxSpeed.setPreferredSize(BTN_DIMENSION);

		btnBack.addActionListener(MainGui::backToTelemetry);
		btnBack.setMaximumSize(BTN_DIMENSION);
		btnBack.setPreferredSize(BTN_DIMENSION);
		btnStartDocking.addActionListener(this::handleStartDocking);
		btnStartDocking.setMaximumSize(BTN_DIMENSION);
		btnStartDocking.setPreferredSize(BTN_DIMENSION);
	}

	@Override
	public void layoutComponents() {
		// Main Panel layout:
		setPreferredSize(PNL_DIMENSION);
		setSize(PNL_DIMENSION);
		setLayout(new BorderLayout());

		// Layout components:
		JPanel pnlMaxSpeed = new JPanel();
		pnlMaxSpeed.setLayout(new BoxLayout(pnlMaxSpeed, BoxLayout.X_AXIS));
		pnlMaxSpeed.add(lblMaxSpeed);
		pnlMaxSpeed.add(Box.createHorizontalGlue());
		pnlMaxSpeed.add(txfMaxSpeed);

		JPanel pnlRoverControls = new JPanel();
		pnlRoverControls.setLayout(new BoxLayout(pnlRoverControls, BoxLayout.Y_AXIS));
		pnlRoverControls.setBorder(MARGIN_BORDER_10_PX_LR);
		pnlRoverControls.add(MainGui.createMarginComponent(0, 6));
		pnlRoverControls.add(pnlMaxSpeed);
		pnlRoverControls.add(Box.createVerticalGlue());

		JPanel pnlButtons = new JPanel();
		pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
		pnlButtons.add(btnStartDocking);
		pnlButtons.add(Box.createHorizontalGlue());
		pnlButtons.add(btnBack);

		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.X_AXIS));
		pnlRoverControls.setAlignmentY(TOP_ALIGNMENT);
		pnlMain.add(pnlRoverControls);

		setLayout(new BorderLayout());
		add(pnlMain, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);
	}

	private void handleStartDocking(ActionEvent e) {
		System.out.println("chamou startdocking");
		Map<String, String> commands = new HashMap<>();
		commands.put(Modulos.MODULO.get(), Modulos.MODULO_DOCKING.get());
		commands.put(Modulos.VELOCIDADE_MAX.get(), txfMaxSpeed.getText());
		MechPeste.newInstance().startModule(commands);
	}

	private boolean validateTextFields() {
		try {
			if (Float.parseFloat(txfMaxSpeed.getText()) > 10) {
				throw new NumberFormatException();
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

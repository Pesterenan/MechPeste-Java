package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Modulos;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import static com.pesterenan.views.MainGui.BTN_DIMENSION;
import static com.pesterenan.views.MainGui.MARGIN_BORDER_10_PX_LR;
import static com.pesterenan.views.MainGui.PNL_DIMENSION;

public class LandingJPanel extends JPanel implements UIMethods {
	private static final long serialVersionUID = 1L;
	private JLabel lblHoverAltitude, lblTWRLimit;
	private JTextField txfHover, txfMaxTWR;
	private JButton btnHover, btnAutoLanding, btnBack;
	private JCheckBox chkHoverAfterLanding;

	public LandingJPanel() {
		initComponents();
		setupComponents();
		layoutComponents();
	}

	@Override
	public void initComponents() {
		// Labels:
		lblHoverAltitude = new JLabel(Bundle.getString("pnl_land_lbl_alt"));
		lblTWRLimit = new JLabel(Bundle.getString("pnl_common_lbl_limit_twr"));

		// Textfields:
		txfHover = new JTextField("50");
		txfMaxTWR = new JTextField("5");

		// Buttons:
		btnHover = new JButton(Bundle.getString("pnl_land_btn_hover"));
		btnAutoLanding = new JButton(Bundle.getString("pnl_land_btn_land"));
		btnBack = new JButton(Bundle.getString("pnl_land_btn_back"));

		// Checkboxes:
		chkHoverAfterLanding = new JCheckBox(Bundle.getString("pnl_land_hover_checkbox"));
	}

	@Override
	public void setupComponents() {
		// Main Panel setup:
		setBorder(new TitledBorder(null, Bundle.getString("pnl_land_border"), TitledBorder.LEADING,
				TitledBorder.TOP, null, null));

		// Setting-up components:
		txfHover.setPreferredSize(BTN_DIMENSION);
		txfHover.setMaximumSize(BTN_DIMENSION);
		txfHover.setHorizontalAlignment(JTextField.RIGHT);
		txfMaxTWR.setPreferredSize(BTN_DIMENSION);
		txfMaxTWR.setMaximumSize(BTN_DIMENSION);
		txfMaxTWR.setHorizontalAlignment(JTextField.RIGHT);

		btnAutoLanding.addActionListener(this::handleLandingAction);
		btnAutoLanding.setActionCommand(Modulos.MODULO_POUSO.get());
		btnAutoLanding.setPreferredSize(BTN_DIMENSION);
		btnAutoLanding.setMaximumSize(BTN_DIMENSION);
		btnHover.addActionListener(this::handleLandingAction);
		btnHover.setActionCommand(Modulos.MODULO_POUSO_SOBREVOAR.get());
		btnHover.setPreferredSize(BTN_DIMENSION);
		btnHover.setMaximumSize(BTN_DIMENSION);
		btnBack.addActionListener(MainGui::backToTelemetry);
		btnBack.setPreferredSize(BTN_DIMENSION);
		btnBack.setMaximumSize(BTN_DIMENSION);
	}

	@Override
	public void layoutComponents() {
		// Main Panel layout:
		setPreferredSize(PNL_DIMENSION);
		setSize(PNL_DIMENSION);
		setLayout(new BorderLayout());

		// Laying out components:
		JPanel pnlHoverControls = new JPanel();
		pnlHoverControls.setBorder(MARGIN_BORDER_10_PX_LR);
		pnlHoverControls.setLayout(new BoxLayout(pnlHoverControls, BoxLayout.X_AXIS));
		pnlHoverControls.add(lblHoverAltitude);
		pnlHoverControls.add(Box.createHorizontalGlue());
		pnlHoverControls.add(txfHover);

		JPanel pnlTWRLimitControls = new JPanel();
		pnlTWRLimitControls.setBorder(MARGIN_BORDER_10_PX_LR);
		pnlTWRLimitControls.setLayout(new BoxLayout(pnlTWRLimitControls, BoxLayout.X_AXIS));
		pnlTWRLimitControls.add(lblTWRLimit);
		pnlTWRLimitControls.add(Box.createHorizontalGlue());
		pnlTWRLimitControls.add(txfMaxTWR);

		JPanel pnlLandingControls = new JPanel();
		pnlLandingControls.setLayout(new BoxLayout(pnlLandingControls, BoxLayout.X_AXIS));
		Border titledEtched = new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				Bundle.getString("pnl_land_lbl_land"), TitledBorder.LEADING, TitledBorder.TOP, null,
				new Color(0, 0, 0));
		Border combined = BorderFactory.createCompoundBorder(titledEtched, MARGIN_BORDER_10_PX_LR);
		pnlLandingControls.setBorder(combined);
		pnlLandingControls.add(btnAutoLanding);
		pnlLandingControls.add(Box.createHorizontalGlue());
		pnlLandingControls.add(btnHover);

		JPanel pnlControls = new JPanel();
		pnlControls.setLayout(new BoxLayout(pnlControls, BoxLayout.Y_AXIS));
		pnlControls.add(MainGui.createMarginComponent(0, 6));
		pnlControls.add(pnlHoverControls);
		pnlControls.add(pnlTWRLimitControls);
		pnlControls.add(pnlLandingControls);

		JPanel pnlOptions = new JPanel();
		pnlOptions.setLayout(new BoxLayout(pnlOptions, BoxLayout.Y_AXIS));
		pnlOptions.setBorder(new TitledBorder(Bundle.getString("pnl_lift_chk_options")));
		pnlOptions.add(chkHoverAfterLanding);
		pnlOptions.add(Box.createHorizontalGlue());

		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.X_AXIS));
		pnlControls.setAlignmentY(TOP_ALIGNMENT);
		pnlOptions.setAlignmentY(TOP_ALIGNMENT);
		pnlMain.add(pnlControls);
		pnlMain.add(pnlOptions);

		JPanel pnlBackbtn = new JPanel();
		pnlBackbtn.setLayout(new BoxLayout(pnlBackbtn, BoxLayout.X_AXIS));
		pnlBackbtn.add(Box.createHorizontalGlue());
		pnlBackbtn.add(btnBack);

		add(pnlMain, BorderLayout.CENTER);
		add(pnlBackbtn, BorderLayout.SOUTH);
	}

	private void handleLandingAction(ActionEvent e) {
		try {
			Map<String, String> commands = new HashMap<>();
			commands.put(Modulos.MODULO.get(), e.getActionCommand());
			validateTextFields();
			commands.put(Modulos.ALTITUDE_SOBREVOO.get(), txfHover.getText());
			commands.put(Modulos.MAX_TWR.get(), txfMaxTWR.getText());
			commands.put(Modulos.SOBREVOO_POS_POUSO.get(), String.valueOf(chkHoverAfterLanding.isSelected()));
			MechPeste.newInstance().startModule(commands);
		} catch (NumberFormatException nfe) {
			StatusJPanel.setStatusMessage(Bundle.getString("pnl_land_hover_alt_err"));
		} catch (NullPointerException npe) {
			StatusJPanel.setStatusMessage(Bundle.getString("pnl_land_hover_alt"));
		}
	}

	private void validateTextFields() throws NumberFormatException, NullPointerException {
		if (txfHover.getText().equals("")
				|| txfHover.getText().equals("0")
				|| txfMaxTWR.getText().equals("")
				|| txfMaxTWR.getText().equals("0")) {
			throw new NullPointerException();
		}
		Float.parseFloat(txfHover.getText());
		Float.parseFloat(txfMaxTWR.getText());
	}
}

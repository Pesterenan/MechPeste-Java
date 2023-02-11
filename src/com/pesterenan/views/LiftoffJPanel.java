package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Modulos;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import static com.pesterenan.views.MainGui.BTN_DIMENSION;
import static com.pesterenan.views.MainGui.MARGIN_BORDER_10_PX_LR;
import static com.pesterenan.views.MainGui.PNL_DIMENSION;

public class LiftoffJPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JLabel lblFinalApoapsis, lblHeading, lblRoll, lblCurveModel, lblLimitTWR;
	private JTextField txfFinalApoapsis, txfHeading, txfLimitTWR;
	private JButton btnLiftoff, btnBack;
	private JComboBox<String> cbGravityCurveModel;
	private JSlider sldRoll;
	private JCheckBox chkOpenPanels, chkDecoupleStages;

	public LiftoffJPanel() {
		initComponents();
		setupComponents();
		layoutComponents();
	}

	private void initComponents() {
		// Labels:
		lblFinalApoapsis = new JLabel(Bundle.getString("pnl_lift_lbl_final_apoapsis"));
		lblHeading = new JLabel(Bundle.getString("pnl_lift_lbl_direction"));
		lblRoll = new JLabel(Bundle.getString("pnl_lift_lbl_roll"));
		lblRoll.setToolTipText(Bundle.getString("pnl_lift_lbl_roll_tooltip"));
		lblCurveModel = new JLabel(Bundle.getString("pnl_lift_lbl_gravity_curve"));
		lblLimitTWR = new JLabel(Bundle.getString("pnl_lift_lbl_limit_twr"));

		// Textfields:
		txfFinalApoapsis = new JTextField("80000");
		txfFinalApoapsis.setToolTipText(Bundle.getString("pnl_lift_txf_final_apo_tooltip"));
		txfHeading = new JTextField("90");
		txfLimitTWR = new JTextField("1.5");

		// Buttons:
		btnLiftoff = new JButton(Bundle.getString("pnl_lift_btn_liftoff"));
		btnBack = new JButton(Bundle.getString("pnl_lift_btn_back"));

		// Checkboxes:
		chkOpenPanels = new JCheckBox(Bundle.getString("pnl_lift_chk_open_panels"));
		chkOpenPanels.setToolTipText(Bundle.getString("pnl_lift_chk_open_panels_tooltip"));
		chkDecoupleStages = new JCheckBox(Bundle.getString("pnl_lift_chk_staging"));
		chkDecoupleStages.setToolTipText(Bundle.getString("pnl_lift_chk_staging_tooltip"));

		// Misc:
		cbGravityCurveModel = new JComboBox<>();
		cbGravityCurveModel.setToolTipText(Bundle.getString("pnl_lift_cb_gravity_curve_tooltip"));
		cbGravityCurveModel.setModel(new DefaultComboBoxModel<>(
				new String[] { Modulos.SINUSOIDAL.get(), Modulos.QUADRATICA.get(), Modulos.CUBICA.get(),
						Modulos.CIRCULAR.get(), Modulos.EXPONENCIAL.get() }));

		sldRoll = new JSlider();
	}

	private void setupComponents() {
		// Main Panel setup:
		setBorder(new TitledBorder(null, Bundle.getString("pnl_lift_pnl_title"), TitledBorder.LEADING,
				TitledBorder.TOP, null, null));

		// Setting-up components:
		lblFinalApoapsis.setLabelFor(txfFinalApoapsis);
		txfFinalApoapsis.setMaximumSize(BTN_DIMENSION);
		txfFinalApoapsis.setPreferredSize(BTN_DIMENSION);
		txfFinalApoapsis.setHorizontalAlignment(JTextField.RIGHT);
		lblHeading.setLabelFor(txfHeading);
		txfHeading.setMaximumSize(BTN_DIMENSION);
		txfHeading.setPreferredSize(BTN_DIMENSION);
		txfHeading.setHorizontalAlignment(JTextField.RIGHT);
		lblLimitTWR.setLabelFor(txfLimitTWR);
		txfLimitTWR.setMaximumSize(BTN_DIMENSION);
		txfLimitTWR.setPreferredSize(BTN_DIMENSION);
		txfLimitTWR.setHorizontalAlignment(JTextField.RIGHT);
		
		cbGravityCurveModel.setSelectedIndex(3);
		cbGravityCurveModel.setPreferredSize(BTN_DIMENSION);
		cbGravityCurveModel.setMaximumSize(BTN_DIMENSION);

		sldRoll.setPaintLabels(true);
		sldRoll.setMajorTickSpacing(90);
		sldRoll.setMaximum(270);
		sldRoll.setSnapToTicks(true);
		sldRoll.setValue(90);
		sldRoll.setPreferredSize(new Dimension(110, 40));
		sldRoll.setMaximumSize(new Dimension(110, 40));

		chkDecoupleStages.setSelected(true);

		btnLiftoff.addActionListener(this);
		btnLiftoff.setPreferredSize(BTN_DIMENSION);
		btnLiftoff.setMaximumSize(BTN_DIMENSION);
		btnBack.addActionListener(this);
		btnBack.setPreferredSize(BTN_DIMENSION);
		btnBack.setMaximumSize(BTN_DIMENSION);
	}

	private void layoutComponents() {
		// Main Panel layout:
		setPreferredSize(PNL_DIMENSION);
		setSize(PNL_DIMENSION);
		setLayout(new BorderLayout());

		// Laying out components:
		JPanel pnlFinalApoapsis = new JPanel();
		pnlFinalApoapsis.setLayout(new BoxLayout(pnlFinalApoapsis, BoxLayout.X_AXIS));
		pnlFinalApoapsis.setBorder(MARGIN_BORDER_10_PX_LR);
		pnlFinalApoapsis.add(lblFinalApoapsis);
		pnlFinalApoapsis.add(Box.createHorizontalGlue());
		pnlFinalApoapsis.add(txfFinalApoapsis);

		JPanel pnlHeading = new JPanel();
		pnlHeading.setLayout(new BoxLayout(pnlHeading, BoxLayout.X_AXIS));
		pnlHeading.setBorder(MARGIN_BORDER_10_PX_LR);
		pnlHeading.add(lblHeading);
		pnlHeading.add(Box.createHorizontalGlue());
		pnlHeading.add(txfHeading);

		JPanel pnlRoll = new JPanel();
		pnlRoll.setLayout(new BoxLayout(pnlRoll, BoxLayout.X_AXIS));
		pnlRoll.setBorder(MARGIN_BORDER_10_PX_LR);
		pnlRoll.add(lblRoll);
		pnlRoll.add(Box.createHorizontalGlue());
		pnlRoll.add(sldRoll);

		JPanel pnlLimitTWR = new JPanel();
		pnlLimitTWR.setLayout(new BoxLayout(pnlLimitTWR, BoxLayout.X_AXIS));
		pnlLimitTWR.setBorder(MARGIN_BORDER_10_PX_LR);
		pnlLimitTWR.add(lblLimitTWR);
		pnlLimitTWR.add(Box.createHorizontalGlue());
		pnlLimitTWR.add(txfLimitTWR);

		JPanel pnlCurveModel = new JPanel();
		pnlCurveModel.setLayout(new BoxLayout(pnlCurveModel, BoxLayout.X_AXIS));
		pnlCurveModel.setBorder(MARGIN_BORDER_10_PX_LR);
		pnlCurveModel.add(lblCurveModel);
		pnlCurveModel.add(Box.createHorizontalGlue());
		pnlCurveModel.add(cbGravityCurveModel);

		JPanel pnlButtons = new JPanel();
		pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
		pnlButtons.add(btnLiftoff);
		pnlButtons.add(Box.createHorizontalGlue());
		pnlButtons.add(btnBack);

		JPanel pnlSetup = new JPanel();
		pnlSetup.setLayout(new BoxLayout(pnlSetup, BoxLayout.Y_AXIS));
		pnlSetup.add(pnlFinalApoapsis);
		pnlSetup.add(pnlHeading);
		pnlSetup.add(pnlRoll);
		pnlSetup.add(pnlLimitTWR);
		pnlSetup.add(pnlCurveModel);

		JPanel pnlOptions = new JPanel();
		pnlOptions.setLayout(new BoxLayout(pnlOptions, BoxLayout.Y_AXIS));
		pnlOptions.setBorder(new TitledBorder(Bundle.getString("pnl_lift_chk_options")));
		pnlOptions.add(chkDecoupleStages);
		pnlOptions.add(chkOpenPanels);

		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.X_AXIS));
		pnlMain.add(pnlSetup);
		pnlSetup.setAlignmentY(Component.TOP_ALIGNMENT);
		pnlMain.add(pnlOptions);
		pnlOptions.setAlignmentY(Component.TOP_ALIGNMENT);

		add(pnlMain, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);
	}

	private boolean validateTextFields() {
		try {
			Float.parseFloat(txfFinalApoapsis.getText());
			Float.parseFloat(txfHeading.getText());
			Float.parseFloat(txfLimitTWR.getText());
		} catch (NumberFormatException e) {
			StatusJPanel.setStatus(Bundle.getString("pnl_lift_stat_only_numbers"));
			return false;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnBack) {
			MainGui.backToTelemetry();
		}
		if (e.getSource() == btnLiftoff) {
			handleBtnLiftoffActionPerformed(e);
			MainGui.backToTelemetry();
		}
	}

	protected void handleBtnLiftoffActionPerformed(ActionEvent e) {
		if (validateTextFields()) {
			Map<String, String> commands = new HashMap<>();
			commands.put(Modulos.MODULO.get(), Modulos.MODULO_DECOLAGEM.get());
			commands.put(Modulos.APOASTRO.get(), txfFinalApoapsis.getText());
			commands.put(Modulos.DIRECAO.get(), txfHeading.getText());
			commands.put(Modulos.MAX_TWR.get(), txfLimitTWR.getText());
			commands.put(Modulos.ROLAGEM.get(), String.valueOf(sldRoll.getValue()));
			commands.put(Modulos.INCLINACAO.get(), cbGravityCurveModel.getSelectedItem().toString());
			commands.put(Modulos.USAR_ESTAGIOS.get(), String.valueOf(chkDecoupleStages.isSelected()));
			commands.put(Modulos.ABRIR_PAINEIS.get(), String.valueOf(chkOpenPanels.isSelected()));
			MechPeste.newInstance().startModule(commands);
		}
	}
}

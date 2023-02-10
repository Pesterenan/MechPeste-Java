package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Modulos;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import static com.pesterenan.views.MainGui.BTN_DIMENSION;
import static com.pesterenan.views.MainGui.dmsPanels;

public class LiftoffJPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JLabel lblFinalApoapsis, lblHeading, lblRoll, lblCurveModel;
	private JTextField txfFinalApoapsis, txfHeading;
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
		lblCurveModel = new JLabel(Bundle.getString("pnl_lift_lbl_gravity_curve"));

		// Textfields:
		txfFinalApoapsis = new JTextField("80000");
		txfHeading = new JTextField("90");

		// Buttons:
		btnLiftoff = new JButton(Bundle.getString("pnl_lift_btn_liftoff"));
		btnBack = new JButton(Bundle.getString("pnl_lift_btn_back"));

		// Misc:
		cbGravityCurveModel = new JComboBox<>();

		lblFinalApoapsis.setLabelFor(txfFinalApoapsis);
		txfFinalApoapsis.setMaximumSize(BTN_DIMENSION);
		txfFinalApoapsis.setPreferredSize(BTN_DIMENSION);
		txfFinalApoapsis.setHorizontalAlignment(JTextField.RIGHT);
		txfFinalApoapsis.setText("80000");
		txfFinalApoapsis.setToolTipText(Bundle.getString("pnl_lift_txf_final_apo_tooltip"));

		btnLiftoff.addActionListener(this);
		btnLiftoff.setSize(BTN_DIMENSION);
		btnLiftoff.setPreferredSize(btnLiftoff.getSize());
		btnLiftoff.setMinimumSize(btnLiftoff.getSize());
		btnLiftoff.setMaximumSize(btnLiftoff.getSize());
		btnBack.addActionListener(this);
		btnBack.setSize(BTN_DIMENSION);
		btnBack.setPreferredSize(btnBack.getSize());
		btnBack.setMinimumSize(btnBack.getSize());
		btnBack.setMaximumSize(btnBack.getSize());

		cbGravityCurveModel.setToolTipText(Bundle.getString("pnl_lift_cb_gravity_curve_tooltip"));

		cbGravityCurveModel.setModel(new DefaultComboBoxModel<>(
				new String[] { Modulos.SINUSOIDAL.get(), Modulos.QUADRATICA.get(), Modulos.CUBICA.get(),
						Modulos.CIRCULAR.get(), Modulos.EXPONENCIAL.get() }));
		cbGravityCurveModel.setSelectedIndex(3);

		lblRoll.setToolTipText(Bundle.getString("pnl_lift_lbl_roll_tooltip"));

		sldRoll = new JSlider();
		sldRoll.setPaintLabels(true);
		sldRoll.setMajorTickSpacing(90);
		sldRoll.setMaximum(270);
		sldRoll.setSnapToTicks(true);
		sldRoll.setValue(90);

		chkOpenPanels = new JCheckBox(Bundle.getString("pnl_lift_chk_open_panels"));
		chkOpenPanels.setToolTipText(Bundle.getString("pnl_lift_chk_open_panels_tooltip"));

		chkDecoupleStages = new JCheckBox(Bundle.getString("pnl_lift_chk_staging"));
		chkDecoupleStages.setSelected(true);
		chkDecoupleStages.setToolTipText(Bundle.getString("pnl_lift_chk_staging_tooltip"));

	}

	private void setupComponents() {
		// Main Panel setup:
		setBorder(new TitledBorder(null, Bundle.getString("pnl_lift_pnl_title"), TitledBorder.LEADING,
				TitledBorder.TOP, null, null));

		// Setting-up components:

	}

	private void layoutComponents() {
		// Main Panel layout:
		setPreferredSize(dmsPanels);
		setSize(dmsPanels);
		setLayout(new BorderLayout());

		// Laying out components:
		JPanel pnlFinalApoapsis = new JPanel();
		pnlFinalApoapsis.setLayout(new BoxLayout(pnlFinalApoapsis, BoxLayout.X_AXIS));
		pnlFinalApoapsis.add(lblFinalApoapsis);
		pnlFinalApoapsis.add(txfFinalApoapsis);

		JPanel pnlHeading = new JPanel();
		pnlHeading.setLayout(new BoxLayout(pnlHeading, BoxLayout.X_AXIS));
		pnlHeading.add(lblHeading);
		pnlHeading.add(txfHeading);

		JPanel pnlRoll = new JPanel();
		pnlRoll.setLayout(new BoxLayout(pnlRoll, BoxLayout.X_AXIS));
		pnlRoll.add(lblRoll);
		pnlRoll.add(sldRoll);

		JPanel pnlCurveModel = new JPanel();
		pnlCurveModel.setLayout(new BoxLayout(pnlCurveModel, BoxLayout.X_AXIS));
		pnlCurveModel.add(lblCurveModel);
		pnlCurveModel.add(cbGravityCurveModel);

		JPanel pnlButtons = new JPanel();
		pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
		pnlButtons.add(btnLiftoff);
		pnlButtons.add(Box.createHorizontalGlue());
		pnlButtons.add(btnBack);

		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
		pnlMain.add(pnlFinalApoapsis);
		pnlMain.add(pnlHeading);
		pnlMain.add(pnlRoll);
		pnlMain.add(pnlCurveModel);

		add(pnlMain, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);
	}

	private boolean validateTextFields() {
		try {
			Float.parseFloat(txfFinalApoapsis.getText());
			Float.parseFloat(txfHeading.getText());
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
			commands.put(Modulos.ROLAGEM.get(), String.valueOf(sldRoll.getValue()));
			commands.put(Modulos.INCLINACAO.get(), cbGravityCurveModel.getSelectedItem().toString());
			commands.put(Modulos.USAR_ESTAGIOS.get(), String.valueOf(chkDecoupleStages.isSelected()));
			commands.put(Modulos.ABRIR_PAINEIS.get(), String.valueOf(chkOpenPanels.isSelected()));
			MechPeste.newInstance().startModule(commands);
		}
	}
}

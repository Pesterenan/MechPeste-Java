package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Modulos;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import static com.pesterenan.views.MainGui.BTN_DIMENSION;
import static com.pesterenan.views.MainGui.MARGIN_BORDER_10_PX_LR;
import static com.pesterenan.views.MainGui.PNL_DIMENSION;

public class ManeuverJPanel extends JPanel implements ActionListener, UIMethods {
	private static final long serialVersionUID = 1L;

	private JLabel lblExecute, lblAdjustInc;
	private JButton btnLowerOrbit, btnApoapsis, btnPeriapsis, btnExecute, btnAdjustInc, btnBack;
	private JCheckBox chkFineAdjusment;

	public ManeuverJPanel() {
		initComponents();
		setupComponents();
		layoutComponents();
	}

	public void initComponents() {
		// Labels:
		lblAdjustInc = new JLabel(Bundle.getString("pnl_mnv_lbl_adj_inc"));
		lblExecute = new JLabel(Bundle.getString("pnl_mnv_lbl_exec_mnv"));

		// Buttons:
		btnAdjustInc = new JButton(Bundle.getString("pnl_mnv_btn_adj_inc"));
		btnApoapsis = new JButton(Bundle.getString("pnl_mnv_btn_apoapsis"));
		btnBack = new JButton(Bundle.getString("pnl_mnv_btn_back"));
		btnExecute = new JButton(Bundle.getString("pnl_mnv_btn_exec_mnv"));
		btnLowerOrbit = new JButton(Bundle.getString("pnl_mnv_btn_lower_orbit"));
		btnPeriapsis = new JButton(Bundle.getString("pnl_mnv_btn_periapsis"));

		// Misc:
		chkFineAdjusment = new JCheckBox(Bundle.getString("pnl_mnv_chk_adj_mnv_rcs"));
	}

	public void setupComponents() {
		// Main Panel setup:
		setBorder(new TitledBorder(null, Bundle.getString("pnl_mnv_border"), TitledBorder.LEADING, TitledBorder.TOP,
				null, null));

		// Setting-up components:
		btnAdjustInc.addActionListener(this);
		btnAdjustInc.setActionCommand(Modulos.AJUSTAR.get());
		btnAdjustInc.setEnabled(false);
		btnAdjustInc.setMaximumSize(BTN_DIMENSION);
		btnAdjustInc.setPreferredSize(BTN_DIMENSION);

		btnApoapsis.addActionListener(this);
		btnApoapsis.setActionCommand(Modulos.APOASTRO.get());
		btnApoapsis.setMaximumSize(BTN_DIMENSION);
		btnApoapsis.setPreferredSize(BTN_DIMENSION);

		btnBack.addActionListener(this);
		btnBack.setMaximumSize(BTN_DIMENSION);
		btnBack.setPreferredSize(BTN_DIMENSION);

		btnExecute.addActionListener(this);
		btnExecute.setActionCommand(Modulos.EXECUTAR.get());
		btnExecute.setMaximumSize(BTN_DIMENSION);
		btnExecute.setPreferredSize(BTN_DIMENSION);

		btnLowerOrbit.addActionListener(this);
		btnLowerOrbit.setActionCommand(Modulos.ORBITA_BAIXA.get());
		btnLowerOrbit.setMaximumSize(BTN_DIMENSION);
		btnLowerOrbit.setPreferredSize(BTN_DIMENSION);

		btnPeriapsis.addActionListener(this);
		btnPeriapsis.setActionCommand(Modulos.PERIASTRO.get());
		btnPeriapsis.setMaximumSize(BTN_DIMENSION);
		btnPeriapsis.setPreferredSize(BTN_DIMENSION);
	}

	public void layoutComponents() {
		// Main Panel layout:
		setPreferredSize(PNL_DIMENSION);
		setSize(PNL_DIMENSION);
		setLayout(new BorderLayout());

		JPanel pnlExecuteManeuver = new JPanel();
		pnlExecuteManeuver.setLayout(new BoxLayout(pnlExecuteManeuver, BoxLayout.X_AXIS));
		pnlExecuteManeuver.setBorder(MARGIN_BORDER_10_PX_LR);
		pnlExecuteManeuver.add(lblExecute);
		pnlExecuteManeuver.add(MainGui.createMarginComponent(10, 0));
		pnlExecuteManeuver.add(btnExecute);

		JPanel pnlAdjustInclination = new JPanel();
		pnlAdjustInclination.setLayout(new BoxLayout(pnlAdjustInclination, BoxLayout.X_AXIS));
		pnlAdjustInclination.setBorder(MARGIN_BORDER_10_PX_LR);
		pnlAdjustInclination.add(lblAdjustInc);
		pnlAdjustInclination.add(Box.createHorizontalGlue());
		pnlAdjustInclination.add(btnAdjustInc);

		JPanel pnlCircularize = new JPanel();
		pnlCircularize.setLayout(new BoxLayout(pnlCircularize, BoxLayout.X_AXIS));
		TitledBorder titled = new TitledBorder(null, Bundle.getString("pnl_mnv_circularize"), TitledBorder.LEADING,
				TitledBorder.TOP, null, null);
		CompoundBorder combined = new CompoundBorder(titled, MARGIN_BORDER_10_PX_LR);
		pnlCircularize.setBorder(combined);
		pnlCircularize.add(btnLowerOrbit);
		pnlCircularize.add(Box.createHorizontalGlue());
		pnlCircularize.add(btnApoapsis);
		pnlCircularize.add(btnPeriapsis);

		JPanel pnlSetup = new JPanel();
		pnlSetup.setLayout(new BoxLayout(pnlSetup, BoxLayout.Y_AXIS));
		pnlSetup.add(pnlExecuteManeuver);
		pnlSetup.add(pnlAdjustInclination);

		JPanel pnlOptions = new JPanel();
		pnlOptions.setLayout(new BoxLayout(pnlOptions, BoxLayout.Y_AXIS));
		pnlOptions.setBorder(new TitledBorder(Bundle.getString("pnl_lift_chk_options")));
		pnlOptions.add(chkFineAdjusment);

		JPanel pnlFunctions = new JPanel();
		pnlFunctions.setLayout(new BoxLayout(pnlFunctions, BoxLayout.X_AXIS));
		pnlFunctions.add(pnlSetup);
		pnlFunctions.add(pnlOptions);

		JPanel pnlButtons = new JPanel();
		pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
		pnlButtons.add(Box.createHorizontalGlue());
		pnlButtons.add(btnBack);

		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
		pnlFunctions.setAlignmentY(TOP_ALIGNMENT);
		pnlMain.add(pnlFunctions);
		pnlCircularize.setAlignmentY(TOP_ALIGNMENT);
		pnlMain.add(pnlCircularize);

		add(pnlMain, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnExecute) {
			handleManeuverFunction(Modulos.EXECUTAR.get());
		}
		if (e.getSource() == btnLowerOrbit) {
			handleManeuverFunction(Modulos.ORBITA_BAIXA.get());
		}
		if (e.getSource() == btnApoapsis) {
			handleManeuverFunction(Modulos.APOASTRO.get());
		}
		if (e.getSource() == btnPeriapsis) {
			handleManeuverFunction(Modulos.PERIASTRO.get());
		}
		if (e.getSource() == btnAdjustInc) {
			handleManeuverFunction(Modulos.AJUSTAR.get());
		}
		if (e.getSource() == btnBack) {
			MainGui.backToTelemetry(e);
		}
	}

	protected void handleManeuverFunction(String maneuverFunction) {
		Map<String, String> commands = new HashMap<>();
		commands.put(Modulos.MODULO.get(), Modulos.MODULO_MANOBRAS.get());
		commands.put(Modulos.FUNCAO.get(), maneuverFunction);
		commands.put(Modulos.AJUSTE_FINO.get(), String.valueOf(chkFineAdjusment.isSelected()));
		MechPeste.newInstance().startModule(commands);
	}
}

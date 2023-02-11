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

public class ManeuverJPanel extends JPanel implements ActionListener, JPanelDesignPattern {
	private static final long serialVersionUID = 1L;

	private JLabel lblExecute, lblAdjustInc;
	private JButton btnApoapsis, btnPeriapsis, btnExecute, btnAdjustInc, btnBack;
	private JCheckBox chkFineAdjusment;

	public ManeuverJPanel() {
		initComponents();
		setupComponents();
		layoutComponents();
	}

	public void initComponents() {
		// Labels:
		lblExecute = new JLabel(Bundle.getString("pnl_mnv_lbl_exec_mnv"));
		lblAdjustInc = new JLabel(Bundle.getString("pnl_mnv_lbl_adj_inc"));

		// Buttons:
		btnApoapsis = new JButton(Bundle.getString("pnl_mnv_btn_apoapsis"));
		btnPeriapsis = new JButton(Bundle.getString("pnl_mnv_btn_periapsis"));
		btnExecute = new JButton(Bundle.getString("pnl_mnv_btn_exec_mnv"));
		btnAdjustInc = new JButton(Bundle.getString("pnl_mnv_btn_adj_inc"));
		btnBack = new JButton(Bundle.getString("pnl_mnv_btn_back"));

		// Misc:
		chkFineAdjusment = new JCheckBox(Bundle.getString("pnl_mnv_chk_adj_mnv_rcs"));
	}

	public void setupComponents() {
		// Main Panel setup:
		setBorder(new TitledBorder(null, Bundle.getString("pnl_mnv_border"), TitledBorder.LEADING, TitledBorder.TOP,
				null, null));

		// Setting-up components:
		btnExecute.addActionListener(this);
		btnExecute.setPreferredSize(BTN_DIMENSION);
		btnExecute.setMaximumSize(BTN_DIMENSION);
		btnExecute.setActionCommand(Modulos.EXECUTAR.get());

		btnAdjustInc.addActionListener(this);
		btnAdjustInc.setPreferredSize(BTN_DIMENSION);
		btnAdjustInc.setMaximumSize(BTN_DIMENSION);
		btnAdjustInc.setActionCommand(Modulos.AJUSTAR.get());
		btnAdjustInc.setEnabled(false);

		btnApoapsis.addActionListener(this);
		btnApoapsis.setPreferredSize(BTN_DIMENSION);
		btnApoapsis.setMaximumSize(BTN_DIMENSION);
		btnApoapsis.setActionCommand(Modulos.APOASTRO.get());
		
		btnPeriapsis.addActionListener(this);
		btnPeriapsis.setPreferredSize(BTN_DIMENSION);
		btnPeriapsis.setMaximumSize(BTN_DIMENSION);
		btnPeriapsis.setActionCommand(Modulos.PERIASTRO.get());
		
		btnBack.addActionListener(this);
		btnBack.setPreferredSize(BTN_DIMENSION);
		btnBack.setMaximumSize(BTN_DIMENSION);
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
		pnlCircularize.add(btnApoapsis);
		pnlCircularize.add(btnPeriapsis);

		JPanel pnlSetup = new JPanel();
		pnlSetup.setLayout(new BoxLayout(pnlSetup, BoxLayout.Y_AXIS));
		pnlSetup.add(pnlExecuteManeuver);
		pnlSetup.add(pnlAdjustInclination);
		pnlSetup.add(pnlCircularize);

		JPanel pnlOptions = new JPanel();
		pnlOptions.setLayout(new BoxLayout(pnlOptions, BoxLayout.Y_AXIS));
		pnlOptions.setBorder(new TitledBorder(Bundle.getString("pnl_lift_chk_options")));
		pnlOptions.add(chkFineAdjusment);

		JPanel pnlButtons = new JPanel();
		pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
		pnlButtons.add(Box.createHorizontalGlue());
		pnlButtons.add(btnBack);

		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.X_AXIS));
		pnlSetup.setAlignmentY(TOP_ALIGNMENT);
		pnlMain.add(pnlSetup);
		pnlOptions.setAlignmentY(TOP_ALIGNMENT);
		pnlMain.add(pnlOptions);

		add(pnlMain, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnExecute) {
			handleManeuverFunction(Modulos.EXECUTAR.get());
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
			MainGui.backToTelemetry();
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

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

public class LiftoffJPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private final JLabel lblFinalApoapsis = new JLabel(Bundle.getString("pnl_lift_lbl_final_apoapsis"));
	// $NON-NLS-1$
	private final JLabel lblHeading = new JLabel(Bundle.getString("pnl_lift_lbl_direction"));
	// $NON-NLS-1$
	private final JLabel lblRoll = new JLabel(Bundle.getString("pnl_lift_lbl_roll")); //$NON-NLS-1$
	private final JLabel lblCurveModel = new JLabel(Bundle.getString("pnl_lift_lbl_gravity_curve"));
	// $NON-NLS-1$
	private final JTextField txfFinalApoapsis = new JTextField("80000"); //$NON-NLS-1$
	private final JTextField txfHeading = new JTextField("90"); //$NON-NLS-1$
	private final JButton btnLiftoff = new JButton(Bundle.getString("pnl_lift_btn_liftoff"));
	// $NON-NLS-1$
	private final JButton btnBack = new JButton(Bundle.getString("pnl_lift_btn_back")); //$NON-NLS-1$
	private final JComboBox<String> cbGravityCurveModel = new JComboBox<>();
	private JSlider sldRoll;
	private JCheckBox chkOpenPanels;
	private JCheckBox chkDecoupleStages;

	public LiftoffJPanel() {
		initComponents();
	}

	private void initComponents() {
		setPreferredSize(new Dimension(464, 216));
		setSize(new Dimension(464, 216));
		setBorder(new TitledBorder(null, Bundle.getString("pnl_lift_pnl_title"), TitledBorder.LEADING,
		                           TitledBorder.TOP,
		                           // $NON-NLS-1$
		                           null, null
		));
		lblFinalApoapsis.setLabelFor(txfFinalApoapsis);
		txfFinalApoapsis.setText("80000"); //$NON-NLS-1$
		txfFinalApoapsis.setToolTipText(Bundle.getString("pnl_lift_txf_final_apo_tooltip")); // $NON
		// -NLS-1$

		btnLiftoff.addActionListener(this);
		btnLiftoff.setSize(ParametersJPanel.BTN_DIMENSION);
		btnLiftoff.setPreferredSize(btnLiftoff.getSize());
		btnLiftoff.setMinimumSize(btnLiftoff.getSize());
		btnLiftoff.setMaximumSize(btnLiftoff.getSize());
		btnBack.addActionListener(this);
		btnBack.setSize(ParametersJPanel.BTN_DIMENSION);
		btnBack.setPreferredSize(btnBack.getSize());
		btnBack.setMinimumSize(btnBack.getSize());
		btnBack.setMaximumSize(btnBack.getSize());

		cbGravityCurveModel.setToolTipText(Bundle.getString("pnl_lift_cb_gravity_curve_tooltip"));
		// $NON-NLS-1$
		cbGravityCurveModel.setModel(new DefaultComboBoxModel<>(
				new String[]{ Modulos.SINUSOIDAL.get(), Modulos.QUADRATICA.get(), Modulos.CUBICA.get(),
						Modulos.CIRCULAR.get(), Modulos.EXPONENCIAL.get() }));
		cbGravityCurveModel.setSelectedIndex(3);

		lblRoll.setToolTipText(Bundle.getString("pnl_lift_lbl_roll_tooltip")); //$NON-NLS-1$

		sldRoll = new JSlider();
		sldRoll.setPaintLabels(true);
		sldRoll.setMajorTickSpacing(90);
		sldRoll.setMaximum(270);
		sldRoll.setSnapToTicks(true);
		sldRoll.setValue(90);

		chkOpenPanels = new JCheckBox(Bundle.getString("pnl_lift_chk_open_panels")); //$NON-NLS-1$
		chkOpenPanels.setToolTipText(Bundle.getString("pnl_lift_chk_open_panels_tooltip")); //$NON-NLS-1$

		chkDecoupleStages = new JCheckBox(Bundle.getString("pnl_lift_chk_staging")); //$NON-NLS-1$
		chkDecoupleStages.setSelected(true);
		chkDecoupleStages.setToolTipText(Bundle.getString("pnl_lift_chk_staging_tooltip")); //$NON-NLS-1$

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
		                                          .addGroup(groupLayout.createSequentialGroup()
		                                                               .addGroup(groupLayout.createParallelGroup(
				                                                                                    Alignment.LEADING)
		                                                                                    .addGroup(
				                                                                                    groupLayout.createSequentialGroup()
				                                                                                               .addGroup(
						                                                                                               groupLayout.createParallelGroup(
								                                                                                                          Alignment.LEADING)
						                                                                                                          .addGroup(
								                                                                                                          groupLayout.createSequentialGroup()
								                                                                                                                     .addGroup(
										                                                                                                                     groupLayout.createParallelGroup(
												                                                                                                                                Alignment.LEADING)
										                                                                                                                                .addGroup(
												                                                                                                                                groupLayout.createSequentialGroup()
												                                                                                                                                           .addContainerGap()
												                                                                                                                                           .addGroup(
														                                                                                                                                           groupLayout.createParallelGroup(
																                                                                                                                                                      Alignment.TRAILING,
																                                                                                                                                                      false
														                                                                                                                                                                          )
														                                                                                                                                                      .addComponent(
																                                                                                                                                                      lblHeading,
																                                                                                                                                                      Alignment.LEADING
														                                                                                                                                                                   )
														                                                                                                                                                      .addComponent(
																                                                                                                                                                      lblFinalApoapsis,
																                                                                                                                                                      Alignment.LEADING,
																                                                                                                                                                      GroupLayout.DEFAULT_SIZE,
																                                                                                                                                                      GroupLayout.DEFAULT_SIZE,
																                                                                                                                                                      Short.MAX_VALUE
														                                                                                                                                                                   )))
										                                                                                                                                .addComponent(
												                                                                                                                                btnLiftoff,
												                                                                                                                                GroupLayout.PREFERRED_SIZE,
												                                                                                                                                GroupLayout.DEFAULT_SIZE,
												                                                                                                                                GroupLayout.PREFERRED_SIZE
										                                                                                                                                             ))
								                                                                                                                     .addGap(72))
						                                                                                                          .addGroup(
								                                                                                                          groupLayout.createSequentialGroup()
								                                                                                                                     .addContainerGap()
								                                                                                                                     .addComponent(
										                                                                                                                     lblRoll,
										                                                                                                                     GroupLayout.DEFAULT_SIZE,
										                                                                                                                     168,
										                                                                                                                     Short.MAX_VALUE
								                                                                                                                                  )
								                                                                                                                     .addPreferredGap(
										                                                                                                                     ComponentPlacement.RELATED)))
				                                                                                               .addGroup(
						                                                                                               groupLayout.createParallelGroup(
								                                                                                                          Alignment.LEADING)
						                                                                                                          .addGroup(
								                                                                                                          groupLayout.createParallelGroup(
										                                                                                                                     Alignment.LEADING,
										                                                                                                                     false
								                                                                                                                                         )
								                                                                                                                     .addComponent(
										                                                                                                                     txfHeading,
										                                                                                                                     Alignment.TRAILING
								                                                                                                                                  )
								                                                                                                                     .addComponent(
										                                                                                                                     txfFinalApoapsis,
										                                                                                                                     Alignment.TRAILING,
										                                                                                                                     GroupLayout.DEFAULT_SIZE,
										                                                                                                                     100,
										                                                                                                                     Short.MAX_VALUE
								                                                                                                                                  ))
						                                                                                                          .addComponent(
								                                                                                                          btnBack,
								                                                                                                          GroupLayout.PREFERRED_SIZE,
								                                                                                                          GroupLayout.DEFAULT_SIZE,
								                                                                                                          GroupLayout.PREFERRED_SIZE
						                                                                                                                       )
						                                                                                                          .addComponent(
								                                                                                                          sldRoll,
								                                                                                                          GroupLayout.PREFERRED_SIZE,
								                                                                                                          100,
								                                                                                                          GroupLayout.PREFERRED_SIZE
						                                                                                                                       )))
		                                                                                    .addGroup(
				                                                                                    groupLayout.createSequentialGroup()
				                                                                                               .addContainerGap()
				                                                                                               .addComponent(
						                                                                                               lblCurveModel)
				                                                                                               .addGap(22)
				                                                                                               .addComponent(
						                                                                                               cbGravityCurveModel,
						                                                                                               GroupLayout.PREFERRED_SIZE,
						                                                                                               100,
						                                                                                               GroupLayout.PREFERRED_SIZE
				                                                                                                            )
				                                                                                               .addGap(0,
				                                                                                                       0,
				                                                                                                       Short.MAX_VALUE
				                                                                                                      ))
		                                                                                    .addGroup(
				                                                                                    groupLayout.createSequentialGroup()
				                                                                                               .addContainerGap()
				                                                                                               .addComponent(
						                                                                                               chkOpenPanels))
		                                                                                    .addGroup(
				                                                                                    groupLayout.createSequentialGroup()
				                                                                                               .addContainerGap()
				                                                                                               .addComponent(
						                                                                                               chkDecoupleStages,
						                                                                                               GroupLayout.PREFERRED_SIZE,
						                                                                                               247,
						                                                                                               GroupLayout.PREFERRED_SIZE
				                                                                                                            )))
		                                                               .addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
		                                        .addGroup(groupLayout.createSequentialGroup()
		                                                             .addGroup(groupLayout.createParallelGroup(
				                                                                                  Alignment.BASELINE)
		                                                                                  .addComponent(
				                                                                                  txfFinalApoapsis,
				                                                                                  GroupLayout.PREFERRED_SIZE,
				                                                                                  GroupLayout.DEFAULT_SIZE,
				                                                                                  GroupLayout.PREFERRED_SIZE
		                                                                                               )
		                                                                                  .addComponent(
				                                                                                  lblFinalApoapsis))
		                                                             .addGap(3)
		                                                             .addGroup(groupLayout.createParallelGroup(
				                                                                                  Alignment.BASELINE)
		                                                                                  .addComponent(lblHeading)
		                                                                                  .addComponent(txfHeading,
		                                                                                                GroupLayout.PREFERRED_SIZE,
		                                                                                                GroupLayout.DEFAULT_SIZE,
		                                                                                                GroupLayout.PREFERRED_SIZE
		                                                                                               ))
		                                                             .addPreferredGap(ComponentPlacement.RELATED)
		                                                             .addGroup(groupLayout.createParallelGroup(
				                                                                                  Alignment.LEADING)
		                                                                                  .addComponent(lblRoll)
		                                                                                  .addComponent(sldRoll,
		                                                                                                GroupLayout.PREFERRED_SIZE,
		                                                                                                30,
		                                                                                                GroupLayout.PREFERRED_SIZE
		                                                                                               ))
		                                                             .addPreferredGap(ComponentPlacement.RELATED)
		                                                             .addGroup(groupLayout.createParallelGroup(
				                                                                                  Alignment.BASELINE)
		                                                                                  .addComponent(
				                                                                                  cbGravityCurveModel,
				                                                                                  GroupLayout.PREFERRED_SIZE,
				                                                                                  GroupLayout.DEFAULT_SIZE,
				                                                                                  GroupLayout.PREFERRED_SIZE
		                                                                                               )
		                                                                                  .addComponent(lblCurveModel))
		                                                             .addPreferredGap(ComponentPlacement.UNRELATED)
		                                                             .addComponent(chkOpenPanels)
		                                                             .addPreferredGap(ComponentPlacement.RELATED)
		                                                             .addComponent(chkDecoupleStages)
		                                                             .addGap(6)
		                                                             .addGroup(groupLayout.createParallelGroup(
				                                                                                  Alignment.BASELINE)
		                                                                                  .addComponent(btnBack,
		                                                                                                GroupLayout.PREFERRED_SIZE,
		                                                                                                GroupLayout.DEFAULT_SIZE,
		                                                                                                GroupLayout.PREFERRED_SIZE
		                                                                                               )
		                                                                                  .addComponent(btnLiftoff,
		                                                                                                GroupLayout.PREFERRED_SIZE,
		                                                                                                GroupLayout.DEFAULT_SIZE,
		                                                                                                GroupLayout.PREFERRED_SIZE
		                                                                                               ))
		                                                             .addGap(37)));
		setLayout(groupLayout);
	}

	private boolean validateTextFields() {
		try {
			Float.parseFloat(txfFinalApoapsis.getText());
			Float.parseFloat(txfHeading.getText());
		} catch (NumberFormatException e) {
			StatusJPanel.setStatus(Bundle.getString("pnl_lift_stat_only_numbers")); //$NON-NLS-1$
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
			MechPeste.startModule(-1, commands);
		}
	}
}

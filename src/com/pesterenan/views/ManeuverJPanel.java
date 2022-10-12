package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Modulos;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class ManeuverJPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private final JLabel lblExecute = new JLabel(Bundle.getString("pnl_mnv_lbl_exec_mnv"));
	//$NON-NLS-1$
	private final JLabel lblAdjustInc = new JLabel(Bundle.getString("pnl_mnv_lbl_adj_inc"));
	//$NON-NLS-1$
	private final JButton btnApoapsis = new JButton(Bundle.getString("pnl_mnv_btn_apoapsis"));
	//$NON-NLS-1$
	private final JButton btnPeriapsis = new JButton(Bundle.getString("pnl_mnv_btn_periapsis"));
	//$NON-NLS-1$
	private final JButton btnExecute = new JButton(Bundle.getString("pnl_mnv_btn_exec_mnv"));
	//$NON-NLS-1$
	private final JButton btnAdjustInc = new JButton(Bundle.getString("pnl_mnv_btn_adj_inc"));
	//$NON-NLS-1$
	private final JButton btnBack = new JButton(Bundle.getString("pnl_mnv_btn_back")); //$NON-NLS-1$
	private final JPanel pnlCircularize = new JPanel();
	private final JCheckBox chkFineAdjusment = new JCheckBox(Bundle.getString("pnl_mnv_chk_adj_mnv_rcs"));
//$NON-NLS-1$

	public ManeuverJPanel() {
		initComponents();
	}

	private void initComponents() {
		setPreferredSize(ParametersJPanel.dmsParameters);
		setSize(ParametersJPanel.dmsParameters);
		setBorder(new TitledBorder(null, Bundle.getString("pnl_mnv_border"), TitledBorder.LEADING, TitledBorder.TOP,
		                           //$NON-NLS-1$
		                           null, null
		));
		btnExecute.addActionListener(this);
		btnExecute.setSize(ParametersJPanel.BTN_DIMENSION);
		btnExecute.setPreferredSize(btnExecute.getSize());
		btnExecute.setMinimumSize(btnExecute.getSize());
		btnExecute.setMaximumSize(btnExecute.getSize());
		btnExecute.setActionCommand(Modulos.EXECUTAR.get());
		btnAdjustInc.addActionListener(this);
		btnAdjustInc.setSize(ParametersJPanel.BTN_DIMENSION);
		btnAdjustInc.setPreferredSize(btnAdjustInc.getSize());
		btnAdjustInc.setMinimumSize(btnAdjustInc.getSize());
		btnAdjustInc.setMaximumSize(btnAdjustInc.getSize());
		btnAdjustInc.setActionCommand(Modulos.AJUSTAR.get());
		btnBack.addActionListener(this);

		btnBack.setSize(ParametersJPanel.BTN_DIMENSION);
		btnBack.setPreferredSize(btnBack.getSize());
		btnBack.setMinimumSize(btnBack.getSize());
		btnBack.setMaximumSize(btnBack.getSize());

		pnlCircularize.setBorder(
				new TitledBorder(null, Bundle.getString("pnl_mnv_circularize"), TitledBorder.LEADING, //$NON-NLS-1$
				                 TitledBorder.TOP, null, null
				));

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
		                                          .addGroup(groupLayout.createSequentialGroup()
		                                                               .addGap(10)
		                                                               .addComponent(lblAdjustInc)
		                                                               .addGap(84)
		                                                               .addComponent(btnAdjustInc,
		                                                                             GroupLayout.PREFERRED_SIZE,
		                                                                             GroupLayout.DEFAULT_SIZE,
		                                                                             GroupLayout.PREFERRED_SIZE
		                                                                            )
		                                                               .addContainerGap())
		                                          .addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
		                                                                                   .addGroup(
				                                                                                   groupLayout.createParallelGroup(
						                                                                                              Alignment.TRAILING)
				                                                                                              .addGroup(
						                                                                                              groupLayout.createSequentialGroup()
						                                                                                                         .addContainerGap()
						                                                                                                         .addComponent(
								                                                                                                         btnBack,
								                                                                                                         GroupLayout.PREFERRED_SIZE,
								                                                                                                         GroupLayout.DEFAULT_SIZE,
								                                                                                                         GroupLayout.PREFERRED_SIZE
						                                                                                                                      ))
				                                                                                              .addGroup(
						                                                                                              groupLayout.createParallelGroup(
								                                                                                                         Alignment.TRAILING)
						                                                                                                         .addGroup(
								                                                                                                         Alignment.LEADING,
								                                                                                                         groupLayout.createSequentialGroup()
								                                                                                                                    .addGap(5)
								                                                                                                                    .addComponent(
										                                                                                                                    pnlCircularize,
										                                                                                                                    GroupLayout.DEFAULT_SIZE,
										                                                                                                                    283,
										                                                                                                                    Short.MAX_VALUE
								                                                                                                                                 )
						                                                                                                                  )
						                                                                                                         .addGroup(
								                                                                                                         Alignment.LEADING,
								                                                                                                         groupLayout.createSequentialGroup()
								                                                                                                                    .addGap(10)
								                                                                                                                    .addGroup(
										                                                                                                                    groupLayout.createParallelGroup(
												                                                                                                                               Alignment.LEADING)
										                                                                                                                               .addComponent(
												                                                                                                                               chkFineAdjusment)
										                                                                                                                               .addGroup(
												                                                                                                                               groupLayout.createSequentialGroup()
												                                                                                                                                          .addComponent(
														                                                                                                                                          lblExecute)
												                                                                                                                                          .addPreferredGap(
														                                                                                                                                          ComponentPlacement.RELATED,
														                                                                                                                                          36,
														                                                                                                                                          Short.MAX_VALUE
												                                                                                                                                                          )
												                                                                                                                                          .addComponent(
														                                                                                                                                          btnExecute,
														                                                                                                                                          GroupLayout.PREFERRED_SIZE,
														                                                                                                                                          GroupLayout.DEFAULT_SIZE,
														                                                                                                                                          GroupLayout.PREFERRED_SIZE
												                                                                                                                                                       )))
						                                                                                                                  )))
		                                                                                   .addGap(5)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
		                                        .addGroup(groupLayout.createSequentialGroup()
		                                                             .addGap(5)
		                                                             .addGroup(groupLayout.createParallelGroup(
				                                                                                  Alignment.BASELINE)
		                                                                                  .addComponent(lblExecute)
		                                                                                  .addComponent(btnExecute,
		                                                                                                GroupLayout.PREFERRED_SIZE,
		                                                                                                GroupLayout.DEFAULT_SIZE,
		                                                                                                GroupLayout.PREFERRED_SIZE
		                                                                                               ))
		                                                             .addPreferredGap(ComponentPlacement.RELATED)
		                                                             .addComponent(chkFineAdjusment)
		                                                             .addPreferredGap(ComponentPlacement.RELATED)
		                                                             .addComponent(pnlCircularize,
		                                                                           GroupLayout.PREFERRED_SIZE, 50,
		                                                                           GroupLayout.PREFERRED_SIZE
		                                                                          )
		                                                             .addPreferredGap(ComponentPlacement.RELATED)
		                                                             .addGroup(groupLayout.createParallelGroup(
				                                                                                  Alignment.BASELINE)
		                                                                                  .addComponent(lblAdjustInc)
		                                                                                  .addComponent(btnAdjustInc,
		                                                                                                GroupLayout.PREFERRED_SIZE,
		                                                                                                GroupLayout.DEFAULT_SIZE,
		                                                                                                GroupLayout.PREFERRED_SIZE
		                                                                                               ))
		                                                             .addGap(25)
		                                                             .addComponent(btnBack, GroupLayout.PREFERRED_SIZE,
		                                                                           GroupLayout.DEFAULT_SIZE,
		                                                                           GroupLayout.PREFERRED_SIZE
		                                                                          )
		                                                             .addContainerGap()));
		btnApoapsis.addActionListener(this);
		btnApoapsis.setSize(ParametersJPanel.BTN_DIMENSION);
		btnApoapsis.setPreferredSize(btnApoapsis.getSize());
		btnApoapsis.setMinimumSize(btnApoapsis.getSize());
		btnApoapsis.setMaximumSize(btnApoapsis.getSize());
		btnApoapsis.setActionCommand(Modulos.APOASTRO.get());
		btnPeriapsis.addActionListener(this);
		btnPeriapsis.setSize(ParametersJPanel.BTN_DIMENSION);
		btnPeriapsis.setPreferredSize(btnPeriapsis.getSize());
		btnPeriapsis.setMinimumSize(btnPeriapsis.getSize());
		btnPeriapsis.setMaximumSize(btnPeriapsis.getSize());
		btnPeriapsis.setActionCommand(Modulos.PERIASTRO.get());
		GroupLayout glPanel = new GroupLayout(pnlCircularize);
		glPanel.setHorizontalGroup(glPanel.createParallelGroup(Alignment.LEADING)
		                                  .addGroup(glPanel.createSequentialGroup()
		                                                   .addContainerGap()
		                                                   .addComponent(btnApoapsis, GroupLayout.PREFERRED_SIZE,
		                                                                 GroupLayout.DEFAULT_SIZE,
		                                                                 GroupLayout.PREFERRED_SIZE
		                                                                )
		                                                   .addPreferredGap(ComponentPlacement.RELATED, 30,
		                                                                    Short.MAX_VALUE
		                                                                   )
		                                                   .addComponent(btnPeriapsis, GroupLayout.PREFERRED_SIZE,
		                                                                 GroupLayout.DEFAULT_SIZE,
		                                                                 GroupLayout.PREFERRED_SIZE
		                                                                )
		                                                   .addContainerGap()));
		glPanel.setVerticalGroup(glPanel.createParallelGroup(Alignment.LEADING)
		                                .addGroup(glPanel.createSequentialGroup()
		                                                 .addGroup(glPanel.createParallelGroup(Alignment.BASELINE)
		                                                                  .addComponent(btnApoapsis,
		                                                                                GroupLayout.PREFERRED_SIZE,
		                                                                                GroupLayout.DEFAULT_SIZE,
		                                                                                GroupLayout.PREFERRED_SIZE
		                                                                               )
		                                                                  .addComponent(btnPeriapsis,
		                                                                                GroupLayout.PREFERRED_SIZE,
		                                                                                GroupLayout.DEFAULT_SIZE,
		                                                                                GroupLayout.PREFERRED_SIZE
		                                                                               ))
		                                                 .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		pnlCircularize.setLayout(glPanel);
		setLayout(groupLayout);
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
			handleBtnVoltarActionPerformed(e);
		}
	}

	protected void handleBtnVoltarActionPerformed(ActionEvent e) {
		MainGui.getParametros().firePropertyChange("Telemetria", false, true);
	}

	protected void handleManeuverFunction(String maneuverFunction) {
		Map<String, String> commands = new HashMap<>();
		commands.put(Modulos.MODULO.get(), Modulos.MODULO_MANOBRAS.get());
		commands.put(Modulos.FUNCAO.get(), maneuverFunction);
		commands.put(Modulos.AJUSTE_FINO.get(), String.valueOf(chkFineAdjusment.isSelected()));
		MechPeste.startModule(commands);
	}
}

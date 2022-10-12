package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.controllers.LandingController;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Modulos;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class LandingJPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private final JTextField txfHover = new JTextField("100"); //$NON-NLS-1$
	private final JButton btnHover = new JButton(Bundle.getString("pnl_land_btn_hover")); //$NON-NLS-1$
	private final JButton btnAutoLanding = new JButton(Bundle.getString("pnl_land_btn_land")); //$NON-NLS-1$
	private final JButton btnBack = new JButton(Bundle.getString("pnl_land_btn_back")); //$NON-NLS-1$
	private final JPanel pnlHover = new JPanel();
	private JLabel lblAlt;
	private final JLabel lblAutolanding = new JLabel(Bundle.getString("pnl_land_lbl_land"));
//$NON-NLS-1$

	public LandingJPanel() {

		initComponents();
	}

	private void initComponents() {
		setPreferredSize(ParametersJPanel.dmsParameters);
		setSize(ParametersJPanel.dmsParameters);
		setBorder(new TitledBorder(null, Bundle.getString("pnl_land_border"), TitledBorder.LEADING,
		                           //$NON-NLS-1$
		                           TitledBorder.TOP, null, null
		));

		txfHover.setHorizontalAlignment(SwingConstants.CENTER);
		txfHover.setColumns(10);

		btnHover.addActionListener(this);
		btnHover.setSize(ParametersJPanel.BTN_DIMENSION);
		btnHover.setPreferredSize(btnHover.getSize());
		btnHover.setMinimumSize(btnHover.getSize());
		btnHover.setMaximumSize(btnHover.getSize());

		btnBack.addActionListener(this);
		btnBack.setSize(ParametersJPanel.BTN_DIMENSION);
		btnBack.setPreferredSize(btnBack.getSize());
		btnBack.setMinimumSize(btnBack.getSize());
		btnBack.setMaximumSize(btnBack.getSize());

		btnAutoLanding.addActionListener(this);
		btnAutoLanding.setSize(ParametersJPanel.BTN_DIMENSION);
		btnAutoLanding.setPreferredSize(btnAutoLanding.getSize());
		btnAutoLanding.setMinimumSize(btnAutoLanding.getSize());
		btnAutoLanding.setMaximumSize(btnAutoLanding.getSize());

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
		                                          .addGroup(groupLayout.createSequentialGroup()
		                                                               .addGroup(groupLayout.createParallelGroup(
				                                                                                    Alignment.LEADING)
		                                                                                    .addGroup(
				                                                                                    groupLayout.createSequentialGroup()
				                                                                                               .addContainerGap()
				                                                                                               .addComponent(
						                                                                                               lblAutolanding)
				                                                                                               .addPreferredGap(
						                                                                                               ComponentPlacement.RELATED,
						                                                                                               105,
						                                                                                               Short.MAX_VALUE
				                                                                                                               )
				                                                                                               .addComponent(
						                                                                                               btnAutoLanding,
						                                                                                               GroupLayout.PREFERRED_SIZE,
						                                                                                               GroupLayout.DEFAULT_SIZE,
						                                                                                               GroupLayout.PREFERRED_SIZE
				                                                                                                            ))
		                                                                                    .addGroup(
				                                                                                    Alignment.TRAILING,
				                                                                                    groupLayout.createSequentialGroup()
				                                                                                               .addContainerGap()
				                                                                                               .addComponent(
						                                                                                               btnBack,
						                                                                                               GroupLayout.PREFERRED_SIZE,
						                                                                                               GroupLayout.DEFAULT_SIZE,
						                                                                                               GroupLayout.PREFERRED_SIZE
				                                                                                                            )
		                                                                                             )
		                                                                                    .addGroup(
				                                                                                    Alignment.TRAILING,
				                                                                                    groupLayout.createSequentialGroup()
				                                                                                               .addGap(6)
				                                                                                               .addComponent(
						                                                                                               pnlHover,
						                                                                                               GroupLayout.DEFAULT_SIZE,
						                                                                                               290,
						                                                                                               Short.MAX_VALUE
				                                                                                                            )
		                                                                                             ))
		                                                               .addGap(6)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
		                                        .addGroup(groupLayout.createSequentialGroup()
		                                                             .addGroup(groupLayout.createParallelGroup(
				                                                                                  Alignment.BASELINE)
		                                                                                  .addComponent(lblAutolanding)
		                                                                                  .addComponent(btnAutoLanding,
		                                                                                                GroupLayout.PREFERRED_SIZE,
		                                                                                                GroupLayout.DEFAULT_SIZE,
		                                                                                                GroupLayout.PREFERRED_SIZE
		                                                                                               ))
		                                                             .addPreferredGap(ComponentPlacement.RELATED)
		                                                             .addComponent(pnlHover,
		                                                                           GroupLayout.PREFERRED_SIZE,
		                                                                           60, GroupLayout.PREFERRED_SIZE
		                                                                          )
		                                                             .addPreferredGap(ComponentPlacement.RELATED, 62,
		                                                                              Short.MAX_VALUE
		                                                                             )
		                                                             .addComponent(btnBack, GroupLayout.PREFERRED_SIZE,
		                                                                           GroupLayout.DEFAULT_SIZE,
		                                                                           GroupLayout.PREFERRED_SIZE
		                                                                          )
		                                                             .addContainerGap()));
		setLayout(groupLayout);
		pnlHover.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				Bundle.getString("pnl_land_pnl_hover_border"), TitledBorder.LEADING, TitledBorder.TOP, null,
				//$NON-NLS-1$
				new Color(0, 0, 0)
		));

		lblAlt = new JLabel(Bundle.getString("pnl_land_lbl_alt")); //$NON-NLS-1$
		GroupLayout glPnlHover = new GroupLayout(pnlHover);
		glPnlHover.setHorizontalGroup(glPnlHover.createParallelGroup(Alignment.TRAILING)
		                                        .addGroup(glPnlHover.createSequentialGroup()
		                                                            .addContainerGap()
		                                                            .addComponent(lblAlt)
		                                                            .addPreferredGap(ComponentPlacement.RELATED)
		                                                            .addComponent(txfHover, GroupLayout.PREFERRED_SIZE,
		                                                                          GroupLayout.DEFAULT_SIZE,
		                                                                          GroupLayout.PREFERRED_SIZE
		                                                                         )
		                                                            .addPreferredGap(ComponentPlacement.RELATED, 21,
		                                                                             Short.MAX_VALUE
		                                                                            )
		                                                            .addComponent(btnHover)));
		glPnlHover.setVerticalGroup(glPnlHover.createParallelGroup(Alignment.LEADING)
		                                      .addGroup(glPnlHover.createSequentialGroup()
		                                                          .addGap(5)
		                                                          .addGroup(glPnlHover.createParallelGroup(
				                                                                              Alignment.BASELINE)
		                                                                              .addComponent(btnHover)
		                                                                              .addComponent(lblAlt)
		                                                                              .addComponent(txfHover,
		                                                                                            GroupLayout.PREFERRED_SIZE,
		                                                                                            GroupLayout.DEFAULT_SIZE,
		                                                                                            GroupLayout.PREFERRED_SIZE
		                                                                                           ))
		                                                          .addContainerGap(17, Short.MAX_VALUE)));
		pnlHover.setLayout(glPnlHover);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnHover) {
			handleBtnHoverActionPerformed(e);
		}
		if (e.getSource() == btnBack) {
			handleBtnBackActionPerformed(e);
		}
		if (e.getSource() == btnAutoLanding) {
			handleBtnAutoLandingActionPerformed(e);
		}
	}

	protected void handleBtnAutoLandingActionPerformed(ActionEvent e) {
		Map<String, String> commands = new HashMap<>();
		commands.put(Modulos.MODULO.get(), Modulos.MODULO_POUSO.get());
		MechPeste.startModule(commands);
	}

	protected void handleBtnBackActionPerformed(ActionEvent e) {
		MainGui.getParametros().firePropertyChange("Telemetria", false, true);
	}

	protected void handleBtnHoverActionPerformed(ActionEvent e) {
		if (btnHover.getText().equals(Bundle.getString("pnl_land_land"))) {
			LandingController.land();
			btnHover.setText(Bundle.getString("pnl_land_btn_hover"));
		} else {
			try {
				if (txfHover.getText().equals("")) { //$NON-NLS-1$
					StatusJPanel.setStatus(Bundle.getString("pnl_land_hover_alt")); //$NON-NLS-1$
					return;
				}
				Integer.parseInt(txfHover.getText());
			} catch (Exception e2) {
				StatusJPanel.setStatus(Bundle.getString("pnl_land_hover_alt_err")); //$NON-NLS-1$
				return;
			}
			Map<String, String> commands = new HashMap<>();
			commands.put(Modulos.MODULO.get(), Modulos.MODULO_POUSO_SOBREVOAR.get());
			commands.put(Modulos.ALTITUDE_SOBREVOO.get(), txfHover.getText());
			MechPeste.startModule(commands);
			btnHover.setText(Bundle.getString("pnl_land_land")); //$NON-NLS-1$
		}
	}
}

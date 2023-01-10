package com.pesterenan.views;

import com.pesterenan.MechPeste;
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

import static com.pesterenan.views.MainGui.BTN_DIMENSION;

public class LandingJPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private final JTextField txfHover = new JTextField("50"); //$NON-NLS-1$
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
		setPreferredSize(MainGui.dmsPanels);
		setSize(MainGui.dmsPanels);
		setBorder(new TitledBorder(null, Bundle.getString("pnl_land_border"), TitledBorder.LEADING,
				// $NON-NLS-1$
				TitledBorder.TOP, null, null));

		txfHover.setHorizontalAlignment(SwingConstants.CENTER);
		txfHover.setColumns(10);

		btnHover.addActionListener(this);
		btnHover.setSize(BTN_DIMENSION);
		btnHover.setPreferredSize(btnHover.getSize());
		btnHover.setMinimumSize(btnHover.getSize());
		btnHover.setMaximumSize(btnHover.getSize());

		btnBack.addActionListener(this);
		btnBack.setSize(BTN_DIMENSION);
		btnBack.setPreferredSize(btnBack.getSize());
		btnBack.setMinimumSize(btnBack.getSize());
		btnBack.setMaximumSize(btnBack.getSize());

		btnAutoLanding.addActionListener(this);
		btnAutoLanding.setSize(BTN_DIMENSION);
		btnAutoLanding.setPreferredSize(btnAutoLanding.getSize());
		btnAutoLanding.setMinimumSize(btnAutoLanding.getSize());
		btnAutoLanding.setMaximumSize(btnAutoLanding.getSize());

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup().addGap(10)
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup().addComponent(lblAutolanding).addGap(18)
										.addComponent(btnAutoLanding, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGroup(groupLayout.createSequentialGroup()
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(pnlHover,
												GroupLayout.PREFERRED_SIZE, 267, GroupLayout.PREFERRED_SIZE)))
						.addContainerGap(175, Short.MAX_VALUE))
				.addGroup(groupLayout.createSequentialGroup().addContainerGap(322, Short.MAX_VALUE)
						.addComponent(btnBack, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblAutolanding).addComponent(
						btnAutoLanding, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(pnlHover, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
				.addComponent(btnBack, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		setLayout(groupLayout);
		pnlHover.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				Bundle.getString("pnl_land_pnl_hover_border"), TitledBorder.LEADING, TitledBorder.TOP, null,
				// $NON-NLS-1$
				new Color(0, 0, 0)));

		lblAlt = new JLabel(Bundle.getString("pnl_land_lbl_alt")); //$NON-NLS-1$
		GroupLayout glPnlHover = new GroupLayout(pnlHover);
		glPnlHover.setHorizontalGroup(glPnlHover.createParallelGroup(Alignment.TRAILING).addGroup(Alignment.LEADING,
				glPnlHover.createSequentialGroup().addContainerGap().addComponent(lblAlt)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(txfHover, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(26).addComponent(btnHover, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addContainerGap(175, Short.MAX_VALUE)));
		glPnlHover.setVerticalGroup(glPnlHover.createParallelGroup(Alignment.LEADING)
				.addGroup(glPnlHover.createSequentialGroup().addGap(5)
						.addGroup(glPnlHover.createParallelGroup(Alignment.BASELINE).addComponent(lblAlt)
								.addComponent(txfHover, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(btnHover, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addContainerGap(18, Short.MAX_VALUE)));
		pnlHover.setLayout(glPnlHover);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnHover) {
			handleBtnHoverActionPerformed(e);
		}
		if (e.getSource() == btnBack) {
			MainGui.backToTelemetry();
		}
		if (e.getSource() == btnAutoLanding) {
			handleBtnAutoLandingActionPerformed(e);
		}
	}

	protected void handleBtnAutoLandingActionPerformed(ActionEvent e) {
		Map<String, String> commands = new HashMap<>();
		commands.put(Modulos.MODULO.get(), Modulos.MODULO_POUSO.get());
		MechPeste.newInstance().startModule(commands);
	}

	protected void handleBtnHoverActionPerformed(ActionEvent e) {
		Map<String, String> commands = new HashMap<>();
		commands.put(Modulos.MODULO.get(), Modulos.MODULO_POUSO_SOBREVOAR.get());
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
		commands.put(Modulos.ALTITUDE_SOBREVOO.get(), txfHover.getText());
		MechPeste.newInstance().startModule(commands);
	}
}

package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StatusJPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final JLabel lblStatus = new JLabel(Bundle.getString("lbl_stat_ready")); //$NON-NLS-1$
	private static final JButton btnConectar = new JButton(Bundle.getString("btn_stat_connect"));
	//$NON-NLS-1$
	private final Dimension dmsStatus = new Dimension(464, 25);

	public StatusJPanel() {
		initComponents();
	}

	public static void setStatus(String newStatus) {
		lblStatus.setText(newStatus);
	}

	public static void isBtnConnectVisible(boolean visible) {
		btnConectar.setVisible(visible);
	}

	private void initComponents() {
		setMinimumSize(new Dimension(0, 0));
		setPreferredSize(dmsStatus);

		btnConectar.addActionListener(new BotConectarActionListener());
		btnConectar.setPreferredSize(new Dimension(100, 18));
		btnConectar.setVisible(false);

		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
		                                          .addGroup(groupLayout.createSequentialGroup()
		                                                               .addGap(10)
		                                                               .addComponent(lblStatus)
		                                                               .addPreferredGap(ComponentPlacement.RELATED,
		                                                                                225,
		                                                                                Short.MAX_VALUE
		                                                                               )
		                                                               .addComponent(btnConectar,
		                                                                             GroupLayout.PREFERRED_SIZE,
		                                                                             GroupLayout.DEFAULT_SIZE,
		                                                                             GroupLayout.PREFERRED_SIZE
		                                                                            )
		                                                               .addGap(10)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
		                                        .addGroup(groupLayout.createSequentialGroup()
		                                                             .addGroup(groupLayout.createParallelGroup(
				                                                                                  Alignment.BASELINE)
		                                                                                  .addComponent(btnConectar,
		                                                                                                GroupLayout.PREFERRED_SIZE,
		                                                                                                18,
		                                                                                                Short.MAX_VALUE
		                                                                                               )
		                                                                                  .addGroup(
				                                                                                  groupLayout.createSequentialGroup()
				                                                                                             .addGap(2)
				                                                                                             .addComponent(
						                                                                                             lblStatus,
						                                                                                             GroupLayout.DEFAULT_SIZE,
						                                                                                             GroupLayout.DEFAULT_SIZE,
						                                                                                             Short.MAX_VALUE
				                                                                                                          )))
		                                                             .addGap(3)));
		setLayout(groupLayout);
	}

	private static class BotConectarActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			setStatus(Bundle.getString("status_connecting"));
			MechPeste.finalizarTarefa();
			MechPeste.getInstance().connectToKSP();
		}
	}
}

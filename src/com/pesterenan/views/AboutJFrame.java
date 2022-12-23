package com.pesterenan.views;

import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AboutJFrame extends JDialog implements ActionListener {

	private static final long serialVersionUID = 0L;
	private JLabel lblMechpeste;
	private JLabel lblAboutInfo;
	private JButton btnOk;

	public AboutJFrame() {
		initComponents();
	}

	private void initComponents() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		setBounds(MainGui.centerDialogOnScreen());
		setAlwaysOnTop(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("MechPeste - por Pesterenan"); //$NON-NLS-1$
		lblMechpeste = new JLabel("MechPeste - v.0.6");
		lblMechpeste.setFont(new Font("Trajan Pro", Font.BOLD, 18));
		lblMechpeste.setHorizontalAlignment(SwingConstants.CENTER);
		lblAboutInfo = new JLabel(
				"<html>Esse app foi desenvolvido com o intuito de auxiliar o controle de naves<br>no game Kerbal Space Program.<br><br>"
						+ "Não há garantias sobre o controle exato do app, portanto fique atento <br>"
						+ "para retomar o controle quando necessário.<br><br>" + "Feito por: Renan Torres<br>"
						+ "Visite meu canal no Youtube! - https://www.youtube.com/@Pesterenan");
		lblAboutInfo.setVerticalAlignment(SwingConstants.TOP);

		btnOk = new JButton("OK");
		btnOk.addActionListener(this);

		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup().addGap(67).addComponent(lblMechpeste,
										GroupLayout.PREFERRED_SIZE, 250, GroupLayout.PREFERRED_SIZE))
								.addGroup(groupLayout.createSequentialGroup().addContainerGap()
										.addComponent(lblAboutInfo, GroupLayout.PREFERRED_SIZE, 364, Short.MAX_VALUE))
								.addGroup(groupLayout.createSequentialGroup().addGap(125).addComponent(btnOk,
										GroupLayout.PREFERRED_SIZE, 135, GroupLayout.PREFERRED_SIZE)))
						.addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup().addGap(15).addComponent(lblMechpeste)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblAboutInfo).addGap(11)
						.addComponent(btnOk).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		getContentPane().setLayout(groupLayout);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnOk) {
			handleBtnOkActionPerformed(e);
		}
	}

	protected void handleBtnOkActionPerformed(ActionEvent e) {
		this.dispose();
	}
}

package com.pesterenan.views;

import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import static com.pesterenan.views.MainGui.BTN_DIMENSION;
import static com.pesterenan.views.MainGui.centerDialogOnScreen;
import static com.pesterenan.views.MainGui.createMarginComponent;

public class AboutJFrame extends JDialog implements JPanelDesignPattern {

	private static final long serialVersionUID = 0L;
	private JLabel lblMechpeste, lblAboutInfo;
	private JButton btnOk;

	public AboutJFrame() {
		initComponents();
		setupComponents();
		layoutComponents();
	}

	@Override
	public void initComponents() {
		// Labels:
		lblMechpeste = new JLabel("MechPeste - v.0.6");
		lblAboutInfo = new JLabel(
				"<html>Esse app foi desenvolvido com o intuito de auxiliar o controle de naves<br>no game Kerbal Space Program.<br><br>"
						+ "Não há garantias sobre o controle exato do app, portanto fique atento <br>"
						+ "para retomar o controle quando necessário.<br><br>" + "Feito por: Renan Torres<br>"
						+ "Visite meu canal no Youtube! - https://www.youtube.com/@Pesterenan</html>");

		// Buttons:
		btnOk = new JButton("OK");
	}

	@Override
	public void setupComponents() {
		// Main Panel setup:
		setTitle("MechPeste - por Pesterenan");
		setBounds(centerDialogOnScreen());
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		setAlwaysOnTop(true);
		setModalityType(ModalityType.APPLICATION_MODAL);

		// Setting-up components:
		lblMechpeste.setFont(new Font("Trajan Pro", Font.BOLD, 18));
		lblMechpeste.setAlignmentX(CENTER_ALIGNMENT);
		lblAboutInfo.setAlignmentX(CENTER_ALIGNMENT);

		btnOk.addActionListener(e -> {
			this.dispose();
		});
		btnOk.setPreferredSize(BTN_DIMENSION);
		btnOk.setMaximumSize(BTN_DIMENSION);
		btnOk.setAlignmentX(CENTER_ALIGNMENT);
	}

	@Override
	public void layoutComponents() {
		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
		pnlMain.setBorder(MainGui.MARGIN_BORDER_10_PX_LR);
		pnlMain.add(createMarginComponent(10, 10));
		pnlMain.add(lblMechpeste);
		pnlMain.add(createMarginComponent(10, 10));
		pnlMain.add(lblAboutInfo);
		pnlMain.add(Box.createVerticalGlue());
		pnlMain.add(btnOk);
		pnlMain.add(createMarginComponent(10, 10));

		getContentPane().add(pnlMain);
		setVisible(true);
	}
}

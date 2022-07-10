package com.pesterenan.views;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.pesterenan.controllers.RoverController;
import com.pesterenan.utils.Modulos;

public class RoverJPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -3157549581689803329L;
	private JButton btnTestar;

	public RoverJPanel() {
		initComponents();
	}

	private void initComponents() {

		btnTestar = new JButton("testar");
		btnTestar.addActionListener(this);
		add(btnTestar);
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == btnTestar) {
			handleBtnTestarActionPerformed(arg0);
		}
	}

	protected void handleBtnTestarActionPerformed(ActionEvent arg0) {
		Map<String, String> commands = new HashMap<>();
		commands.put(Modulos.MODULO.get(), Modulos.MODULO_MANOBRAS.get());
		RoverController rc = new RoverController(commands);
	}
}

package com.pesterenan.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import com.pesterenan.utils.Status;

import static com.pesterenan.utils.Status.*;
import static com.pesterenan.utils.Dicionario.*;

public class StatusJPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static JLabel statusLabel = new JLabel("Pronto.");
	private static JButton botConectar = new JButton(CONECTAR.get());

	public StatusJPanel() {

		botConectar.setPreferredSize(new Dimension(90, 18));
		botConectar.setVisible(false);
		botConectar.addActionListener(this);
		botConectar.setActionCommand(CONECTAR.get());
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2),
				BorderFactory.createBevelBorder(BevelBorder.LOWERED)));

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{0.0, 0.0};
		gridBagLayout.columnWidths = new int[]{0, 0};
		setLayout(gridBagLayout);
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 1;
		gc.weighty = 0;
		gc.insets = new Insets(1, 1, 1, 1);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.WEST;
		add(statusLabel, gc);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 0.2;
		gbc.weighty = 0;
		gbc.gridx = 1;
		gbc.insets = new Insets(0, 1, 0, 1);

		add(botConectar, gbc);

	}

	public static void setStatus(String novoStatus) {
		statusLabel.setText(novoStatus);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(CONECTAR.get())) {
			setStatus(CONECTANDO.get());
			firePropertyChange(CONECTAR.get(), 0, 1);
		}

	}

	public static void botConectarVisivel(boolean estado) {
		botConectar.setVisible(estado);
	}


}

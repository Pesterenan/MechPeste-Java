package com.pesterenan.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class TelemetriaJPanel extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	private JLabel nomeValorLabel = new JLabel("");
	private JLabel altitudeValorLabel = new JLabel("");
	private JLabel apoastroValorLabel = new JLabel("");
	private JLabel periastroValorLabel = new JLabel("");
	private JLabel velVValorLabel = new JLabel("");
	private JLabel velHValorLabel = new JLabel("");
	private JLabel bateriaValorLabel = new JLabel("");
	private JLabel tempoValorLabel = new JLabel("");

	public TelemetriaJPanel() {

		JLabel nomeLabel = new JLabel("Nome: ");
		JLabel altitudeLabel = new JLabel("Altitude: ");
		JLabel apoastroLabel = new JLabel("Apoastro: ");
		JLabel periastroLabel = new JLabel("Periastro: ");
		JLabel velVLabel = new JLabel("Vel. Vertical: ");
		JLabel velHLabel = new JLabel("Vel. Horizontal:");
		JLabel bateriaLabel = new JLabel("Bateria: ");
		JLabel tempoLabel = new JLabel("Tempo de Missão: ");
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 0.1;
		gc.weighty = 0.05;
		gc.gridx = 0;
		gc.anchor = GridBagConstraints.LINE_START;
		add(nomeLabel, gc);
		add(altitudeLabel, gc);
		add(apoastroLabel, gc);
		add(periastroLabel, gc);
		add(velVLabel, gc);
		add(velHLabel, gc);
		add(bateriaLabel, gc);
		add(tempoLabel, gc);
		gc.weightx = 1;
		gc.gridx = 1;
		gc.anchor = GridBagConstraints.EAST;
		add(nomeValorLabel, gc);
		add(altitudeValorLabel, gc);
		add(apoastroValorLabel, gc);
		add(periastroValorLabel, gc);
		add(velVValorLabel, gc);
		add(velHValorLabel, gc);
		add(bateriaValorLabel, gc);
		add(tempoValorLabel, gc);
		gc.weighty = 0.8;
		add(new JPanel(), gc);
		return;

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case "altitude":
			altitudeValorLabel.setText(String.format("%1$.0f", evt.getNewValue()) + "m");
			break;
		case "carga":
			bateriaValorLabel.setText(String.format("%1$.0f", evt.getNewValue()) + "%");
			break;
		case "apoastro":
			apoastroValorLabel.setText(String.format("%1$.0f", evt.getNewValue()) + "m");
			break;
		case "periastro":
			periastroValorLabel.setText(String.format("%1$.0f", evt.getNewValue()) + "m");
			break;
		case "tempoDeMissao":
			int segTotaisTdm = (int) evt.getNewValue();
			int horasTdm = segTotaisTdm / 3600;
			int minutosTdm = (segTotaisTdm % 3600) / 60;
			int segundosTdm = segTotaisTdm % 60;
			tempoValorLabel
					.setText("Tempo de Miss�o: " + String.format("%02d:%02d:%02d", horasTdm, minutosTdm, segundosTdm));
			break;
		case "velVert":
			velVValorLabel.setText(String.format("%1$.0f", evt.getNewValue()) + "m/s");
			break;
		case "velHorz":
			velHValorLabel.setText(String.format("%1$.0f", evt.getNewValue()) + "m/s");
			break;
		case "tempoRestante":
			int segTotaisTr = (int) evt.getNewValue();
			int horasTr = segTotaisTr / 3600;
			int minutosTr = (segTotaisTr % 3600) / 60;
			int segundosTr = segTotaisTr % 60;
			tempoValorLabel.setText("Tempo Restante: " + String.format("%02d:%02d:%02d", horasTr, minutosTr, segundosTr));
			break;
		}
	}

}

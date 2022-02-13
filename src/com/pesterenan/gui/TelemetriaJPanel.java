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

	private JLabel altitudeValorLabel = new JLabel("");
	private JLabel altitudeSupValorLabel = new JLabel("");
	private JLabel apoastroValorLabel = new JLabel("");
	private JLabel periastroValorLabel = new JLabel("");
	private JLabel velVValorLabel = new JLabel("");
	private JLabel velHValorLabel = new JLabel("");
	private JLabel bateriaValorLabel = new JLabel("");
	private JLabel tempoValorLabel = new JLabel("");
	private JLabel estagioValorLabel = new JLabel("");

	public TelemetriaJPanel() {

		JLabel altitudeLabel = new JLabel("Altitude: ");
		JLabel altitudeSupLabel = new JLabel("Alt. Superfície: ");
		JLabel apoastroLabel = new JLabel("Apoastro: ");
		JLabel periastroLabel = new JLabel("Periastro: ");
		JLabel velVLabel = new JLabel("Vel. Vertical: ");
		JLabel velHLabel = new JLabel("Vel. Horizontal:");
		JLabel bateriaLabel = new JLabel("Bateria: ");
		JLabel tempoLabel = new JLabel("Tempo de Missão: ");
		JLabel estagioLabel = new JLabel("Estágio Atual: ");
		addPropertyChangeListener(this);
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 0.1;
		gc.weighty = 0.05;
		gc.gridx = 0;
		gc.anchor = GridBagConstraints.LINE_START;
		add(altitudeLabel, gc);
		add(altitudeSupLabel, gc);
		add(apoastroLabel, gc);
		add(periastroLabel, gc);
		add(velVLabel, gc);
		add(velHLabel, gc);
		add(bateriaLabel, gc);
		add(tempoLabel, gc);
		add(estagioLabel, gc);
		gc.weightx = 1;
		gc.gridx = 1;
		gc.anchor = GridBagConstraints.EAST;
		add(altitudeValorLabel, gc);
		add(altitudeSupValorLabel, gc);
		add(apoastroValorLabel, gc);
		add(periastroValorLabel, gc);
		add(velVValorLabel, gc);
		add(velHValorLabel, gc);
		add(bateriaValorLabel, gc);
		add(tempoValorLabel, gc);
		add(estagioValorLabel, gc);
		gc.weighty = 0.8;
		add(new JPanel(), gc);
		return;

	}

	private String converterMetros(Object obj) {
		Double metros = Math.abs((double) obj);
		String casasDecimais = "%.2f";
		if ((double) obj < 0d) {
			return new String(String.format(casasDecimais + "m", 0d));
		}
		if (metros > 1000000000) {
			return String.format(casasDecimais + "Gm", metros / 1000000000);
		} else if (metros > 1000000) {
			return String.format(casasDecimais + "Mm", metros / 1000000);
		} else if (metros > 1000) {
			return String.format(casasDecimais + "km", metros / 1000);
		} else {
			return new String(String.format(casasDecimais + "m", metros));
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case "altitude":
			altitudeValorLabel.setText(converterMetros(evt.getNewValue()));
			break;
		case "altitudeSup":
			altitudeSupValorLabel.setText(converterMetros(evt.getNewValue()));
			break;
		case "bateria":
			bateriaValorLabel.setText(String.format("%.0f", evt.getNewValue()) + "%");
			break;
		case "apoastro":
			apoastroValorLabel.setText(converterMetros(evt.getNewValue()));
			break;
		case "periastro":
			periastroValorLabel.setText(converterMetros(evt.getNewValue()));
			break;
		case "tempoMissao":
			Double tempoDouble = (Double) evt.getNewValue();
			int segTotaisTdm = tempoDouble.intValue();
			int horasTdm = segTotaisTdm / 3600;
			int minutosTdm = (segTotaisTdm % 3600) / 60;
			int segundosTdm = segTotaisTdm % 60;
			tempoValorLabel.setText(String.format("%02d:%02d:%02d", horasTdm, minutosTdm, segundosTdm));
			break;
		case "velVertical":
			velVValorLabel.setText(String.format("%,.1f", evt.getNewValue()) + "m/s");
			break;
		case "velHorizontal":
			velHValorLabel.setText(String.format("%,.1f", evt.getNewValue()) + "m/s");
			break;
		case "tempoRestante":
			int segTotaisTr = (int) evt.getNewValue();
			int horasTr = segTotaisTr / 3600;
			int minutosTr = (segTotaisTr % 3600) / 60;
			int segundosTr = segTotaisTr % 60;
			tempoValorLabel.setText(String.format("%02d:%02d:%02d", horasTr, minutosTr, segundosTr));
			break;
		case "estagio":
			estagioValorLabel.setText(String.format("%.1f", evt.getNewValue()));
			break;
		}
	}

}

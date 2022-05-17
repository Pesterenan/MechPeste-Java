package com.pesterenan.view;

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
	private JLabel distanciaValorLabel = new JLabel("");

	public TelemetriaJPanel() {

		JLabel altitudeLabel = new JLabel("Altitude: ");
		JLabel altitudeSupLabel = new JLabel("Alt. Superfície: ");
		JLabel apoastroLabel = new JLabel("Apoastro: ");
		JLabel periastroLabel = new JLabel("Periastro: ");
		JLabel velVLabel = new JLabel("Vel. Vertical: ");
		JLabel velHLabel = new JLabel("Vel. Horizontal:");
		JLabel bateriaLabel = new JLabel("Bateria: ");
		JLabel tempoLabel = new JLabel("Tempo de Missão: ");
		JLabel distanciaLabel = new JLabel("Distancia ate o pouso:");
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
		add(distanciaLabel, gc);
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
		add(distanciaValorLabel, gc);
		gc.weighty = 0.8;
		add(new JPanel(), gc);
		return;

	}

	private String converterMetros(Object obj) {
		Double metros = Math.abs((double) obj);
		String casasDecimais = "%.2f";
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

	private String formatarTempoDecorrido(Double segundosTotais) {
		int anos = (segundosTotais.intValue() / 9201600);
		int dias = (segundosTotais.intValue() / 21600) % 426;
		int horas = (segundosTotais.intValue() / 3600) % 6;
		int minutos = (segundosTotais.intValue() % 3600) / 60;
		int segundos = segundosTotais.intValue() % 60;
		return String.format("%dA-%dd-%02d:%02d:%02d",anos, dias, horas, minutos, segundos);
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
		case "tempoRestante":
		case "tempoMissao":
			tempoValorLabel.setText(formatarTempoDecorrido((Double) evt.getNewValue()));
			break;
		case "velVertical":
			velVValorLabel.setText(converterMetros(evt.getNewValue()) + "/s");
			break;
		case "velHorizontal":
			velHValorLabel.setText(converterMetros(evt.getNewValue()) + "/s");
			break;
		case "distancia":
			distanciaValorLabel.setText(converterMetros(evt.getNewValue()));
		}
	}

}

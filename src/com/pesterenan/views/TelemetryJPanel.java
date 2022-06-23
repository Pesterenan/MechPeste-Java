package com.pesterenan.views;

import static com.pesterenan.views.ParametersJPanel.BTN_DIMENSION;
import static com.pesterenan.views.ParametersJPanel.dmsParameters;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.pesterenan.MechPeste;

public class TelemetryJPanel extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	private JLabel tempoValorLabel = new JLabel("");
	private JLabel altitudeValorLabel;
	private JLabel altitudeSupValorLabel;
	private JLabel apoastroValorLabel;
	private JLabel periastroValorLabel;
	private JLabel velVValorLabel;
	private JLabel velHValorLabel;
	private JLabel bateriaValorLabel;
	private JLabel distanciaValorLabel;
	private JLabel label_10;

	public TelemetryJPanel() {

		initComponents();
	}

	private void initComponents() {
		setPreferredSize(dmsParameters);
		setSize(dmsParameters);
		setBorder(new TitledBorder(null, "Telemetria:", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		addPropertyChangeListener(this);
		setLayout(new GridLayout(0, 2, 0, 0));
		JLabel altitudeLabel = new JLabel("Altitude: ");
		add(altitudeLabel);

		altitudeValorLabel = new JLabel("");
		add(altitudeValorLabel);
		JLabel altitudeSupLabel = new JLabel("Alt. Superfície: ");
		add(altitudeSupLabel);

		altitudeSupValorLabel = new JLabel("");
		add(altitudeSupValorLabel);
		JLabel apoastroLabel = new JLabel("Apoastro: ");
		add(apoastroLabel);

		apoastroValorLabel = new JLabel("");
		add(apoastroValorLabel);
		JLabel periastroLabel = new JLabel("Periastro: ");
		add(periastroLabel);

		periastroValorLabel = new JLabel("");
		add(periastroValorLabel);
		JLabel velVLabel = new JLabel("Vel. Vertical: ");
		add(velVLabel);

		velVValorLabel = new JLabel("");
		add(velVValorLabel);
		JLabel velHLabel = new JLabel("Vel. Horizontal:");
		add(velHLabel);

		velHValorLabel = new JLabel("");
		add(velHValorLabel);
		JLabel bateriaLabel = new JLabel("Bateria: ");
		add(bateriaLabel);

		bateriaValorLabel = new JLabel("");
		add(bateriaValorLabel);
		JLabel tempoLabel = new JLabel("Tempo de Missão: ");
		add(tempoLabel);
		add(tempoValorLabel);
		JLabel distanciaLabel = new JLabel("Distancia ate o pouso:");
		add(distanciaLabel);

		distanciaValorLabel = new JLabel("");
		add(distanciaValorLabel);

		label_10 = new JLabel("");
		add(label_10);
		JButton btnCancelar = new JButton("Cancelar");
		btnCancelar.setSize(BTN_DIMENSION);
		btnCancelar.setPreferredSize(btnCancelar.getSize());
		btnCancelar.setMinimumSize(btnCancelar.getSize());
		btnCancelar.setMaximumSize(btnCancelar.getSize());
		btnCancelar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MechPeste.finalizarTarefa();
			}
		});
		add(btnCancelar);
	}

	private String converterMetros(Object obj) {
		Double metros = Math.abs((double) obj);
		String casasDecimais = "%.2f";
		if (metros >= 1000000000) {
			return String.format(casasDecimais + "Gm", metros / 1000000000);
		} else if (metros >= 1000000) {
			return String.format(casasDecimais + "Mm", metros / 1000000);
		} else if (metros >= 1000) {
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
		return String.format("%dA-%dd-%02d:%02d:%02d", anos, dias, horas, minutos, segundos);
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

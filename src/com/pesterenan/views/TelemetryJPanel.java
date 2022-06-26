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
	private JLabel timeValueLabel = new JLabel("");
	private JLabel altitudeValueLabel;
	private JLabel altitudeSupValueLabel;
	private JLabel apoastroValueLabel;
	private JLabel periapsisValueLabel;
	private JLabel verticalSpeedValueLabel;
	private JLabel horizontalValueLabel;
	private JLabel batteryValueLabel;
	private JLabel distanceValueLabel;
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

		altitudeValueLabel = new JLabel("");
		add(altitudeValueLabel);
		JLabel altitudeSupLabel = new JLabel("Alt. Superfície: ");
		add(altitudeSupLabel);

		altitudeSupValueLabel = new JLabel("");
		add(altitudeSupValueLabel);
		JLabel apoastroLabel = new JLabel("Apoastro: ");
		add(apoastroLabel);

		apoastroValueLabel = new JLabel("");
		add(apoastroValueLabel);
		JLabel periastroLabel = new JLabel("Periastro: ");
		add(periastroLabel);

		periapsisValueLabel = new JLabel("");
		add(periapsisValueLabel);
		JLabel velVLabel = new JLabel("Vel. Vertical: ");
		add(velVLabel);

		verticalSpeedValueLabel = new JLabel("");
		add(verticalSpeedValueLabel);
		JLabel velHLabel = new JLabel("Vel. Horizontal:");
		add(velHLabel);

		horizontalValueLabel = new JLabel("");
		add(horizontalValueLabel);
		JLabel bateriaLabel = new JLabel("Bateria: ");
		add(bateriaLabel);

		batteryValueLabel = new JLabel("");
		add(batteryValueLabel);
		JLabel tempoLabel = new JLabel("Tempo de Missão: ");
		add(tempoLabel);
		add(timeValueLabel);
		JLabel distanciaLabel = new JLabel("Distancia ate o pouso:");
		add(distanciaLabel);

		distanceValueLabel = new JLabel("");
		add(distanceValueLabel);

		label_10 = new JLabel("");
		add(label_10);
		JButton btnCancelar = new JButton("Cancelar");
		btnCancelar.setSize(BTN_DIMENSION);
		btnCancelar.setPreferredSize(btnCancelar.getSize());
		btnCancelar.setMinimumSize(btnCancelar.getSize());
		btnCancelar.setMaximumSize(btnCancelar.getSize());
		btnCancelar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MechPeste.endTask();
			}
		});
		add(btnCancelar);
	}

	private String convertMeters(Object obj) {
		Double metros = Math.abs((double) obj);
		String decimalPlaces = "%.2f";
		if (metros >= 1000000000) {
			return String.format(decimalPlaces + "Gm", metros / 1000000000);
		} else if (metros >= 1000000) {
			return String.format(decimalPlaces + "Mm", metros / 1000000);
		} else if (metros >= 1000) {
			return String.format(decimalPlaces + "km", metros / 1000);
		} else {
			return new String(String.format(decimalPlaces + "m", metros));
		}
	}

	private String formatElapsedTime(Double totalSeconds) {
		int anos = (totalSeconds.intValue() / 9201600);
		int dias = (totalSeconds.intValue() / 21600) % 426;
		int horas = (totalSeconds.intValue() / 3600) % 6;
		int minutos = (totalSeconds.intValue() % 3600) / 60;
		int segundos = totalSeconds.intValue() % 60;
		return String.format("%dA-%dd-%02d:%02d:%02d", anos, dias, horas, minutos, segundos);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case "altitude":
			altitudeValueLabel.setText(convertMeters(evt.getNewValue()));
			break;
		case "surfAltitude":
			altitudeSupValueLabel.setText(convertMeters(evt.getNewValue()));
			break;
		case "bateria":
			batteryValueLabel.setText(String.format("%.0f", evt.getNewValue()) + "%");
			break;
		case "apoapsis":
			apoastroValueLabel.setText(convertMeters(evt.getNewValue()));
			break;
		case "periapsis":
			periapsisValueLabel.setText(convertMeters(evt.getNewValue()));
			break;
		case "tempoRestante":
		case "missionTime":
			timeValueLabel.setText(formatElapsedTime((Double) evt.getNewValue()));
			break;
		case "verticalSpeed":
			verticalSpeedValueLabel.setText(convertMeters(evt.getNewValue()) + "/s");
			break;
		case "horizontalSpeed":
			horizontalValueLabel.setText(convertMeters(evt.getNewValue()) + "/s");
			break;
		case "distancia":
			distanceValueLabel.setText(convertMeters(evt.getNewValue()));
		}
	}

}

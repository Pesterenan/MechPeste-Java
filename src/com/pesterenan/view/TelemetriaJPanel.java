package com.pesterenan.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.pesterenan.MechPeste;

import javax.swing.JButton;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import javax.swing.JSeparator;
import java.awt.GridLayout;

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
		setBorder(null);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 158, 158, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);
		JLabel altitudeLabel = new JLabel("Altitude: ");
		altitudeLabel.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_altitudeLabel = new GridBagConstraints();
		gbc_altitudeLabel.fill = GridBagConstraints.BOTH;
		gbc_altitudeLabel.gridx = 0;
		gbc_altitudeLabel.gridy = 0;
		add(altitudeLabel, gbc_altitudeLabel);
		GridBagConstraints gbc_altitudeValorLabel = new GridBagConstraints();
		gbc_altitudeValorLabel.fill = GridBagConstraints.BOTH;
		gbc_altitudeValorLabel.gridx = 1;
		gbc_altitudeValorLabel.gridy = 0;
		add(altitudeValorLabel, gbc_altitudeValorLabel);

		JLabel altitudeSupLabel = new JLabel("Alt. Superfície: ");
		GridBagConstraints gbc_altitudeSupLabel = new GridBagConstraints();
		gbc_altitudeSupLabel.fill = GridBagConstraints.BOTH;
		gbc_altitudeSupLabel.gridx = 0;
		gbc_altitudeSupLabel.gridy = 1;
		add(altitudeSupLabel, gbc_altitudeSupLabel);
		GridBagConstraints gbc_altitudeSupValorLabel = new GridBagConstraints();
		gbc_altitudeSupValorLabel.fill = GridBagConstraints.BOTH;
		gbc_altitudeSupValorLabel.gridx = 1;
		gbc_altitudeSupValorLabel.gridy = 1;
		add(altitudeSupValorLabel, gbc_altitudeSupValorLabel);

		JLabel apoastroLabel = new JLabel("Apoastro: ");
		GridBagConstraints gbc_apoastroLabel = new GridBagConstraints();
		gbc_apoastroLabel.fill = GridBagConstraints.BOTH;
		gbc_apoastroLabel.gridx = 0;
		gbc_apoastroLabel.gridy = 2;
		add(apoastroLabel, gbc_apoastroLabel);
		GridBagConstraints gbc_apoastroValorLabel = new GridBagConstraints();
		gbc_apoastroValorLabel.fill = GridBagConstraints.BOTH;
		gbc_apoastroValorLabel.gridx = 1;
		gbc_apoastroValorLabel.gridy = 2;
		add(apoastroValorLabel, gbc_apoastroValorLabel);

		JLabel periastroLabel = new JLabel("Periastro: ");
		GridBagConstraints gbc_periastroLabel = new GridBagConstraints();
		gbc_periastroLabel.fill = GridBagConstraints.BOTH;
		gbc_periastroLabel.gridx = 0;
		gbc_periastroLabel.gridy = 3;
		add(periastroLabel, gbc_periastroLabel);
		GridBagConstraints gbc_periastroValorLabel = new GridBagConstraints();
		gbc_periastroValorLabel.fill = GridBagConstraints.BOTH;
		gbc_periastroValorLabel.gridx = 1;
		gbc_periastroValorLabel.gridy = 3;
		add(periastroValorLabel, gbc_periastroValorLabel);

		JLabel velVLabel = new JLabel("Vel. Vertical: ");
		GridBagConstraints gbc_velVLabel = new GridBagConstraints();
		gbc_velVLabel.fill = GridBagConstraints.BOTH;
		gbc_velVLabel.gridx = 0;
		gbc_velVLabel.gridy = 4;
		add(velVLabel, gbc_velVLabel);
		GridBagConstraints gbc_velVValorLabel = new GridBagConstraints();
		gbc_velVValorLabel.fill = GridBagConstraints.BOTH;
		gbc_velVValorLabel.gridx = 1;
		gbc_velVValorLabel.gridy = 4;
		add(velVValorLabel, gbc_velVValorLabel);

		JLabel velHLabel = new JLabel("Vel. Horizontal:");
		GridBagConstraints gbc_velHLabel = new GridBagConstraints();
		gbc_velHLabel.fill = GridBagConstraints.BOTH;
		gbc_velHLabel.gridx = 0;
		gbc_velHLabel.gridy = 5;
		add(velHLabel, gbc_velHLabel);
		GridBagConstraints gbc_velHValorLabel = new GridBagConstraints();
		gbc_velHValorLabel.fill = GridBagConstraints.BOTH;
		gbc_velHValorLabel.gridx = 1;
		gbc_velHValorLabel.gridy = 5;
		add(velHValorLabel, gbc_velHValorLabel);

		JLabel bateriaLabel = new JLabel("Bateria: ");
		GridBagConstraints gbc_bateriaLabel = new GridBagConstraints();
		gbc_bateriaLabel.fill = GridBagConstraints.BOTH;
		gbc_bateriaLabel.gridx = 0;
		gbc_bateriaLabel.gridy = 6;
		add(bateriaLabel, gbc_bateriaLabel);
		GridBagConstraints gbc_bateriaValorLabel = new GridBagConstraints();
		gbc_bateriaValorLabel.fill = GridBagConstraints.BOTH;
		gbc_bateriaValorLabel.gridx = 1;
		gbc_bateriaValorLabel.gridy = 6;
		add(bateriaValorLabel, gbc_bateriaValorLabel);

		JLabel tempoLabel = new JLabel("Tempo de Missão: ");
		GridBagConstraints gbc_tempoLabel = new GridBagConstraints();
		gbc_tempoLabel.fill = GridBagConstraints.BOTH;
		gbc_tempoLabel.gridx = 0;
		gbc_tempoLabel.gridy = 7;
		add(tempoLabel, gbc_tempoLabel);
		GridBagConstraints gbc_tempoValorLabel = new GridBagConstraints();
		gbc_tempoValorLabel.fill = GridBagConstraints.BOTH;
		gbc_tempoValorLabel.gridx = 1;
		gbc_tempoValorLabel.gridy = 7;
		add(tempoValorLabel, gbc_tempoValorLabel);

		JLabel distanciaLabel = new JLabel("Distancia ate o pouso:");
		GridBagConstraints gbc_distanciaLabel = new GridBagConstraints();
		gbc_distanciaLabel.fill = GridBagConstraints.BOTH;
		gbc_distanciaLabel.gridx = 0;
		gbc_distanciaLabel.gridy = 8;
		add(distanciaLabel, gbc_distanciaLabel);
		GridBagConstraints gbc_distanciaValorLabel = new GridBagConstraints();
		gbc_distanciaValorLabel.fill = GridBagConstraints.BOTH;
		gbc_distanciaValorLabel.gridx = 1;
		gbc_distanciaValorLabel.gridy = 8;
		add(distanciaValorLabel, gbc_distanciaValorLabel);

		JLabel label_9 = new JLabel("");
		GridBagConstraints gbc_label_9 = new GridBagConstraints();
		gbc_label_9.fill = GridBagConstraints.BOTH;
		gbc_label_9.gridx = 0;
		gbc_label_9.gridy = 9;
		add(label_9, gbc_label_9);
		JButton btnCancelar = new JButton("Cancelar");
		btnCancelar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MechPeste.finalizarTarefa();
				} catch (IOException e2) {
				}
			}
		});

		GridBagConstraints gbc_btnCancelar = new GridBagConstraints();
		gbc_btnCancelar.fill = GridBagConstraints.BOTH;
		gbc_btnCancelar.gridx = 1;
		gbc_btnCancelar.gridy = 9;
		add(btnCancelar, gbc_btnCancelar);

		addPropertyChangeListener(this);
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

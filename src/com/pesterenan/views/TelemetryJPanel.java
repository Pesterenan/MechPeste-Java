package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static com.pesterenan.views.ParametersJPanel.BTN_DIMENSION;
import static com.pesterenan.views.ParametersJPanel.dmsParameters;

public class TelemetryJPanel extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	private final JLabel tempoValorLabel = new JLabel(""); //$NON-NLS-1$
	private JLabel altitudeValorLabel;
	private JLabel altitudeSupValorLabel;
	private JLabel apoastroValorLabel;
	private JLabel periastroValorLabel;
	private JLabel velVValorLabel;
	private JLabel velHValorLabel;
	private JLabel bateriaValorLabel;
	private JLabel label_10;

	public TelemetryJPanel() {

		initComponents();
	}

	private void initComponents() {
		setPreferredSize(dmsParameters);
		setSize(dmsParameters);
		setBorder(
				new TitledBorder(null, Bundle.getString("pnl_tel_border"), TitledBorder.LEADING, TitledBorder.TOP,
				                 null,
				                 null
				)); //$NON-NLS-1$

		addPropertyChangeListener(this);
		setLayout(new GridLayout(0, 2, 0, 0));
		JLabel altitudeLabel = new JLabel(Bundle.getString("pnl_tel_lbl_alt")); //$NON-NLS-1$
		add(altitudeLabel);

		altitudeValorLabel = new JLabel(""); //$NON-NLS-1$
		add(altitudeValorLabel);
		JLabel altitudeSupLabel = new JLabel(Bundle.getString("pnl_tel_lbl_alt_sur")); //$NON-NLS-1$
		add(altitudeSupLabel);

		altitudeSupValorLabel = new JLabel(""); //$NON-NLS-1$
		add(altitudeSupValorLabel);
		JLabel apoastroLabel = new JLabel(Bundle.getString("pnl_tel_lbl_apoapsis")); //$NON-NLS-1$
		add(apoastroLabel);

		apoastroValorLabel = new JLabel("");
		add(apoastroValorLabel);
		JLabel periastroLabel = new JLabel(Bundle.getString("pnl_tel_lbl_periapsis")); //$NON-NLS-1$
		add(periastroLabel);

		periastroValorLabel = new JLabel(""); //$NON-NLS-1$
		add(periastroValorLabel);
		JLabel velVLabel = new JLabel(Bundle.getString("pnl_tel_lbl_vert_spd")); //$NON-NLS-1$
		add(velVLabel);

		velVValorLabel = new JLabel(""); //$NON-NLS-1$
		add(velVValorLabel);
		JLabel velHLabel = new JLabel(Bundle.getString("pnl_tel_lbl_horz_spd")); //$NON-NLS-1$
		add(velHLabel);

		velHValorLabel = new JLabel(""); //$NON-NLS-1$
		add(velHValorLabel);
		JLabel bateriaLabel = new JLabel(Bundle.getString("pnl_tel_lbl_battery")); //$NON-NLS-1$
		add(bateriaLabel);

		bateriaValorLabel = new JLabel("");
		add(bateriaValorLabel);
		JLabel tempoLabel = new JLabel(Bundle.getString("pnl_tel_lbl_mission_time")); //$NON-NLS-1$
		add(tempoLabel);
		add(tempoValorLabel);

		label_10 = new JLabel(""); //$NON-NLS-1$
		add(label_10);
		JButton btnCancelar = new JButton(Bundle.getString("pnl_tel_btn_cancel")); //$NON-NLS-1$
		btnCancelar.setSize(BTN_DIMENSION);
		btnCancelar.setPreferredSize(btnCancelar.getSize());
		btnCancelar.setMinimumSize(btnCancelar.getSize());
		btnCancelar.setMaximumSize(btnCancelar.getSize());
		btnCancelar.addActionListener(e -> MechPeste.finalizarTarefa());
		add(btnCancelar);
	}

	private String converterMetros(Object obj) {
		double metros = Math.abs((double) obj);
		String casasDecimais = "%.2f"; //$NON-NLS-1$
		if (metros >= 1000000000) {
			return String.format(casasDecimais + "Gm", metros / 1000000000); //$NON-NLS-1$
		} else if (metros >= 1000000) {
			return String.format(casasDecimais + "Mm", metros / 1000000); //$NON-NLS-1$
		} else if (metros >= 1000) {
			return String.format(casasDecimais + "km", metros / 1000); //$NON-NLS-1$
		} else {
			return String.format(casasDecimais + "m", metros); //$NON-NLS-1$
		}
	}

	private String formatarTempoDecorrido(Double segundosTotais) {
		int anos = (segundosTotais.intValue() / 9201600);
		int dias = (segundosTotais.intValue() / 21600) % 426;
		int horas = (segundosTotais.intValue() / 3600) % 6;
		int minutos = (segundosTotais.intValue() % 3600) / 60;
		int segundos = segundosTotais.intValue() % 60;
		return String.format(Bundle.getString("pnl_tel_lbl_date_template"), anos, dias, horas, minutos,
		                     segundos
		                    ); //$NON-NLS-1$
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
			case "altitude": //$NON-NLS-1$
				altitudeValorLabel.setText(converterMetros(evt.getNewValue()));
				break;
			case "altitudeSup": //$NON-NLS-1$
				altitudeSupValorLabel.setText(converterMetros(evt.getNewValue()));
				break;
			case "bateria": //$NON-NLS-1$
				bateriaValorLabel.setText(String.format("%.1f", evt.getNewValue()) + "%"); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case "apoastro": //$NON-NLS-1$
				apoastroValorLabel.setText(converterMetros(evt.getNewValue()));
				break;
			case "periastro": //$NON-NLS-1$
				periastroValorLabel.setText(converterMetros(evt.getNewValue()));
				break;
			case "tempoRestante": //$NON-NLS-1$
			case "tempoMissao": //$NON-NLS-1$
				tempoValorLabel.setText(formatarTempoDecorrido((Double) evt.getNewValue()));
				break;
			case "velVertical": //$NON-NLS-1$
				velVValorLabel.setText(converterMetros(evt.getNewValue()) + "/s"); //$NON-NLS-1$
				break;
			case "velHorizontal": //$NON-NLS-1$
				velHValorLabel.setText(converterMetros(evt.getNewValue()) + "/s"); //$NON-NLS-1$
				break;
		}
	}

}

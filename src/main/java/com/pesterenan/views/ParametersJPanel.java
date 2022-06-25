package com.pesterenan.views;

import static com.pesterenan.utils.Dicionario.DECOLAGEM_ORBITAL;
import static com.pesterenan.utils.Dicionario.MANOBRAS;
import static com.pesterenan.utils.Dicionario.POUSO_AUTOMATICO;
import static com.pesterenan.utils.Dicionario.ROVER_AUTONOMO;
import static com.pesterenan.utils.Dicionario.TELEMETRIA;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

public class ParametersJPanel extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	private JPanel pnlParameters = this;
	private CardLayout cardLayout = new CardLayout(0, 0);
	public static Dimension dmsParameters = new Dimension(314, 216);
	public static final Dimension BTN_DIMENSION = new Dimension(110, 25);
	private TelemetryJPanel pnlTelemetry = new TelemetryJPanel();
	private JPanel pnlLiftoff = new LiftoffJPanel();
	private JPanel pnlLanding = new PousoAutomaticoJPanel();
	private JPanel pnlManeuver = new ManeuverJPanel();
	private JPanel pnlRover = new JPanel();

	public ParametersJPanel() {
		initComponents();
	}

	private void initComponents() {
		setMinimumSize(new Dimension(0, 0));
		setSize(dmsParameters);
		setBorder(null);
		setLayout(cardLayout);
		pnlParameters.addPropertyChangeListener(this);

		add(pnlTelemetry, TELEMETRIA.get());
		add(pnlLiftoff, DECOLAGEM_ORBITAL.get());
		add(pnlLanding, POUSO_AUTOMATICO.get());
		add(pnlManeuver, MANOBRAS.get());
		add(pnlRover, ROVER_AUTONOMO.get());

	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == pnlParameters) {
			handlePnlTelemetriaPropertyChange(evt);
		}
	}

	protected void handlePnlTelemetriaPropertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(DECOLAGEM_ORBITAL.get())) {
			cardLayout.show(pnlParameters, DECOLAGEM_ORBITAL.get());
		}
		if (evt.getPropertyName().equals(POUSO_AUTOMATICO.get())) {
			cardLayout.show(pnlParameters, POUSO_AUTOMATICO.get());
		}
		if (evt.getPropertyName().equals(MANOBRAS.get())) {
			cardLayout.show(pnlParameters, MANOBRAS.get());
		}
		if (evt.getPropertyName().equals(ROVER_AUTONOMO.get())) {
			cardLayout.show(pnlParameters, ROVER_AUTONOMO.get());
		}
		if (evt.getPropertyName().equals(TELEMETRIA.get())) {
			cardLayout.show(pnlParameters, TELEMETRIA.get());
		}
	}

	public TelemetryJPanel getTelemetria() {
		return pnlTelemetry;
	}
}
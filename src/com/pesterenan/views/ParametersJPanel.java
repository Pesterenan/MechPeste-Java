package com.pesterenan.views;

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
	private JPanel pnlLanding = new LandingJPanel();
	private JPanel pnlManeuver = new ManeuverJPanel();
	private JPanel pnlRover = new RoverJPanel();

	public ParametersJPanel() {
		initComponents();
	}

	private void initComponents() {
		setMinimumSize(new Dimension(0, 0));
		setSize(dmsParameters);
		setBorder(null);
		setLayout(cardLayout);
		pnlParameters.addPropertyChangeListener(this);

		add(pnlTelemetry, "Telemetria");
		add(pnlLiftoff, "Decolagem Orbital");
		add(pnlLanding, "Pouso Automático");
		add(pnlManeuver, "Manobras");
		add(pnlRover, "Rover Autônomo");

	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == pnlParameters) {
			handlePnlTelemetriaPropertyChange(evt);
		}
	}

	protected void handlePnlTelemetriaPropertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("Decolagem Orbital")) {
			cardLayout.show(pnlParameters, "Decolagem Orbital");
		}
		if (evt.getPropertyName().equals("Pouso Automático")) {
			cardLayout.show(pnlParameters, "Pouso Automático");
		}
		if (evt.getPropertyName().equals("Manobras")) {
			cardLayout.show(pnlParameters, "Manobras");
		}
		if (evt.getPropertyName().equals("Rover Autônomo")) {
			cardLayout.show(pnlParameters, "Rover Autônomo");
		}
		if (evt.getPropertyName().equals("Telemetria")) {
			cardLayout.show(pnlParameters, "Telemetria");
		}
	}

	public TelemetryJPanel getTelemetria() {
		return pnlTelemetry;
	}
}
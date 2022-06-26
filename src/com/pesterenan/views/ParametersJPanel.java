package com.pesterenan.views;

import static com.pesterenan.utils.Dictionary.ORBITAL_LIFTOFF;
import static com.pesterenan.utils.Dictionary.MANEUVERS;
import static com.pesterenan.utils.Dictionary.AUTOMATIC_LAND;
import static com.pesterenan.utils.Dictionary.AUTOMATIC_ROVER;
import static com.pesterenan.utils.Dictionary.TELEMETRY;

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

		add(pnlTelemetry, TELEMETRY.get());
		add(pnlLiftoff, ORBITAL_LIFTOFF.get());
		add(pnlLanding, AUTOMATIC_LAND.get());
		add(pnlManeuver, MANEUVERS.get());
		add(pnlRover, AUTOMATIC_ROVER.get());

	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == pnlParameters) {
			handlePnlTelemetriaPropertyChange(evt);
		}
	}

	protected void handlePnlTelemetriaPropertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(ORBITAL_LIFTOFF.get())) {
			cardLayout.show(pnlParameters, ORBITAL_LIFTOFF.get());
		}
		if (evt.getPropertyName().equals(AUTOMATIC_LAND.get())) {
			cardLayout.show(pnlParameters, AUTOMATIC_LAND.get());
		}
		if (evt.getPropertyName().equals(MANEUVERS.get())) {
			cardLayout.show(pnlParameters, MANEUVERS.get());
		}
		if (evt.getPropertyName().equals(AUTOMATIC_ROVER.get())) {
			cardLayout.show(pnlParameters, AUTOMATIC_ROVER.get());
		}
		if (evt.getPropertyName().equals(TELEMETRY.get())) {
			cardLayout.show(pnlParameters, TELEMETRY.get());
		}
	}

	public TelemetryJPanel getTelemetry() {
		return pnlTelemetry;
	}
}
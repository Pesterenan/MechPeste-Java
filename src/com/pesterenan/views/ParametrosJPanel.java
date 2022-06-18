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

public class ParametrosJPanel extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	private JPanel parametros = this;
	private CardLayout layout = new CardLayout(0, 0);
	public static Dimension dmsParametros = new Dimension(314, 216);
	public static final Dimension BTN_DIMENSION = new Dimension(110, 25);
	private TelemetriaJPanel pnlTelemetria = new TelemetriaJPanel();
	private JPanel pnlDecolagem = new DecolagemJPanel();
	private JPanel pnlPouso = new PousoAutomaticoJPanel();
	private JPanel pnlManobras = new ManobrasJPanel();
	private JPanel pnlRover = new JPanel();

	public ParametrosJPanel() {
		initComponents();
	}

	private void initComponents() {
		setMinimumSize(new Dimension(0, 0));
		setSize(dmsParametros);
		setBorder(null);
		setLayout(layout);
		parametros.addPropertyChangeListener(this);

		add(pnlTelemetria, TELEMETRIA.get());
		add(pnlDecolagem, DECOLAGEM_ORBITAL.get());
		add(pnlPouso, POUSO_AUTOMATICO.get());
		add(pnlManobras, MANOBRAS.get());
		add(pnlRover, ROVER_AUTONOMO.get());

	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == parametros) {
			handlePnlTelemetriaPropertyChange(evt);
		}
	}

	protected void handlePnlTelemetriaPropertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(DECOLAGEM_ORBITAL.get())) {
			layout.show(parametros, DECOLAGEM_ORBITAL.get());
		}
		if (evt.getPropertyName().equals(POUSO_AUTOMATICO.get())) {
			layout.show(parametros, POUSO_AUTOMATICO.get());
		}
		if (evt.getPropertyName().equals(MANOBRAS.get())) {
			layout.show(parametros, MANOBRAS.get());
		}
		if (evt.getPropertyName().equals(ROVER_AUTONOMO.get())) {
			layout.show(parametros, ROVER_AUTONOMO.get());
		}
		if (evt.getPropertyName().equals(TELEMETRIA.get())) {
			layout.show(parametros, TELEMETRIA.get());
		}
	}

	public TelemetriaJPanel getTelemetria() {
		return pnlTelemetria;
	}
}
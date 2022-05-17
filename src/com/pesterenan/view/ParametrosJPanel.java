package com.pesterenan.view;

import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import static com.pesterenan.utils.Dicionario.*;

public class ParametrosJPanel extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	private EmptyBorder bordaVazia = new EmptyBorder(5, 5, 5, 5);

	private JPanel telemetria = new TelemetriaJPanel();
	private JPanel decolagem = new DecolagemJPanel();
	private JPanel manobras = new ManobrasJPanel();
	private JPanel pouso = new PousoAutomaticoJPanel();
	
	private CardLayout layout = new CardLayout();

	public ParametrosJPanel() {
		setLayout(layout);
		setBorder(BorderFactory.createCompoundBorder(((Border) BorderFactory.createCompoundBorder(bordaVazia,
				BorderFactory.createTitledBorder(TXT_PARAMETROS.get()))), bordaVazia));
		add(telemetria, TELEMETRIA.get());
		add(decolagem, DECOLAGEM_ORBITAL.get());
		add(pouso, POUSO_AUTOMATICO.get());
		add(manobras, MANOBRAS.get());
		addPropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(DECOLAGEM_ORBITAL.get())){
			layout.show(this, DECOLAGEM_ORBITAL.get());
		}
		if (evt.getPropertyName().equals(POUSO_AUTOMATICO.get())){
			layout.show(this, POUSO_AUTOMATICO.get());
		}
		if (evt.getPropertyName().equals(MANOBRAS.get())){
			layout.show(this, MANOBRAS.get());
		}
		if (evt.getPropertyName().equals(TELEMETRIA.get())){
			layout.show(this, TELEMETRIA.get());
		}
	}

}
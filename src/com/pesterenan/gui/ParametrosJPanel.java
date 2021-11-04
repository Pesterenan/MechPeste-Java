package com.pesterenan.gui;

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
	private CardLayout layout = new CardLayout();

	public ParametrosJPanel() {
		setLayout(layout);
		setBorder(BorderFactory.createCompoundBorder(((Border) BorderFactory.createCompoundBorder(bordaVazia,
				BorderFactory.createTitledBorder(TXT_PARAMETROS.get()))), bordaVazia));
		add(telemetria, TELEMETRIA.get());
		add(decolagem, DECOLAGEM_ORBITAL.get());
		addPropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(DECOLAGEM_ORBITAL.get())){
			layout.show(this, DECOLAGEM_ORBITAL.get());
		}
		if (evt.getPropertyName().equals(TELEMETRIA.get())){
			layout.show(this, TELEMETRIA.get());
		}
	}

}

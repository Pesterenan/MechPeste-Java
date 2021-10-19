package com.pesterenan.gui;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class ParametrosJPanel extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	private EmptyBorder bordaVazia = new EmptyBorder(5, 5, 5, 5);

	private JPanel telemetria = new TelemetriaJPanel();
	private JPanel decolagem = new DecolagemJPanel();

	public ParametrosJPanel() {
		setBorder(BorderFactory.createCompoundBorder(((Border) BorderFactory.createCompoundBorder(bordaVazia,
				BorderFactory.createTitledBorder("Parâmetros:"))), bordaVazia));
		setLayout(new CardLayout());
		add(FuncoesJPanel.telemetria, telemetria);
		add(FuncoesJPanel.decolagemOrbital, decolagem);
		addPropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	switch (evt.getPropertyName()) {
		case FuncoesJPanel.decolagemOrbital:
			CardLayout pp = (CardLayout) getLayout();
			pp.show(this, FuncoesJPanel.decolagemOrbital);
			break;
		case FuncoesJPanel.pousoAutomatico:
			System.out.println("Pouso!");
			break;
		}
	}



}

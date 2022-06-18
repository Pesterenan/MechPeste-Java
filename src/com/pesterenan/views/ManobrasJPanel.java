package com.pesterenan.views;

import static com.pesterenan.utils.Dicionario.TELEMETRIA;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;

import com.pesterenan.MechPeste;
import com.pesterenan.utils.Modulos;

public class ManobrasJPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private final JLabel lblCircularizar = new JLabel("Circularizar órbita no:");
	private final JLabel lblExecutar = new JLabel("Executar próxima manobra: ");
	private final JLabel lblAjustar = new JLabel("Ajustar inclinação:");
	private final JButton btnApoastro = new JButton("Apoastro");
	private final JButton btnPeriastro = new JButton("Periastro");
	private final JButton btnExecutar = new JButton("Executar");
	private final JButton btnAjustar = new JButton("Ajustar");
	private final JButton btnVoltar = new JButton("Voltar");

	public ManobrasJPanel() {

		initComponents();
	}

	private void initComponents() {
		setPreferredSize(ParametrosJPanel.dmsParametros);
		setSize(ParametrosJPanel.dmsParametros);
		setBorder(new TitledBorder(null, "Manobras:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		btnExecutar.addActionListener(this);
		btnExecutar.setSize(ParametrosJPanel.BTN_DIMENSION);
		btnExecutar.setPreferredSize(btnExecutar.getSize());
		btnExecutar.setMinimumSize(btnExecutar.getSize());
		btnExecutar.setMaximumSize(btnExecutar.getSize());
		btnExecutar.setActionCommand(Modulos.EXECUTAR.get());
		btnApoastro.addActionListener(this);
		btnApoastro.setSize(ParametrosJPanel.BTN_DIMENSION);
		btnApoastro.setPreferredSize(btnApoastro.getSize());
		btnApoastro.setMinimumSize(btnApoastro.getSize());
		btnApoastro.setMaximumSize(btnApoastro.getSize());
		btnApoastro.setActionCommand(Modulos.APOASTRO.get());
		btnPeriastro.addActionListener(this);
		btnPeriastro.setSize(ParametrosJPanel.BTN_DIMENSION);
		btnPeriastro.setPreferredSize(btnPeriastro.getSize());
		btnPeriastro.setMinimumSize(btnPeriastro.getSize());
		btnPeriastro.setMaximumSize(btnPeriastro.getSize());
		btnPeriastro.setActionCommand(Modulos.PERIASTRO.get());
		btnAjustar.addActionListener(this);
		btnAjustar.setSize(ParametrosJPanel.BTN_DIMENSION);
		btnAjustar.setPreferredSize(btnAjustar.getSize());
		btnAjustar.setMinimumSize(btnAjustar.getSize());
		btnAjustar.setMaximumSize(btnAjustar.getSize());
		btnAjustar.setActionCommand(Modulos.AJUSTAR.get());
		btnAjustar.setEnabled(false);
		btnVoltar.addActionListener(this);

		btnVoltar.setSize(ParametrosJPanel.BTN_DIMENSION);
		btnVoltar.setPreferredSize(btnVoltar.getSize());
		btnVoltar.setMinimumSize(btnVoltar.getSize());
		btnVoltar.setMaximumSize(btnVoltar.getSize());
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup().addContainerGap()
				.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(btnVoltar)
						.addGroup(groupLayout.createSequentialGroup()
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblExecutar)
										.addComponent(lblCircularizar).addComponent(lblAjustar))
								.addGap(18)
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
										.addComponent(btnExecutar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(btnApoastro, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(btnPeriastro, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(btnAjustar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE))))
				.addGap(124)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblExecutar).addComponent(
						btnExecutar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(12)
				.addGroup(groupLayout
						.createParallelGroup(Alignment.BASELINE).addComponent(lblCircularizar).addComponent(btnApoastro,
								GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(
						btnPeriastro, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(12)
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblAjustar).addComponent(
						btnAjustar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(12).addContainerGap().addComponent(btnVoltar)));
		setLayout(groupLayout);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnExecutar) {
			handleManeuverFunction(Modulos.EXECUTAR.get());
		}
		if (e.getSource() == btnApoastro) {
			handleManeuverFunction(Modulos.APOASTRO.get());
		}
		if (e.getSource() == btnPeriastro) {
			handleManeuverFunction(Modulos.PERIASTRO.get());
		}
		if (e.getSource() == btnAjustar) {
			handleManeuverFunction(Modulos.AJUSTAR.get());
		}
		if (e.getSource() == btnVoltar) {
			handleBtnVoltarActionPerformed(e);
		}
	}

	protected void handleBtnVoltarActionPerformed(ActionEvent e) {
		MainGui.getParametros().firePropertyChange(TELEMETRIA.get(), false, true);
	}

	protected void handleManeuverFunction(String maneuverFunction) {
		Map<String, String> commands = new HashMap<>();
		commands.put(Modulos.MODULO.get(), Modulos.MODULO_MANOBRAS.get());
		commands.put(Modulos.FUNCAO.get(), maneuverFunction);
		MechPeste.iniciarModulo(commands);
	}
}

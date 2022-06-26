package com.pesterenan.views;

import static com.pesterenan.utils.Dictionary.TELEMETRY;

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
import com.pesterenan.utils.Modules;

public class ManeuverJPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private final JLabel lblCircularize = new JLabel("Circularizar órbita no:");
	private final JLabel lblExecute = new JLabel("Executar próxima manobra: ");
	private final JLabel lblAdjustInc = new JLabel("Ajustar inclinação:");
	private final JButton btnApoapsis = new JButton("Apoastro");
	private final JButton btnPeriapsis = new JButton("Periastro");
	private final JButton btnExecute = new JButton("Executar");
	private final JButton btnAdjustInc = new JButton("Ajustar");
	private final JButton btnBack = new JButton("Voltar");

	public ManeuverJPanel() {
		initComponents();
	}

	private void initComponents() {
		setPreferredSize(ParametersJPanel.dmsParameters);
		setSize(ParametersJPanel.dmsParameters);
		setBorder(new TitledBorder(null, "Manobras:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		btnExecute.addActionListener(this);
		btnExecute.setSize(ParametersJPanel.BTN_DIMENSION);
		btnExecute.setPreferredSize(btnExecute.getSize());
		btnExecute.setMinimumSize(btnExecute.getSize());
		btnExecute.setMaximumSize(btnExecute.getSize());
		btnExecute.setActionCommand(Modules.EXECUTE.get());
		btnApoapsis.addActionListener(this);
		btnApoapsis.setSize(ParametersJPanel.BTN_DIMENSION);
		btnApoapsis.setPreferredSize(btnApoapsis.getSize());
		btnApoapsis.setMinimumSize(btnApoapsis.getSize());
		btnApoapsis.setMaximumSize(btnApoapsis.getSize());
		btnApoapsis.setActionCommand(Modules.APOAPSIS.get());
		btnPeriapsis.addActionListener(this);
		btnPeriapsis.setSize(ParametersJPanel.BTN_DIMENSION);
		btnPeriapsis.setPreferredSize(btnPeriapsis.getSize());
		btnPeriapsis.setMinimumSize(btnPeriapsis.getSize());
		btnPeriapsis.setMaximumSize(btnPeriapsis.getSize());
		btnPeriapsis.setActionCommand(Modules.PERIAPSIS.get());
		btnAdjustInc.addActionListener(this);
		btnAdjustInc.setSize(ParametersJPanel.BTN_DIMENSION);
		btnAdjustInc.setPreferredSize(btnAdjustInc.getSize());
		btnAdjustInc.setMinimumSize(btnAdjustInc.getSize());
		btnAdjustInc.setMaximumSize(btnAdjustInc.getSize());
		btnAdjustInc.setActionCommand(Modules.AJUST.get());
		btnAdjustInc.setEnabled(false);
		btnBack.addActionListener(this);

		btnBack.setSize(ParametersJPanel.BTN_DIMENSION);
		btnBack.setPreferredSize(btnBack.getSize());
		btnBack.setMinimumSize(btnBack.getSize());
		btnBack.setMaximumSize(btnBack.getSize());
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup().addContainerGap()
				.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(btnBack)
						.addGroup(groupLayout.createSequentialGroup()
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblExecute)
										.addComponent(lblCircularize).addComponent(lblAdjustInc))
								.addGap(18)
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
										.addComponent(btnExecute, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(btnApoapsis, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(btnPeriapsis, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(btnAdjustInc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE))))
				.addGap(124)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblExecute).addComponent(
						btnExecute, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(12)
				.addGroup(groupLayout
						.createParallelGroup(Alignment.BASELINE).addComponent(lblCircularize).addComponent(btnApoapsis,
								GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(
						btnPeriapsis, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(12)
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblAdjustInc).addComponent(
						btnAdjustInc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(12).addContainerGap().addComponent(btnBack)));
		setLayout(groupLayout);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnExecute) {
			handleManeuverFunction(Modules.EXECUTE.get());
		}
		if (e.getSource() == btnApoapsis) {
			handleManeuverFunction(Modules.APOAPSIS.get());
		}
		if (e.getSource() == btnPeriapsis) {
			handleManeuverFunction(Modules.PERIAPSIS.get());
		}
		if (e.getSource() == btnAdjustInc) {
			handleManeuverFunction(Modules.AJUST.get());
		}
		if (e.getSource() == btnBack) {
			handleBtnVoltarActionPerformed(e);
		}
	}

	protected void handleBtnVoltarActionPerformed(ActionEvent e) {
		MainGui.getParameters().firePropertyChange(TELEMETRY.get(), false, true);
	}

	protected void handleManeuverFunction(String maneuverFunction) {
		Map<String, String> commands = new HashMap<>();
		commands.put(Modules.MODULE.get(), Modules.MANEUVER_MODULE.get());
		commands.put(Modules.FUNCTION.get(), maneuverFunction);
		MechPeste.startModule(commands);
	}
}

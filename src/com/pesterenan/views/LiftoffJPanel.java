package com.pesterenan.views;

import static com.pesterenan.utils.Dictionary.TELEMETRY;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;

import com.pesterenan.MechPeste;
import com.pesterenan.utils.Modules;

public class LiftoffJPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private final JLabel lblFinalApoapsis = new JLabel("Apoastro final: ");
	private final JLabel lblHeading = new JLabel("Direção: ");
	private final JLabel lblRoll = new JLabel("Rolagem:");
	private final JLabel lblCurveModel = new JLabel("Modelo da Curva Gravitacional:");
	private final JTextField txfFinalApoapsis = new JTextField("80000");
	private final JTextField txfHeading = new JTextField("90");
	private final JButton btnTakeOff = new JButton("Decolagem");
	private final JButton btnBack = new JButton("Voltar");
	private final JComboBox<String> cbGravityCurveModel = new JComboBox<String>();
	private JSlider sldRoll;

	public LiftoffJPanel() {
		initComponents();
	}

	private void initComponents() {
		setPreferredSize(ParametersJPanel.dmsParameters);
		setSize(ParametersJPanel.dmsParameters);
		setBorder(new TitledBorder(null, "Decolagem Orbital", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		lblFinalApoapsis.setLabelFor(txfFinalApoapsis);
		txfFinalApoapsis.setText("80000");
		txfFinalApoapsis.setToolTipText("Altura de apoapsis final em metros para a decolagem.");

		btnTakeOff.addActionListener(this);
		btnTakeOff.setSize(ParametersJPanel.BTN_DIMENSION);
		btnTakeOff.setPreferredSize(btnTakeOff.getSize());
		btnTakeOff.setMinimumSize(btnTakeOff.getSize());
		btnTakeOff.setMaximumSize(btnTakeOff.getSize());
		btnBack.addActionListener(this);
		btnBack.setSize(ParametersJPanel.BTN_DIMENSION);
		btnBack.setPreferredSize(btnBack.getSize());
		btnBack.setMinimumSize(btnBack.getSize());
		btnBack.setMaximumSize(btnBack.getSize());

		cbGravityCurveModel.setToolTipText(
				"Escolha o modelo da curva gravitacional a ser realizada.\r\nVai da mais leve (Sinusoidal) até a mais exagerada (Exponencial).");
		cbGravityCurveModel.setModel(new DefaultComboBoxModel<String>(new String[] { Modules.SINUSOIDAL.get(),
				Modules.QUADRATIC.get(), Modules.CUBIC.get(), Modules.CIRCLE.get(), Modules.EXPONENTIAL.get() }));
		cbGravityCurveModel.setSelectedIndex(3);

		lblRoll.setToolTipText("Direção para qual a nave irá \"rolar\" durante o lançamento, em graus.");

		sldRoll = new JSlider();
		sldRoll.setPaintLabels(true);
		sldRoll.setMajorTickSpacing(90);
		sldRoll.setMaximum(270);
		sldRoll.setSnapToTicks(true);
		sldRoll.setValue(90);

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(groupLayout
				.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout
								.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup().addContainerGap().addGroup(groupLayout
										.createParallelGroup(Alignment.LEADING)
										.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
												.addComponent(lblHeading, Alignment.LEADING).addComponent(
														lblFinalApoapsis, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
														GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addComponent(lblCurveModel)))
								.addComponent(btnTakeOff, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
								.addGap(22))
						.addGroup(groupLayout.createSequentialGroup().addContainerGap()
								.addComponent(lblRoll, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addPreferredGap(ComponentPlacement.RELATED)))
				.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(cbGravityCurveModel, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(txfHeading, Alignment.TRAILING).addComponent(txfFinalApoapsis,
										Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
						.addComponent(btnBack, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(sldRoll, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(txfFinalApoapsis, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(lblFinalApoapsis))
				.addGap(3)
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblHeading).addComponent(
						txfHeading, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblRoll).addComponent(sldRoll,
						GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
				.addGap(32)
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblCurveModel).addComponent(
						cbGravityCurveModel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE))
				.addGap(36)
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnBack, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(btnTakeOff, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(37)));
		setLayout(groupLayout);
	}

	private boolean validateTextFields() {
		try {
			Float.parseFloat(txfFinalApoapsis.getText());
			Float.parseFloat(txfHeading.getText());
		} catch (NumberFormatException e) {
			StatusJPanel.setStatus("Erro: Os campos só aceitam números.");
			return false;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnBack) {
			handleBtnBackActionPerformed(e);
		}
		if (e.getSource() == btnTakeOff) {
			handleBtnTakeOffActionPerformed(e);
		}
	}

	protected void handleBtnTakeOffActionPerformed(ActionEvent e) {
		if (validateTextFields()) {
			Map<String, String> commands = new HashMap<>();
			commands.put(Modules.MODULE.get(), Modules.LIFTOFF_MODULE.get());
			commands.put(Modules.APOAPSIS.get(), txfFinalApoapsis.getText());
			commands.put(Modules.DIRECTION.get(), txfHeading.getText());
			commands.put(Modules.ROLL.get(), String.valueOf(sldRoll.getValue()));
			commands.put(Modules.INCLINATION.get(), cbGravityCurveModel.getSelectedItem().toString());
			MechPeste.startModule(commands);
		}
	}

	protected void handleBtnBackActionPerformed(ActionEvent e) {
		MainGui.getParameters().firePropertyChange(TELEMETRY.get(), false, true);
	}
}

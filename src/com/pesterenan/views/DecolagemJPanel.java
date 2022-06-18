package com.pesterenan.views;

import static com.pesterenan.utils.Dicionario.TELEMETRIA;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;

import com.pesterenan.MechPeste;
import com.pesterenan.utils.Modulos;

public class DecolagemJPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JLabel lblFinalApoapsis = new JLabel("Apoastro final: ");
	private JLabel lblHeading = new JLabel("Direção: ");
	private JFormattedTextField txfFinalApoapsis = new JFormattedTextField();
	private JTextField txfHeading = new JTextField("90");
	private final JButton btnTakeOff = new JButton("Decolagem");
	private final JButton btnBack = new JButton("Voltar");
	private final JLabel lblModeloDaCurva = new JLabel("Modelo da Curva Gravitacional:");
	private JComboBox<String> cbGravityCurveModel;

	public DecolagemJPanel() {
		initComponents();
	}

	private void initComponents() {
		setPreferredSize(ParametrosJPanel.dmsParametros);
		setSize(ParametrosJPanel.dmsParametros);
		setBorder(new TitledBorder(null, "Decolagem Orbital", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		lblFinalApoapsis.setLabelFor(txfFinalApoapsis);
		txfFinalApoapsis.setText("80000");
		txfFinalApoapsis.setToolTipText("Altura de apoastro final em metros para a decolagem.");

		btnTakeOff.addActionListener(this);
		btnTakeOff.setSize(ParametrosJPanel.BTN_DIMENSION);
		btnTakeOff.setPreferredSize(btnTakeOff.getSize());
		btnTakeOff.setMinimumSize(btnTakeOff.getSize());
		btnTakeOff.setMaximumSize(btnTakeOff.getSize());
		btnBack.addActionListener(this);
		btnBack.setSize(ParametrosJPanel.BTN_DIMENSION);
		btnBack.setPreferredSize(btnBack.getSize());
		btnBack.setMinimumSize(btnBack.getSize());
		btnBack.setMaximumSize(btnBack.getSize());

		cbGravityCurveModel = new JComboBox<String>();
		cbGravityCurveModel.setToolTipText(
				"Escolha o modelo da curva gravitacional a ser realizada.\r\nVai da mais leve (Sinusoidal) até a mais exagerada (Exponencial).");
		cbGravityCurveModel.setModel(new DefaultComboBoxModel<String>(new String[] { Modulos.SINUSOIDAL.get(),
				Modulos.QUADRATICA.get(), Modulos.CUBICA.get(), Modulos.CIRCULAR.get(), Modulos.EXPONENCIAL.get() }));
		cbGravityCurveModel.setSelectedIndex(3);

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup().addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(btnTakeOff, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
										.addComponent(
												btnBack, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE))
								.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup().addGroup(groupLayout
										.createParallelGroup(Alignment.TRAILING)
										.addGroup(groupLayout.createSequentialGroup()
												.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
														.addComponent(lblHeading).addComponent(lblFinalApoapsis))
												.addGap(98))
										.addGroup(groupLayout.createSequentialGroup().addComponent(lblModeloDaCurva)
												.addGap(12)))
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
												.addComponent(cbGravityCurveModel, 0, GroupLayout.DEFAULT_SIZE,
														Short.MAX_VALUE)
												.addComponent(txfHeading, Alignment.TRAILING)
												.addComponent(txfFinalApoapsis, Alignment.TRAILING,
														GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))))
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
				.addGap(15)
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblModeloDaCurva)
						.addComponent(cbGravityCurveModel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(82)
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnBack, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(btnTakeOff, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		setLayout(groupLayout);
	}

	private boolean validarCampos() {
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
		if (validarCampos()) {
			Map<String, String> comandos = new HashMap<>();
			comandos.put(Modulos.MODULO.get(), Modulos.MODULO_DECOLAGEM.get());
			comandos.put(Modulos.APOASTRO.get(), txfFinalApoapsis.getText());
			comandos.put(Modulos.DIRECAO.get(), txfHeading.getText());
			comandos.put(Modulos.INCLINACAO.get(), cbGravityCurveModel.getSelectedItem().toString());
			MechPeste.iniciarModulo(comandos);
		}
	}

	protected void handleBtnBackActionPerformed(ActionEvent e) {
		MainGui.getParametros().firePropertyChange(TELEMETRIA.get(), 0, 1);
	}
}

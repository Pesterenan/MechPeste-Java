package com.pesterenan.views;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.pesterenan.MechPeste;
import com.pesterenan.utils.Dictionary;
import com.pesterenan.utils.Modules;

public class PousoAutomaticoJPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JTextField txfHover = new JTextField("100");
	private JButton btnHover = new JButton("Sobrevoar");
	private JButton btnAutoLanding = new JButton("Pousar");
	private JButton btnBack = new JButton("Voltar");
	private final JPanel pnlHover = new JPanel();
	private JLabel lblAlt;
	private final JLabel lblAutolanding = new JLabel("Executar Pouso Automático: ");

	public PousoAutomaticoJPanel() {

		initComponents();
	}

	private void initComponents() {
		setPreferredSize(ParametersJPanel.dmsParameters);
		setSize(ParametersJPanel.dmsParameters);
		setBorder(new TitledBorder(null, "Pouso Autom\u00E1tico", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		txfHover.setHorizontalAlignment(SwingConstants.CENTER);
		txfHover.setColumns(10);

		btnHover.addActionListener(this);
		btnHover.setSize(ParametersJPanel.BTN_DIMENSION);
		btnHover.setPreferredSize(btnHover.getSize());
		btnHover.setMinimumSize(btnHover.getSize());
		btnHover.setMaximumSize(btnHover.getSize());

		btnBack.addActionListener(this);
		btnBack.setSize(ParametersJPanel.BTN_DIMENSION);
		btnBack.setPreferredSize(btnBack.getSize());
		btnBack.setMinimumSize(btnBack.getSize());
		btnBack.setMaximumSize(btnBack.getSize());

		btnAutoLanding.addActionListener(this);
		btnAutoLanding.setSize(ParametersJPanel.BTN_DIMENSION);
		btnAutoLanding.setPreferredSize(btnAutoLanding.getSize());
		btnAutoLanding.setMinimumSize(btnAutoLanding.getSize());
		btnAutoLanding.setMaximumSize(btnAutoLanding.getSize());

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(lblAutolanding)
								.addPreferredGap(ComponentPlacement.RELATED, 105, Short.MAX_VALUE)
								.addComponent(btnAutoLanding, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGroup(Alignment.TRAILING,
								groupLayout.createSequentialGroup().addContainerGap().addComponent(btnBack,
										GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup().addGap(6)
								.addComponent(pnlHover, GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)))
				.addGap(6)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblAutolanding).addComponent(
						btnAutoLanding, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(pnlHover, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED, 62, Short.MAX_VALUE)
				.addComponent(btnBack, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		setLayout(groupLayout);
		pnlHover.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Sobrevoar a \u00E1rea:", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));

		lblAlt = new JLabel("Alt:");
		GroupLayout glPnlHover = new GroupLayout(pnlHover);
		glPnlHover.setHorizontalGroup(glPnlHover.createParallelGroup(Alignment.TRAILING)
				.addGroup(glPnlHover.createSequentialGroup().addContainerGap().addComponent(lblAlt)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(txfHover, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED, 21, Short.MAX_VALUE).addComponent(btnHover)));
		glPnlHover.setVerticalGroup(glPnlHover.createParallelGroup(Alignment.LEADING)
				.addGroup(glPnlHover.createSequentialGroup().addGap(5)
						.addGroup(glPnlHover.createParallelGroup(Alignment.BASELINE).addComponent(btnHover)
								.addComponent(lblAlt).addComponent(txfHover, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addContainerGap(17, Short.MAX_VALUE)));
		pnlHover.setLayout(glPnlHover);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnHover) {
			handleBtnHoverActionPerformed(e);
		}
		if (e.getSource() == btnBack) {
			handleBtnBackActionPerformed(e);
		}
		if (e.getSource() == btnAutoLanding) {
			handleBtnAutoLandingActionPerformed(e);
		}
	}

	protected void handleBtnAutoLandingActionPerformed(ActionEvent e) {
		Map<String, String> commands = new HashMap<>();
		commands.put(Modules.MODULE.get(), Modules.LANDING_MODULE.get());
		MechPeste.startModule(commands);
	}

	protected void handleBtnBackActionPerformed(ActionEvent e) {
		MainGui.getParameters().firePropertyChange(Dictionary.TELEMETRY.get(), false, true);
	}

	protected void handleBtnHoverActionPerformed(ActionEvent e) {
		try {
			if (txfHover.getText().equals("")) {
				StatusJPanel.setStatus("Digite a altitude para sobrevoo.");
				return;
			}
			Integer.parseInt(txfHover.getText());
		} catch (Exception e2) {
			StatusJPanel.setStatus("A altitude para sobrevoo tem que ser um número.");
			return;
		}
		Map<String, String> commands = new HashMap<>();
		commands.put(Modules.MODULE.get(), Modules.LANGING_FLIGHT_MODULE.get());
		commands.put(Modules.FLIGHT_ALTITUDE.get(), txfHover.getText());
		MechPeste.startModule(commands);
		btnHover.setText("Descer");
	}
}

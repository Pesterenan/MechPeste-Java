package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Modulos;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import static com.pesterenan.views.MainGui.BTN_DIMENSION;

public class RoverJPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -3157549581689803329L;
	private final JLabel lblWaypointName = new JLabel(Bundle.getString("pnl_rover_waypoint_name"));
	//$NON-NLS-1$
	private final JTextField txfWaypointName = new JTextField(Bundle.getString("pnl_rover_default_name"));
	private final JButton btnBack = new JButton();
	private final JButton btnDrive = new JButton();
	private final JPanel pnlTargetChoice = new JPanel();
	private final ButtonGroup bgChoice = new ButtonGroup();
	private JRadioButton rbTargetVessel;
	private JRadioButton rbWaypointOnMap;
	private JLabel lblMaxSpeed;
	private JTextField txfMaxSpeed;

	public RoverJPanel() {
		initComponents();
	}

	private void initComponents() {
		setPreferredSize(MainGui.dmsPanels);
		setSize(MainGui.dmsPanels);
		setBorder(new TitledBorder(null, Bundle.getString("pnl_rover_border"), TitledBorder.LEADING,
		                           // $NON-NLS-1$
		                           TitledBorder.TOP, null, null
		));

		btnBack.setText(Bundle.getString("pnl_rover_btn_back")); //$NON-NLS-1$
		btnBack.addActionListener(this);
		btnBack.setSize(BTN_DIMENSION);
		btnBack.setPreferredSize(btnBack.getSize());
		btnBack.setMinimumSize(btnBack.getSize());
		btnBack.setMaximumSize(btnBack.getSize());

		btnDrive.setText(Bundle.getString("pnl_rover_btn_drive")); //$NON-NLS-1$
		btnDrive.addActionListener(this);
		btnDrive.setSize(BTN_DIMENSION);
		btnDrive.setPreferredSize(btnDrive.getSize());
		btnDrive.setMinimumSize(btnDrive.getSize());
		btnDrive.setMaximumSize(btnDrive.getSize());

		txfWaypointName.setHorizontalAlignment(SwingConstants.CENTER);
		txfWaypointName.setColumns(10);

		lblMaxSpeed = new JLabel(Bundle.getString("pnl_rover_lbl_max_speed"));

		txfMaxSpeed = new JTextField();
		txfMaxSpeed.setText("10"); //$NON-NLS-1$
		txfMaxSpeed.setColumns(10);

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
		                                          .addGroup(groupLayout.createSequentialGroup()
		                                                               .addContainerGap()
		                                                               .addGroup(groupLayout.createParallelGroup(
				                                                                                    Alignment.LEADING)
		                                                                                    .addComponent(
				                                                                                    pnlTargetChoice,
				                                                                                    GroupLayout.DEFAULT_SIZE,
				                                                                                    286,
				                                                                                    Short.MAX_VALUE
		                                                                                                 )
		                                                                                    .addGroup(
				                                                                                    groupLayout.createSequentialGroup()
				                                                                                               .addComponent(
						                                                                                               btnDrive,
						                                                                                               GroupLayout.PREFERRED_SIZE,
						                                                                                               GroupLayout.DEFAULT_SIZE,
						                                                                                               GroupLayout.PREFERRED_SIZE
				                                                                                                            )
				                                                                                               .addGap(66)
				                                                                                               .addComponent(
						                                                                                               btnBack,
						                                                                                               GroupLayout.PREFERRED_SIZE,
						                                                                                               GroupLayout.DEFAULT_SIZE,
						                                                                                               GroupLayout.PREFERRED_SIZE
				                                                                                                            ))
		                                                                                    .addGroup(
				                                                                                    groupLayout.createSequentialGroup()
				                                                                                               .addGroup(
						                                                                                               groupLayout.createParallelGroup(
								                                                                                                          Alignment.LEADING)
						                                                                                                          .addComponent(
								                                                                                                          lblWaypointName)
						                                                                                                          .addComponent(
								                                                                                                          lblMaxSpeed,
								                                                                                                          GroupLayout.PREFERRED_SIZE,
								                                                                                                          94,
								                                                                                                          GroupLayout.PREFERRED_SIZE
						                                                                                                                       ))
				                                                                                               .addPreferredGap(
						                                                                                               ComponentPlacement.UNRELATED)
				                                                                                               .addGroup(
						                                                                                               groupLayout.createParallelGroup(
								                                                                                                          Alignment.LEADING)
						                                                                                                          .addComponent(
								                                                                                                          txfMaxSpeed,
								                                                                                                          GroupLayout.PREFERRED_SIZE,
								                                                                                                          46,
								                                                                                                          GroupLayout.PREFERRED_SIZE
						                                                                                                                       )
						                                                                                                          .addComponent(
								                                                                                                          txfWaypointName,
								                                                                                                          GroupLayout.DEFAULT_SIZE,
								                                                                                                          197,
								                                                                                                          Short.MAX_VALUE
						                                                                                                                       ))))
		                                                               .addGap(6)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
		                                        .addGroup(groupLayout.createSequentialGroup()
		                                                             .addComponent(pnlTargetChoice,
		                                                                           GroupLayout.PREFERRED_SIZE, 60,
		                                                                           GroupLayout.PREFERRED_SIZE
		                                                                          )
		                                                             .addPreferredGap(ComponentPlacement.RELATED)
		                                                             .addGroup(groupLayout.createParallelGroup(
				                                                                                  Alignment.BASELINE)
		                                                                                  .addComponent(lblWaypointName)
		                                                                                  .addComponent(txfWaypointName,
		                                                                                                GroupLayout.PREFERRED_SIZE,
		                                                                                                GroupLayout.DEFAULT_SIZE,
		                                                                                                GroupLayout.PREFERRED_SIZE
		                                                                                               ))
		                                                             .addPreferredGap(ComponentPlacement.RELATED)
		                                                             .addGroup(groupLayout.createParallelGroup(
				                                                                                  Alignment.BASELINE)
		                                                                                  .addComponent(lblMaxSpeed)
		                                                                                  .addComponent(txfMaxSpeed,
		                                                                                                GroupLayout.PREFERRED_SIZE,
		                                                                                                GroupLayout.DEFAULT_SIZE,
		                                                                                                GroupLayout.PREFERRED_SIZE
		                                                                                               ))
		                                                             .addPreferredGap(ComponentPlacement.RELATED, 52,
		                                                                              Short.MAX_VALUE
		                                                                             )
		                                                             .addGroup(groupLayout.createParallelGroup(
				                                                                                  Alignment.TRAILING)
		                                                                                  .addComponent(btnBack,
		                                                                                                GroupLayout.PREFERRED_SIZE,
		                                                                                                GroupLayout.DEFAULT_SIZE,
		                                                                                                GroupLayout.PREFERRED_SIZE
		                                                                                               )
		                                                                                  .addComponent(btnDrive,
		                                                                                                GroupLayout.PREFERRED_SIZE,
		                                                                                                GroupLayout.DEFAULT_SIZE,
		                                                                                                GroupLayout.PREFERRED_SIZE
		                                                                                               ))
		                                                             .addContainerGap()));
		setLayout(groupLayout);
		pnlTargetChoice.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				Bundle.getString("pnl_rover_target_choice"), TitledBorder.LEADING, TitledBorder.TOP, null,
				new Color(0, 0, 0)
		));

		rbTargetVessel = new JRadioButton(Bundle.getString("pnl_rover_target_vessel")); //$NON-NLS-1$
		rbTargetVessel.setActionCommand(Modulos.NAVE_ALVO.get());
		rbWaypointOnMap = new JRadioButton(Bundle.getString("pnl_rover_waypoint_on_map")); //$NON-NLS-1$
		rbWaypointOnMap.setActionCommand(Modulos.MARCADOR_MAPA.get());
		rbTargetVessel.setSelected(true);
		bgChoice.add(rbTargetVessel);
		bgChoice.add(rbWaypointOnMap);

		GroupLayout glPnlTargetChoice = new GroupLayout(pnlTargetChoice);
		glPnlTargetChoice.setHorizontalGroup(glPnlTargetChoice.createParallelGroup(Alignment.TRAILING)
		                                                      .addGroup(Alignment.LEADING,
		                                                                glPnlTargetChoice.createSequentialGroup()
		                                                                                 .addGap(32)
		                                                                                 .addComponent(rbTargetVessel)
		                                                                                 .addGap(18)
		                                                                                 .addComponent(rbWaypointOnMap)
		                                                                                 .addContainerGap(28,
		                                                                                                  Short.MAX_VALUE
		                                                                                                 )
		                                                               ));
		glPnlTargetChoice.setVerticalGroup(glPnlTargetChoice.createParallelGroup(Alignment.LEADING)
		                                                    .addGroup(glPnlTargetChoice.createSequentialGroup()
		                                                                               .addContainerGap()
		                                                                               .addGroup(
				                                                                               glPnlTargetChoice.createParallelGroup(
						                                                                                                Alignment.BASELINE)
				                                                                                                .addComponent(
						                                                                                                rbTargetVessel)
				                                                                                                .addComponent(
						                                                                                                rbWaypointOnMap))
		                                                                               .addContainerGap(18,
		                                                                                                Short.MAX_VALUE
		                                                                                               )));
		pnlTargetChoice.setLayout(glPnlTargetChoice);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnBack) {
			MainGui.backToTelemetry();
		}
		if (e.getSource() == btnDrive) {
			handleBtnDriveActionPerformed(e);
		}
	}

	protected void handleBtnDriveActionPerformed(ActionEvent e) {
		if (validateTextFields()) {
			Map<String, String> commands = new HashMap<>();
			commands.put(Modulos.MODULO.get(), Modulos.MODULO_ROVER.get());
			commands.put(Modulos.TIPO_ALVO_ROVER.get(), bgChoice.getSelection().getActionCommand());
			commands.put(Modulos.NOME_MARCADOR.get(), txfWaypointName.getText());
			commands.put(Modulos.VELOCIDADE_MAX.get(), txfMaxSpeed.getText());
			MechPeste.newInstance().startModule(commands);
		}
	}

	private boolean validateTextFields() {
		try {
			if (Float.parseFloat(txfMaxSpeed.getText()) < 3) {
				throw new NumberFormatException();
			}
			if (txfWaypointName.getText().equals("")) {
				throw new IllegalArgumentException();
			}
		} catch (NumberFormatException e) {
			StatusJPanel.setStatus(Bundle.getString("pnl_rover_max_speed_above_3")); //$NON-NLS-1$
			return false;
		} catch (IllegalArgumentException e) {
			StatusJPanel.setStatus(Bundle.getString("pnl_rover_waypoint_name_not_empty")); //$NON-NLS-1$
			return false;
		}
		return true;
	}
}
package com.pesterenan.views;

import com.pesterenan.MechPeste;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionEvent;

public class ChangeVesselDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JList<String> listActiveVessels;
	private JLabel lblPanelInfo;
	private JButton btnChangeToVessel;
	private JLabel lblVesselStatus;
	private JScrollPane scrollPane;
	private JPanel pnlSearchArea;
	private JRadioButton rbClosestVessels;
	private JRadioButton rbOnSameBody;
	private JRadioButton rbAllVessels;

	public ChangeVesselDialog() {
		initComponents();
	}

	private void initComponents() {
		setModal(true);
		setTitle("Troca de naves");
		setBounds(MainGui.centerDialogOnScreen());
		setModalityType(ModalityType.MODELESS);
		setResizable(false);

		lblPanelInfo = new JLabel("Use esse painel para verificar e trocar para as naves próximas.");

		btnChangeToVessel = new JButton("Mudar para");
		btnChangeToVessel.addActionListener(e -> handleBtnChangeToVesselActionPerformed(e));

		lblVesselStatus = new JLabel("Selecione uma nave na lista.");

		scrollPane = new JScrollPane();

		pnlSearchArea = new JPanel();
		pnlSearchArea.setBorder(
				new TitledBorder(null, "\u00C1rea de procura:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
		                                          .addGroup(groupLayout.createSequentialGroup()
		                                                               .addContainerGap()
		                                                               .addGroup(groupLayout.createParallelGroup(
				                                                                                    Alignment.LEADING)
		                                                                                    .addComponent(
				                                                                                    lblVesselStatus,
				                                                                                    GroupLayout.DEFAULT_SIZE,
				                                                                                    364,
				                                                                                    Short.MAX_VALUE
		                                                                                                 )
		                                                                                    .addComponent(lblPanelInfo)
		                                                                                    .addGroup(
				                                                                                    groupLayout.createSequentialGroup()
				                                                                                               .addGroup(
						                                                                                               groupLayout.createParallelGroup(
								                                                                                                          Alignment.LEADING,
								                                                                                                          false
						                                                                                                                              )
						                                                                                                          .addComponent(
								                                                                                                          pnlSearchArea,
								                                                                                                          GroupLayout.PREFERRED_SIZE,
								                                                                                                          179,
								                                                                                                          GroupLayout.PREFERRED_SIZE
						                                                                                                                       )
						                                                                                                          .addComponent(
								                                                                                                          btnChangeToVessel,
								                                                                                                          GroupLayout.PREFERRED_SIZE,
								                                                                                                          135,
								                                                                                                          GroupLayout.PREFERRED_SIZE
						                                                                                                                       ))
				                                                                                               .addPreferredGap(
						                                                                                               ComponentPlacement.RELATED)
				                                                                                               .addComponent(
						                                                                                               scrollPane,
						                                                                                               GroupLayout.PREFERRED_SIZE,
						                                                                                               179,
						                                                                                               GroupLayout.PREFERRED_SIZE
				                                                                                                            )))
		                                                               .addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
		                                        .addGroup(groupLayout.createSequentialGroup()
		                                                             .addContainerGap()
		                                                             .addComponent(lblPanelInfo)
		                                                             .addGroup(groupLayout.createParallelGroup(
				                                                                                  Alignment.TRAILING)
		                                                                                  .addGroup(
				                                                                                  groupLayout.createSequentialGroup()
				                                                                                             .addGap(8)
				                                                                                             .addComponent(
						                                                                                             pnlSearchArea,
						                                                                                             0,
						                                                                                             92,
						                                                                                             Short.MAX_VALUE
				                                                                                                          )
				                                                                                             .addPreferredGap(
						                                                                                             ComponentPlacement.RELATED)
				                                                                                             .addComponent(
						                                                                                             btnChangeToVessel,
						                                                                                             GroupLayout.PREFERRED_SIZE,
						                                                                                             25,
						                                                                                             GroupLayout.PREFERRED_SIZE
				                                                                                                          )
				                                                                                             .addGap(5))
		                                                                                  .addGroup(
				                                                                                  groupLayout.createSequentialGroup()
				                                                                                             .addPreferredGap(
						                                                                                             ComponentPlacement.RELATED)
				                                                                                             .addComponent(
						                                                                                             scrollPane,
						                                                                                             GroupLayout.PREFERRED_SIZE,
						                                                                                             GroupLayout.DEFAULT_SIZE,
						                                                                                             GroupLayout.PREFERRED_SIZE
				                                                                                                          )))
		                                                             .addPreferredGap(ComponentPlacement.UNRELATED)
		                                                             .addComponent(lblVesselStatus)
		                                                             .addGap(15)));
		ButtonGroup rbBtnGroup = new ButtonGroup();
		rbClosestVessels = new JRadioButton("Naves próximas (2km)");
		rbClosestVessels.addActionListener(e -> listActiveVessels.setModel(MechPeste.getActiveVessels("closest")));
		rbClosestVessels.setSelected(true);

		rbOnSameBody = new JRadioButton("No mesmo corpo celeste");
		rbOnSameBody.addActionListener(e -> listActiveVessels.setModel(MechPeste.getActiveVessels("samebody")));

		rbAllVessels = new JRadioButton("Todas as naves");
		rbAllVessels.addActionListener(e -> listActiveVessels.setModel(MechPeste.getActiveVessels("all")));
		rbBtnGroup.add(rbClosestVessels);
		rbBtnGroup.add(rbOnSameBody);
		rbBtnGroup.add(rbAllVessels);
		GroupLayout glPanel = new GroupLayout(pnlSearchArea);
		glPanel.setHorizontalGroup(glPanel.createParallelGroup(Alignment.LEADING)
		                                  .addGroup(glPanel.createSequentialGroup()
		                                                   .addContainerGap()
		                                                   .addGroup(glPanel.createParallelGroup(Alignment.LEADING)
		                                                                    .addGroup(glPanel.createSequentialGroup()
		                                                                                     .addComponent(rbOnSameBody,
		                                                                                                   GroupLayout.DEFAULT_SIZE,
		                                                                                                   161,
		                                                                                                   Short.MAX_VALUE
		                                                                                                  )
		                                                                                     .addContainerGap())
		                                                                    .addGroup(glPanel.createSequentialGroup()
		                                                                                     .addGroup(
				                                                                                     glPanel.createParallelGroup(
						                                                                                            Alignment.LEADING)
				                                                                                            .addGroup(
						                                                                                            glPanel.createSequentialGroup()
						                                                                                                   .addComponent(
								                                                                                                   rbClosestVessels,
								                                                                                                   GroupLayout.DEFAULT_SIZE,
								                                                                                                   GroupLayout.DEFAULT_SIZE,
								                                                                                                   Short.MAX_VALUE
						                                                                                                                )
						                                                                                                   .addGap(36))
				                                                                                            .addGroup(
						                                                                                            glPanel.createSequentialGroup()
						                                                                                                   .addComponent(
								                                                                                                   rbAllVessels,
								                                                                                                   GroupLayout.DEFAULT_SIZE,
								                                                                                                   131,
								                                                                                                   Short.MAX_VALUE
						                                                                                                                )
						                                                                                                   .addGap(42)))
		                                                                                     .addGap(0)))));
		glPanel.setVerticalGroup(glPanel.createParallelGroup(Alignment.LEADING)
		                                .addGroup(glPanel.createSequentialGroup()
		                                                 .addComponent(rbClosestVessels)
		                                                 .addPreferredGap(ComponentPlacement.RELATED)
		                                                 .addComponent(rbOnSameBody)
		                                                 .addPreferredGap(ComponentPlacement.RELATED)
		                                                 .addComponent(rbAllVessels)
		                                                 .addContainerGap(8, Short.MAX_VALUE)));
		pnlSearchArea.setLayout(glPanel);

		listActiveVessels = new JList<>(MechPeste.getActiveVessels("closest"));
		listActiveVessels.setToolTipText("Aqui são mostradas as naves próximas de acordo com o filtro da esquerda.");
		listActiveVessels.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(listActiveVessels);
		listActiveVessels.addListSelectionListener(e -> handleListActiveVesselsValueChanged(e));
		getContentPane().setLayout(groupLayout);
		setAlwaysOnTop(true);
		setVisible(true);
	}

	protected void handleBtnChangeToVesselActionPerformed(ActionEvent e) {
		if (listActiveVessels.getSelectedIndex() == -1) {
			return;
		}
		int vesselHashCode = Integer.parseInt(listActiveVessels.getSelectedValue().split(" - ")[0]);
		MechPeste.changeToVessel(vesselHashCode);
	}

	protected void handleListActiveVesselsValueChanged(ListSelectionEvent e) {
		String selected = listActiveVessels.getSelectedValue();
		if (selected != null) {
			int vesselHashCode = Integer.parseInt(selected.split(" - ")[0]);
			lblVesselStatus.setText(MechPeste.getVesselInfo(vesselHashCode));
			btnChangeToVessel.setEnabled(true);
			return;
		}
		lblVesselStatus.setText("Selecione uma nave da lista.");
		btnChangeToVessel.setEnabled(false);
	}

}

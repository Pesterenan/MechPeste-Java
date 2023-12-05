package com.pesterenan.views;

import com.pesterenan.MechPeste;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionEvent;

import static com.pesterenan.views.MainGui.BTN_DIMENSION;

public class ChangeVesselDialog extends JDialog implements UIMethods {

	private static final long serialVersionUID = 1L;
	private JLabel lblPanelInfo, lblVesselStatus;
	private JList<String> listActiveVessels;
	private JButton btnChangeToVessel;
	private JRadioButton rbClosestVessels, rbOnSameBody, rbAllVessels;

	public ChangeVesselDialog() {
		initComponents();
		setupComponents();
		layoutComponents();
	}

	@Override
	public void initComponents() {
		// Labels:
		lblPanelInfo = new JLabel("Use esse painel para verificar e trocar para as naves próximas.");
		lblVesselStatus = new JLabel("Selecione uma nave na lista.");

		// Buttons:
		btnChangeToVessel = new JButton("Mudar para");
		rbClosestVessels = new JRadioButton("Naves próximas (10km)");
		rbOnSameBody = new JRadioButton("No mesmo corpo celeste");
		rbAllVessels = new JRadioButton("Todas as naves");

		// Misc:
		listActiveVessels = new JList<>(MechPeste.getActiveVessels("closest"));
		listActiveVessels.setToolTipText("Aqui são mostradas as naves próximas de acordo com o filtro da esquerda.");
	}

	@Override
	public void setupComponents() {
		// Main Panel setup:
		setTitle("Troca de naves");
		setBounds(MainGui.centerDialogOnScreen());
		setModal(true);
		setModalityType(ModalityType.MODELESS);
		setResizable(false);
		setAlwaysOnTop(true);

		// Setting-up components:
		btnChangeToVessel.addActionListener(this::handleChangeToVessel);
		btnChangeToVessel.setPreferredSize(BTN_DIMENSION);
		btnChangeToVessel.setMaximumSize(BTN_DIMENSION);

		rbClosestVessels.setSelected(true);
		rbClosestVessels.addActionListener(this::handleBuildVesselList);
		rbOnSameBody.addActionListener(this::handleBuildVesselList);
		rbAllVessels.addActionListener(this::handleBuildVesselList);
		rbClosestVessels.setActionCommand("closest");
		rbOnSameBody.setActionCommand("samebody");
		rbAllVessels.setActionCommand("all");

		ButtonGroup rbBtnGroup = new ButtonGroup();
		rbBtnGroup.add(rbClosestVessels);
		rbBtnGroup.add(rbOnSameBody);
		rbBtnGroup.add(rbAllVessels);

		listActiveVessels.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listActiveVessels.addListSelectionListener(e -> handleListActiveVesselsValueChanged(e));
	}

	@Override
	public void layoutComponents() {

		JPanel pnlSearchArea = new JPanel();
		pnlSearchArea.setBorder(
				new TitledBorder(null, "\u00C1rea de procura:"));
		pnlSearchArea.setLayout(new BoxLayout(pnlSearchArea, BoxLayout.Y_AXIS));
		pnlSearchArea.add(rbClosestVessels);
		pnlSearchArea.add(rbOnSameBody);
		pnlSearchArea.add(rbAllVessels);

		JPanel pnlOptions = new JPanel();
		pnlOptions.setLayout(new BoxLayout(pnlOptions, BoxLayout.Y_AXIS));
		pnlOptions.add(pnlSearchArea);
		pnlOptions.add(Box.createVerticalStrut(10));
		pnlOptions.add(btnChangeToVessel);
		btnChangeToVessel.setAlignmentX(LEFT_ALIGNMENT);
		pnlSearchArea.setAlignmentX(LEFT_ALIGNMENT);

		JPanel pnlScroll = new JPanel();
		pnlScroll.setLayout(new BoxLayout(pnlScroll, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(listActiveVessels);
		pnlScroll.add(Box.createVerticalStrut(6));
		pnlScroll.add(scrollPane);
		pnlScroll.add(Box.createHorizontalStrut(190));

		JPanel pnlOptionsAndList = new JPanel();
		pnlOptionsAndList.setLayout(new BoxLayout(pnlOptionsAndList, BoxLayout.X_AXIS));
		pnlOptions.setAlignmentY(TOP_ALIGNMENT);
		pnlScroll.setAlignmentY(TOP_ALIGNMENT);
		pnlOptionsAndList.add(pnlOptions);
		pnlOptionsAndList.add(Box.createHorizontalStrut(5));
		pnlOptionsAndList.add(pnlScroll);

		JPanel pnlStatus = new JPanel();
		pnlStatus.setLayout(new BoxLayout(pnlStatus, BoxLayout.X_AXIS));
		pnlStatus.add(lblVesselStatus);
		pnlStatus.add(Box.createHorizontalGlue());
		pnlStatus.add(Box.createVerticalStrut(10));

		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
		pnlMain.setBorder(MainGui.MARGIN_BORDER_10_PX_LR);
		lblPanelInfo.setAlignmentX(CENTER_ALIGNMENT);
		pnlMain.add(lblPanelInfo);
		pnlOptionsAndList.setAlignmentY(TOP_ALIGNMENT);
		pnlMain.add(pnlOptionsAndList);
		pnlMain.add(Box.createVerticalStrut(5));
		pnlMain.add(pnlStatus);
		pnlMain.add(Box.createVerticalStrut(10));

		getContentPane().add(pnlMain);
		setVisible(true);
	}

	protected void handleChangeToVessel(ActionEvent e) {
		if (listActiveVessels.getSelectedIndex() == -1) {
			return;
		}
		int vesselHashCode = Integer.parseInt(listActiveVessels.getSelectedValue().split(" - ")[0]);
		MechPeste.changeToVessel(vesselHashCode);
	}

	protected void handleBuildVesselList(ActionEvent e) {
		listActiveVessels.setModel(MechPeste.getActiveVessels(e.getActionCommand()));
	}

	protected void handleListActiveVesselsValueChanged(ListSelectionEvent e) {
		String selectedValue = listActiveVessels.getSelectedValue();
		if (selectedValue == null) {
			lblVesselStatus.setText("Selecione uma nave da lista.");
			btnChangeToVessel.setEnabled(false);
			return;
		}
		int vesselId = Integer.parseInt(selectedValue.split(" - ")[0]);
		lblVesselStatus.setText(MechPeste.getVesselInfo(vesselId));
		btnChangeToVessel.setEnabled(true);
	}
}

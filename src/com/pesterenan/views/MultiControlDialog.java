package com.pesterenan.views;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;

import com.pesterenan.MechPeste;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JScrollPane;

public class MultiControlDialog extends JDialog implements ActionListener, ListSelectionListener {

	private static final long serialVersionUID = 1L;
	private JList<String> listActiveVessels;
	private JLabel lblMultiControlInfo;
	private JButton btnUpdateList;
	private JButton btnChangeToVessel;
	private JButton btnCleanList;
	private JLabel lblHowManyWillRun;
	private JScrollPane scrollPane;

	public MultiControlDialog() {
		initComponents();
	}

	private void initComponents() {
		setModal(true);
		setTitle("MultiControl");
		setBounds(MainGui.centerDialogOnScreen());
		setModalityType(ModalityType.MODELESS);

		lblMultiControlInfo = new JLabel("Use esse painel para controlar diversas naves próximas de uma vez");

		btnUpdateList = new JButton("Atualizar");
		btnUpdateList.addActionListener(this);

		btnChangeToVessel = new JButton("Mudar para");
		btnChangeToVessel.addActionListener(this);

		btnCleanList = new JButton("Limpar");
		btnCleanList.addActionListener(this);

		lblHowManyWillRun = new JLabel("Somente a nave atual executará a próxima ação.");

		scrollPane = new JScrollPane();
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup().addContainerGap()
				.addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
						.createParallelGroup(Alignment.LEADING).addComponent(lblMultiControlInfo, Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
								.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 202, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(btnChangeToVessel, GroupLayout.PREFERRED_SIZE, 135,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(btnCleanList, GroupLayout.PREFERRED_SIZE, 135,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(btnUpdateList, GroupLayout.PREFERRED_SIZE, 135,
												GroupLayout.PREFERRED_SIZE))))
						.addComponent(lblHowManyWillRun))
				.addContainerGap(31, Short.MAX_VALUE)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(lblMultiControlInfo)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 126, GroupLayout.PREFERRED_SIZE)
								.addGroup(groupLayout.createSequentialGroup().addComponent(btnUpdateList)
										.addPreferredGap(ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
										.addComponent(btnChangeToVessel).addGap(5).addComponent(btnCleanList)))
						.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(lblHowManyWillRun)
						.addContainerGap(36, Short.MAX_VALUE)));

		listActiveVessels = new JList<String>(MechPeste.getActiveVessels());
		scrollPane.setViewportView(listActiveVessels);
		listActiveVessels.addListSelectionListener(this);
		getContentPane().setLayout(groupLayout);
		setAlwaysOnTop(true);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnCleanList) {
			handleBtnCleanListActionPerformed(e);
		}
		if (e.getSource() == btnChangeToVessel) {
			handleBtnChangeToVesselActionPerformed(e);
		}
		if (e.getSource() == btnUpdateList) {
			handleBtnAtualizarActionPerformed(e);
		}
	}

	protected void handleBtnAtualizarActionPerformed(ActionEvent e) {
		listActiveVessels.setModel(new DefaultListModel<String>());
		System.out.println(listActiveVessels.getModel());
		listActiveVessels.setModel(MechPeste.getActiveVessels());
		System.out.println(listActiveVessels.getModel());
	}

	protected void handleBtnChangeToVesselActionPerformed(ActionEvent e) {
		if (listActiveVessels.getSelectedIndex() == -1)
			return;
		int vesselHashCode = Integer.valueOf(listActiveVessels.getSelectedValue().split(" - ")[0]);
		MechPeste.changeToVessel(vesselHashCode);
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == listActiveVessels) {
			handleListActiveVesselsValueChanged(e);
		}
	}

	protected void handleListActiveVesselsValueChanged(ListSelectionEvent e) {
		List<String> selected = listActiveVessels.getSelectedValuesList();
		lblHowManyWillRun.setText(selected.size() == 0 ? "Somente a nave atual executará a próxima ação."
				: selected.size() == 1
						? "Somente a nave " + listActiveVessels.getSelectedValue() + " executará a próxima ação."
						: "Um total de " + selected.size() + " naves executarão a próxima ação.");
		btnChangeToVessel.setEnabled(selected.size() == 1);
		MechPeste.setVesselsToControl(selected);
	}

	protected void handleBtnCleanListActionPerformed(ActionEvent e) {
		listActiveVessels.clearSelection();
	}
}

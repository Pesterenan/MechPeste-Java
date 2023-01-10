package com.pesterenan.views;

import com.pesterenan.resources.Bundle;
import com.pesterenan.updater.KrpcInstaller;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

public class InstallKrpcDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private JLabel lblInstallerInfo;
	private final JSeparator separator = new JSeparator();
	private final JPanel pnlKspFolderPath = new JPanel();
	private final JTextField txfPath = new JTextField();
	private JButton btnBrowsePath;
	private JButton btnDownloadInstall;
	private JButton btnCancel;
	private JPanel pnlStatus;
	private static JLabel lblStatus;

	public InstallKrpcDialog() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			initComponents();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void initComponents() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		setBounds(MainGui.centerDialogOnScreen());
		setAlwaysOnTop(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle(Bundle.getString("installer_dialog_title")); //$NON-NLS-1$

		lblInstallerInfo = new JLabel(Bundle.getString("installer_dialog_txt_info")); //$NON-NLS-1$

		pnlKspFolderPath.setBorder(
				new TitledBorder(null, Bundle.getString("installer_dialog_pnl_path"), TitledBorder.LEADING,//$NON
				                 // -NLS-1$
				                 TitledBorder.TOP, null, null
				));

		btnDownloadInstall = new JButton(Bundle.getString("installer_dialog_btn_download")); //$NON
		// -NLS-1$
		btnDownloadInstall.addActionListener((e) -> KrpcInstaller.downloadAndInstallKrpc());
		btnDownloadInstall.setEnabled(false);

		btnCancel = new JButton(Bundle.getString("installer_dialog_btn_cancel")); //$NON-NLS-1$
		btnCancel.addActionListener((e) -> this.dispose());

		pnlStatus = new JPanel();
		pnlStatus.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
		                                          .addGroup(groupLayout.createSequentialGroup()
		                                                               .addContainerGap()
		                                                               .addGroup(groupLayout.createParallelGroup(
				                                                                                    Alignment.LEADING)
		                                                                                    .addComponent(
				                                                                                    pnlKspFolderPath,
				                                                                                    GroupLayout.DEFAULT_SIZE,
				                                                                                    414,
				                                                                                    Short.MAX_VALUE
		                                                                                                 )
		                                                                                    .addComponent(
				                                                                                    lblInstallerInfo,
				                                                                                    Alignment.TRAILING,
				                                                                                    GroupLayout.DEFAULT_SIZE,
				                                                                                    414,
				                                                                                    Short.MAX_VALUE
		                                                                                                 )
		                                                                                    .addComponent(separator,
		                                                                                                  Alignment.TRAILING,
		                                                                                                  GroupLayout.DEFAULT_SIZE,
		                                                                                                  414,
		                                                                                                  Short.MAX_VALUE
		                                                                                                 )
		                                                                                    .addGroup(
				                                                                                    groupLayout.createSequentialGroup()
				                                                                                               .addComponent(
						                                                                                               btnDownloadInstall)
				                                                                                               .addPreferredGap(
						                                                                                               ComponentPlacement.RELATED,
						                                                                                               184,
						                                                                                               Short.MAX_VALUE
				                                                                                                               )
				                                                                                               .addComponent(
						                                                                                               btnCancel)))
		                                                               .addGap(10))
		                                          .addComponent(pnlStatus, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
		                                                        434, Short.MAX_VALUE
		                                                       ));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
		                                        .addGroup(groupLayout.createSequentialGroup()
		                                                             .addContainerGap()
		                                                             .addComponent(lblInstallerInfo,
		                                                                           GroupLayout.PREFERRED_SIZE, 60,
		                                                                           GroupLayout.PREFERRED_SIZE
		                                                                          )
		                                                             .addGap(2)
		                                                             .addComponent(separator,
		                                                                           GroupLayout.PREFERRED_SIZE, 2,
		                                                                           GroupLayout.PREFERRED_SIZE
		                                                                          )
		                                                             .addPreferredGap(ComponentPlacement.UNRELATED)
		                                                             .addComponent(pnlKspFolderPath,
		                                                                           GroupLayout.PREFERRED_SIZE, 51,
		                                                                           GroupLayout.PREFERRED_SIZE
		                                                                          )
		                                                             .addPreferredGap(ComponentPlacement.UNRELATED)
		                                                             .addGroup(groupLayout.createParallelGroup(
				                                                                                  Alignment.BASELINE)
		                                                                                  .addComponent(
				                                                                                  btnDownloadInstall)
		                                                                                  .addComponent(btnCancel))
		                                                             .addPreferredGap(ComponentPlacement.RELATED, 60,
		                                                                              Short.MAX_VALUE
		                                                                             )
		                                                             .addComponent(pnlStatus,
		                                                                           GroupLayout.PREFERRED_SIZE, 25,
		                                                                           GroupLayout.PREFERRED_SIZE
		                                                                          )));

		lblStatus = new JLabel();
		GroupLayout glPnlStatus = new GroupLayout(pnlStatus);
		glPnlStatus.setHorizontalGroup(glPnlStatus.createParallelGroup(Alignment.LEADING)
		                                          .addGroup(glPnlStatus.createSequentialGroup()
		                                                               .addContainerGap()
		                                                               .addComponent(lblStatus)
		                                                               .addContainerGap(389, Short.MAX_VALUE)));
		glPnlStatus.setVerticalGroup(glPnlStatus.createParallelGroup(Alignment.TRAILING)
		                                        .addGroup(glPnlStatus.createSequentialGroup()
		                                                             .addGap(2)
		                                                             .addComponent(lblStatus, GroupLayout.DEFAULT_SIZE,
		                                                                           GroupLayout.DEFAULT_SIZE,
		                                                                           Short.MAX_VALUE
		                                                                          )
		                                                             .addGap(0)));
		pnlStatus.setLayout(glPnlStatus);

		txfPath.setEditable(false);
		txfPath.setColumns(10);

		btnBrowsePath = new JButton(Bundle.getString("installer_dialog_btn_browse")); //$NON-NLS-1$
		btnBrowsePath.addActionListener(e -> {
			chooseKSPFolder();
			txfPath.setText(KrpcInstaller.getKspFolder());
		});
		GroupLayout glPnlKspFolderPath = new GroupLayout(pnlKspFolderPath);
		glPnlKspFolderPath.setHorizontalGroup(glPnlKspFolderPath.createParallelGroup(Alignment.LEADING)
		                                                        .addGroup(Alignment.TRAILING,
		                                                                  glPnlKspFolderPath.createSequentialGroup()
		                                                                                    .addContainerGap()
		                                                                                    .addComponent(txfPath,
		                                                                                                  GroupLayout.DEFAULT_SIZE,
		                                                                                                  273,
		                                                                                                  Short.MAX_VALUE
		                                                                                                 )
		                                                                                    .addPreferredGap(
				                                                                                    ComponentPlacement.RELATED)
		                                                                                    .addComponent(btnBrowsePath,
		                                                                                                  GroupLayout.PREFERRED_SIZE,
		                                                                                                  103,
		                                                                                                  GroupLayout.PREFERRED_SIZE
		                                                                                                 )
		                                                                                    .addContainerGap()
		                                                                 ));
		glPnlKspFolderPath.setVerticalGroup(glPnlKspFolderPath.createParallelGroup(Alignment.LEADING)
		                                                      .addGroup(glPnlKspFolderPath.createSequentialGroup()
		                                                                                  .addGroup(
				                                                                                  glPnlKspFolderPath.createParallelGroup(
						                                                                                                    Alignment.BASELINE)
				                                                                                                    .addComponent(
						                                                                                                    txfPath,
						                                                                                                    GroupLayout.PREFERRED_SIZE,
						                                                                                                    23,
						                                                                                                    GroupLayout.PREFERRED_SIZE
				                                                                                                                 )
				                                                                                                    .addComponent(
						                                                                                                    btnBrowsePath))
		                                                                                  .addContainerGap(24,
		                                                                                                   Short.MAX_VALUE
		                                                                                                  )));
		pnlKspFolderPath.setLayout(glPnlKspFolderPath);
		getContentPane().setLayout(groupLayout);

		setVisible(true);
	}

	public void chooseKSPFolder() {
		JFileChooser kspDir = new JFileChooser();
		kspDir.setDialogTitle("Escolha a pasta do KSP na Steam");
		kspDir.setMultiSelectionEnabled(false);
		kspDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int response = kspDir.showOpenDialog(this);
		if (response == JFileChooser.APPROVE_OPTION) {
			KrpcInstaller.setKspFolder(kspDir.getSelectedFile().getPath());
			btnDownloadInstall.setEnabled(true);
			setStatus("Pasta escolhida, pronto para instalar.");
		} else {
			KrpcInstaller.setKspFolder(null);
			btnDownloadInstall.setEnabled(false);
			setStatus("");
		}
	}

	public static void setStatus(String status) {
		lblStatus.setText(status);
	}
}

package com.pesterenan.views;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.pesterenan.resources.Bundle;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MainGui extends JFrame implements ActionListener {
	public static final int INPUT_WIDTH = 100;
	private static MainGui mainGui = null;

	private static final long serialVersionUID = 1L;

	private final Dimension dmsMainGui = new Dimension(480, 300);
	private JPanel ctpMainGui = new JPanel();
	private static StatusJPanel pnlStatus;
	private static FunctionsJPanel pnlFuncoes;
	private static ParametersJPanel pnlParametros;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmExit;
	private JMenu mnHelp;
	private JMenuItem mntmAbout;

	public static MainGui getInstance() {
		if (mainGui == null) {

			mainGui = new MainGui();
		}
		return mainGui;
	}

	private MainGui() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			initComponents();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void initComponents() {
		setAlwaysOnTop(true);
		setTitle("MechPeste - Pesterenan"); //$NON-NLS-1$
		setVisible(true);
		setResizable(false);
		setLocation(100, 100);
		setSize(dmsMainGui);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu(Bundle.getString("main_mn_file")); //$NON-NLS-1$
		menuBar.add(mnFile);

		mntmExit = new JMenuItem(Bundle.getString("main_mntm_exit")); //$NON-NLS-1$
		mntmExit.addActionListener(this);
		mnFile.add(mntmExit);

		mnHelp = new JMenu(Bundle.getString("main_mn_help")); //$NON-NLS-1$
		menuBar.add(mnHelp);

		mntmAbout = new JMenuItem(Bundle.getString("main_mntm_about")); //$NON-NLS-1$
		mnHelp.add(mntmAbout);
		setContentPane(ctpMainGui);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		pnlFuncoes = new FunctionsJPanel();
		pnlParametros = new ParametersJPanel();
		pnlStatus = new StatusJPanel();
		ctpMainGui.setLayout(new BorderLayout(0, 0));
		ctpMainGui.add(pnlFuncoes, BorderLayout.WEST);
		ctpMainGui.add(pnlParametros, BorderLayout.CENTER);
		ctpMainGui.add(pnlStatus, BorderLayout.SOUTH);
	}

	public static ParametersJPanel getParametros() {
		return pnlParametros;
	}

	public static StatusJPanel getStatus() {
		return pnlStatus;
	}

	public static FunctionsJPanel getFuncoes() {
		return pnlFuncoes;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mntmExit) {
			handleMntmExitActionPerformed(e);
		}
	}

	protected void handleMntmExitActionPerformed(ActionEvent e) {
		System.exit(0);
	}
}

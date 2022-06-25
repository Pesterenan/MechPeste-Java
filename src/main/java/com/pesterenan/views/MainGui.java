package com.pesterenan.views;

import static com.pesterenan.utils.Dicionario.MECHPESTE;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class MainGui extends JFrame {

	public static final int INPUT_WIDTH = 100;
	private static MainGui mainGui = null;

	private static final long serialVersionUID = 1L;

	private final Dimension dmsMainGui = new Dimension(480, 280);
	private JPanel ctpMainGui = new JPanel();
	private static StatusJPanel pnlStatus;
	private static FunctionsJPanel pnlFuncoes;
	private static ParametersJPanel pnlParametros;

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
		setTitle(MECHPESTE.get());
		setVisible(true);
		setResizable(false);
		setLocation(100, 100);
		setSize(dmsMainGui);
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

}

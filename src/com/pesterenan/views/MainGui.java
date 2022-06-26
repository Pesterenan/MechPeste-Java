package com.pesterenan.views;

import static com.pesterenan.utils.Dictionary.MECHPESTE;

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
	private static StatusJPanel statusPanel;
	private static FunctionsJPanel functionsPanel;
	private static ParametersJPanel parametersPanel;

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

		functionsPanel = new FunctionsJPanel();
		parametersPanel = new ParametersJPanel();
		statusPanel = new StatusJPanel();
		ctpMainGui.setLayout(new BorderLayout(0, 0));
		ctpMainGui.add(functionsPanel, BorderLayout.WEST);
		ctpMainGui.add(parametersPanel, BorderLayout.CENTER);
		ctpMainGui.add(statusPanel, BorderLayout.SOUTH);
	}

	public static ParametersJPanel getParameters() {
		return parametersPanel;
	}

	public static StatusJPanel getStatus() {
		return statusPanel;
	}

	public static FunctionsJPanel getFuncoes() {
		return functionsPanel;
	}

}

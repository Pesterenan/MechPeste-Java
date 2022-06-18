package com.pesterenan.views;

import static com.pesterenan.utils.Dicionario.MECHPESTE;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class MainGui extends JFrame {

	public static final int INPUT_WIDTH = 100;
	private static MainGui mainGui = null;

	private static final long serialVersionUID = 1L;

	private final Dimension dmsMainGui = new Dimension(480, 280);
	private JPanel ctpMain = new JPanel();
	private static JPanel pnlStatus;
	private static JPanel pnlFuncoes;
	private static ParametrosJPanel pnlParametros;

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
		ctpMain.addMouseMotionListener(new CtpMainMouseMotionListener());
		setContentPane(ctpMain);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		pnlFuncoes = new FuncoesJPanel();
		pnlParametros = new ParametrosJPanel();
		pnlStatus = new StatusJPanel();
		ctpMain.setLayout(new BorderLayout(0, 0));
		ctpMain.add(pnlFuncoes, BorderLayout.WEST);
		ctpMain.add(pnlParametros, BorderLayout.CENTER);
		ctpMain.add(pnlStatus, BorderLayout.SOUTH);
	}

	public static ParametrosJPanel getParametros() {
		return pnlParametros;
	}

	public static JPanel getStatus() {
		return pnlStatus;
	}

	public static JPanel getFuncoes() {
		return pnlFuncoes;
	}

	private class CtpMainMouseMotionListener extends MouseMotionAdapter {

		@Override
		public void mouseMoved(MouseEvent e) {
			ctpMain.setToolTipText(e.getPoint().toString());
		}
	}
}

package com.pesterenan.views;

import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Modulos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MainGui extends JFrame implements ActionListener, PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	public static final Dimension dmsPanels = new Dimension(464, 216);

	private static MainGui mainGui = null;
	private static StatusJPanel pnlStatus;
	private static FunctionsAndTelemetryJPanel pnlFunctionsAndTelemetry;
	private final Dimension dmsMainGui = new Dimension(480, 300);
	private final JPanel ctpMainGui = new JPanel();
	private final static JPanel cardJPanels = new JPanel();
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmExit;
	private JMenu mnHelp;
	private JMenuItem mntmAbout;
	private JMenuItem mntmInstallKrpc;
	private LiftoffJPanel pnlLiftoff;

	private final CardLayout cardLayout = new CardLayout(0, 0);
	private LandingJPanel pnlLanding;
	private ManeuverJPanel pnlManeuver;
	private RoverJPanel pnlRover;

	private MainGui() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			initComponents();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void newInstance() {
		if (mainGui == null) {
			mainGui = new MainGui();
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

		mntmInstallKrpc = new JMenuItem(Bundle.getString("main_mntm_install_krpc"));
		mntmInstallKrpc.addActionListener(this);
		mnFile.add(mntmInstallKrpc);

		mnFile.add(new JSeparator());
		mntmExit = new JMenuItem(Bundle.getString("main_mntm_exit")); //$NON-NLS-1$
		mntmExit.addActionListener(this);
		mnFile.add(mntmExit);

		mnHelp = new JMenu(Bundle.getString("main_mn_help")); //$NON-NLS-1$
		menuBar.add(mnHelp);

		mntmAbout = new JMenuItem(Bundle.getString("main_mntm_about")); //$NON-NLS-1$
		mnHelp.add(mntmAbout);
		setContentPane(ctpMainGui);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		pnlFunctionsAndTelemetry = new FunctionsAndTelemetryJPanel();
		pnlLiftoff = new LiftoffJPanel();
		pnlLanding = new LandingJPanel();
		pnlManeuver = new ManeuverJPanel();
		pnlRover = new RoverJPanel();
		pnlStatus = new StatusJPanel();

		cardJPanels.setLayout(cardLayout);
		cardJPanels.setSize(dmsPanels);
		cardJPanels.add(pnlFunctionsAndTelemetry, Modulos.MODULO_TELEMETRIA.get());
		cardJPanels.add(pnlLiftoff, Modulos.MODULO_DECOLAGEM.get());
		cardJPanels.add(pnlLanding, Modulos.MODULO_POUSO.get());
		cardJPanels.add(pnlManeuver, Modulos.MODULO_MANOBRAS.get());
		cardJPanels.add(pnlRover, Modulos.MODULO_ROVER.get());
		cardJPanels.addPropertyChangeListener(this);

		ctpMainGui.setLayout(new BorderLayout(0, 0));
		ctpMainGui.add(cardJPanels, BorderLayout.CENTER);
		ctpMainGui.add(pnlStatus, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mntmInstallKrpc) {
			handleMntmInstallKrpcActionPerformed(e);
		}
		if (e.getSource() == mntmExit) {
			handleMntmExitActionPerformed(e);
		}
	}

	protected void handleMntmInstallKrpcActionPerformed(ActionEvent e) {
		InstallKrpcDialog ikd = new InstallKrpcDialog();
	}

	protected void handleMntmExitActionPerformed(ActionEvent e) {
		System.exit(0);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == cardJPanels) {
			handlePnlTelemetriaPropertyChange(evt);
		}
	}

	protected void handlePnlTelemetriaPropertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(Modulos.MODULO_DECOLAGEM.get())) {
			cardLayout.show(cardJPanels, Modulos.MODULO_DECOLAGEM.get());
		}
		if (evt.getPropertyName().equals(Modulos.MODULO_POUSO.get())) {
			cardLayout.show(cardJPanels, Modulos.MODULO_POUSO.get());
		}
		if (evt.getPropertyName().equals(Modulos.MODULO_MANOBRAS.get())) {
			cardLayout.show(cardJPanels, Modulos.MODULO_MANOBRAS.get());
		}
		if (evt.getPropertyName().equals(Modulos.MODULO_ROVER.get())) {
			cardLayout.show(cardJPanels, Modulos.MODULO_ROVER.get());
		}
		if (evt.getPropertyName().equals(Modulos.MODULO_TELEMETRIA.get())) {
			cardLayout.show(cardJPanels, Modulos.MODULO_TELEMETRIA.get());
		}
	}

	public static JPanel getCardJPanels() {
		return cardJPanels;
	}

	public static void backToTelemetry() {
		cardJPanels.firePropertyChange(Modulos.MODULO_TELEMETRIA.get(), false, true);
	}
}

package com.pesterenan.views;

import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Module;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainGui extends JFrame implements ActionListener, UIMethods {

    private static final long serialVersionUID = 1L;

    private final Dimension APP_DIMENSION = new Dimension(480, 300);
    public static final Dimension PNL_DIMENSION = new Dimension(464, 216);
    public static final Dimension BTN_DIMENSION = new Dimension(110, 25);
    public static final EmptyBorder MARGIN_BORDER_10_PX_LR = new EmptyBorder(0, 10, 0, 10);
    private static MainGui mainGui = null;
    private static StatusJPanel pnlStatus;
    private static FunctionsAndTelemetryJPanel pnlFunctionsAndTelemetry;
    private final JPanel ctpMainGui = new JPanel();
    private final static JPanel cardJPanels = new JPanel();
    private JMenuBar menuBar;
    private JMenu mnFile, mnOptions, mnHelp;
    private JMenuItem mntmInstallKrpc, mntmExit, mntmChangeVessels, mntmAbout;

    private final static CardLayout cardLayout = new CardLayout(0, 0);
    private LiftoffJPanel pnlLiftoff;
    private LandingJPanel pnlLanding;
    private CreateManeuverJPanel pnlCreateManeuvers;
    private RunManeuverJPanel pnlRunManeuvers;
    private RoverJPanel pnlRover;
    private DockingJPanel pnlDocking;

    private MainGui() {
        initComponents();
        setupComponents();
        layoutComponents();
    }

    public static MainGui newInstance() {
        if (mainGui == null) {
            mainGui = new MainGui();
        }
        return mainGui;
    }

    @Override
    public void initComponents() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Menu bar
        menuBar = new JMenuBar();

        // Menus
        mnFile = new JMenu(Bundle.getString("main_mn_file"));
        mnOptions = new JMenu(Bundle.getString("main_mn_options"));
        mnHelp = new JMenu(Bundle.getString("main_mn_help"));

        // Menu Items
        mntmInstallKrpc = new JMenuItem(Bundle.getString("main_mntm_install_krpc"));
        mntmChangeVessels = new JMenuItem(Bundle.getString("main_mntm_change_vessels"));
        mntmExit = new JMenuItem(Bundle.getString("main_mntm_exit"));
        mntmAbout = new JMenuItem(Bundle.getString("main_mntm_about"));

        // Panels
        pnlFunctionsAndTelemetry = new FunctionsAndTelemetryJPanel();
        pnlLiftoff = new LiftoffJPanel();
        pnlLanding = new LandingJPanel();
        pnlCreateManeuvers = new CreateManeuverJPanel();
        pnlRunManeuvers = new RunManeuverJPanel();
        pnlRover = new RoverJPanel();
        pnlDocking = new DockingJPanel();
        pnlStatus = new StatusJPanel();
    }

    @Override
    public void setupComponents() {
        // Main Panel setup:
        setAlwaysOnTop(true);
        setTitle("MechPeste - Pesterenan");
        setJMenuBar(menuBar);
        setResizable(false);
        setLocation(100, 100);
        setContentPane(ctpMainGui);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Setting-up components:
        mntmAbout.addActionListener(this);
        mntmChangeVessels.addActionListener(this);
        mntmExit.addActionListener(this);
        mntmInstallKrpc.addActionListener(this);

        cardJPanels.setPreferredSize(PNL_DIMENSION);
        cardJPanels.setSize(PNL_DIMENSION);
    }

    @Override
    public void layoutComponents() {
        // Main Panel layout:
        setPreferredSize(APP_DIMENSION);
        setSize(APP_DIMENSION);
        setVisible(true);

        // Laying out components:
        ctpMainGui.setLayout(new BorderLayout());
        ctpMainGui.add(cardJPanels, BorderLayout.CENTER);
        ctpMainGui.add(pnlStatus, BorderLayout.SOUTH);

        mnFile.add(mntmInstallKrpc);
        mnFile.add(new JSeparator());
        mnFile.add(mntmExit);
        mnOptions.add(mntmChangeVessels);
        mnHelp.add(mntmAbout);
        menuBar.add(mnFile);
        menuBar.add(mnOptions);
        menuBar.add(mnHelp);

        JTabbedPane pnlManeuverJTabbedPane = new JTabbedPane();
        pnlManeuverJTabbedPane.addTab("Criar Manobras", pnlCreateManeuvers);
        pnlManeuverJTabbedPane.addTab("Executar Manobras", pnlRunManeuvers);

        cardJPanels.setLayout(cardLayout);
        cardJPanels.add(pnlFunctionsAndTelemetry, Module.TELEMETRY.get());
        cardJPanels.add(pnlLiftoff, Module.LIFTOFF.get());
        cardJPanels.add(pnlLanding, Module.LANDING.get());
        cardJPanels.add(pnlManeuverJTabbedPane, Module.MANEUVER.get());
        cardJPanels.add(pnlRover, Module.ROVER.get());
        cardJPanels.add(pnlDocking, Module.DOCKING.get());
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == mntmAbout) {
            handleMntmAboutActionPerformed(e);
        }
        if (e.getSource() == mntmInstallKrpc) {
            handleMntmInstallKrpcActionPerformed(e);
        }
        if (e.getSource() == mntmExit) {
            handleMntmExitActionPerformed(e);
        }
        if (e.getSource() == mntmChangeVessels) {
            handleMntmMultiControlActionPerformed(e);
        }
    }

    private void handleMntmMultiControlActionPerformed(ActionEvent e) {
        new ChangeVesselDialog();
    }

    protected void handleMntmInstallKrpcActionPerformed(ActionEvent e) {
        new InstallKrpcDialog();
    }

    protected void handleMntmExitActionPerformed(ActionEvent e) {
        System.exit(0);
    }

    public static Rectangle centerDialogOnScreen() {
        Dimension SCREEN_DIMENSIONS = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension DIALOG_DIMENSIONS = new Dimension(400, 240);
        int w = DIALOG_DIMENSIONS.width;
        int h = DIALOG_DIMENSIONS.height;
        int x = (SCREEN_DIMENSIONS.width - w) / 2;
        int y = (SCREEN_DIMENSIONS.height - h) / 2;
        return new Rectangle(x, y, w, h);
    }

    public static JPanel getCardJPanels() {
        return cardJPanels;
    }

    public static void changeToPage(ActionEvent e) {
        cardLayout.show(cardJPanels, e.getActionCommand());
    }

    public static void backToTelemetry(ActionEvent e) {
        cardLayout.show(cardJPanels, Module.TELEMETRY.get());
    }

    protected void handleMntmAboutActionPerformed(ActionEvent e) {
        new AboutJFrame();
    }

    public static Component createMarginComponent(int width, int height) {
        Component marginComp = Box.createRigidArea(new Dimension(width, height));
        return marginComp;
    }
}

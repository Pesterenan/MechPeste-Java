package com.pesterenan.views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.pesterenan.model.VesselManager;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Module;

public class MainGui extends JFrame implements ActionListener, UIMethods {

    private static final long serialVersionUID = 1L;

    public static final Dimension PNL_DIMENSION = new Dimension(464, 216);
    public static final Dimension BTN_DIMENSION = new Dimension(110, 25);
    public static final EmptyBorder MARGIN_BORDER_10_PX_LR = new EmptyBorder(0, 10, 0, 10);
    private static MainGui instance = null;

    private final static JPanel cardJPanels = new JPanel();

    private final static CardLayout cardLayout = new CardLayout(0, 0);

    public static MainGui getInstance() {
        return instance;
    }

    public static MainGui newInstance() {
        if (instance == null) {
            instance = new MainGui();
        }
        return instance;
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

    public static Component createMarginComponent(int width, int height) {
        Component marginComp = Box.createRigidArea(new Dimension(width, height));
        return marginComp;
    }

    private FunctionsAndTelemetryJPanel pnlFunctionsAndTelemetry;

    private StatusJPanel pnlStatus;

    private final Dimension APP_DIMENSION = new Dimension(480, 300);

    private final JPanel ctpMainGui = new JPanel();

    private JMenuBar menuBar;
    private JMenu mnFile, mnOptions, mnHelp;
    private JMenuItem mntmInstallKrpc, mntmExit, mntmChangeVessels, mntmAbout;
    private LiftoffJPanel pnlLiftoff;
    private LandingJPanel pnlLanding;

    private CreateManeuverJPanel pnlCreateManeuvers;

    private RunManeuverJPanel pnlRunManeuvers;

    private RoverJPanel pnlRover;

    private DockingJPanel pnlDocking;

    private VesselManager vesselManager;
    private MainGui() {
        initComponents();
        setupComponents();
        layoutComponents();
    }

    public FunctionsAndTelemetryJPanel getFunctionsAndTelemetryPanel() {
        return pnlFunctionsAndTelemetry;
    }

    public StatusJPanel getStatusPanel() {
        return pnlStatus;
    }

    public LiftoffJPanel getLiftoffPanel() {
        return pnlLiftoff;
    }

    public LandingJPanel getLandingPanel() {
        return pnlLanding;
    }

    public CreateManeuverJPanel getCreateManeuverPanel() {
        return pnlCreateManeuvers;
    }

    public void setVesselManager(VesselManager vesselManager) {
        this.vesselManager = vesselManager;
        pnlFunctionsAndTelemetry.setVesselManager(vesselManager);
        pnlDocking.setVesselManager(vesselManager);
        pnlRover.setVesselManager(vesselManager);
        pnlLiftoff.setVesselManager(vesselManager);
        pnlLanding.setVesselManager(vesselManager);
        pnlCreateManeuvers.setVesselManager(vesselManager);
        pnlRunManeuvers.setVesselManager(vesselManager);
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
        pnlStatus = new StatusJPanel();
        pnlFunctionsAndTelemetry = new FunctionsAndTelemetryJPanel(getStatusPanel());
        pnlLiftoff = new LiftoffJPanel(getStatusPanel());
        pnlLanding = new LandingJPanel(getStatusPanel());
        pnlCreateManeuvers = new CreateManeuverJPanel(getStatusPanel());
        pnlRunManeuvers = new RunManeuverJPanel(getStatusPanel());
        pnlRover = new RoverJPanel(getStatusPanel());
        pnlDocking = new DockingJPanel(getStatusPanel());
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

    protected void handleMntmInstallKrpcActionPerformed(ActionEvent e) {
        new InstallKrpcDialog();
    }

    protected void handleMntmExitActionPerformed(ActionEvent e) {
        System.exit(0);
    }

    protected void handleMntmAboutActionPerformed(ActionEvent e) {
        new AboutJFrame();
    }

    private void handleMntmMultiControlActionPerformed(ActionEvent e) {
        new ChangeVesselDialog(vesselManager);
    }
}

package com.pesterenan.views;

import static com.pesterenan.views.MainGui.BTN_DIMENSION;
import static com.pesterenan.views.MainGui.MARGIN_BORDER_10_PX_LR;
import static com.pesterenan.views.MainGui.PNL_DIMENSION;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import com.pesterenan.MechPeste;
import com.pesterenan.model.VesselManager;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Module;

import krpc.client.RPCException;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Orbit;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;

public class RunManeuverJPanel extends JPanel implements ActionListener, UIMethods {
    private static final long serialVersionUID = 1L;

    private StatusDisplay statusDisplay;

    private JLabel lblExecute;
    private JButton btnLowerOrbit, btnApoapsis, btnPeriapsis, btnExecute, btnBack, btnAlignPlanes, btnRendezvous;
    private JCheckBox chkFineAdjusment;
    private final ControlePID ctrlManeuver = new ControlePID();
    private VesselManager vesselManager;

    public RunManeuverJPanel(StatusDisplay statusDisplay) {
        this.statusDisplay = statusDisplay;
        initComponents();
        setupComponents();
        layoutComponents();
    }

    public void setVesselManager(VesselManager vesselManager) {
        this.vesselManager = vesselManager;
    }

    public void initComponents() {
        // Labels:
        lblExecute = new JLabel(Bundle.getString("pnl_mnv_lbl_exec_mnv"));

        // Buttons:
        btnApoapsis = new JButton(Bundle.getString("pnl_mnv_btn_apoapsis"));
        btnBack = new JButton(Bundle.getString("pnl_mnv_btn_back"));
        btnExecute = new JButton(Bundle.getString("pnl_mnv_btn_exec_mnv"));
        btnLowerOrbit = new JButton(Bundle.getString("pnl_mnv_btn_lower_orbit"));
        btnPeriapsis = new JButton(Bundle.getString("pnl_mnv_btn_periapsis"));
        btnAlignPlanes = new JButton("Alinhar planos");
        btnRendezvous = new JButton("Rendezvous");

        // Misc:
        chkFineAdjusment = new JCheckBox(Bundle.getString("pnl_mnv_chk_adj_mnv_rcs"));
    }

    public void createManeuver() {
        System.out.println("Create maneuver");
        try {
            createManeuver(vesselManager.getSpaceCenter().getUT() + 60);
        } catch (RPCException e) {
        }
    }

    public void createManeuver(double atFutureTime) {
        System.out.println("Create maneuver overloaded");
        try {
            Vessel vessel = vesselManager.getSpaceCenter().getActiveVessel();
            System.out.println("vessel: " + vessel);

            if (vessel.getSituation() != VesselSituation.ORBITING) {
                statusDisplay.setStatusMessage("Não é possível criar a manobra fora de órbita.");
                return;
            }
            vessel.getControl().addNode(atFutureTime, 0, 0, 0);
        } catch (Exception e) {
        }
    }

    public void positionManeuverAt(String node) {
        try {
            MechPeste.newInstance();
            Vessel vessel = vesselManager.getSpaceCenter().getActiveVessel();
            Orbit orbit = vessel.getOrbit();
            Node currentManeuver = vessel.getControl().getNodes().get(0);
            double timeToNode = 0;
            switch (node) {
                case "apoapsis" :
                    timeToNode = vesselManager.getSpaceCenter().getUT() + orbit.getTimeToApoapsis();
                    break;
                case "periapsis" :
                    timeToNode = vesselManager.getSpaceCenter().getUT() + orbit.getTimeToPeriapsis();
                    break;
                case "ascending" :
                    double ascendingAnomaly = orbit
                            .trueAnomalyAtAN(vesselManager.getSpaceCenter().getTargetVessel().getOrbit());
                    timeToNode = orbit.uTAtTrueAnomaly(ascendingAnomaly);
                    break;
                case "descending" :
                    double descendingAnomaly = orbit
                            .trueAnomalyAtDN(vesselManager.getSpaceCenter().getTargetVessel().getOrbit());
                    timeToNode = orbit.uTAtTrueAnomaly(descendingAnomaly);
                    break;
            }
            currentManeuver.setUT(timeToNode);
            // Print the maneuver node information
            System.out.println("Maneuver Node updated:");
            System.out.println("  Time to node: " + currentManeuver.getTimeTo() + " s");
        } catch (Exception e) {
        }
    }

    public void setupComponents() {
        // Setting-up components:
        btnAlignPlanes.addActionListener(this);
        btnAlignPlanes.setMaximumSize(BTN_DIMENSION);
        btnAlignPlanes.setPreferredSize(BTN_DIMENSION);

        btnRendezvous.addActionListener(this);
        btnRendezvous.setMaximumSize(BTN_DIMENSION);
        btnRendezvous.setPreferredSize(BTN_DIMENSION);

        btnApoapsis.addActionListener(this);
        btnApoapsis.setMaximumSize(BTN_DIMENSION);
        btnApoapsis.setPreferredSize(BTN_DIMENSION);

        btnBack.addActionListener(this);
        btnBack.setMaximumSize(BTN_DIMENSION);
        btnBack.setPreferredSize(BTN_DIMENSION);

        btnExecute.addActionListener(this);
        btnExecute.setMaximumSize(BTN_DIMENSION);
        btnExecute.setPreferredSize(BTN_DIMENSION);

        btnLowerOrbit.addActionListener(this);
        btnLowerOrbit.setMaximumSize(BTN_DIMENSION);
        btnLowerOrbit.setPreferredSize(BTN_DIMENSION);

        btnPeriapsis.addActionListener(this);
        btnPeriapsis.setMaximumSize(BTN_DIMENSION);
        btnPeriapsis.setPreferredSize(BTN_DIMENSION);
    }

    public void layoutComponents() {
        // Main Panel layout:
        setPreferredSize(PNL_DIMENSION);
        setSize(PNL_DIMENSION);
        setLayout(new BorderLayout());

        JPanel pnlExecuteManeuver = new JPanel();
        pnlExecuteManeuver.setLayout(new BoxLayout(pnlExecuteManeuver, BoxLayout.X_AXIS));
        pnlExecuteManeuver.setBorder(MARGIN_BORDER_10_PX_LR);
        pnlExecuteManeuver.add(lblExecute);
        pnlExecuteManeuver.add(MainGui.createMarginComponent(10, 0));
        pnlExecuteManeuver.add(btnExecute);

        JPanel pnlAutoPosition = new JPanel();
        pnlAutoPosition.setLayout(new BoxLayout(pnlAutoPosition, BoxLayout.X_AXIS));
        pnlAutoPosition.setBorder(new TitledBorder("Auto posição:"));
        pnlAutoPosition.add(btnAlignPlanes);
        pnlAutoPosition.add(btnRendezvous);

        JPanel pnlCircularize = new JPanel();
        pnlCircularize.setLayout(new BoxLayout(pnlCircularize, BoxLayout.X_AXIS));
        TitledBorder titled = new TitledBorder(null, Bundle.getString("pnl_mnv_circularize"), TitledBorder.LEADING,
                TitledBorder.TOP, null, null);
        CompoundBorder combined = new CompoundBorder(titled, MARGIN_BORDER_10_PX_LR);
        pnlCircularize.setBorder(combined);
        pnlCircularize.add(btnLowerOrbit);
        pnlCircularize.add(Box.createHorizontalGlue());
        pnlCircularize.add(btnApoapsis);
        pnlCircularize.add(btnPeriapsis);

        JPanel pnlSetup = new JPanel();
        pnlSetup.setLayout(new BoxLayout(pnlSetup, BoxLayout.Y_AXIS));
        pnlSetup.add(pnlExecuteManeuver);
        pnlSetup.add(pnlAutoPosition);

        JPanel pnlOptions = new JPanel();
        pnlOptions.setLayout(new BoxLayout(pnlOptions, BoxLayout.Y_AXIS));
        pnlOptions.setBorder(new TitledBorder(Bundle.getString("pnl_lift_chk_options")));
        pnlOptions.add(chkFineAdjusment);

        JPanel pnlFunctions = new JPanel();
        pnlFunctions.setLayout(new BoxLayout(pnlFunctions, BoxLayout.X_AXIS));
        pnlFunctions.add(pnlSetup);
        pnlFunctions.add(pnlOptions);

        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
        pnlButtons.add(Box.createHorizontalGlue());
        pnlButtons.add(btnBack);

        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlFunctions.setAlignmentY(TOP_ALIGNMENT);
        pnlMain.add(pnlFunctions);
        pnlCircularize.setAlignmentY(TOP_ALIGNMENT);
        pnlMain.add(pnlCircularize);

        add(pnlMain, BorderLayout.CENTER);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnExecute) {
            handleManeuverFunction(Module.EXECUTE.get());
        }
        if (e.getSource() == btnLowerOrbit) {
            handleManeuverFunction(Module.LOW_ORBIT.get());
        }
        if (e.getSource() == btnApoapsis) {
            handleManeuverFunction(Module.APOAPSIS.get());
        }
        if (e.getSource() == btnPeriapsis) {
            handleManeuverFunction(Module.PERIAPSIS.get());
        }
        if (e.getSource() == btnAlignPlanes) {
            handleManeuverFunction(Module.ADJUST.get());
        }
        if (e.getSource() == btnRendezvous) {
            handleManeuverFunction(Module.RENDEZVOUS.get());
        }
        if (e.getSource() == btnBack) {
            MainGui.backToTelemetry(e);
        }
    }

    protected void handleManeuverFunction(String maneuverFunction) {
        Map<String,String> commands = new HashMap<>();
        commands.put(Module.MODULO.get(), Module.MANEUVER.get());
        commands.put(Module.FUNCTION.get(), maneuverFunction.toString());
        commands.put(Module.FINE_ADJUST.get(), String.valueOf(chkFineAdjusment.isSelected()));
        MechPeste.newInstance().getVesselManager().startModule(commands);
    }
}

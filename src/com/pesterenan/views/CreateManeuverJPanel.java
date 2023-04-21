package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;

import krpc.client.RPCException;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Orbit;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.pesterenan.views.MainGui.PNL_DIMENSION;
import static com.pesterenan.views.MainGui.BTN_DIMENSION;
import static com.pesterenan.views.MainGui.MARGIN_BORDER_10_PX_LR;

public class CreateManeuverJPanel extends JPanel implements ActionListener, UIMethods {

    private JLabel lblCreateManeuver, lblManeuverPosition;
    private JButton btnCreateManeuver, btnApoapsis, btnPeriapsis, btnBack;

    public CreateManeuverJPanel() {
        initComponents();
        setupComponents();
        layoutComponents();
    }

    @Override
    public void initComponents() {
        // Labels:
        lblCreateManeuver = new JLabel("Criar plano de manobra:");
        lblManeuverPosition = new JLabel("Posicionar manobra no:");

        // Buttons:
        btnCreateManeuver = new JButton("Criar");
        btnApoapsis = new JButton("Apoastro");
        btnPeriapsis = new JButton("Periastro");
        btnBack = new JButton(Bundle.getString("pnl_mnv_btn_back"));
    }

    @Override
    public void setupComponents() {
        // Main Panel setup:
        setBorder(new TitledBorder(null, Bundle.getString("pnl_mnv_border"), TitledBorder.LEADING, TitledBorder.TOP,
                null, null));

        // Setting-up components:
        btnCreateManeuver.addActionListener(this);
        btnApoapsis.addActionListener(this);
        btnPeriapsis.addActionListener(this);
        btnBack.addActionListener(this);
        btnBack.setMaximumSize(BTN_DIMENSION);
        btnBack.setPreferredSize(BTN_DIMENSION);
    }

    @Override
    public void layoutComponents() {
        // Main Panel layout:
        setPreferredSize(PNL_DIMENSION);
        setSize(PNL_DIMENSION);
        setLayout(new BorderLayout());

        JPanel pnlCreateManeuver = new JPanel();
        pnlCreateManeuver.setLayout(new BoxLayout(pnlCreateManeuver, BoxLayout.X_AXIS));
        pnlCreateManeuver.setBorder(MARGIN_BORDER_10_PX_LR);
        pnlCreateManeuver.add(lblCreateManeuver);
        pnlCreateManeuver.add(Box.createHorizontalGlue());
        pnlCreateManeuver.add(btnCreateManeuver);

        JPanel pnlPositionManeuver = new JPanel();
        pnlPositionManeuver.setLayout(new BoxLayout(pnlPositionManeuver, BoxLayout.X_AXIS));
        pnlPositionManeuver.setBorder(MARGIN_BORDER_10_PX_LR);
        pnlPositionManeuver.add(lblManeuverPosition);
        pnlPositionManeuver.add(Box.createHorizontalGlue());
        pnlPositionManeuver.add(btnApoapsis);
        pnlPositionManeuver.add(Box.createHorizontalGlue());
        pnlPositionManeuver.add(btnPeriapsis);

        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
        pnlButtons.add(Box.createHorizontalGlue());
        pnlButtons.add(btnBack);

        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.add(pnlCreateManeuver);
        pnlMain.add(pnlPositionManeuver);

        add(pnlMain, BorderLayout.CENTER);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    private void createManeuver() {
        try {
            MechPeste.newInstance();
            Vessel vessel = MechPeste.getSpaceCenter().getActiveVessel();

            if (vessel.getSituation() != VesselSituation.ORBITING) {
                System.out.println("Não é possível criar a manobra fora de órbita.");
                return;
            }
            double oneMinuteAhead = MechPeste.getSpaceCenter().getUT() + 60;
            Node maneuverNode = vessel.getControl().addNode(oneMinuteAhead, 0, 0, 0);
            // Print the maneuver node information
            System.out.println("Maneuver Node created:");
            System.out.println("  Time to node: " + maneuverNode.getTimeTo() + " s");
        } catch (Exception e) {
        }
    }

    private void positionManeuverAt(Boolean apoastro) {
        try {
            MechPeste.newInstance();
            Vessel vessel = MechPeste.getSpaceCenter().getActiveVessel();
            Orbit orbit = vessel.getOrbit();
            Node currentManeuver = vessel.getControl().getNodes().get(0);
            double timeToNode = apoastro ? orbit.getTimeToApoapsis() : orbit.getTimeToPeriapsis();
            double maneuverTime = MechPeste.getSpaceCenter().getUT() + timeToNode;
            currentManeuver.setUT(maneuverTime);
            // Print the maneuver node information
            System.out.println("Maneuver Node updated:");
            System.out.println("  Time to node: " + currentManeuver.getTimeTo() + " s");
        } catch (Exception e) {
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCreateManeuver) {
            createManeuver();
        }
        if (e.getSource() == btnApoapsis) {
            positionManeuverAt(true);
        }
        if (e.getSource() == btnPeriapsis) {
            positionManeuverAt(false);
        }
        if (e.getSource() == btnBack) {
            MainGui.backToTelemetry(e);
        }
    }
}

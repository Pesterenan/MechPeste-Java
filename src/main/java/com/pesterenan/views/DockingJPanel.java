package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Module;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import static com.pesterenan.views.MainGui.BTN_DIMENSION;
import static com.pesterenan.views.MainGui.MARGIN_BORDER_10_PX_LR;
import static com.pesterenan.views.MainGui.PNL_DIMENSION;

public class DockingJPanel extends JPanel implements UIMethods {

    private static final long serialVersionUID = 0L;

    private JLabel lblMaxSpeed, lblSafeDistance, lblCurrentDockingStepText;
    private static JLabel lblDockingStep;

    private JTextField txfMaxSpeed, txfSafeDistance;
    private JButton btnBack, btnStartDocking;

    public DockingJPanel() {
        initComponents();
        setupComponents();
        layoutComponents();
    }

    @Override
    public void initComponents() {
        // Labels:
        lblMaxSpeed = new JLabel(Bundle.getString("pnl_docking_max_speed"));
        lblSafeDistance = new JLabel(Bundle.getString("pnl_docking_safe_distance"));
        lblDockingStep = new JLabel(Bundle.getString("pnl_docking_step_ready"));
        lblCurrentDockingStepText = new JLabel(Bundle.getString("pnl_docking_current_step"));
        // Textfields:
        txfMaxSpeed = new JTextField("3");
        txfSafeDistance = new JTextField("50");

        // Buttons:
        btnBack = new JButton(Bundle.getString("pnl_rover_btn_back"));
        btnStartDocking = new JButton(Bundle.getString("pnl_rover_btn_docking"));
    }

    @Override
    public void setupComponents() {
        // Main Panel setup:
        setBorder(new TitledBorder(null, Bundle.getString("btn_func_docking")));

        // Setting-up components:
        txfMaxSpeed.setHorizontalAlignment(SwingConstants.RIGHT);
        txfMaxSpeed.setMaximumSize(BTN_DIMENSION);
        txfMaxSpeed.setPreferredSize(BTN_DIMENSION);
        txfSafeDistance.setHorizontalAlignment(SwingConstants.RIGHT);
        txfSafeDistance.setMaximumSize(BTN_DIMENSION);
        txfSafeDistance.setPreferredSize(BTN_DIMENSION);

        btnBack.addActionListener(MainGui::backToTelemetry);
        btnBack.setMaximumSize(BTN_DIMENSION);
        btnBack.setPreferredSize(BTN_DIMENSION);
        btnStartDocking.addActionListener(this::handleStartDocking);
        btnStartDocking.setMaximumSize(BTN_DIMENSION);
        btnStartDocking.setPreferredSize(BTN_DIMENSION);
    }

    @Override
    public void layoutComponents() {
        // Main Panel layout:
        setPreferredSize(PNL_DIMENSION);
        setSize(PNL_DIMENSION);
        setLayout(new BorderLayout());

        // Layout components:
        JPanel pnlMaxSpeed = new JPanel();
        pnlMaxSpeed.setLayout(new BoxLayout(pnlMaxSpeed, BoxLayout.X_AXIS));
        pnlMaxSpeed.add(lblMaxSpeed);
        pnlMaxSpeed.add(Box.createHorizontalGlue());
        pnlMaxSpeed.add(txfMaxSpeed);

        JPanel pnlSafeDistance = new JPanel();
        pnlSafeDistance.setLayout(new BoxLayout(pnlSafeDistance, BoxLayout.X_AXIS));
        pnlSafeDistance.add(lblSafeDistance);
        pnlSafeDistance.add(Box.createHorizontalGlue());
        pnlSafeDistance.add(txfSafeDistance);

        JPanel pnlDockingStep = new JPanel();
        pnlDockingStep.setLayout(new BoxLayout(pnlDockingStep, BoxLayout.X_AXIS));
        pnlDockingStep.add(lblCurrentDockingStepText);
        pnlDockingStep.add(Box.createHorizontalGlue());
        pnlDockingStep.add(lblDockingStep);

        JPanel pnlDockingControls = new JPanel();
        pnlDockingControls.setLayout(new BoxLayout(pnlDockingControls, BoxLayout.Y_AXIS));
        pnlDockingControls.setBorder(MARGIN_BORDER_10_PX_LR);
        pnlDockingControls.add(MainGui.createMarginComponent(0, 6));
        pnlDockingControls.add(pnlMaxSpeed);
        pnlDockingControls.add(pnlSafeDistance);
        pnlDockingControls.add(Box.createVerticalGlue());
        pnlDockingControls.add(pnlDockingStep);
        pnlDockingControls.add(Box.createVerticalGlue());

        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
        pnlButtons.add(btnStartDocking);
        pnlButtons.add(Box.createHorizontalGlue());
        pnlButtons.add(btnBack);

        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.X_AXIS));
        pnlDockingControls.setAlignmentY(TOP_ALIGNMENT);
        pnlMain.add(pnlDockingControls);

        setLayout(new BorderLayout());
        add(pnlMain, BorderLayout.CENTER);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    public static void setDockingStep(String step) {
        lblDockingStep.setText(step);
    }

    private void handleStartDocking(ActionEvent e) {
        if (validateTextFields()) {
            Map<String,String> commands = new HashMap<>();
            commands.put(Module.MODULO.get(), Module.DOCKING.get());
            commands.put(Module.SAFE_DISTANCE.get(), txfSafeDistance.getText());
            commands.put(Module.MAX_SPEED.get(), txfMaxSpeed.getText());
            MechPeste.newInstance().startModule(commands);
        }
    }

    private boolean validateTextFields() {
        try {
            if (Float.parseFloat(txfMaxSpeed.getText()) > 10) {
                StatusJPanel.setStatusMessage("Velocidade de acoplagem muito alta. Tem que ser menor que 10m/s.");
                return false;
            }
            if (Float.parseFloat(txfSafeDistance.getText()) > 200) {
                StatusJPanel.setStatusMessage("Distância segura muito alta. Tem que ser menor que 200m.");
                return false;
            }
        } catch (NumberFormatException e) {
            StatusJPanel.setStatusMessage(Bundle.getString("pnl_lift_stat_only_numbers"));
            return false;
        } catch (IllegalArgumentException e) {
            StatusJPanel.setStatusMessage(Bundle.getString("pnl_rover_waypoint_name_not_empty"));
            return false;
        }
        return true;
    }
}

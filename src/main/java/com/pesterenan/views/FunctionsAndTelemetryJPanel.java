package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Module;
import com.pesterenan.utils.Telemetry;
import com.pesterenan.utils.Utilities;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Map;

import static com.pesterenan.views.MainGui.PNL_DIMENSION;

public class FunctionsAndTelemetryJPanel extends JPanel implements UIMethods {

    private static final long serialVersionUID = 0L;

    private final Dimension btnFuncDimension = new Dimension(140, 25);
    private JButton btnLiftoff, btnLanding, btnManeuvers, btnDocking, btnRover, btnCancel;
    private static JLabel lblAltitude, lblSurfaceAlt, lblApoapsis, lblPeriapsis, lblVertSpeed, lblHorzSpeed;
    private static JLabel lblAltitudeValue, lblSurfaceAltValue, lblApoapsisValue;
    private static JLabel lblPeriapsisValue, lblVertSpeedValue, lblHorzSpeedValue;

    public FunctionsAndTelemetryJPanel() {
        initComponents();
        setupComponents();
        layoutComponents();
    }

    @Override
    public void initComponents() {
        // Labels:
        lblAltitude = new JLabel(Bundle.getString("pnl_tel_lbl_alt"));
        lblSurfaceAlt = new JLabel(Bundle.getString("pnl_tel_lbl_alt_sur"));
        lblApoapsis = new JLabel(Bundle.getString("pnl_tel_lbl_apoapsis"));
        lblPeriapsis = new JLabel(Bundle.getString("pnl_tel_lbl_periapsis"));
        lblVertSpeed = new JLabel(Bundle.getString("pnl_tel_lbl_vert_spd"));
        lblHorzSpeed = new JLabel(Bundle.getString("pnl_tel_lbl_horz_spd"));
        lblAltitudeValue = new JLabel("---");
        lblSurfaceAltValue = new JLabel("---");
        lblApoapsisValue = new JLabel("---");
        lblPeriapsisValue = new JLabel("---");
        lblVertSpeedValue = new JLabel("---");
        lblHorzSpeedValue = new JLabel("---");

        // Buttons:
        btnLiftoff = new JButton(Bundle.getString("btn_func_liftoff"));
        btnLanding = new JButton(Bundle.getString("btn_func_landing"));
        btnManeuvers = new JButton(Bundle.getString("btn_func_maneuver"));
        btnRover = new JButton(Bundle.getString("btn_func_rover"));
        btnDocking = new JButton(Bundle.getString("btn_func_docking"));
        btnCancel = new JButton(Bundle.getString("pnl_tel_btn_cancel"));
    }

    @Override
    public void setupComponents() {
        setPreferredSize(PNL_DIMENSION);
        setBorder(new TitledBorder(null, Bundle.getString("pnl_func_title"), TitledBorder.LEADING, TitledBorder.TOP,
                null, null));
        setLayout(new BorderLayout());

        // Setting up components:
        btnCancel.addActionListener(MechPeste::cancelControl);
        btnCancel.setMaximumSize(btnFuncDimension);
        btnCancel.setPreferredSize(btnFuncDimension);
        btnLanding.addActionListener(this::changeFunctionPanel);
        btnLanding.setActionCommand(Module.LANDING.get());
        btnLanding.setMaximumSize(btnFuncDimension);
        btnLanding.setPreferredSize(btnFuncDimension);
        btnLiftoff.addActionListener(this::changeFunctionPanel);
        btnLiftoff.setActionCommand(Module.LIFTOFF.get());
        btnLiftoff.setMaximumSize(btnFuncDimension);
        btnLiftoff.setPreferredSize(btnFuncDimension);
        btnManeuvers.addActionListener(this::changeFunctionPanel);
        btnManeuvers.setActionCommand(Module.MANEUVER.get());
        btnManeuvers.setMaximumSize(btnFuncDimension);
        btnManeuvers.setPreferredSize(btnFuncDimension);
        btnRover.addActionListener(this::changeFunctionPanel);
        btnRover.setActionCommand(Module.ROVER.get());
        btnRover.setMaximumSize(btnFuncDimension);
        btnRover.setPreferredSize(btnFuncDimension);
        btnDocking.addActionListener(this::changeFunctionPanel);
        btnDocking.setActionCommand(Module.DOCKING.get());
        btnDocking.setMaximumSize(btnFuncDimension);
        btnDocking.setPreferredSize(btnFuncDimension);
    }

    @Override
    public void layoutComponents() {

        JPanel pnlFunctionControls = new JPanel();
        pnlFunctionControls.setLayout(new BoxLayout(pnlFunctionControls, BoxLayout.Y_AXIS));
        pnlFunctionControls.add(MainGui.createMarginComponent(0, 4));
        pnlFunctionControls.add(btnLiftoff);
        pnlFunctionControls.add(btnLanding);
        pnlFunctionControls.add(btnRover);
        pnlFunctionControls.add(btnDocking);
        pnlFunctionControls.add(btnManeuvers);
        pnlFunctionControls.add(Box.createVerticalGlue());

        JPanel pnlLeftPanel = new JPanel();
        pnlLeftPanel.setBorder(MainGui.MARGIN_BORDER_10_PX_LR);
        pnlLeftPanel.setLayout(new BoxLayout(pnlLeftPanel, BoxLayout.Y_AXIS));
        pnlLeftPanel.add(lblAltitude);
        pnlLeftPanel.add(Box.createVerticalStrut(5));
        pnlLeftPanel.add(lblSurfaceAlt);
        pnlLeftPanel.add(Box.createVerticalStrut(5));
        pnlLeftPanel.add(lblApoapsis);
        pnlLeftPanel.add(Box.createVerticalStrut(5));
        pnlLeftPanel.add(lblPeriapsis);
        pnlLeftPanel.add(Box.createVerticalStrut(5));
        pnlLeftPanel.add(lblHorzSpeed);
        pnlLeftPanel.add(Box.createVerticalStrut(5));
        pnlLeftPanel.add(lblVertSpeed);
        pnlLeftPanel.add(Box.createGlue());

        JPanel pnlRightPanel = new JPanel();
        pnlRightPanel.setBorder(MainGui.MARGIN_BORDER_10_PX_LR);
        pnlRightPanel.setLayout(new BoxLayout(pnlRightPanel, BoxLayout.Y_AXIS));
        lblAltitudeValue.setAlignmentX(RIGHT_ALIGNMENT);
        lblSurfaceAltValue.setAlignmentX(RIGHT_ALIGNMENT);
        lblApoapsisValue.setAlignmentX(RIGHT_ALIGNMENT);
        lblPeriapsisValue.setAlignmentX(RIGHT_ALIGNMENT);
        lblHorzSpeedValue.setAlignmentX(RIGHT_ALIGNMENT);
        lblVertSpeedValue.setAlignmentX(RIGHT_ALIGNMENT);
        pnlRightPanel.add(lblAltitudeValue);
        pnlRightPanel.add(Box.createVerticalStrut(5));
        pnlRightPanel.add(lblSurfaceAltValue);
        pnlRightPanel.add(Box.createVerticalStrut(5));
        pnlRightPanel.add(lblApoapsisValue);
        pnlRightPanel.add(Box.createVerticalStrut(5));
        pnlRightPanel.add(lblPeriapsisValue);
        pnlRightPanel.add(Box.createVerticalStrut(5));
        pnlRightPanel.add(lblHorzSpeedValue);
        pnlRightPanel.add(Box.createVerticalStrut(5));
        pnlRightPanel.add(lblVertSpeedValue);
        pnlRightPanel.add(Box.createGlue());

        JPanel pnlLeftRightContainer = new JPanel();
        pnlLeftRightContainer.setLayout(new BoxLayout(pnlLeftRightContainer, BoxLayout.X_AXIS));
        pnlLeftRightContainer.add(pnlLeftPanel);
        pnlLeftRightContainer.add(pnlRightPanel);

        JPanel pnlTelemetry = new JPanel();
        pnlTelemetry.setLayout(new BoxLayout(pnlTelemetry, BoxLayout.Y_AXIS));
        pnlTelemetry.setBorder(new TitledBorder(null, Bundle.getString("pnl_tel_border")));
        pnlTelemetry.add(pnlLeftRightContainer);
        pnlTelemetry.add(Box.createGlue());
        btnCancel.setAlignmentX(CENTER_ALIGNMENT);
        pnlTelemetry.add(btnCancel);

        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.X_AXIS));
        pnlFunctionControls.setAlignmentY(TOP_ALIGNMENT);
        pnlTelemetry.setAlignmentY(TOP_ALIGNMENT);
        pnlMain.add(pnlFunctionControls);
        pnlMain.add(pnlTelemetry);

        add(pnlMain, BorderLayout.CENTER);
    }

    private void changeFunctionPanel(ActionEvent e) {
        MainGui.changeToPage(e);
    }

    public static void updateTelemetry(Map<Telemetry,Double> telemetryData) {
        synchronized (telemetryData) {
            for (Telemetry key : telemetryData.keySet()) {
                switch (key) {
                    case ALTITUDE :
                        lblAltitudeValue.setText(Utilities.convertToMetersMagnitudes(telemetryData.get(key)));
                        break;
                    case ALT_SURF :
                        lblSurfaceAltValue.setText(Utilities.convertToMetersMagnitudes(telemetryData.get(key)));
                        break;
                    case APOAPSIS :
                        lblApoapsisValue.setText(Utilities.convertToMetersMagnitudes(telemetryData.get(key)));
                        break;
                    case PERIAPSIS :
                        lblPeriapsisValue.setText(Utilities.convertToMetersMagnitudes(telemetryData.get(key)));
                        break;
                    case VERT_SPEED :
                        lblVertSpeedValue.setText(Utilities.convertToMetersMagnitudes(telemetryData.get(key)) + "/s");
                        break;
                    case HORZ_SPEED :
                        lblHorzSpeedValue.setText(Utilities.convertToMetersMagnitudes(telemetryData.get(key)) + "/s");
                        break;
                }
            }
        }
    }
}

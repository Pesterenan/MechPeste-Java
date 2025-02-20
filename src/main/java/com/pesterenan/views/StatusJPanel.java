package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;

import javax.swing.*;

import static com.pesterenan.views.MainGui.BTN_DIMENSION;
import static com.pesterenan.views.MainGui.MARGIN_BORDER_10_PX_LR;

import java.awt.*;
import java.awt.event.ActionEvent;

public class StatusJPanel extends JPanel implements UIMethods {
    private static final long serialVersionUID = 1L;

    private static JLabel lblStatus;
    private static JButton btnConnect;
    private Dimension pnlDimension = new Dimension(464, 30);

    public StatusJPanel() {
        initComponents();
        setupComponents();
        layoutComponents();
    }

    @Override
    public void initComponents() {
        // Labels:
        lblStatus = new JLabel(Bundle.getString("lbl_stat_ready"));

        // Buttons:
        btnConnect = new JButton(Bundle.getString("btn_stat_connect"));
    }

    @Override
    public void setupComponents() {
        // Main Panel setup:
        setBorder(MARGIN_BORDER_10_PX_LR);
        setPreferredSize(pnlDimension);

        // Setting-up components:
        btnConnect.addActionListener(this::handleConnect);
        btnConnect.setPreferredSize(BTN_DIMENSION);
        btnConnect.setMaximumSize(BTN_DIMENSION);
        btnConnect.setVisible(false);
    }

    @Override
    public void layoutComponents() {
        // Main Panel layout:
        setLayout(new BorderLayout());

        // Laying out components:
        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.X_AXIS));
        pnlMain.add(lblStatus);
        pnlMain.add(Box.createHorizontalGlue());
        pnlMain.add(btnConnect);

        add(pnlMain, BorderLayout.CENTER);
    }

    public static void setStatusMessage(String newStatus) {
        lblStatus.setText(newStatus);
    }

    public static void isBtnConnectVisible(boolean visible) {
        btnConnect.setVisible(visible);
    }

    private void handleConnect(ActionEvent e) {
        setStatusMessage(Bundle.getString("status_connecting"));
        MechPeste.newInstance().connectToKSP();
    }
}

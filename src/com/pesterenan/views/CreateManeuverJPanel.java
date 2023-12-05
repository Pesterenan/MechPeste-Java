package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.ControlePID;

import krpc.client.RPCException;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Orbit;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import org.javatuples.Pair;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import static com.pesterenan.views.MainGui.PNL_DIMENSION;
import static com.pesterenan.views.MainGui.BTN_DIMENSION;

public class CreateManeuverJPanel extends JPanel implements ActionListener, UIMethods {

    private static JLabel lblManeuverInfo;
    private static JButton btnCreateManeuver, btnDeleteManeuver, btnBack, btnAp, btnPe, btnAN, btnDN;
    private static JButton btnIncrease, btnDecrease, btnNextOrbit, btnPrevOrbit;
    private static JSlider sldScale;
    private static JList<String> listCurrentManeuvers;
    private static int selectedManeuverIndex = 0;
    private static JRadioButton rbPrograde, rbNormal, rbRadial, rbTime;
    private static ButtonGroup bgManeuverType;
    private static Map<Integer, Float> sliderValues = new HashMap<>();
    private final ControlePID ctrlManeuver = new ControlePID();

    public CreateManeuverJPanel() {
        initComponents();
        setupComponents();
        layoutComponents();
    }

    @Override
    public void initComponents() {
        // Labels:
        lblManeuverInfo = new JLabel("");

        // Buttons:
        btnCreateManeuver = new JButton("Criar");
        btnDeleteManeuver = new JButton("Apagar");
        btnAp = new JButton("AP");
        btnPe = new JButton("PE");
        btnAN = new JButton("NA");
        btnDN = new JButton("ND");
        btnBack = new JButton(Bundle.getString("pnl_mnv_btn_back"));
        btnIncrease = new JButton("+");
        btnDecrease = new JButton("-");
        btnNextOrbit = new JButton(">");
        btnPrevOrbit = new JButton("<");

        // Radio buttons:
        rbPrograde = new JRadioButton("Pro");
        rbNormal = new JRadioButton("Nor");
        rbRadial = new JRadioButton("Rad");
        rbTime = new JRadioButton("Tmp");

        // Misc:
        listCurrentManeuvers = new JList<>();
        sldScale = new JSlider(JSlider.VERTICAL, 0, 5, 2);
        bgManeuverType = new ButtonGroup();
        sliderValues.put(0, 0.01f);
        sliderValues.put(1, 0.1f);
        sliderValues.put(2, 1f);
        sliderValues.put(3, 10f);
        sliderValues.put(4, 100f);
        sliderValues.put(5, 1000f);

        ctrlManeuver.adjustOutput(-100, 100);
    }

    @Override
    public void setupComponents() {
        // Setting-up components:
        btnCreateManeuver.addActionListener(this);
        btnCreateManeuver.setMaximumSize(BTN_DIMENSION);
        btnCreateManeuver.setPreferredSize(BTN_DIMENSION);
        btnDeleteManeuver.addActionListener(this);
        btnDeleteManeuver.setMaximumSize(BTN_DIMENSION);
        btnDeleteManeuver.setPreferredSize(BTN_DIMENSION);
        btnAp.addActionListener(this);
        btnAp.setActionCommand("apoapsis");
        btnPe.addActionListener(this);
        btnPe.setActionCommand("periapsis");
        btnAN.addActionListener(this);
        btnAN.setActionCommand("ascending");
        btnDN.addActionListener(this);
        btnDN.setActionCommand("descending");
        btnBack.addActionListener(this);
        btnBack.setMaximumSize(BTN_DIMENSION);
        btnBack.setPreferredSize(BTN_DIMENSION);
        btnIncrease.setActionCommand("increase");
        btnIncrease.addActionListener(this);
        btnIncrease.addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation();
            if (rotation > 0) {
                changeManeuverDeltaV(btnDecrease.getActionCommand());
            } else {
                changeManeuverDeltaV(btnIncrease.getActionCommand());
            }
        });
        btnIncrease.setMaximumSize(new Dimension(70, 26));
        btnIncrease.setPreferredSize(new Dimension(70, 26));
        btnIncrease.setMargin(new Insets(0, 0, 0, 0));
        btnDecrease.setActionCommand("decrease");
        btnDecrease.addActionListener(this);
        btnDecrease.addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation();
            if (rotation > 0) {
                changeManeuverDeltaV(btnIncrease.getActionCommand());
            } else {
                changeManeuverDeltaV(btnDecrease.getActionCommand());
            }
        });
        btnDecrease.setMaximumSize(new Dimension(70, 26));
        btnDecrease.setPreferredSize(new Dimension(70, 26));
        btnDecrease.setMargin(new Insets(0, 0, 0, 0));
        btnNextOrbit.setActionCommand("next_orbit");
        btnNextOrbit.addActionListener(this);
        btnNextOrbit.setMaximumSize(new Dimension(35, 26));
        btnNextOrbit.setPreferredSize(new Dimension(35, 26));
        btnNextOrbit.setMargin(new Insets(0, 0, 0, 0));
        btnPrevOrbit.setActionCommand("prev_orbit");
        btnPrevOrbit.addActionListener(this);
        btnPrevOrbit.setMaximumSize(new Dimension(35, 26));
        btnPrevOrbit.setPreferredSize(new Dimension(35, 26));
        btnPrevOrbit.setMargin(new Insets(0, 0, 0, 0));

        rbPrograde.setActionCommand("prograde");
        rbPrograde.addChangeListener(e -> handleChangeButtonText(sldScale.getValue()));
        rbNormal.setActionCommand("normal");
        rbNormal.addChangeListener(e -> handleChangeButtonText(sldScale.getValue()));
        rbRadial.setActionCommand("radial");
        rbRadial.addChangeListener(e -> handleChangeButtonText(sldScale.getValue()));
        rbTime.setActionCommand("time");
        rbTime.addChangeListener(e -> handleChangeButtonText(sldScale.getValue()));
        bgManeuverType.add(rbPrograde);
        bgManeuverType.add(rbNormal);
        bgManeuverType.add(rbRadial);
        bgManeuverType.add(rbTime);
        bgManeuverType.setSelected(rbPrograde.getModel(), true);

        listCurrentManeuvers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listCurrentManeuvers.addListSelectionListener(e -> selectedManeuverIndex = e.getFirstIndex());

        sldScale.setSnapToTicks(true);
        sldScale.setPaintTicks(true);
        sldScale.setMajorTickSpacing(1);
        sldScale.setMinorTickSpacing(1);
        sldScale.setPaintLabels(false);
        sldScale.addChangeListener(e -> handleChangeButtonText(sldScale.getValue()));
        sldScale.addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation();
            if (rotation < 0) {
                sldScale.setValue(sldScale.getValue() + sldScale.getMinorTickSpacing());
            } else {
                sldScale.setValue(sldScale.getValue() - sldScale.getMinorTickSpacing());
            }
        });
    }

    @Override
    public void layoutComponents() {
        // Main Panel layout:
        setPreferredSize(PNL_DIMENSION);
        setSize(PNL_DIMENSION);
        setLayout(new BorderLayout());

        JPanel pnlPositionManeuver = new JPanel();
        pnlPositionManeuver.setLayout(new BoxLayout(pnlPositionManeuver, BoxLayout.X_AXIS));
        pnlPositionManeuver.setBorder(new TitledBorder("Posicionar manobra no:"));
        pnlPositionManeuver.add(Box.createHorizontalGlue());
        pnlPositionManeuver.add(btnAp);
        pnlPositionManeuver.add(btnPe);
        pnlPositionManeuver.add(btnAN);
        pnlPositionManeuver.add(btnDN);
        pnlPositionManeuver.add(Box.createHorizontalGlue());

        JPanel pnlRadioButtons = new JPanel(new GridLayout(4, 1));
        pnlRadioButtons.setMaximumSize(new Dimension(50, 100));
        pnlRadioButtons.add(rbPrograde);
        pnlRadioButtons.add(rbNormal);
        pnlRadioButtons.add(rbRadial);
        pnlRadioButtons.add(rbTime);

        JPanel pnlSlider = new JPanel();
        pnlSlider.setLayout(new BoxLayout(pnlSlider, BoxLayout.Y_AXIS));
        pnlSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlSlider.setBorder(new TitledBorder("Escala:"));
        pnlSlider.add(sldScale);
        pnlSlider.add(Box.createHorizontalStrut(40));

        JPanel pnlOrbitControl = new JPanel();
        pnlOrbitControl.setLayout(new BoxLayout(pnlOrbitControl, BoxLayout.X_AXIS));
        pnlOrbitControl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlOrbitControl.add(btnPrevOrbit);
        pnlOrbitControl.add(btnNextOrbit);

        JPanel pnlManeuverButtons = new JPanel();
        pnlManeuverButtons.setLayout(new BoxLayout(pnlManeuverButtons, BoxLayout.Y_AXIS));
        pnlManeuverButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlManeuverButtons.add(btnIncrease);
        pnlManeuverButtons.add(pnlOrbitControl);
        pnlManeuverButtons.add(btnDecrease);

        JPanel pnlManeuverController = new JPanel();
        pnlManeuverController.setLayout(new BoxLayout(pnlManeuverController, BoxLayout.X_AXIS));
        pnlManeuverController.setBorder(new TitledBorder("Controlador de Manobra:"));
        pnlManeuverController.setMaximumSize(new Dimension(180, 300));
        pnlManeuverController.add(pnlRadioButtons);
        pnlManeuverController.add(pnlSlider);
        pnlManeuverController.add(pnlManeuverButtons);

        JPanel pnlManeuverInfo = new JPanel();
        pnlManeuverInfo.setLayout(new BoxLayout(pnlManeuverInfo, BoxLayout.Y_AXIS));
        pnlManeuverInfo.setBorder(new TitledBorder("Info. Manobra:"));
        pnlManeuverInfo.setMaximumSize(new Dimension(300, 300));

        JPanel pnlMCpnlAP = new JPanel();
        pnlMCpnlAP.setLayout(new BoxLayout(pnlMCpnlAP, BoxLayout.X_AXIS));
        pnlMCpnlAP.add(pnlManeuverController);
        pnlMCpnlAP.add(pnlManeuverInfo);

        JPanel pnlControls = new JPanel();
        pnlControls.setLayout(new BoxLayout(pnlControls, BoxLayout.Y_AXIS));
        pnlControls.add(pnlPositionManeuver);
        pnlControls.add(pnlMCpnlAP);

        JPanel pnlManeuverList = new JPanel();
        pnlManeuverList.setLayout(new BoxLayout(pnlManeuverList, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(listCurrentManeuvers);
        pnlManeuverList.add(scrollPane);
        pnlManeuverList.add(btnCreateManeuver);
        pnlManeuverList.add(btnDeleteManeuver);

        JPanel pnlOptions = new JPanel();
        pnlOptions.setLayout(new BoxLayout(pnlOptions, BoxLayout.Y_AXIS));
        pnlOptions.setBorder(new TitledBorder("Lista de Manobras:"));
        pnlOptions.add(pnlManeuverList);
        pnlOptions.setPreferredSize(new Dimension(110, 300));
        pnlOptions.setMaximumSize(new Dimension(110, 300));

        JPanel pnlOptionsAndList = new JPanel();
        pnlOptionsAndList.setLayout(new BoxLayout(pnlOptionsAndList, BoxLayout.X_AXIS));
        pnlControls.setAlignmentY(TOP_ALIGNMENT);
        pnlOptions.setAlignmentY(TOP_ALIGNMENT);
        pnlOptionsAndList.add(pnlOptions);
        pnlOptionsAndList.add(Box.createHorizontalStrut(5));
        pnlOptionsAndList.add(pnlControls);

        JPanel pnlBackButton = new JPanel();
        pnlBackButton.setLayout(new BoxLayout(pnlBackButton, BoxLayout.X_AXIS));
        pnlBackButton.add(lblManeuverInfo);
        pnlBackButton.add(Box.createHorizontalGlue());
        pnlBackButton.add(btnBack);

        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.X_AXIS));
        pnlMain.add(pnlOptionsAndList);

        add(pnlMain, BorderLayout.CENTER);
        add(pnlBackButton, BorderLayout.SOUTH);
    }

    public static void updatePanel(ListModel<String> list) {
        try {
            boolean hasManeuverNodes = list.getSize() > 0;
            boolean hasTargetVessel = MechPeste.getSpaceCenter().getTargetVessel() != null;
            listCurrentManeuvers.setModel(list);
            listCurrentManeuvers.setSelectedIndex(selectedManeuverIndex);
            btnDeleteManeuver.setEnabled(hasManeuverNodes);
            btnAp.setEnabled(hasManeuverNodes);
            btnPe.setEnabled(hasManeuverNodes);
            btnAN.setEnabled(hasManeuverNodes && hasTargetVessel);
            btnDN.setEnabled(hasManeuverNodes && hasTargetVessel);
            btnIncrease.setEnabled(hasManeuverNodes);
            btnDecrease.setEnabled(hasManeuverNodes);
            btnNextOrbit.setEnabled(hasManeuverNodes);
            btnPrevOrbit.setEnabled(hasManeuverNodes);
            lblManeuverInfo.setText(listCurrentManeuvers.getSelectedValue());
        } catch (RPCException ignored) {
        }
    }

    private static void handleChangeButtonText(int value) {
        String decimalPlaces = value > 1 ? "%.0f" : "%.2f";
        String formattedValue = String.format(Locale.ENGLISH, decimalPlaces, sliderValues.get(value));
        String suffix = bgManeuverType.getSelection() == rbTime.getModel() ? " s" : "m/s";
        btnIncrease.setText("+ " + formattedValue + suffix);
        btnDecrease.setText("- " + formattedValue + suffix);
    }

    private void createManeuver() {
        try {
            createManeuver(MechPeste.getSpaceCenter().getUT() + 60);
        } catch (RPCException e) {
        }
    }

    private void createManeuver(double atFutureTime) {
        try {
            MechPeste.newInstance();
            Vessel vessel = MechPeste.getSpaceCenter().getActiveVessel();

            if (vessel.getSituation() != VesselSituation.ORBITING) {
                StatusJPanel.setStatusMessage("Não é possível criar a manobra fora de órbita.");
                return;
            }
            vessel.getControl().addNode(atFutureTime, 0, 0, 0);
        } catch (Exception e) {
        }
    }

    private void deleteManeuver() {
        try {
            MechPeste.newInstance();
            Vessel vessel = MechPeste.getSpaceCenter().getActiveVessel();
            Node currentManeuver = vessel.getControl().getNodes().get(selectedManeuverIndex);
            currentManeuver.remove();
        } catch (Exception e) {
        }
    }

    private void positionManeuverAt(String node) {
        try {
            MechPeste.newInstance();
            Vessel vessel = MechPeste.getSpaceCenter().getActiveVessel();
            Orbit orbit = vessel.getOrbit();
            Node currentManeuver = vessel.getControl().getNodes().get(selectedManeuverIndex);
            double timeToNode = 0;
            switch (node) {
                case "apoapsis":
                    timeToNode = MechPeste.getSpaceCenter().getUT() + orbit.getTimeToApoapsis();
                    break;
                case "periapsis":
                    timeToNode = MechPeste.getSpaceCenter().getUT() + orbit.getTimeToPeriapsis();
                    break;
                case "ascending":
                    double ascendingAnomaly = orbit
                            .trueAnomalyAtAN(MechPeste.getSpaceCenter().getTargetVessel().getOrbit());
                    timeToNode = orbit.uTAtTrueAnomaly(ascendingAnomaly);
                    break;
                case "descending":
                    double descendingAnomaly = orbit
                            .trueAnomalyAtDN(MechPeste.getSpaceCenter().getTargetVessel().getOrbit());
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

    private void changeManeuverDeltaV(String command) {
        try {
            MechPeste.newInstance();
            Vessel vessel = MechPeste.getSpaceCenter().getActiveVessel();
            Node currentManeuver = vessel.getControl().getNodes().get(selectedManeuverIndex);
            String maneuverType = bgManeuverType.getSelection().getActionCommand();
            float currentSliderValue = sliderValues.get(sldScale.getValue());
            currentSliderValue = command == "decrease" ? -currentSliderValue : currentSliderValue;

            switch (maneuverType) {
                case "prograde":
                    currentManeuver.setPrograde(currentManeuver.getPrograde() + currentSliderValue);
                    break;
                case "normal":
                    currentManeuver.setNormal(currentManeuver.getNormal() + currentSliderValue);
                    break;
                case "radial":
                    currentManeuver.setRadial(currentManeuver.getRadial() + currentSliderValue);
                    break;
                case "time":
                    currentManeuver.setUT(currentManeuver.getUT() + currentSliderValue);
                    break;
            }
        } catch (RPCException e) {
            System.err.println("Erro RPC ao mudar o delta-V da manobra: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao mudar o delta-V da manobra: " + e.getMessage());
        }

    }

    private void changeOrbit(String command) {
        try {
            MechPeste.newInstance();
            Vessel vessel;
            vessel = MechPeste.getSpaceCenter().getActiveVessel();
            Node currentManeuver = vessel.getControl().getNodes().get(selectedManeuverIndex);
            double currentOrbitPeriod = vessel.getOrbit().getPeriod();
            if (command == "next_orbit") {
                currentManeuver.setUT(currentManeuver.getUT() + currentOrbitPeriod);
            } else {
                double newUT = currentManeuver.getUT() - currentOrbitPeriod;
                newUT = newUT < MechPeste.getSpaceCenter().getUT() ? MechPeste.getSpaceCenter().getUT() + 60 : newUT;
                currentManeuver.setUT(newUT);
            }
        } catch (RPCException ignored) {
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCreateManeuver) {
            createManeuver();
        }
        if (e.getSource() == btnDeleteManeuver) {
            deleteManeuver();
        }
        if (e.getSource() == btnIncrease || e.getSource() == btnDecrease) {
            changeManeuverDeltaV(e.getActionCommand());
        }
        if (e.getSource() == btnNextOrbit || e.getSource() == btnPrevOrbit) {
            changeOrbit(e.getActionCommand());
        }
        if (e.getSource() == btnAp || e.getSource() == btnPe || e.getSource() == btnAN || e.getSource() == btnDN) {
            positionManeuverAt(e.getActionCommand());
        }
        if (e.getSource() == btnBack) {
            MainGui.backToTelemetry(e);
        }
    }
}

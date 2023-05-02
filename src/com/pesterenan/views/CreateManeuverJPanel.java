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

import org.javatuples.Pair;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static com.pesterenan.views.MainGui.PNL_DIMENSION;
import static com.pesterenan.views.MainGui.BTN_DIMENSION;

public class CreateManeuverJPanel extends JPanel implements ActionListener, UIMethods {

    private static JButton btnCreateManeuver, btnDeleteManeuver, btnBack, btnAp, btnPe, btnAN, btnDN;
    private static JButton btnIncrease, btnDecrease, btnNextOrbit, btnPrevOrbit;
    private static JSlider sldScale;
    private static JList<String> listCurrentManeuvers;
    private static int selectedManeuverIndex = 0;
    private static JRadioButton rbPrograde, rbNormal, rbRadial, rbTime;
    private static ButtonGroup bgManeuverType;
    private static Map<Integer, Pair<String, Float>> sliderValues = new HashMap<>();

    public CreateManeuverJPanel() {
        initComponents();
        setupComponents();
        layoutComponents();
    }

    @Override
    public void initComponents() {
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
        sliderValues.put(0, new Pair<>("0.01m/s", 0.01f));
        sliderValues.put(1, new Pair<>("0.10m/s", 0.1f));
        sliderValues.put(2, new Pair<>("1.0 m/s", 1f));
        sliderValues.put(3, new Pair<>("10  m/s", 10f));
        sliderValues.put(4, new Pair<>("100 m/s", 100f));
        sliderValues.put(5, new Pair<>("1k  m/s", 1000f));
    }

    @Override
    public void setupComponents() {
        // Main Panel setup:
        setBorder(new TitledBorder(null, Bundle.getString("pnl_mnv_border"), TitledBorder.LEADING, TitledBorder.TOP,
                null, null));

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
        btnDecrease.setActionCommand("decrease");
        btnNextOrbit.setActionCommand("next_orbit");
        btnPrevOrbit.setActionCommand("prev_orbit");
        btnIncrease.addActionListener(this);
        btnIncrease.addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation();
            if (rotation > 0) {
                changeManeuverDeltaV(btnDecrease.getActionCommand());
            } else {
                changeManeuverDeltaV(btnIncrease.getActionCommand());
            }
        });
        btnDecrease.addActionListener(this);
        btnDecrease.addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation();
            if (rotation > 0) {
                changeManeuverDeltaV(btnIncrease.getActionCommand());
            } else {
                changeManeuverDeltaV(btnDecrease.getActionCommand());
            }
        });;
        btnNextOrbit.addActionListener(this);
        btnPrevOrbit.addActionListener(this);
        btnIncrease.setMaximumSize(new Dimension(70, 26));
        btnDecrease.setMaximumSize(new Dimension(70, 26));
        btnNextOrbit.setMaximumSize(new Dimension(35, 26));
        btnPrevOrbit.setMaximumSize(new Dimension(35, 26));
        btnIncrease.setPreferredSize(new Dimension(70, 26));
        btnDecrease.setPreferredSize(new Dimension(70, 26));
        btnNextOrbit.setPreferredSize(new Dimension(35, 26));
        btnPrevOrbit.setPreferredSize(new Dimension(35, 26));
        btnIncrease.setMargin(new Insets(0, 0, 0, 0));
        btnDecrease.setMargin(new Insets(0, 0, 0, 0));
        btnNextOrbit.setMargin(new Insets(0, 0, 0, 0));
        btnPrevOrbit.setMargin(new Insets(0, 0, 0, 0));

        rbPrograde.setActionCommand("prograde");
        rbNormal.setActionCommand("normal");
        rbRadial.setActionCommand("radial");
        rbTime.setActionCommand("time");
        rbPrograde.setSelected(true);
        bgManeuverType.add(rbPrograde);
        bgManeuverType.add(rbNormal);
        bgManeuverType.add(rbRadial);
        bgManeuverType.add(rbTime);

        listCurrentManeuvers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listCurrentManeuvers.addListSelectionListener(e -> selectedManeuverIndex = e.getFirstIndex());

        sldScale.setSnapToTicks(true);
        sldScale.setPaintTicks(true);
        sldScale.setMajorTickSpacing(1);
        sldScale.setMinorTickSpacing(1);
        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(0, new JLabel(sliderValues.get(0).getValue0()));
        labelTable.put(1, new JLabel(sliderValues.get(1).getValue0()));
        labelTable.put(2, new JLabel(sliderValues.get(2).getValue0()));
        labelTable.put(3, new JLabel(sliderValues.get(3).getValue0()));
        labelTable.put(4, new JLabel(sliderValues.get(4).getValue0()));
        labelTable.put(5, new JLabel(sliderValues.get(5).getValue0()));
        sldScale.setLabelTable(labelTable);
        sldScale.setPaintLabels(true);
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
        pnlSlider.setPreferredSize(new Dimension(50, 100));
        pnlSlider.add(sldScale);

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
        pnlManeuverController.setMaximumSize(new Dimension(400, 300));
        pnlManeuverController.add(pnlRadioButtons);
        pnlManeuverController.add(pnlSlider);
        pnlManeuverController.add(pnlManeuverButtons);

        JPanel pnlControls = new JPanel();
        pnlControls.setLayout(new BoxLayout(pnlControls, BoxLayout.Y_AXIS));
        pnlControls.add(pnlPositionManeuver);
        pnlControls.add(pnlManeuverController);

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
        pnlBackButton.add(Box.createHorizontalGlue());
        pnlBackButton.add(btnBack);

        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.X_AXIS));
        pnlMain.add(pnlOptionsAndList);

        add(pnlMain, BorderLayout.CENTER);
        add(pnlBackButton, BorderLayout.SOUTH);
    }

    public static void updateManeuverList(ListModel<String> list) {
        listCurrentManeuvers.setModel(list);
        listCurrentManeuvers.setSelectedIndex(selectedManeuverIndex);
        btnDeleteManeuver.setEnabled(list.getSize() > 0);
        try {
            btnAp.setEnabled(list.getSize() > 0);
            btnPe.setEnabled(list.getSize() > 0);
            btnAN.setEnabled(list.getSize() > 0 && MechPeste.getSpaceCenter().getTargetVessel() != null);
            btnDN.setEnabled(list.getSize() > 0 && MechPeste.getSpaceCenter().getTargetVessel() != null);
        } catch (RPCException ignored) {
        }
    }

    private static void handleChangeButtonText(int value) {
        btnIncrease.setText("+ " + sliderValues.get(value).getValue0());
        btnDecrease.setText("- " + sliderValues.get(value).getValue0());
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
            float currentSliderValue = sliderValues.get(sldScale.getValue()).getValue1();
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
        if (e.getSource() == btnAp || e.getSource() == btnPe || e.getSource() == btnAN || e.getSource() == btnDN) {
            positionManeuverAt(e.getActionCommand());
        }
        if (e.getSource() == btnBack) {
            MainGui.backToTelemetry(e);
        }
    }
}

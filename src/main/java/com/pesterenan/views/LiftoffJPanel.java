package com.pesterenan.views;

import static com.pesterenan.views.MainGui.BTN_DIMENSION;
import static com.pesterenan.views.MainGui.MARGIN_BORDER_10_PX_LR;
import static com.pesterenan.views.MainGui.PNL_DIMENSION;

import com.pesterenan.model.VesselManager;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Module;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class LiftoffJPanel extends JPanel implements UIMethods {

  private static final long serialVersionUID = 1L;

  private JLabel lblFinalApoapsis, lblHeading, lblRoll, lblCurveModel, lblLimitTWR;
  private JTextField txfFinalApoapsis, txfHeading, txfLimitTWR;
  private JButton btnLiftoff, btnBack;
  private JComboBox<String> cbGravityCurveModel;
  private JSlider sldRoll;
  private JCheckBox chkOpenPanels, chkDecoupleStages;
  private VesselManager vesselManager;

  private StatusDisplay statusDisplay;

  public LiftoffJPanel(StatusDisplay statusDisplay) {
    this.statusDisplay = statusDisplay;
    initComponents();
    setupComponents();
    layoutComponents();
  }

  public void setVesselManager(VesselManager vesselManager) {
    this.vesselManager = vesselManager;
    btnLiftoff.setEnabled(true);
  }

  @Override
  public void initComponents() {
    // Labels:
    lblFinalApoapsis = new JLabel(Bundle.getString("pnl_lift_lbl_final_apoapsis"));
    lblHeading = new JLabel(Bundle.getString("pnl_lift_lbl_direction"));
    lblRoll = new JLabel(Bundle.getString("pnl_lift_lbl_roll"));
    lblRoll.setToolTipText(Bundle.getString("pnl_lift_lbl_roll_tooltip"));
    lblCurveModel = new JLabel(Bundle.getString("pnl_lift_lbl_gravity_curve"));
    lblLimitTWR = new JLabel(Bundle.getString("pnl_common_lbl_limit_twr"));

    // Textfields:
    txfFinalApoapsis = new JTextField("80000");
    txfFinalApoapsis.setToolTipText(Bundle.getString("pnl_lift_txf_final_apo_tooltip"));
    txfHeading = new JTextField("90");
    txfLimitTWR = new JTextField("2.2");

    // Buttons:
    btnLiftoff = new JButton(Bundle.getString("pnl_lift_btn_liftoff"));
    btnLiftoff.setEnabled(false);
    btnBack = new JButton(Bundle.getString("pnl_lift_btn_back"));

    // Checkboxes:
    chkOpenPanels = new JCheckBox(Bundle.getString("pnl_lift_chk_open_panels"));
    chkOpenPanels.setToolTipText(Bundle.getString("pnl_lift_chk_open_panels_tooltip"));
    chkDecoupleStages = new JCheckBox(Bundle.getString("pnl_lift_chk_staging"));
    chkDecoupleStages.setToolTipText(Bundle.getString("pnl_lift_chk_staging_tooltip"));

    // Misc:
    cbGravityCurveModel = new JComboBox<>();
    cbGravityCurveModel.setToolTipText(Bundle.getString("pnl_lift_cb_gravity_curve_tooltip"));

    sldRoll = new JSlider();
  }

  @Override
  public void setupComponents() {
    // Main Panel setup:
    setBorder(
        new TitledBorder(
            null,
            Bundle.getString("pnl_lift_pnl_title"),
            TitledBorder.LEADING,
            TitledBorder.TOP,
            null,
            null));

    // Setting-up components:
    lblFinalApoapsis.setLabelFor(txfFinalApoapsis);
    txfFinalApoapsis.setMaximumSize(BTN_DIMENSION);
    txfFinalApoapsis.setPreferredSize(BTN_DIMENSION);
    txfFinalApoapsis.setHorizontalAlignment(JTextField.RIGHT);
    lblHeading.setLabelFor(txfHeading);
    txfHeading.setMaximumSize(BTN_DIMENSION);
    txfHeading.setPreferredSize(BTN_DIMENSION);
    txfHeading.setHorizontalAlignment(JTextField.RIGHT);
    lblLimitTWR.setLabelFor(txfLimitTWR);
    txfLimitTWR.setMaximumSize(BTN_DIMENSION);
    txfLimitTWR.setPreferredSize(BTN_DIMENSION);
    txfLimitTWR.setHorizontalAlignment(JTextField.RIGHT);

    cbGravityCurveModel.setModel(
        new DefaultComboBoxModel<>(
            new String[] {
              Module.SINUSOIDAL.get(),
              Module.QUADRATIC.get(),
              Module.CUBIC.get(),
              Module.CIRCULAR.get(),
              Module.EXPONENCIAL.get()
            }));
    cbGravityCurveModel.setSelectedIndex(3);
    cbGravityCurveModel.setPreferredSize(BTN_DIMENSION);
    cbGravityCurveModel.setMaximumSize(BTN_DIMENSION);

    sldRoll.setPaintLabels(true);
    sldRoll.setMajorTickSpacing(90);
    sldRoll.setMaximum(270);
    sldRoll.setSnapToTicks(true);
    sldRoll.setValue(90);
    sldRoll.setPreferredSize(new Dimension(110, 40));
    sldRoll.setMaximumSize(new Dimension(110, 40));

    chkDecoupleStages.setSelected(true);

    btnLiftoff.addActionListener(this::handleLiftoff);
    btnLiftoff.setPreferredSize(BTN_DIMENSION);
    btnLiftoff.setMaximumSize(BTN_DIMENSION);
    btnBack.addActionListener(MainGui::backToTelemetry);
    btnBack.setPreferredSize(BTN_DIMENSION);
    btnBack.setMaximumSize(BTN_DIMENSION);
  }

  @Override
  public void layoutComponents() {
    // Main Panel layout:
    setPreferredSize(PNL_DIMENSION);
    setSize(PNL_DIMENSION);
    setLayout(new BorderLayout());

    // Laying out components:
    JPanel pnlFinalApoapsis = new JPanel();
    pnlFinalApoapsis.setLayout(new BoxLayout(pnlFinalApoapsis, BoxLayout.X_AXIS));
    pnlFinalApoapsis.setBorder(MARGIN_BORDER_10_PX_LR);
    pnlFinalApoapsis.add(lblFinalApoapsis);
    pnlFinalApoapsis.add(Box.createHorizontalGlue());
    pnlFinalApoapsis.add(txfFinalApoapsis);

    JPanel pnlHeading = new JPanel();
    pnlHeading.setLayout(new BoxLayout(pnlHeading, BoxLayout.X_AXIS));
    pnlHeading.setBorder(MARGIN_BORDER_10_PX_LR);
    pnlHeading.add(lblHeading);
    pnlHeading.add(Box.createHorizontalGlue());
    pnlHeading.add(txfHeading);

    JPanel pnlRoll = new JPanel();
    pnlRoll.setLayout(new BoxLayout(pnlRoll, BoxLayout.X_AXIS));
    pnlRoll.setBorder(MARGIN_BORDER_10_PX_LR);
    pnlRoll.add(lblRoll);
    pnlRoll.add(Box.createHorizontalGlue());
    pnlRoll.add(sldRoll);

    JPanel pnlLimitTWR = new JPanel();
    pnlLimitTWR.setLayout(new BoxLayout(pnlLimitTWR, BoxLayout.X_AXIS));
    pnlLimitTWR.setBorder(MARGIN_BORDER_10_PX_LR);
    pnlLimitTWR.add(lblLimitTWR);
    pnlLimitTWR.add(Box.createHorizontalGlue());
    pnlLimitTWR.add(txfLimitTWR);

    JPanel pnlCurveModel = new JPanel();
    pnlCurveModel.setLayout(new BoxLayout(pnlCurveModel, BoxLayout.X_AXIS));
    pnlCurveModel.setBorder(MARGIN_BORDER_10_PX_LR);
    pnlCurveModel.add(lblCurveModel);
    pnlCurveModel.add(Box.createHorizontalGlue());
    pnlCurveModel.add(cbGravityCurveModel);

    JPanel pnlButtons = new JPanel();
    pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
    pnlButtons.add(btnLiftoff);
    pnlButtons.add(Box.createHorizontalGlue());
    pnlButtons.add(btnBack);

    JPanel pnlSetup = new JPanel();
    pnlSetup.setLayout(new BoxLayout(pnlSetup, BoxLayout.Y_AXIS));
    pnlSetup.add(MainGui.createMarginComponent(0, 6));
    pnlSetup.add(pnlFinalApoapsis);
    pnlSetup.add(pnlHeading);
    pnlSetup.add(pnlRoll);
    pnlSetup.add(pnlLimitTWR);
    pnlSetup.add(pnlCurveModel);

    JPanel pnlOptions = new JPanel();
    pnlOptions.setLayout(new BoxLayout(pnlOptions, BoxLayout.Y_AXIS));
    pnlOptions.setBorder(new TitledBorder(Bundle.getString("pnl_lift_chk_options")));
    pnlOptions.add(chkDecoupleStages);
    pnlOptions.add(chkOpenPanels);

    JPanel pnlMain = new JPanel();
    pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.X_AXIS));
    pnlMain.add(pnlSetup);
    pnlSetup.setAlignmentY(Component.TOP_ALIGNMENT);
    pnlMain.add(pnlOptions);
    pnlOptions.setAlignmentY(Component.TOP_ALIGNMENT);

    add(pnlMain, BorderLayout.CENTER);
    add(pnlButtons, BorderLayout.SOUTH);
  }

  private boolean validateTextFields() {
    try {
      Float.parseFloat(txfFinalApoapsis.getText());
      Float.parseFloat(txfHeading.getText());
      Float.parseFloat(txfLimitTWR.getText());
    } catch (NumberFormatException e) {
      statusDisplay.setStatusMessage(Bundle.getString("pnl_lift_stat_only_numbers"));
      return false;
    }
    return true;
  }

  private void handleLiftoff(ActionEvent e) {
    if (vesselManager == null) {
      statusDisplay.setStatusMessage("Conexão não estabelecida.");
      return;
    }
    if (validateTextFields()) {
      Map<String, String> commands = new HashMap<>();
      commands.put(Module.MODULO.get(), Module.LIFTOFF.get());
      commands.put(Module.APOAPSIS.get(), txfFinalApoapsis.getText());
      commands.put(Module.DIRECTION.get(), txfHeading.getText());
      commands.put(Module.MAX_TWR.get(), txfLimitTWR.getText());
      commands.put(Module.ROLL.get(), String.valueOf(sldRoll.getValue()));
      commands.put(Module.INCLINATION.get(), cbGravityCurveModel.getSelectedItem().toString());
      commands.put(Module.STAGE.get(), String.valueOf(chkDecoupleStages.isSelected()));
      commands.put(Module.OPEN_PANELS.get(), String.valueOf(chkOpenPanels.isSelected()));
      vesselManager.startModule(commands);
      MainGui.backToTelemetry(e);
    }
  }
}

package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Modulos;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import static com.pesterenan.views.MainGui.BTN_DIMENSION;
import static com.pesterenan.views.MainGui.MARGIN_BORDER_10_PX_LR;

public class LandingJPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private final JTextField txfHover = new JTextField("50");
	private final JTextField txfMaxTWR = new JTextField("5");
	private final JButton btnHover = new JButton(Bundle.getString("pnl_land_btn_hover"));
	private final JButton btnAutoLanding = new JButton(Bundle.getString("pnl_land_btn_land"));
	private final JButton btnBack = new JButton(Bundle.getString("pnl_land_btn_back"));
	private JLabel lblHoverAltitude;
	private final JLabel lblAutolanding = new JLabel(Bundle.getString("pnl_land_lbl_land"));

	public LandingJPanel() {

		initComponents();
	}

	private void initComponents() {
		setPreferredSize(MainGui.PNL_DIMENSION);
		setSize(MainGui.PNL_DIMENSION);
		setBorder(new TitledBorder(null, Bundle.getString("pnl_land_border"), TitledBorder.LEADING,
				TitledBorder.TOP, null, null));

		btnHover.addActionListener(this);
		btnHover.setPreferredSize(BTN_DIMENSION);

		btnBack.addActionListener(this);
		btnBack.setPreferredSize(BTN_DIMENSION);

		btnAutoLanding.addActionListener(this);
		btnAutoLanding.setPreferredSize(BTN_DIMENSION);

		setLayout(new BorderLayout());

		JPanel pnlLandingControls = new JPanel();
		pnlLandingControls.setBorder(new EmptyBorder(0, 10, 0, 10));
		pnlLandingControls.setLayout(new BoxLayout(pnlLandingControls, BoxLayout.X_AXIS));
		pnlLandingControls.add(lblAutolanding);
		pnlLandingControls.add(Box.createHorizontalGlue());
		pnlLandingControls.add(btnAutoLanding);

		JPanel pnlTWRLimitControls = new JPanel();
		pnlTWRLimitControls.setBorder(new EmptyBorder(0, 10, 0, 10));
		pnlTWRLimitControls.setLayout(new BoxLayout(pnlTWRLimitControls, BoxLayout.X_AXIS));
		pnlTWRLimitControls.add(new JLabel("TWR Limit:"));
		txfMaxTWR.setPreferredSize(BTN_DIMENSION);
		txfMaxTWR.setMaximumSize(BTN_DIMENSION);
		pnlTWRLimitControls.add(Box.createHorizontalGlue());
		pnlTWRLimitControls.add(txfMaxTWR);

		JPanel pnlHoverControls = new JPanel();
		pnlHoverControls.setLayout(new BoxLayout(pnlHoverControls, BoxLayout.X_AXIS));
		Border titledEtched = new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				Bundle.getString("pnl_land_pnl_hover_border"), TitledBorder.LEADING, TitledBorder.TOP, null,
				new Color(0, 0, 0));
		Border combined = BorderFactory.createCompoundBorder(titledEtched, MARGIN_BORDER_10_PX_LR);
		pnlHoverControls.setBorder(combined);
		lblHoverAltitude = new JLabel(Bundle.getString("pnl_land_lbl_alt"));
		pnlHoverControls.add(lblHoverAltitude);
		txfHover.setPreferredSize(BTN_DIMENSION);
		txfHover.setMaximumSize(BTN_DIMENSION);
		pnlHoverControls.add(Box.createRigidArea(new Dimension(10,0)));
		pnlHoverControls.add(txfHover);
		pnlHoverControls.add(Box.createHorizontalGlue());
		pnlHoverControls.add(btnHover);

		JPanel pnlMainControls = new JPanel();
		pnlMainControls.setLayout(new BoxLayout(pnlMainControls, BoxLayout.Y_AXIS));
		pnlMainControls.add(pnlLandingControls);
		pnlMainControls.add(pnlTWRLimitControls);
		pnlMainControls.add(pnlHoverControls);

		JPanel pnlBackbtn = new JPanel();
		pnlBackbtn.setLayout(new BoxLayout(pnlBackbtn,BoxLayout.X_AXIS));
		pnlBackbtn.add(Box.createHorizontalGlue());
		pnlBackbtn.add(btnBack);

		add(pnlMainControls, BorderLayout.CENTER);
		add(pnlBackbtn, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnHover) {
			handleBtnHoverActionPerformed(e);
		}
		if (e.getSource() == btnBack) {
			MainGui.backToTelemetry();
		}
		if (e.getSource() == btnAutoLanding) {
			handleBtnAutoLandingActionPerformed(e);
		}
	}

	protected void handleBtnAutoLandingActionPerformed(ActionEvent e) {
		Map<String, String> commands = new HashMap<>();
		commands.put(Modulos.MODULO.get(), Modulos.MODULO_POUSO.get());
		commands.put(Modulos.MAX_TWR.get(), txfMaxTWR.getText());
		MechPeste.newInstance().startModule(commands);
	}

	protected void handleBtnHoverActionPerformed(ActionEvent e) {
		Map<String, String> commands = new HashMap<>();
		commands.put(Modulos.MODULO.get(), Modulos.MODULO_POUSO_SOBREVOAR.get());
		try {
			if (txfHover.getText().equals("")) {
				StatusJPanel.setStatus(Bundle.getString("pnl_land_hover_alt"));
				return;
			}
			Integer.parseInt(txfHover.getText());
		} catch (Exception e2) {
			StatusJPanel.setStatus(Bundle.getString("pnl_land_hover_alt_err"));
			return;
		}
		commands.put(Modulos.ALTITUDE_SOBREVOO.get(), txfHover.getText());
		MechPeste.newInstance().startModule(commands);
	}
}

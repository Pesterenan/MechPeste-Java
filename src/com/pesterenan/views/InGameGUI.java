package com.pesterenan.views;

import com.pesterenan.MechPeste;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Modulos;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.UI;
import krpc.client.services.UI.*;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.HashMap;
import java.util.Map;

public class InGameGUI {
private static InGameGUI iggui;
private static Connection connection;
private static Text txtStatus;
private static Pair<Double, Double> screenSize;
private static Panel pnlStatus;
private static Panel pnlLiftoff;
private static Panel pnlFunctions;
public final double MARGIN = 10.0;
private final double BUTTON_WIDTH = 100.0;
private final double BUTTON_HEIGHT = 30.0;
private UI ui;
private UI.Canvas canvas;
private Button btnAbort;
// Functions in-game panel buttons
private Button btnLiftoff;
private Stream<Boolean> btnLiftoffClicked;
private Button btnLanding;
private Stream<Boolean> btnLandingClicked;
private Button btnManeuver;
private Stream<Boolean> btnManeuverClicked;
private Button btnAutoRover;
private Stream<Boolean> btnAutoRoverClicked;
// Liftoff in-game panel buttons
private Button btnLiftoffExec;
private Stream<Boolean> btnLiftoffExecClicked;
private Pair<Double, Double> inGamePanelDimension = new Pair(450.0, 40.0);
private Pair<Double, Double> inGamePanelPosition;


private InGameGUI() {
	try {
		connection = MechPeste.getConnection();
		ui = UI.newInstance(connection);
		canvas = ui.getStockCanvas();
		screenSize = canvas.getRectTransform().getSize();
		inGamePanelPosition = new Pair<>(
						-(screenSize.getValue0() / 2) + MARGIN * 2, (screenSize.getValue1() / 2) - MARGIN * 8);
		pnlFunctions = createFunctionPanel(canvas);
		pnlStatus = createStatusPanel(canvas);
		pnlLiftoff = createLiftoffPanel(canvas);
// Add some text displaying the total engine thrust
		txtStatus = pnlStatus.addText(Bundle.getString("status_ready"), true);
		txtStatus.getRectTransform()
						 .setSize(new Pair<Double, Double>(pnlStatus.getRectTransform().getSize()
																												.getValue0(), 20.0));
		txtStatus.setColor(new Triplet<Double, Double, Double>(1.0, 1.0, 1.0));
		txtStatus.getRectTransform().setPivot(new Pair<>(0.0, 1.0));
		txtStatus.getRectTransform().setAnchor(new Pair<>(0.0, 1.0));
		txtStatus.getRectTransform().setPosition(new Pair<Double, Double>(5.0, -18.0));
		txtStatus.setSize(15);
// Set up a stream to monitor the throttle button
//		Stream<Boolean> buttonClicked = connection.addStream(button, "getClicked");
	} catch (RPCException | StreamException e) {
		throw new RuntimeException(e);
	}
}

public static void setTxtStatus(String status) {
	if (connection == null) return;
	try {
		txtStatus.setContent(status);
	} catch (RPCException ignored) {
	}
}

public static InGameGUI getInstance() {
	if (iggui == null) {
		iggui = new InGameGUI();
	}
	return iggui;
}

private Panel createStatusPanel(UI.Canvas canvas) throws RPCException {
	Panel panel = canvas.addPanel(true);
	RectTransform rect = panel.getRectTransform();
	rect.setPivot(new Pair<>(0.0, 1.0));
	rect.setPosition(inGamePanelPosition);
	rect.setSize(inGamePanelDimension);
	Text txtStatus = panel.addText("Status: ", true);
	txtStatus.getRectTransform().setPivot(new Pair<>(0.0, 1.0));
	txtStatus.getRectTransform().setAnchor(new Pair<>(0.0, 1.0));
	txtStatus.getRectTransform().setPosition(new Pair<Double, Double>(5.0, -5.0));
	txtStatus.setColor(new Triplet<Double, Double, Double>(1.0, 1.0, 1.0));
	txtStatus.setSize(12);
	btnAbort = createButton(panel, Bundle.getString("pnl_tel_btn_cancel"), false,
					inGamePanelDimension.getValue0() + MARGIN, -MARGIN / 2);
	panel.setVisible(false);
	return panel;
}

private Panel createFunctionPanel(UI.Canvas canvas) throws RPCException, StreamException {
	Panel panel = canvas.addPanel(true);
	RectTransform rect = panel.getRectTransform();
	rect.setPivot(new Pair<>(0.0, 1.0));
	rect.setPosition(inGamePanelPosition);
	rect.setSize(inGamePanelDimension);
	Text txtStatus = panel.addText(Bundle.getString("pnl_func_title"), true);
	txtStatus.getRectTransform().setPivot(new Pair<>(0.0, 1.0));
	txtStatus.getRectTransform().setAnchor(new Pair<>(0.0, 1.0));
	txtStatus.getRectTransform().setPosition(new Pair<Double, Double>(5.0, -5.0));
	txtStatus.setColor(new Triplet<Double, Double, Double>(1.0, 1.0, 1.0));
	txtStatus.setSize(12);
	btnLiftoff = createButton(panel, Bundle.getString("btn_func_liftoff"), true, MARGIN,
					-MARGIN / 2);
	btnLiftoffClicked = connection.addStream(btnLiftoff, "getClicked");
	btnLanding = createButton(panel, Bundle.getString("btn_func_landing"), true,
					MARGIN / 2 + btnLiftoff.getRectTransform().getUpperRight().getValue0(), -MARGIN / 2);
	btnLandingClicked = connection.addStream(btnLanding, "getClicked");
	btnManeuver = createButton(panel, Bundle.getString("btn_func_maneuvers"), true,
					MARGIN / 2 + btnLanding.getRectTransform().getUpperRight().getValue0(), -MARGIN / 2);
	btnManeuverClicked = connection.addStream(btnManeuver, "getClicked");
	btnAutoRover = createButton(panel, Bundle.getString("btn_func_rover"), true,
					MARGIN / 2 + btnManeuver.getRectTransform().getUpperRight().getValue0(), -MARGIN / 2);
	btnAutoRoverClicked = connection.addStream(btnAutoRover, "getClicked");
	return panel;
}

private Panel createLiftoffPanel(UI.Canvas canvas) throws RPCException, StreamException {
	Panel panel = canvas.addPanel(true);
	RectTransform rect = panel.getRectTransform();
	rect.setPivot(new Pair<>(0.0, 1.0));
	rect.setPosition(inGamePanelPosition);
	rect.setSize(inGamePanelDimension);
	Text txtApoapsis = panel.addText(Bundle.getString("pnl_lift_lbl_final_apoapsis"), true);
	txtApoapsis.getRectTransform().setPivot(new Pair<>(0.0, 1.0));
	txtApoapsis.getRectTransform().setAnchor(new Pair<>(0.0, 1.0));
	txtApoapsis.getRectTransform()
						 .setPosition(new Pair<Double, Double>(MARGIN, -inGamePanelDimension.getValue1() / 2));
	txtApoapsis.setColor(new Triplet<Double, Double, Double>(1.0, 1.0, 1.0));
	txtApoapsis.setSize(12);
	InputField infApoapsis = panel.addInputField(true);
	btnLiftoffExec = createButton(panel, Bundle.getString("btn_func_liftoff"), true,
					inGamePanelDimension.getValue0() - BUTTON_WIDTH - MARGIN, -MARGIN / 2);
	btnLiftoffExecClicked = connection.addStream(btnLiftoffExec, "getClicked");
	panel.setVisible(false);
	return panel;
}

private Button createButton(Panel panel, String btnText, boolean isVisible, double xPos,
														double yPos) throws RPCException {
	Button btn = panel.addButton(btnText, isVisible);
	btn.getRectTransform().setPivot(new Pair<>(0.0, 1.0));
	btn.getText().setSize(10);
	btn.getRectTransform().setAnchor(new Pair<>(0.0, 1.0));
	btn.getRectTransform().setSize(new Pair<>(BUTTON_WIDTH, BUTTON_HEIGHT));
	btn.getRectTransform().setPosition(new Pair<>(xPos, yPos));
	return btn;
}

public void checkButtons() throws RPCException, StreamException {
	if (btnLiftoffClicked.get()) {
		pnlFunctions.setVisible(false);
		pnlLiftoff.setVisible(true);
		btnLiftoff.setClicked(false);
	}
	if (btnLiftoffExecClicked.get()) {
		Map<String, String> commands = new HashMap<>();
		commands.put(Modulos.MODULO.get(), Modulos.MODULO_DECOLAGEM.get());
		commands.put(Modulos.APOASTRO.get(), String.valueOf(80000.0));
		commands.put(Modulos.DIRECAO.get(), String.valueOf(90.0));
		commands.put(Modulos.ROLAGEM.get(), String.valueOf(90.0));
		commands.put(Modulos.INCLINACAO.get(), "Circular");
		commands.put(Modulos.USAR_ESTAGIOS.get(), String.valueOf(true));
		commands.put(Modulos.ABRIR_PAINEIS.get(), String.valueOf(true));
		MechPeste.startModule(commands);
		pnlLiftoff.setVisible(false);
		pnlStatus.setVisible(true);
		btnLiftoffExec.setClicked(false);
	}
}
}

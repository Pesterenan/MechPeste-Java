package com.pesterenan.controllers;

import com.pesterenan.model.ActiveVessel;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Navigation;
import com.pesterenan.utils.Utilities;
import com.pesterenan.views.StatusJPanel;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.VesselSituation;

import java.util.Map;


public class LandingController extends ActiveVessel implements Runnable {

	public static final double MAX_VELOCITY = 5;
	private static final double velP = 0.025;
	private static final double velI = 0.001;
	private static final double velD = 0.01;
	private static boolean landFromHovering = false;
	private final ControlePID altitudeCtrl = new ControlePID();
	private final ControlePID velocityCtrl = new ControlePID();
	private final Navigation navigation = new Navigation();
	private final int HUNDRED_PERCENT = 100;
	private double hoverAltitude;
	private boolean hoveringMode = false;
	private MODE currentMode;
	private double altitudeErrorPercentage;

	public LandingController(Map<String, String> commands) {
		super(getConexao());
		this.commands = commands;
		initializeParameters();
	}

	public static void land() {
		landFromHovering = true;
	}

	@Override
	public void run() {
		if (commands.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO_SOBREVOAR.get())) {
			this.hoverAltitude = Double.parseDouble(commands.get(Modulos.ALTITUDE_SOBREVOO.get()));
			hoveringMode = true;
			hoverArea();
		}
		if (commands.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO.get())) {
			autoLanding();
		}
	}

	private void initializeParameters() {
		try {
			altitudeCtrl.adjustOutput(0, 1);
			velocityCtrl.adjustOutput(0, 1);
			currentBody = naveAtual.getOrbit().getBody();
			pontoRefSuperficie = naveAtual.getSurfaceReferenceFrame();
			pontoRefOrbital = currentBody.getReferenceFrame();
			parametrosDeVoo = naveAtual.flight(pontoRefOrbital);
			altitudeSup = getConexao().addStream(parametrosDeVoo, "getSurfaceAltitude");
			velVertical = getConexao().addStream(parametrosDeVoo, "getVerticalSpeed");
			velHorizontal = getConexao().addStream(parametrosDeVoo, "getHorizontalSpeed");
			apoastro = getConexao().addStream(naveAtual.getOrbit(), "getApoapsisAltitude");
			periastro = getConexao().addStream(naveAtual.getOrbit(), "getPeriapsisAltitude");
			gravityAcel = currentBody.getSurfaceGravity();
			hoveringMode = false;
		} catch (StreamException | RPCException ignored) {
		}
	}

	private void hoverArea() {
		try {
			ap.engage();
			while (hoveringMode) {
				try {
					altitudeErrorPercentage = altitudeSup.get() / hoverAltitude * HUNDRED_PERCENT;
					// Select which mode depending on altitude error:
					if (altitudeErrorPercentage > HUNDRED_PERCENT) {
						currentMode = MODE.GOING_DOWN;
					} else if (altitudeErrorPercentage < 90) {
						currentMode = MODE.GOING_UP;
					} else {
						currentMode = MODE.HOVERING;
					}
					// Land if asked:
					if (landFromHovering) {
						StatusJPanel.setStatus(Bundle.getString("status_starting_landing"));
						naveAtual.getControl().setGear(true);
						currentMode = MODE.LANDING;
					}
					changeControlMode();
					Thread.sleep(25);
				} catch (RPCException | StreamException e) {
					hoveringMode = false;
					disengageAfterException(Bundle.getString("status_function_abort"));
					break;
				}
			}
		} catch (InterruptedException | RPCException e) {
			disengageAfterException(Bundle.getString("status_liftoff_abort"));
		}
	}

	private void changeControlMode() throws RPCException, StreamException, InterruptedException {
		adjustPIDbyTWR();
		double velPID = 0, altPID = 0;

		// Change vessel behavior depending on which mode is active
		switch (currentMode) {
			case DEORBITING:
				deOrbitShip();
				currentMode = MODE.WAITING;
				break;
			case WAITING:
				if (velVertical.get() > 0) {
					StatusJPanel.setStatus(Bundle.getString("status_waiting_for_landing"));
					throttle(0.0f);
				} else {
					currentMode = MODE.APPROACHING;
				}
				break;
			case APPROACHING:
				altitudeCtrl.adjustOutput(0, 1);
				velocityCtrl.adjustOutput(0, 1);
				double currentVelocity = calculateCurrentVelocityMagnitude();
				double zeroVelocity = calculateZeroVelocityMagnitude();
				double landingDistanceThreshold = Math.max(100, getMaxAcel() * 3);
				double threshold = Utilities.clamp(
						((currentVelocity + zeroVelocity) - landingDistanceThreshold) / landingDistanceThreshold, 0,
						1);
				altPID = altitudeCtrl.calcPID(currentVelocity / zeroVelocity * HUNDRED_PERCENT, HUNDRED_PERCENT);
				velPID = velocityCtrl.calcPID(velVertical.get(), -Utilities.clamp(altitudeSup.get() * 0.1, 1, 10));
				throttle(Utilities.linearInterpolation(velPID, altPID, threshold));
				navigation.aimForLanding();

				if (threshold < 0.25) {
					hoverAltitude = landingDistanceThreshold;
					naveAtual.getControl().setGear(true);
					currentMode = MODE.LANDING;
				}
				break;
			case GOING_UP:
				altitudeCtrl.adjustOutput(-0.5, 0.5);
				velocityCtrl.adjustOutput(-0.5, 0.5);
				altPID = altitudeCtrl.calcPID(altitudeErrorPercentage, HUNDRED_PERCENT);
				velPID = velocityCtrl.calcPID(velVertical.get(), MAX_VELOCITY);
				throttle(altPID + velPID);
				navigation.aimAtRadialOut();
				break;
			case GOING_DOWN:
				controlThrottleByMatchingVerticalVelocity(-MAX_VELOCITY);
				navigation.aimAtRadialOut();
				break;
			case LANDING:
				controlThrottleByMatchingVerticalVelocity(
						velHorizontal.get() > 2 ? 0 : -Utilities.clamp(altitudeSup.get() * 0.1, 1, 10));
				navigation.aimForLanding();
				hasTheVesselLanded();
				break;
			case HOVERING:
				altitudeCtrl.adjustOutput(-0.5, 0.5);
				velocityCtrl.adjustOutput(-0.5, 0.5);
				altPID = altitudeCtrl.calcPID(altitudeErrorPercentage, HUNDRED_PERCENT);
				velPID = velocityCtrl.calcPID(velVertical.get(), 0);
				throttle(altPID + velPID);
				navigation.aimAtRadialOut();
				break;
		}
	}

	private void controlThrottleByMatchingVerticalVelocity(double velocityToMatch) throws RPCException,
			StreamException {
		velocityCtrl.adjustOutput(0, 1);
		throttle(velocityCtrl.calcPID(velVertical.get(), velocityToMatch));
	}

	private void deOrbitShip() throws RPCException, StreamException {
		throttle(0.0f);
		if (naveAtual.getSituation().equals(VesselSituation.ORBITING) ||
				naveAtual.getSituation().equals(VesselSituation.SUB_ORBITAL)) {
			StatusJPanel.setStatus(Bundle.getString("status_going_suborbital"));
			ap.engage();
			while (ap.getError() > 5) {
				navigation.aimForLanding();
				StatusJPanel.setStatus(Bundle.getString("status_orienting_ship"));
			}
			while (periastro.get() > -apoastro.get()) {
				navigation.aimForLanding();
				throttle(altitudeCtrl.calcPID(-apoastro.get(), periastro.get()));
				StatusJPanel.setStatus(Bundle.getString("status_lowering_periapsis"));
			}
			throttle(0.0f);
		}
	}

	private void autoLanding() {
		try {
			StatusJPanel.setStatus(Bundle.getString("status_starting_landing_at") + " " + currentBody.getName());
			currentMode = MODE.DEORBITING;
			changeControlMode();
			ap.engage();
			StatusJPanel.setStatus(Bundle.getString("status_starting_landing"));
			while (!hasTheVesselLanded()) {
				naveAtual.getControl().setBrakes(true);
				changeControlMode();
				Thread.sleep(25);
			}
		} catch (RPCException | StreamException | InterruptedException e) {
			disengageAfterException(Bundle.getString("status_couldnt_land"));
		}
	}

	/**
	 * Adjust altitude and velocity PID gains according to current ship TWR:
	 */
	private void adjustPIDbyTWR() throws RPCException, StreamException {
		velocityCtrl.adjustPID(getTWR() * velP, velI, velD);
		altitudeCtrl.adjustPID(getTWR() * velP, velI, velD);
	}

	private double calculateCurrentVelocityMagnitude() throws RPCException, StreamException {
		double timeToGround = altitudeSup.get() / velVertical.get();
		double horizontalDistance = velHorizontal.get() * timeToGround;
		return calculateEllipticTrajectory(horizontalDistance, altitudeSup.get());
	}

	private boolean hasTheVesselLanded() throws RPCException {
		if (naveAtual.getSituation().equals(VesselSituation.LANDED) ||
				naveAtual.getSituation().equals(VesselSituation.SPLASHED)) {
			StatusJPanel.setStatus(Bundle.getString("status_landed"));
			hoveringMode = false;
			landFromHovering = false;
			throttle(0.0f);
			naveAtual.getControl().setSAS(true);
			naveAtual.getControl().setRCS(true);
			naveAtual.getControl().setBrakes(false);
			ap.disengage();
			return true;
		}
		return false;
	}

	private double calculateZeroVelocityMagnitude() throws RPCException, StreamException {
		double zeroVelocityDistance = calculateEllipticTrajectory(velHorizontal.get(), velVertical.get());
		double zeroVelocityBurnTime = zeroVelocityDistance / getMaxAcel();
		return zeroVelocityDistance * zeroVelocityBurnTime;
	}

	private double calculateEllipticTrajectory(double a, double b) {
		double semiMajor = Math.max(a * 2, b * 2);
		double semiMinor = Math.min(a * 2, b * 2);
		double trajectoryLength = Math.PI * Math.sqrt((semiMajor * semiMajor + semiMinor * semiMinor)) / 4;
		return trajectoryLength;
	}

	private static enum MODE {
		DEORBITING, APPROACHING, GOING_UP, HOVERING, GOING_DOWN, LANDING, WAITING;
	}
}

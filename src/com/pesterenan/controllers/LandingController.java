package com.pesterenan.controllers;

import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Navigation;
import com.pesterenan.utils.Utilities;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.VesselSituation;

import java.util.Map;

public class LandingController extends Controller {

	public static final double MAX_VELOCITY = 5;
	private static final double velP = 0.025;
	private static final double velI = 0.001;
	private static final double velD = 0.01;
	private final ControlePID altitudeCtrl = new ControlePID();
	private final ControlePID velocityCtrl = new ControlePID();
	private Navigation navigation;
	private final int HUNDRED_PERCENT = 100;
	private double hoverAltitude;
	private boolean hoveringMode = false;
	private MODE currentMode;
	private double altitudeErrorPercentage;
	private float maxTWR;

	public LandingController(Map<String, String> commands) {
		super();
		this.commands = commands;
		this.navigation = new Navigation(getNaveAtual());
		this.initializeParameters();
	}

	private void initializeParameters() {
		altitudeCtrl.adjustOutput(0, 1);
		velocityCtrl.adjustOutput(0, 1);
	}

	@Override
	public void run() {
		if (commands.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO_SOBREVOAR.get())) {
			hoverAltitude = Double.parseDouble(commands.get(Modulos.ALTITUDE_SOBREVOO.get()));
			hoveringMode = true;
			hoverArea();
		}
		if (commands.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO.get())) {
			maxTWR = Float.parseFloat(commands.get(Modulos.MAX_TWR.get()));
			autoLanding();
		}
	}

	private void hoverArea() {
		try {
			ap.engage();
			tuneAutoPilot();
			while (hoveringMode) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				try {
					altitudeErrorPercentage = altitudeSup.get() / hoverAltitude * HUNDRED_PERCENT;
					// Select which mode depending on altitude error:
					if (altitudeErrorPercentage > HUNDRED_PERCENT) {
						currentMode = MODE.GOING_DOWN;
					} else if (altitudeErrorPercentage < HUNDRED_PERCENT * 0.9) {
						currentMode = MODE.GOING_UP;
					} else {
						currentMode = MODE.HOVERING;
					}
					changeControlMode();
				} catch (RPCException | StreamException ignored) {
				}
				Thread.sleep(50);
			}
		} catch (InterruptedException | RPCException ignored) {
//			disengageAfterException(Bundle.getString("status_liftoff_abort"));
		}
	}

	private void changeControlMode() throws RPCException, StreamException, InterruptedException {
		adjustPIDbyTWR();
		double velPID, altPID;
		// Change vessel behavior depending on which mode is active
		switch (currentMode) {
			case DEORBITING:
				deOrbitShip();
				currentMode = MODE.WAITING;
				break;
			case WAITING:
				if (velVertical.get() > 0) {
					setCurrentStatus(Bundle.getString("status_waiting_for_landing"));
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
				double landingDistanceThreshold = Math.max(100, getMaxAcel(maxTWR) * 3);
				double threshold = Utilities.clamp(
						((currentVelocity + zeroVelocity) - landingDistanceThreshold) / landingDistanceThreshold, 0,
						1);
				altPID = altitudeCtrl.calcPID(currentVelocity / zeroVelocity * HUNDRED_PERCENT, HUNDRED_PERCENT);
				velPID = velocityCtrl.calcPID(velVertical.get(), -Utilities.clamp(altitudeSup.get() * 0.1, 1, 10));
				throttle(Utilities.linearInterpolation(velPID, altPID, threshold));
				navigation.aimForLanding();
				if (threshold < 0.25) {
					hoverAltitude = landingDistanceThreshold;
					getNaveAtual().getControl().setGear(true);
					currentMode = MODE.LANDING;
				}
				setCurrentStatus("Se aproximando do momento do pouso...");
				break;
			case GOING_UP:
				altitudeCtrl.adjustOutput(-0.5, 0.5);
				velocityCtrl.adjustOutput(-0.5, 0.5);
				altPID = altitudeCtrl.calcPID(altitudeErrorPercentage, HUNDRED_PERCENT);
				velPID = velocityCtrl.calcPID(velVertical.get(), MAX_VELOCITY);
				throttle(altPID + velPID);
				navigation.aimAtRadialOut();
				setCurrentStatus("Subindo altitude...");
				break;
			case GOING_DOWN:
				controlThrottleByMatchingVerticalVelocity(-MAX_VELOCITY);
				navigation.aimAtRadialOut();
				setCurrentStatus("Baixando altitude...");
				break;
			case LANDING:
				controlThrottleByMatchingVerticalVelocity(
						velHorizontal.get() > 2 ? 0 : -Utilities.clamp(altitudeSup.get() * 0.1, 1, 10));
				navigation.aimForLanding();
				setCurrentStatus("Pousando...");
				hasTheVesselLanded();
				break;
			case HOVERING:
				altitudeCtrl.adjustOutput(-0.5, 0.5);
				velocityCtrl.adjustOutput(-0.5, 0.5);
				altPID = altitudeCtrl.calcPID(altitudeErrorPercentage, HUNDRED_PERCENT);
				velPID = velocityCtrl.calcPID(velVertical.get(), 0);
				throttle(altPID + velPID);
				navigation.aimAtRadialOut();
				setCurrentStatus("Sobrevoando area...");
				break;
		}
	}

	private void controlThrottleByMatchingVerticalVelocity(double velocityToMatch) throws RPCException,
			StreamException {
		velocityCtrl.adjustOutput(0, 1);
		throttle(velocityCtrl.calcPID(velVertical.get(), velocityToMatch));
	}

	private void deOrbitShip() throws RPCException, StreamException, InterruptedException {
		throttle(0.0f);
		if (getNaveAtual().getSituation().equals(VesselSituation.ORBITING) ||
				getNaveAtual().getSituation().equals(VesselSituation.SUB_ORBITAL)) {
			setCurrentStatus(Bundle.getString("status_going_suborbital"));
			ap.engage();
			getNaveAtual().getControl().setRCS(true);
			while (ap.getError() > 5) {
				navigation.aimForLanding();
				setCurrentStatus(Bundle.getString("status_orienting_ship"));
				ap.wait_();
				Thread.sleep(100);
			}
			while (periastro.get() > -apoastro.get()) {
				navigation.aimForLanding();
				throttle(altitudeCtrl.calcPID(-currentBody.getEquatorialRadius()/2, periastro.get()));
				setCurrentStatus(Bundle.getString("status_lowering_periapsis"));
				Thread.sleep(100);
			}
			getNaveAtual().getControl().setRCS(false);
			throttle(0.0f);
		}
	}

	private void autoLanding() {
		try {
			setCurrentStatus(Bundle.getString("status_starting_landing_at") + " " + currentBody.getName());
			currentMode = MODE.DEORBITING;
			ap.engage();
			changeControlMode();
			tuneAutoPilot();
			setCurrentStatus(Bundle.getString("status_starting_landing"));
			while (!hasTheVesselLanded()) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				getNaveAtual().getControl().setBrakes(true);
				changeControlMode();
				Thread.sleep(100);
			}
		} catch (RPCException | StreamException | InterruptedException e) {
			setCurrentStatus(Bundle.getString("status_ready"));
		}
	}

	/**
	 * Adjust altitude and velocity PID gains according to current ship TWR:
	 */
	private void adjustPIDbyTWR() throws RPCException, StreamException {
		double currentTWR = Math.min(getTWR(), maxTWR);
		velocityCtrl.adjustPID(currentTWR * velP, velI, velD);
		altitudeCtrl.adjustPID(currentTWR * velP, velI, velD);
	}

	private double calculateCurrentVelocityMagnitude() throws RPCException, StreamException {
		double timeToGround = altitudeSup.get() / velVertical.get();
		double horizontalDistance = velHorizontal.get() * timeToGround;
		return calculateEllipticTrajectory(horizontalDistance, altitudeSup.get());
	}

	private boolean hasTheVesselLanded() throws RPCException {
		if (getNaveAtual().getSituation().equals(VesselSituation.LANDED) ||
				getNaveAtual().getSituation().equals(VesselSituation.SPLASHED)) {
			setCurrentStatus(Bundle.getString("status_landed"));
			hoveringMode = false;
			throttle(0.0f);
			getNaveAtual().getControl().setSAS(true);
			getNaveAtual().getControl().setRCS(true);
			getNaveAtual().getControl().setBrakes(false);
			ap.disengage();
			return true;
		}
		return false;
	}

	private double calculateZeroVelocityMagnitude() throws RPCException, StreamException {
		double zeroVelocityDistance = calculateEllipticTrajectory(velHorizontal.get(), velVertical.get());
		double zeroVelocityBurnTime = zeroVelocityDistance / getMaxAcel(maxTWR);
		return zeroVelocityDistance * zeroVelocityBurnTime;
	}

	private double calculateEllipticTrajectory(double a, double b) {
		double semiMajor = Math.max(a * 2, b * 2);
		double semiMinor = Math.min(a * 2, b * 2);
		return Math.PI * Math.sqrt((semiMajor * semiMajor + semiMinor * semiMinor)) / 4;
	}

	private enum MODE {
		DEORBITING, APPROACHING, GOING_UP, HOVERING, GOING_DOWN, LANDING, WAITING
	}
}

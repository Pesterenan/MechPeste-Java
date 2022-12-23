package com.pesterenan.controllers;

import com.pesterenan.model.ActiveVessel;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Modulos;
import com.pesterenan.utils.Navigation;
import com.pesterenan.utils.Utilities;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.VesselSituation;

public class LandingController extends Controller implements Runnable {

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
	private long timer = 0;

	public LandingController(ActiveVessel activeVessel) {
		super(activeVessel);
		this.navigation = new Navigation(activeVessel);
		initializeParameters();
	}

	private void initializeParameters() {
		altitudeCtrl.adjustOutput(0, 1);
		velocityCtrl.adjustOutput(0, 1);
		hoveringMode = false;
	}

	@Override
	public void run() {
		if (activeVessel.commands.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO_SOBREVOAR.get())) {
			this.hoverAltitude = Double.parseDouble(activeVessel.commands.get(Modulos.ALTITUDE_SOBREVOO.get()));
			hoveringMode = true;
			System.out.println(activeVessel.commands.get(Modulos.POUSAR.get()));
			hoverArea();
		}
		if (activeVessel.commands.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO.get())) {
			autoLanding();
		}
	}

	private void hoverArea() {
		try {
			activeVessel.ap.engage();
			while (hoveringMode) {
				long currentTime = System.currentTimeMillis();
				if (currentTime > timer + 25) {
					try {
						// Land if asked:
						boolean landFromHovering = Boolean.getBoolean(activeVessel.commands.get(Modulos.POUSAR.get()));
						if (landFromHovering) {
							activeVessel.setCurrentStatus(Bundle.getString("status_starting_landing"));
							activeVessel.getNaveAtual().getControl().setGear(true);
							currentMode = MODE.LANDING;
						}
						altitudeErrorPercentage = activeVessel.altitudeSup.get() / hoverAltitude * HUNDRED_PERCENT;
						// Select which mode depending on altitude error:
						if (altitudeErrorPercentage > HUNDRED_PERCENT) {
							currentMode = MODE.GOING_DOWN;
						} else if (altitudeErrorPercentage < 90) {
							currentMode = MODE.GOING_UP;
						} else {
							currentMode = MODE.HOVERING;
						}
						changeControlMode();
					} catch (RPCException | StreamException e) {
						hoveringMode = false;
						activeVessel.disengageAfterException(Bundle.getString("status_function_abort"));
						break;
					}
					timer = currentTime;
				}
			}
		} catch (InterruptedException | RPCException e) {
			activeVessel.disengageAfterException(Bundle.getString("status_liftoff_abort"));
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
				if (activeVessel.velVertical.get() > 0) {
					activeVessel.setCurrentStatus(Bundle.getString("status_waiting_for_landing"));
					activeVessel.throttle(0.0f);
				} else {
					currentMode = MODE.APPROACHING;
				}
				break;
			case APPROACHING:
				altitudeCtrl.adjustOutput(0, 1);
				velocityCtrl.adjustOutput(0, 1);
				double currentVelocity = calculateCurrentVelocityMagnitude();
				double zeroVelocity = calculateZeroVelocityMagnitude();
				double landingDistanceThreshold = Math.max(100, activeVessel.getMaxAcel() * 3);
				double threshold = Utilities.clamp(
						((currentVelocity + zeroVelocity) - landingDistanceThreshold) / landingDistanceThreshold, 0,
						1);
				altPID = altitudeCtrl.calcPID(currentVelocity / zeroVelocity * HUNDRED_PERCENT, HUNDRED_PERCENT);
				velPID = velocityCtrl.calcPID(activeVessel.velVertical.get(),
				                              -Utilities.clamp(activeVessel.altitudeSup.get() * 0.1, 1, 10)
				                             );
				activeVessel.throttle(Utilities.linearInterpolation(velPID, altPID, threshold));
				navigation.aimForLanding();
				if (threshold < 0.25) {
					hoverAltitude = landingDistanceThreshold;
					activeVessel.getNaveAtual().getControl().setGear(true);
					currentMode = MODE.LANDING;
				}
				activeVessel.setCurrentStatus("Se aproximando do momento do pouso...");
				break;
			case GOING_UP:
				altitudeCtrl.adjustOutput(-0.5, 0.5);
				velocityCtrl.adjustOutput(-0.5, 0.5);
				altPID = altitudeCtrl.calcPID(altitudeErrorPercentage, HUNDRED_PERCENT);
				velPID = velocityCtrl.calcPID(activeVessel.velVertical.get(), MAX_VELOCITY);
				activeVessel.throttle(altPID + velPID);
				navigation.aimAtRadialOut();
				activeVessel.setCurrentStatus("Subindo altitude...");
				break;
			case GOING_DOWN:
				controlThrottleByMatchingVerticalVelocity(-MAX_VELOCITY);
				navigation.aimAtRadialOut();
				activeVessel.setCurrentStatus("Baixando altitude...");
				break;
			case LANDING:
				controlThrottleByMatchingVerticalVelocity(activeVessel.velHorizontal.get() > 2
				                                          ? 0
				                                          : -Utilities.clamp(activeVessel.altitudeSup.get() * 0.1, 1,
				                                                             10
				                                                            ));
				navigation.aimForLanding();
				activeVessel.setCurrentStatus("Pousando...");
				hasTheVesselLanded();
				break;
			case HOVERING:
				altitudeCtrl.adjustOutput(-0.5, 0.5);
				velocityCtrl.adjustOutput(-0.5, 0.5);
				altPID = altitudeCtrl.calcPID(altitudeErrorPercentage, HUNDRED_PERCENT);
				velPID = velocityCtrl.calcPID(activeVessel.velVertical.get(), 0);
				activeVessel.throttle(altPID + velPID);
				navigation.aimAtRadialOut();
				activeVessel.setCurrentStatus("Sobrevoando area...");
				break;
		}
	}

	private void controlThrottleByMatchingVerticalVelocity(double velocityToMatch) throws RPCException,
			StreamException {
		velocityCtrl.adjustOutput(0, 1);
		activeVessel.throttle(velocityCtrl.calcPID(activeVessel.velVertical.get(), velocityToMatch));
	}

	private void deOrbitShip() throws RPCException, StreamException {
		activeVessel.throttle(0.0f);
		if (activeVessel.getNaveAtual().getSituation().equals(VesselSituation.ORBITING) ||
				activeVessel.getNaveAtual().getSituation().equals(VesselSituation.SUB_ORBITAL)) {
			activeVessel.setCurrentStatus(Bundle.getString("status_going_suborbital"));
			activeVessel.ap.engage();
			while (activeVessel.ap.getError() > 5) {
				navigation.aimForLanding();
				activeVessel.setCurrentStatus(Bundle.getString("status_orienting_ship"));
			}
			while (activeVessel.periastro.get() > -activeVessel.apoastro.get()) {
				navigation.aimForLanding();
				activeVessel.throttle(altitudeCtrl.calcPID(-activeVessel.apoastro.get(),
				                                           activeVessel.periastro.get()));
				activeVessel.setCurrentStatus(Bundle.getString("status_lowering_periapsis"));
			}
			activeVessel.throttle(0.0f);
		}
	}

	private void autoLanding() {
		try {
			activeVessel.setCurrentStatus(
					Bundle.getString("status_starting_landing_at") + " " + activeVessel.currentBody.getName());
			currentMode = MODE.DEORBITING;
			changeControlMode();
			activeVessel.ap.engage();
			activeVessel.setCurrentStatus(Bundle.getString("status_starting_landing"));
			while (!hasTheVesselLanded()) {
				long currentTime = System.currentTimeMillis();
				if (currentTime > timer + 100) {
					activeVessel.getNaveAtual().getControl().setBrakes(true);
					changeControlMode();
					timer = currentTime;
				}
			}
		} catch (RPCException | StreamException | InterruptedException e) {
			activeVessel.disengageAfterException(Bundle.getString("status_couldnt_land"));
		}
	}

	/**
	 * Adjust altitude and velocity PID gains according to current ship TWR:
	 */
	private void adjustPIDbyTWR() throws RPCException, StreamException {
		velocityCtrl.adjustPID(activeVessel.getTWR() * velP, velI, velD);
		altitudeCtrl.adjustPID(activeVessel.getTWR() * velP, velI, velD);
	}

	private double calculateCurrentVelocityMagnitude() throws RPCException, StreamException {
		double timeToGround = activeVessel.altitudeSup.get() / activeVessel.velVertical.get();
		double horizontalDistance = activeVessel.velHorizontal.get() * timeToGround;
		return calculateEllipticTrajectory(horizontalDistance, activeVessel.altitudeSup.get());
	}

	private boolean hasTheVesselLanded() throws RPCException {
		if (activeVessel.getNaveAtual().getSituation().equals(VesselSituation.LANDED) ||
				activeVessel.getNaveAtual().getSituation().equals(VesselSituation.SPLASHED)) {
			activeVessel.setCurrentStatus(Bundle.getString("status_landed"));
			hoveringMode = false;
			activeVessel.throttle(0.0f);
			activeVessel.getNaveAtual().getControl().setSAS(true);
			activeVessel.getNaveAtual().getControl().setRCS(true);
			activeVessel.getNaveAtual().getControl().setBrakes(false);
			activeVessel.ap.disengage();
			return true;
		}
		return false;
	}

	private double calculateZeroVelocityMagnitude() throws RPCException, StreamException {
		double zeroVelocityDistance =
				calculateEllipticTrajectory(activeVessel.velHorizontal.get(), activeVessel.velVertical.get());
		double zeroVelocityBurnTime = zeroVelocityDistance / activeVessel.getMaxAcel();
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

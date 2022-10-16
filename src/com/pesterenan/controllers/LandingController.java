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

	private static final double velP = 0.025;
	private static final double velI = 0.001;
	private static final double velD = 0.01;
	private static boolean landFromHovering = false;
	private final ControlePID altitudeCtrl = new ControlePID();
	private final ControlePID velocityCtrl = new ControlePID();
	private final Navigation navigation = new Navigation();
	private double hoverAltitude = 100;
	private boolean hoveringMode = false;

	public LandingController(Map<String, String> commands) {
		super(getConexao());
		this.commands = commands;
		initializeParameters();
	}

	public static void land() {
		landFromHovering = true;
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
			periastro = getConexao().addStream(naveAtual.getOrbit(), "getPeriapsisAltitude");
			gravityAcel = currentBody.getSurfaceGravity();
		} catch (StreamException | RPCException ignored) {
		}
	}

	@Override
	public void run() {
		if (commands.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO_SOBREVOAR.get())) {
			this.hoverAltitude = Double.parseDouble(commands.get(Modulos.ALTITUDE_SOBREVOO.get()));
			hoveringMode = true;
			altitudeCtrl.adjustOutput(-0.2, 1);
			hoverArea();
		}
		if (commands.get(Modulos.MODULO.get()).equals(Modulos.MODULO_POUSO.get())) {
			startAutoLanding();
		}
	}

	private void hoverArea() {
		try {
			ap.engage();
			while (hoveringMode) {
				try {
					if (velHorizontal.get() > 15) {
						navigation.targetLanding();
					} else {
						navigation.targetRadialOut();
					}
					velocityCtrl.adjustPID(velP, velI, velD);
					double altPID = altitudeCtrl.calcPID((altitudeSup.get() / hoverAltitude) * 10000, 10000);
					double velPID = velocityCtrl.calcPID(velVertical.get(), altPID * gravityAcel * 2);
					throttle(velPID);
					if (landFromHovering) {
						naveAtual.getControl().setGear(true);
						hoverAltitude = 2;
						hasTheVesselLanded();
					}
					Thread.sleep(25);
				} catch (RPCException | StreamException e) {
					disengageAfterException(Bundle.getString("status_function_abort"));
					break;
				}
			}
		} catch (InterruptedException | RPCException e) {
			disengageAfterException(Bundle.getString("status_liftoff_abort"));
		}
	}

	private void startAutoLanding() {
		try {
			StatusJPanel.setStatus(Bundle.getString("status_starting_landing_at") + " " + currentBody.getName());
			deOrbitShip();
			autoLanding();
		} catch (RPCException | StreamException | InterruptedException e) {
			disengageAfterException(Bundle.getString("status_couldnt_land"));
		}
	}

	private void deOrbitShip() throws RPCException, InterruptedException, StreamException {
		throttle(0.0f);
		if (naveAtual.getSituation().equals(VesselSituation.ORBITING) ||
				naveAtual.getSituation().equals(VesselSituation.SUB_ORBITAL)) {
			StatusJPanel.setStatus(Bundle.getString("status_going_suborbital"));
			Thread.sleep(1000);
			ap.engage();
			while (ap.getHeadingError() > 5) {
				navigation.targetLanding();
				Thread.sleep(100);
				StatusJPanel.setStatus(Bundle.getString("status_orienting_ship"));
			}
			while (periastro.get() > 0) {
				navigation.targetLanding();
				throttle(altitudeCtrl.calcPID(0, periastro.get()));
				Thread.sleep(100);
				StatusJPanel.setStatus(Bundle.getString("status_lowering_periapsis"));
			}
			throttle(0.0f);
		}
	}

	private void autoLanding() throws InterruptedException, RPCException, StreamException {
		ap.engage();
		while (!hasTheVesselLanded()) {
			naveAtual.getControl().setBrakes(true);
			if (velVertical.get() > 1) {
				StatusJPanel.setStatus(Bundle.getString("status_waiting_for_landing"));
				changeDirection();
				throttle(0.0f);
			} else {
				StatusJPanel.setStatus(Bundle.getString("status_starting_landing"));
				changeDirection();
				checkAltitude();
			}
			Thread.sleep(25);
		}
	}

	private void checkAltitude() throws RPCException, StreamException {
		velocityCtrl.adjustPID(getTWR() * velP, velI, velD);
		altitudeCtrl.adjustPID(getTWR() * velP, velI, velD);
		double currentVelocityMagnitude = calculateCurrentVelocityMagnitude();
		double zeroVelocityMagnitude = calculateZeroVelocityMagnitude();
		double landingDistanceThreshold = Math.max(300, getMaxAcel() * 3);
		if (altitudeSup.get() < landingDistanceThreshold) {
			naveAtual.getControl().setGear(true);
		}
		changeThrottle(currentVelocityMagnitude, zeroVelocityMagnitude, landingDistanceThreshold);
	}

	private void changeThrottle(double currentVelocityMagnitude, double zeroVelocityMagnitude,
	                            double landingDistanceThreshold) throws RPCException, StreamException {
		double acelPIDValue = altitudeCtrl.calcPID(currentVelocityMagnitude / zeroVelocityMagnitude * 100, 100);
		double velPIDValue = velocityCtrl.calcPID(velVertical.get(), -Utilities.clamp(altitudeSup.get() * 0.1, 3, 30));
		double threshold = Utilities.clamp(
				((currentVelocityMagnitude + zeroVelocityMagnitude) - landingDistanceThreshold) /
						landingDistanceThreshold, 0, 1);
		throttle(Utilities.linearInterpolation(velPIDValue, acelPIDValue, threshold));
	}

	private void changeDirection() throws RPCException, StreamException {
		if (Math.abs(velHorizontal.get()) > 0.5) {
			navigation.targetLanding();
		} else {
			navigation.targetRadialOut();
		}
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

	private double calculateCurrentVelocityMagnitude() throws RPCException, StreamException {
		double timeToGround = altitudeSup.get() / velVertical.get();
		double horizontalDistance = velHorizontal.get() * timeToGround;
		return calculateElipseTrajectory(horizontalDistance, altitudeSup.get());
	}

	private double calculateZeroVelocityMagnitude() throws RPCException, StreamException {
		double zeroVelocityDistance = calculateElipseTrajectory(velHorizontal.get(), velVertical.get());
		double zeroVelocityBurnTime = zeroVelocityDistance / getMaxAcel();
		return zeroVelocityDistance * zeroVelocityBurnTime;
	}

	private double calculateElipseTrajectory(double a, double b) {
		double semiMajor = Math.max(a * 2, b * 2);
		double semiMinor = Math.min(a * 2, b * 2);
		double totalCircumference = 2 * Math.PI * Math.sqrt((semiMajor * semiMajor + semiMinor * semiMinor) / 2);
		return totalCircumference / 4;
	}
}

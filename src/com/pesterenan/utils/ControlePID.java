package com.pesterenan.utils;

import krpc.client.RPCException;
import krpc.client.services.SpaceCenter;

public class ControlePID {
	private SpaceCenter spaceCenter = null;
	private double outputMin = -1;
	private double outputMax = 1;
	private double kp = 0.025;
	private double ki = 0.001;
	private double kd = 0.01;
	private double integralTerm = 0.0;
	private double previousError, lastTime = 0.0;
	private double timeSample = 0.025;
	private double proportionalTerm;
	private double derivativeTerm;

	public ControlePID() {
	}

	public ControlePID(SpaceCenter spaceCenter, double timeSample) {
		this.spaceCenter = spaceCenter;
		setTimeSample(timeSample);
	}

	public ControlePID(double kp, double ki, double kd, double outputMin, double outputMax) {
		this.kp = kp;
		this.ki = ki;
		this.kd = kd;
		this.outputMin = outputMin;
		this.outputMax = outputMax;
	}

	public double calculate(double currentValue, double setPoint) {
		double now = this.getCurrentTime();
		double changeInTime = now - lastTime;
		System.out.println(changeInTime);

		if (changeInTime >= timeSample) {
			double error = setPoint - currentValue;
			proportionalTerm = kp * error;

			integralTerm += ki * error;
			integralTerm = limitOutput(integralTerm);

			derivativeTerm = kd * (error - previousError);
			previousError = error;
			lastTime = now;
		}
		return limitOutput(proportionalTerm + integralTerm + derivativeTerm);
	}

	private double limitOutput(double value) {
		return Utilities.clamp(value, outputMin, outputMax);
	}

	public void setOutput(double min, double max) {
		if (min > max) {
			return;
		}
		outputMin = min;
		outputMax = max;
		integralTerm = limitOutput(integralTerm);
	}

	public void setPIDValues(double Kp, double Ki, double Kd) {
		if (Kp > 0) {
			this.kp = Kp;
		}
		if (Ki >= 0) {
			this.ki = Ki;
		}
		if (Kd >= 0) {
			this.kd = Kd;
		}
	}

	public void setTimeSample(double milliseconds) {
		timeSample = milliseconds > 0 ? milliseconds / 1000 : timeSample;
	}

	private double getCurrentTime() {
		try {
			return spaceCenter.getUT();
		} catch (RPCException | NullPointerException ignored) {
			System.err.println("Não foi possível buscar o tempo do jogo, retornando do sistema");
			return System.currentTimeMillis();
		}
	}

}

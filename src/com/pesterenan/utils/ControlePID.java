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
	private double previousError, previousMeasurement, lastTime = 0.0;
	private double timeSample = 0.025; // 25 millisegundos
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

	public void reset() {
		this.previousError = 0;
		this.previousMeasurement = 0;
		this.proportionalTerm = 0;
		this.integralTerm = 0;
		this.derivativeTerm = 0;
	}

	public double calculate(double measurement, double setPoint) {
		double now = this.getCurrentTime();
		double changeInTime = now - lastTime;

		if (changeInTime >= timeSample) {
			// Error signal
			double error = setPoint - measurement;
			// Proportional
			proportionalTerm = kp * error;

			// Integral
			// integralTerm += 0.5f * ki * timeSample * (error + previousError);
			// integralTerm += ki * (error + previousError);
			integralTerm = ki * (integralTerm + (error * timeSample));
			integralTerm = limitOutput(integralTerm);

			derivativeTerm = kd * ((error - previousError) / timeSample);
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

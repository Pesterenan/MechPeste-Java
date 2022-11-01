package com.pesterenan.utils;

public class ControlePID {
	private double limiteMin = -1;
	private double limiteMax = 1;
	private double kp = 0.025;
	private double ki = 0.001;
	private double kd = 0.01;
	private final double timeSample = 25;
	private double proportionalTerm, integralTerm, derivativeTerm = 0;
	private double lastValue, lastTime = 0;

	public double calcPID(double currentValue, double limitValue) {
		double now = System.currentTimeMillis();
		double changeInTime = now - this.lastTime;

		if (changeInTime >= this.timeSample) {
			double error = limitValue - currentValue;
			double changeInValues = (currentValue - this.lastValue);

			this.proportionalTerm = this.kp * error;
			this.integralTerm = limitOutput(this.integralTerm + ki * error);
			this.derivativeTerm = kd * -changeInValues;
			this.lastValue = currentValue;
			this.lastTime = now;
		}
		return limitOutput(proportionalTerm + ki * this.integralTerm + derivativeTerm);
	}

	private double limitOutput(double valor) {
		return Utilities.clamp(valor, this.limiteMin, this.limiteMax);
	}

	public void adjustOutput(double min, double max) {
		if (min > max) {
			return;
		}
		this.limiteMin = min;
		this.limiteMax = max;
		this.integralTerm = limitOutput(this.integralTerm);

	}

	public void adjustPID(double Kp, double Ki, double Kd) {
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

}
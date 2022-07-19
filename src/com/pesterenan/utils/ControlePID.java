package com.pesterenan.utils;
/*Controlador Proporcional Integral Derivativo
Autor: Renan Torres <pesterenan@gmail.com>
 Data: 22/08/2018 
Atualizado: 18/07/2022*/

public class ControlePID {
	private double limiteMin = -1;
	private double limiteMax = 1;

	private double kp = 0.025;
	private double ki = 0.05;
	private double kd = 0.01;
	private double timeSample = 25;

	private double valorSaida = 1;
	private double proportionalTerm, integralTerm, derivativeTerm = 0;
	private double lastValue, lastTime = 0;

	private double limitarValor(double valor) {
		return Utilities.clamp(valor, this.limiteMin, this.limiteMax);
	}

	public double computarPID(double currentValue, double limitValue) {
		double now = System.currentTimeMillis();
		double changeInTime = now - this.lastTime;

		if (changeInTime >= this.timeSample) {
			double error = limitValue - currentValue;
			double changeInValues = (currentValue - this.lastValue);

			this.proportionalTerm = this.kp * error;
			this.integralTerm = limitarValor(this.integralTerm + ki * error);
			this.derivativeTerm = kd * -changeInValues;
			this.valorSaida = limitarValor(proportionalTerm + ki * this.integralTerm + derivativeTerm);

			this.lastValue = currentValue;
			this.lastTime = now;
		}
		return valorSaida;
	}

	public void limitarSaida(double min, double max) {
		if (min > max)
			return;
		this.limiteMin = min;
		this.limiteMax = max;
		this.integralTerm = limitarValor(this.integralTerm);

	}

	public void ajustarPID(double Kp, double Ki, double Kd) {
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

	public void setAmostragem(double milissegundos) {
		if (milissegundos > 0) {
			this.timeSample = milissegundos;
		}
	}
}
package com.pesterenan.utils;
/*Controlador Proporcional Integral Derivativo
Autor: Renan Torres <pesterenan@gmail.com>
 Data: 22/08/2018 
Atualizado: 06/06/2022*/

public class PIDcontrol {
	private double minimumLimit = -1;
	private double maximumLimit = 1;

	private double kp = 0.025;
	private double ki = 0.05;
	private double kd = 0.01;
	private double samplig = 25;

	private double outputValue = 1;
	private double integralTerm = 0;
	private double lastInput, lastCalculation = 0;

	private double limitValue(double value) {
		return Utilities.clamp(value, this.minimumLimit, this.maximumLimit);
	}

	/**
	 * Computar o value de saida do controlador, usando os valores de entrada e
	 * limit
	 * 
	 * @returns Valor computado do controlador
	 */
	public double computePID(double inputValue, double valueLimit) {
		double now = System.currentTimeMillis();
		double timeVariation = now - this.lastCalculation;

		if (timeVariation >= this.samplig) {
			double error = valueLimit - inputValue;
			double inputDifference = (inputValue - this.lastInput);

			this.integralTerm = limitValue(this.integralTerm + ki * error);
			this.outputValue = limitValue(kp * error + ki * this.integralTerm - kd * inputDifference);

			this.lastInput = inputValue;
			this.lastCalculation = now;
		}
		return outputValue;
	}

	public void limitarSaida(double min, double max) {
		if (min > max)
			return;
		this.minimumLimit = min;
		this.maximumLimit = max;
		this.integralTerm = limitValue(this.integralTerm);

	}

	public void ajustPID(double Kp, double Ki, double Kd) {
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

	public void setSamplig(double milissegundos) {
		if (milissegundos > 0) {
			this.samplig = milissegundos;
		}
	}
}
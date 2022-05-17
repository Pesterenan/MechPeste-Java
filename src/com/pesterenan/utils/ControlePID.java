package com.pesterenan.utils;
/*Controlador Proporcional Integral Derivativo
Autor: Renan Torres <pesterenan@gmail.com>
 Data: 22/08/2018 
Atualizado: 26/02/2019*/

public class ControlePID {
// Controlador PID escrito em Java para uso com o mod MechPeste
	private double limiteMin = -1;
	private double limiteMax = 1;
// Vari�veis padr�o de ajuste do PID:
	private double kp = 0.025;
	private double ki = 0.05;
	private double kd = 0.01;
	private double amostragem = 25; // Tempo para amostragem

	private double valorEntrada = 0, valorSaida = 1, valorLimite = 100; // vari�veis de valores
	private double termoIntegral, ultimaEntrada; // vari�veis de c�lculo de erro
	private double ultimoCalculo = 0; // tempo do �ltimo c�lculo

	public ControlePID() {
	}

	public static double interpolacaoLinear(double v0, double v1, double t) {
		return (1 - t) * v0 + t * v1;
	}
	
	public double computarPID() {
// M�todo que computa o incremento do PID
		double agora = System.currentTimeMillis(); // Buscar tempo imediato
		double mudancaTempo = agora - this.ultimoCalculo; // Comparar com o �ltimo c�lculo

		if (mudancaTempo >= this.amostragem) {
// Vari�veis para o c�lculo do valor de sa�da:
			double erro = this.valorLimite - this.valorEntrada;
			termoIntegral += ki * erro;
			if (termoIntegral > limiteMax) {
				termoIntegral = limiteMax;
			} else if (termoIntegral < limiteMin) {
				termoIntegral = limiteMin;
			}
			double diferencaEntrada = (this.valorEntrada - this.ultimaEntrada);

// Computar o valor de sa�da:
			this.valorSaida = kp * erro + ki * termoIntegral - kd * diferencaEntrada;
// Limitar valor de sa�da:
			if (this.valorSaida > limiteMax) {
				this.valorSaida = limiteMax;
			} else if (this.valorSaida < limiteMin) {
				this.valorSaida = limiteMin;
			}

// Guardando os valores atuais para o pr�ximo c�lculo:
			this.ultimaEntrada = this.valorEntrada;
			this.ultimoCalculo = agora;
		}
// Retorna o valor de sa�da calculado:
		return this.valorSaida;
	}

	public void setEntradaPID(double valor) {
// Informar valor de entrada:
		this.valorEntrada = valor;
	}

	public void setLimitePID(double valor) {
// Limite para o valor de entrada alcan�ar:
		this.valorLimite = valor;
	}

	public void limitarSaida(double min, double max) {
		if (min > max)
			return;
		limiteMin = min;
		limiteMax = max;

		if (termoIntegral > limiteMax) {
			termoIntegral = limiteMax;
		} else if (termoIntegral < limiteMin) {
			termoIntegral = limiteMin;
		}

		if (this.valorSaida > limiteMax) {
			this.valorSaida = limiteMax;
		} else if (this.valorSaida < limiteMin) {
			this.valorSaida = limiteMin;
		}
	}

	public void ajustarPID(double Kp, double Ki, double Kd) {
		if (Kp > 0) {
			kp = Kp;
		}
		if (Ki >= 0) {
			ki = Ki;
		}
		if (Kd >= 0) {
			kd = Kd;
		}
	}

	public void setAmostragem(double milissegundos) {
		if (milissegundos > 0) {
			this.amostragem = milissegundos;
		}
	}
}
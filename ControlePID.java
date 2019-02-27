import java.io.IOException;

import krpc.client.RPCException;
import krpc.client.StreamException;

/*Controlador Proporcional Integral Derivativo
	Autor: Renan Torres <pesterenan@gmail.com>
 	Data: 22/08/2018 
	Atualizado: 26/02/2019*/

public class ControlePID {
	//Controlador PID escrito em Java para uso com o mod MechPeste
	private double saidaMin = -1; 
	private double saidaMax = 1;
	// Variáveis padrão de ajuste do PID:
	private double kp = 0.025;
	private double ki = 0.001;
	private double kd = 0.1;
	private double amostraTempo = 20; // Tempo para amostragem

	private double valorEntrada, valorSaida, valorLimite; //variáveis de valores
	private double termoIntegral, ultimaEntrada; // variáveis de cálculo de erro
	private double ultimoCalculo = 0; 	//tempo do último cálculo
	
	public double computarPID() throws RPCException, StreamException, IOException{
		// Método que computa o incremento do PID
		double agora = System.currentTimeMillis(); 	// Buscar tempo imediato
		double mudancaTempo = agora - this.ultimoCalculo; 	// Comparar com o último cálculo

		if (mudancaTempo >= this.amostraTempo) { 
			// Variáveis para o cálculo do valor de saída:
			double erro = this.valorLimite - this.valorEntrada;
			termoIntegral += ki * erro;
			if (termoIntegral > saidaMax) {termoIntegral = saidaMax;}
			else if (termoIntegral < saidaMin) {termoIntegral = saidaMin;}
			double diferencaEntrada = (this.valorEntrada - this.ultimaEntrada);
			
			// Computar o valor de saída:
			this.valorSaida = kp * erro + ki * termoIntegral - kd * diferencaEntrada;
			
			// Limitar valor de saída:
			if (this.valorSaida > saidaMax) {this.valorSaida = saidaMax;}
			else if (this.valorSaida < saidaMin) {this.valorSaida = saidaMin;}
			
			// Guardando os valores atuais para o próximo cálculo:
			this.ultimaEntrada = this.valorEntrada;
			this.ultimoCalculo = agora;
		}
		// Retorna o valor de saída calculado:
		return this.valorSaida;
	}

	public void setEntradaPID(double valor) {
		// Informar valor de entrada:
		this.valorEntrada = valor;
	}
	public void setLimitePID(double valor) {
		// Limite para o valor de entrada alcançar:
		this.valorLimite = valor;
	}
	public void limitarSaida(double Min, double Max) {
		if (Min > Max) return;
		saidaMin = Min;
		saidaMax = Max;

		if (termoIntegral >saidaMax) {termoIntegral = saidaMax;}
		else if (termoIntegral < saidaMin) {termoIntegral = saidaMin;}

		if (this.valorSaida > saidaMax) {this.valorSaida = saidaMax;}
		else if (this.valorSaida < saidaMin) {this.valorSaida = saidaMin;}
	}
	public void ajustarPID(double Kp, double Ki, double Kd) {
		if (Kp > 0) {kp = Kp;}
		if (Ki >= 0) {ki = Ki;}
		if (Kd >= 0) {kd = Kd;}
	}
	public void setAmostraTempo(double novaAmostraTempo) {
		// Tempo para fazer a amostragem dos valores em milissegundos:
		if (novaAmostraTempo > 0) {
			this.amostraTempo = novaAmostraTempo;
		}
	}
}
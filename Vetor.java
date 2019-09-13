package com.pesterenan;
import org.javatuples.Triplet;

/* Autor: Renan Torres <pesterenan@gmail.com>
 Data: 27/02/2019
 Classe Vetor*/

public class Vetor {

	public double x = 0;
	public double y = 0;
	public double z = 0;

	/**
	 * Cria um vetor informando valores X,Y,Z manualmente
	 * 
	 * @param X - Valor eixo X
	 * @param Y - Valor eixo Y
	 * @param Z - Valor eixo Z
	 */
	public Vetor(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Cria um vetor com valores de uma tupla (Triplet)
	 * 
	 * @param tupla - Triplet com valores X,Y,Z em conjunto
	 */
	public Vetor(Triplet<Double, Double, Double> tupla) {
		this.x = tupla.getValue0();
		this.y = tupla.getValue1();
		this.z = tupla.getValue2();
	}

	/**
	 * Retorna um String com os valores do Vetor
	 * 
	 * @return ex: "(X: 3.0, Y: 4.0, Z: 5.0)"
	 */
	@Override
	public String toString() {
		return "( X: " + x + ", Y: " + y + ", Z:" + z + ")";
	}

	/**
	 * Modifica um vetor informando novos valores X,Y,Z
	 * 
	 * @param X - Valor eixo X
	 * @param Y - Valor eixo Y
	 * @param Z - Valor eixo Z
	 */
	public void setVetor(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * @return Retorna um novo Vetor com os valores X e Y invertidos
	 */
	public Vetor inverte() {
		return new Vetor(y, x, z);
	}

	/**
	 * Magnitude do Vetor
	 * 
	 * @return Retorna a magnitude (comprimento) do Vetor no eixo X e Y.
	 */
	public double Magnitude() {
		return Math.sqrt(x * x + y * y);
	}

	/**
	 * Magnitude do Vetor
	 * 
	 * @return Retorna a magnitude (comprimento) do Vetor em todos os eixos.
	 */
	public double Magnitude3d() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	/**
	 * Normalizar Vetor
	 * 
	 * @return Retorna um novo Vetor normalizado (magnitude de 1).
	 */
	public Vetor Normalizar() {
		double m = Magnitude3d();
		if (m != 0) {
			return new Vetor(x / m, y / m, z / m);
		}
		return new Vetor(x, y, z);
	}

	/**
	 * Limitar Vetor
	 * 
	 * @return Limita a magnitude do vetor a um valor de uma escalar .
	 */
	void Limitar(double max) {
		if (Magnitude() > max) {
			Normalizar();
			multiplica(max);
		}
	}

	/**
	 * Soma os componentes de outro vetor com o vetor informado
	 * 
	 * @param outroVetor - Vetor para somar os componentes
	 * @return Novo vetor com a soma dos componentes dos dois
	 */
	public Vetor soma(Vetor outroVetor) {
		return new Vetor(x + outroVetor.x, y + outroVetor.y, z + outroVetor.z);
	}

	/**
	 * Subtrai os componentes de outro vetor com o vetor informado
	 * 
	 * @param outroVetor - Vetor para subtrair os componentes
	 * @return Novo vetor com a subtração dos componentes dos dois
	 */
	public Vetor subtrai(Vetor outroVetor) {
		return new Vetor(x - outroVetor.x, y - outroVetor.y, z - outroVetor.z);
	}

	/**
	 * Multiplica os componentes desse vetor por uma escalar
	 * 
	 * @param escalar - Fator para multiplicar os componentes
	 * @return Novo vetor com os componentes multiplicados pela escalar. Caso a
	 *         escalar informada for 0, o Vetor retornado terá 0 como seus
	 *         componentes.
	 */
	public Vetor multiplica(double escalar) {
		if (escalar != 0) {
			return new Vetor(x * escalar, y * escalar, z * escalar);
		}
		return new Vetor(0, 0, 0);
	}

	/**
	 * Divide os componentes desse vetor por uma escalar
	 * 
	 * @param escalar - Fator para dividir os componentes
	 * @return Novo vetor com os componentes divididos pela escalar. Caso a escalar
	 *         informada for 0, o Vetor retornado terá 0 como seus componentes.
	 */
	public Vetor divide(double escalar) {
		if (escalar != 0) {
			return new Vetor(x / escalar, y / escalar, z / escalar);
		}
		return new Vetor(0, 0, 0);
	}

	/**
	 * Calcula o ângulo do vetor de direção informado
	 * 
	 * @param vetor - Vetor para calcular o ângulo
	 * @return - O ângulo da direção desse vetor, entre -180 a 180 graus.
	 */
	public static float anguloDirecao(Vetor vetor) {
		float direcao = (float) ((Math.atan2(vetor.y, vetor.x) / Math.PI) * 180);
		return direcao;
	}

	/**
	 * Calcula o Vetor da direção do ponto de origem até o alvo.
	 * 
	 * @param origem - Tupla contendo os componentes da posição do ponto de origem.
	 * @param alvo   - Tupla contendo os componentes da posição do alvo.
	 * @return - Vetor com a soma dos valores do ponto de origem com os valores do
	 *         alvo.
	 */
	public static Vetor direcaoAlvo(Triplet<Double, Double, Double> origem, Triplet<Double, Double, Double> alvo) {
		return new Vetor(alvo.getValue1() - origem.getValue1(), alvo.getValue2() - origem.getValue2(),
				alvo.getValue0() - origem.getValue0());
	}

	/**
	 * Calcula o Vetor da direção CONTRÁRIA do ponto de origem até o alvo.
	 * 
	 * @param origem - Tupla contendo os componentes da posição do ponto de origem.
	 * @param alvo   - Tupla contendo os componentes da posição do alvo.
	 * @return - Vetor inverso, com a soma dos valores do ponto de origem com o
	 *         negativo dos valores do alvo.
	 */
	public static Vetor direcaoAlvoContraria(Triplet<Double, Double, Double> origem,
			Triplet<Double, Double, Double> alvo) {
		return new Vetor(-alvo.getValue2() + origem.getValue2(), -alvo.getValue1() + origem.getValue1(),
				alvo.getValue0() - origem.getValue0());
	}

	/**
	 * Transforma um Vetor em uma tupla com os valores.
	 * 
	 * @param vetor - Vetor para transformar em tupla.
	 * @return - Nova tupla contendo os valores do vetor em seus componentes.
	 */
	public static Triplet<Double, Double, Double> paraTriplet(Vetor vetor) {
		return new Triplet<Double, Double, Double>(vetor.x, vetor.y, vetor.z);
	}
}

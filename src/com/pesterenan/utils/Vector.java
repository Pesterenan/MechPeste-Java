package com.pesterenan.utils;

import org.javatuples.Triplet;

/* Autor: Renan Torres <pesterenan@gmail.com>
 Data: 27/02/2019
 Classe Vector*/

public class Vector {

	public double x = 0;
	public double y = 0;
	public double z = 0;

	/**
	 * Cria um vector informando valores X,Y,Z manualmente
	 * 
	 * @param X - Valor eixo X
	 * @param Y - Valor eixo Y
	 * @param Z - Valor eixo Z
	 */
	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Cria um vector informando valores X,Y,Z manualmente
	 * 
	 * @param X - Valor eixo X
	 * @param Y - Valor eixo Y
	 * @param Z - Valor eixo Z
	 */
	public Vector(Vector v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}

	/**
	 * Cria um vector com valores de uma tupla (Triplet)
	 * 
	 * @param tupla - Triplet com valores X,Y,Z em conjunto
	 */
	public Vector(Triplet<Double, Double, Double> tupla) {
		this.x = tupla.getValue0();
		this.y = tupla.getValue1();
		this.z = tupla.getValue2();
	}

	/**
	 * Retorna um String com os valores do Vector
	 * 
	 * @return ex: "(X: 3.0, Y: 4.0, Z: 5.0)"
	 */
	@Override
	public String toString() {
		return String.format("( X: %.2f Y: %.2f Z: %.2f)", x, y, z);
	}

	/**
	 * Modifica um vector informando novos valores X,Y,Z
	 * 
	 * @param X - Valor eixo X
	 * @param Y - Valor eixo Y
	 * @param Z - Valor eixo Z
	 */
	public void setVetor(double X, double Y, double Z) {
		this.x = X;
		this.y = Y;
		this.z = Z;
	}

	/**
	 * @return Retorna um novo Vector com os valores X e Y invertidos
	 */
	public Vector reverse() {
		return new Vector(y, x, z);
	}

	/**
	 * Magnitude do Vector
	 * 
	 * @return Retorna a magnitude (comprimento) do Vector no eixo X e Y.
	 */
	public double Magnitude() {
		return Math.sqrt(x * x + y * y);
	}

	/**
	 * Magnitude do Vector
	 * 
	 * @return Retorna a magnitude (comprimento) do Vector em todos os eixos.
	 */
	public double Magnitude3d() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	/**
	 * normalizeVector Vector
	 * 
	 * @return Retorna um novo Vector normalizado (magnitude de 1).
	 */
	public Vector normalizeVector() {
		double m = Magnitude3d();
		if (m != 0) {
			return new Vector(x / m, y / m, z / m);
		}
		return new Vector(x, y, z);
	}

	/**
	 * limit Vector
	 * 
	 * @return Limita a magnitude do vector a um value de uma scalar .
	 */
	void limit(double max) {
		if (Magnitude() > max) {
			normalizeVector();
			multiply(max);
		}
	}

	/**
	 * Soma os componentes de outro vector com o vector informado
	 * 
	 * @param otherVector - Vector para somar os componentes
	 * @return Novo vector com a sum dos componentes dos dois
	 */
	public double dotP(Vector otherVector) {
		return (x * otherVector.x + y * otherVector.y + z * otherVector.z);
	}
	
	public double determinant(Vector otherVector) {
		return (x * otherVector.y - y * otherVector.x - z * otherVector.z);
	}

	/**
	 * Soma os componentes de outro vector com o vector informado
	 * 
	 * @param otherVector - Vector para somar os componentes
	 * @return Novo vector com a sum dos componentes dos dois
	 */
	public Vector sum(Vector otherVector) {
		return new Vector(x + otherVector.x, y + otherVector.y, z + otherVector.z);
	}

	/**
	 * Subtrai os componentes de outro vector com o vector informado
	 * 
	 * @param otherVector - Vector para subtrair os componentes
	 * @return Novo vector com a subtração dos componentes dos dois
	 */
	public Vector subtract(Vector otherVector) {
		return new Vector(x - otherVector.x, y - otherVector.y, z - otherVector.z);
	}

	/**
	 * Multiplica os componentes desse vector por uma scalar
	 * 
	 * @param scalar - Fator para multiplicar os componentes
	 * @return Novo vector com os componentes multiplicados pela scalar. Caso a
	 *         scalar informada for 0, o Vector retornado terá 0 como seus
	 *         componentes.
	 */
	public Vector multiply(double scalar) {
		if (scalar != 0) {
			return new Vector(x * scalar, y * scalar, z * scalar);
		}
		return new Vector(0, 0, 0);
	}

	/**
	 * Divide os componentes desse vector por uma scalar
	 * 
	 * @param scalar - Fator para dividir os componentes
	 * @return Novo vector com os componentes divididos pela scalar. Caso a scalar
	 *         informada for 0, o Vector retornado terá 0 como seus componentes.
	 */
	public Vector divide(double scalar) {
		if (scalar != 0) {
			return new Vector(x / scalar, y / scalar, z / scalar);
		}
		return new Vector(0, 0, 0);
	}

	/**
	 * Calcula o ângulo do vector de direção informado
	 * 
	 * @param vector - Vector para calcular o ângulo
	 * @return - O ângulo da direção desse vector, entre -180 a 180 graus.
	 */
	public static float angleDirection(Vector vector) {
		float direction = (float) (Math.toDegrees(Math.atan2(vector.y, vector.x)));
		return direction;
	}

	/**
	 * Calcula o Vector da direção do spot de origin até o target.
	 * 
	 * @param origin - Tupla contendo os componentes da posição do spot de origin.
	 * @param target   - Tupla contendo os componentes da posição do target.
	 * @return - Vector com a sum dos valores do spot de origin com os valores do
	 *         target.
	 */
	public static Vector targetDirection(Triplet<Double, Double, Double> origin, Triplet<Double, Double, Double> target) {
		return new Vector(target.getValue1() - origin.getValue1(), target.getValue2() - origin.getValue2(),
				target.getValue0() - origin.getValue0());
	}

	/**
	 * Calcula o Vector da direção CONTRÁRIA do spot de origin até o target.
	 * 
	 * @param origin - Tupla contendo os componentes da posição do spot de origin.
	 * @param target   - Tupla contendo os componentes da posição do target.
	 * @return - Vector inverso, com a sum dos valores do spot de origin com o
	 *         negativo dos valores do target.
	 */
	public static Vector oppositeTargeDirection(Triplet<Double, Double, Double> origin,
			Triplet<Double, Double, Double> target) {
		return new Vector(-target.getValue2() + origin.getValue2(), -target.getValue1() + origin.getValue1(),
				target.getValue0() - origin.getValue0());
	}

	/**
	 * Transforma um Vector em uma tupla com os valores.
	 * 
	 * @param vector - Vector para transformar em tupla.
	 * @return - Nova tupla contendo os valores do vector em seus componentes.
	 */
	public Triplet<Double, Double, Double> toTriplet() {
		return new Triplet<Double, Double, Double>(this.x, this.y, this.z);
	}
}

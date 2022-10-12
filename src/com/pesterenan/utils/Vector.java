package com.pesterenan.utils;

import org.javatuples.Triplet;

public class Vector {

public double x = 0;
public double y = 0;
public double z = 0;

public Vector() {

}

/**
 * Cria um vetor informando valores X,Y,Z manualmente
 *
 * @param X - Valor eixo X
 * @param Y - Valor eixo Y
 * @param Z - Valor eixo Z
 */
public Vector(double X, double Y, double Z) {
	this.x = X;
	this.y = Y;
	this.z = Z;
}

/**
 * Cria um vetor a partir de outro Vetor
 *
 * @param v - Outro Vetor
 */
public Vector(Vector newVector) {
	this.x = newVector.x;
	this.y = newVector.y;
	this.z = newVector.z;
}

/**
 * Cria um vetor com valores de uma tupla (Triplet)
 *
 * @param triplet - Triplet com valores X,Y,Z em conjunto
 */
public Vector(Triplet<Double, Double, Double> triplet) {
	this.x = triplet.getValue0();
	this.y = triplet.getValue1();
	this.z = triplet.getValue2();
}

/**
 * Calcula o ângulo do vetor de direção informado
 *
 * @param vector - Vetor para calcular o ângulo
 * @return - O ângulo da direção desse vetor, entre -180 a 180 graus.
 */
public double heading() {
	return Math.toDegrees(Math.atan2(this.y, this.x));
}

public static double distance(Vector start, Vector end) {
	if (start.equals(null) || end.equals(null)) {
		return 0;
	}
	return end.subtract(start)
						.magnitude();
}

/**
 * Calcula o Vetor da direção do ponto de origem até o alvo.
 *
 * @param start - Vetor contendo os componentes da posição do ponto de origem.
 * @param end   - Vetor contendo os componentes da posição do alvo.
 * @return - Vetor com a soma dos valores do ponto de origem com os valores do
 * alvo.
 */
public static Vector targetDirection(Vector start, Vector end) {
	return end.subtract(start)
						.normalize();
}

/**
 * Calcula o Vetor da direção CONTRÁRIA do ponto de origem até o alvo.
 *
 * @param start - Tupla contendo os componentes da posição do ponto de origem.
 * @param end   - Tupla contendo os componentes da posição do alvo.
 * @return - Vetor inverso, com a soma dos valores do ponto de origem com o
 * negativo dos valores do alvo.
 */
public static Vector targetOppositeDirection(Vector start, Vector end) {
	return new Vector(-end.x + start.x, -end.y + start.y, end.z - start.z).normalize();
}

/**
 * Retorna um String com os valores do Vetor
 *
 * @return ex: "(X: 3.0, Y: 4.0, Z: 5.0)"
 */
@Override
public String toString() {
	return String.format("( X: %.2f Y: %.2f Z: %.2f)", this.x, this.y, this.z);
}

/**
 * Modifica um vetor informando novos valores X,Y,Z
 *
 * @param X - Valor eixo X
 * @param Y - Valor eixo Y
 * @param Z - Valor eixo Z
 */
public void setVector(double X, double Y, double Z) {
	this.x = X;
	this.y = Y;
	this.z = Z;
}

/**
 * @return Retorna um novo Vetor com os valores X e Y invertidos
 */
public Vector invertXY() {
	return new Vector(y, x, z);
}

/**
 * Magnitude do Vetor
 *
 * @return Retorna a magnitude (comprimento) do Vetor no eixo X e Y.
 */
public double magnitudeXY() {
	return Math.sqrt(x * x + y * y);
}

/**
 * Magnitude do Vetor
 *
 * @return Retorna a magnitude (comprimento) do Vetor em todos os eixos.
 */
public double magnitude() {
	return Math.sqrt(x * x + y * y + z * z);
}

/**
 * Normalizar Vetor
 *
 * @return Retorna um novo Vetor normalizado (magnitude de 1).
 */
public Vector normalize() {
	double m = magnitude();
	if (m != 0) {
		return new Vector(x / m, y / m, z / m);
	}
	return new Vector(x, y, z);
}

/**
 * Soma os componentes de outro vetor com o vetor informado
 *
 * @param otherVector - Vetor para somar os componentes
 * @return Novo vetor com a soma dos componentes dos dois
 */
public double dotP(Vector otherVector) {
	return (x * otherVector.x + y * otherVector.y + z * otherVector.z);
}

public double determinant(Vector otherVector) {
	return (x * otherVector.y - y * otherVector.x - z * otherVector.z);
}

/**
 * Soma os componentes de outro vetor com o vetor informado
 *
 * @param otherVector - Vetor para somar os componentes
 * @return Novo vetor com a soma dos componentes dos dois
 */
public Vector sum(Vector otherVector) {
	return new Vector(x + otherVector.x, y + otherVector.y, z + otherVector.z);
}

/**
 * Subtrai os componentes de outro vetor com o vetor informado
 *
 * @param otherVector - Vetor para subtrair os componentes
 * @return Novo vetor com a subtração dos componentes dos dois
 */
public Vector subtract(Vector otherVector) {
	return new Vector(x - otherVector.x, y - otherVector.y, z - otherVector.z);
}

/**
 * Multiplica os componentes desse vetor por uma escalar
 *
 * @param scalar - Fator para multiplicar os componentes
 * @return Novo vetor com os componentes multiplicados pela escalar. Caso a
 * escalar informada for 0, o Vetor retornado terá 0 como seus
 * componentes.
 */
public Vector multiply(double scalar) {
	if (scalar != 0) {
		return new Vector(x * scalar, y * scalar, z * scalar);
	}
	return new Vector(0, 0, 0);
}

/**
 * Divide os componentes desse vetor por uma escalar
 *
 * @param scalar - Fator para dividir os componentes
 * @return Novo vetor com os componentes divididos pela escalar. Caso a escalar
 * informada for 0, o Vetor retornado terá 0 como seus componentes.
 */
public Vector divide(double scalar) {
	if (scalar != 0) {
		return new Vector(x / scalar, y / scalar, z / scalar);
	}
	return new Vector(0, 0, 0);
}

/**
 * Transforma um Vetor em uma tupla com os valores.
 *
 * @return - Nova tupla contendo os valores do vetor em seus componentes.
 */
public Triplet<Double, Double, Double> toTriplet() {
	return new Triplet<>(this.x, this.y, this.z);
}
}

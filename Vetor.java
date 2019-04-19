import org.javatuples.Triplet;

/* Autor: Renan Torres <pesterenan@gmail.com>
 Data: 27/02/2019
 Classe Vetor*/

public class Vetor {

	public double x = 0;
	public double y = 0;
	public double z = 0;

	public Vetor(double arg0, double arg1, double arg2) {
		x = arg0;
		y = arg1;
		z = arg2;
	}

	public Vetor(Triplet<Double, Double, Double> tupla) {
		x = tupla.getValue0();
		y = tupla.getValue1();
		z = tupla.getValue2();
	}

	public String toString() {
		return "( X: " + x + ", Y: " + y + ", Z:" + z + ")";
	}

	public void setVetor(double arg0, double arg1, double arg2) {
		x = arg0;
		y = arg1;
		z = arg2;
	}

	public Vetor inverte() {
		return new Vetor(y, x, z);
	}

	public double Magnitude() {
		return Math.sqrt(x * x + y * y);
	}

	public double Magnitude3d() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	public Vetor Normalizar() {
		double m = Magnitude();
		if (m != 0) {
			return new Vetor(x / m, y / m, z / m);
		}
		return new Vetor(x, y, z);
	}

	void Limitar(double max) {
		if (Magnitude() > max) {
			Normalizar();
			multiplica(max);
		}
	}

	public Vetor soma(Vetor outro) {
		return new Vetor(x + outro.x, y + outro.y, z + outro.z);
	}

	public Vetor subtrai(Vetor outro) {
		return new Vetor(x - outro.x, y - outro.y, z - outro.z);
	}

	public Vetor multiplica(double escalar) {
		return new Vetor(x * escalar, y * escalar, z * escalar);
	}

	public Vetor divide(double escalar) {
		if (escalar != 0) {
			return new Vetor(x / escalar, y / escalar, z / escalar);
		}
		return new Vetor(0, 0, 0);
	}

	public static float anguloDirecao(Vetor vetor) {
		float direcao = (float) ((Math.atan2(vetor.y, vetor.x) / Math.PI) * 180);
		return direcao;
	}

	public static Vetor vetorDistancia(Triplet<Double, Double, Double> vetorA, Triplet<Double, Double, Double> vetorB) {
		return new Vetor(-((Double) vetorA.getValue2()).doubleValue() + ((Double) vetorB.getValue2()).doubleValue(),
				-((Double) vetorA.getValue1()).doubleValue() + ((Double) vetorB.getValue1()).doubleValue(), 0);
	}
}

import org.javatuples.Triplet;

/* Autor: Renan Torres <pesterenan@gmail.com>
 Data: 27/02/2019
 Classe Vetor*/

public class Vetor {

	public double x = 0;
	public double y = 0;
	public static double direcao = 0;

	public Vetor(double arg0, double arg1) {
		x = arg0;
		y = arg1;
	}
	
	public String toString() {
		return "("+ x + ", "+ y +")";
	}
	public void setVetor(double arg0, double arg1) {
		x = arg0;
		y = arg1;
	}
	public Vetor inverte()
	{
		return new Vetor(y, x);
	}
	public double Magnitude() {
		return Math.sqrt(x * x + y * y);
	}
	public Vetor Normalizar() {
		double m = Magnitude();
		if (m != 0) {
			return new Vetor(x / m, y / m);
		}
		return new Vetor(x , y);
	}
	void Limitar(double max) {
		if (Magnitude() > max) {
			Normalizar();
			multiplica(max);
		}
	}
	public Vetor soma(Vetor outro) {
		return new Vetor(x + outro.x, y + outro.y);
	}
	public Vetor subtrai(Vetor outro) {
		return new Vetor(x - outro.x, y - outro.y);
	}
	public Vetor multiplica(double escalar)  {
		return new Vetor(x * escalar, y * escalar);
	}
	public Vetor divide(double escalar)  {
		if (escalar != 0){
			return new Vetor(x / escalar, y / escalar);
		}
		return new Vetor(0,0);
	}
	public static float anguloDirecao(Vetor vetor) {
		direcao = (Math.atan2(vetor.y, vetor.x) / Math.PI) * 180;
		return Math.round(direcao);
	}
	public static Vetor vetorDistancia(Triplet<Double, Double, Double> vetorA, Triplet<Double, Double, Double> vetorB)
	{
		return new Vetor(-((Double) vetorA.getValue2()).doubleValue() + ((Double) vetorB.getValue2()).doubleValue(),
						-((Double) vetorA.getValue1()).doubleValue() + ((Double) vetorB.getValue1()).doubleValue());
	}
}

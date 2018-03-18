import org.javatuples.Triplet;

public class Vetor {

	public double x = 0;
	public double y = 0;

	public Vetor(double arg0, double arg1) {
		x = arg0;
		y = arg1;
	}
	public static Vetor vetorDistancia(Triplet<Double, Double, Double> t0, Triplet<Double, Double, Double> t1)
	{
		return new Vetor(-((Double)t0.getValue2()).doubleValue() + ((Double)t1.getValue2()).doubleValue(),
						-((Double)t0.getValue1()).doubleValue() + ((Double)t1.getValue1()).doubleValue());
	}
	public String toString() {
		return "("+ x + ", "+ y +")";
	}
	public void Set(double arg0, double arg1) {
		x = arg0;
		y = arg1;
	}
	public Vetor inverte()
	{
		return new Vetor(y, x);
	}
	public double Magnitude() {
		return (Math.sqrt(x*x + y*y));
	}
	public Vetor Normalizar() {
		double m = Magnitude();
		if (m != 0) {
			return new Vetor(x/m,y/m);
		}
		return new Vetor(x,y);
	}
	public Vetor Normalizar(double m) {
		if (m != 0) {
			return new Vetor(x/m,y/m);
		}
		return new Vetor(x,y);
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
	public Vetor multiplica(double scalar)  {
		return new Vetor(x * scalar, y * scalar);
	}
	public Vetor divide(double scalar)  {
		if (scalar != 0){
			return new Vetor(x / scalar, y / scalar);
		}
		return new Vetor(0,0);
	}
}

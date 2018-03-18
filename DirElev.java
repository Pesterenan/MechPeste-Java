
public class DirElev {
	public float direcao;
	public float elevacao;
	
	public DirElev(int d, int e)
	{
		direcao = d;
		elevacao = e;
	}
	public String toString() {
		return "("+direcao+","+elevacao+")";
	}
}

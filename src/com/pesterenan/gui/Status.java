package com.pesterenan.gui;

public enum Status {
	CONECTANDO("Conectando..."), CONECTADO("Conectado."), ERROCONEXAO("Erro na conex„o. O jogo n„o est· sendo executado."),
	ERRODECOLAGEM("Erro ao executar a Decolagem Orbital!"), ERROSUICIDE("Erro ao executar o Pouso Autom√°tico!"),
	ERROROVER("Erro ao executar o Controle de Rover!"), ERROMANOBRAS("Erro ao executar a manobra!"),
	EXECSUICIDE("Executando Suicide Burn..."), EXECDECOLAGEM("Executando Decolagem Orbital..."),
	EXECROVER("Executando Auto Rover..."), JAEXEC("J√° est√° em execu√ß√£o"), PRONTO("Pronto."),
	EXECMANOBRAS("Executando Manobras...");

	String t;

	private Status(String texto) {
		this.t = texto;
	}

	public String get() {
		return this.t;
	}

}
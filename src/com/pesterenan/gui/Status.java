package com.pesterenan.gui;

public enum Status {
	CONECTANDO("Conectando..."), CONECTADO("Conectado."), ERROCONEXAO("Erro na conexão!"),
	ERRODECOLAGEM("Erro ao executar a Decolagem Orbital!"), ERROSUICIDE("Erro ao executar o Pouso Automático!"),
	ERROROVER("Erro ao executar o Controle de Rover!"), ERROMANOBRAS("Erro ao executar a manobra!"),
	EXECSUICIDE("Executando Suicide Burn..."), EXECDECOLAGEM("Executando Decolagem Orbital..."),
	EXECROVER("Executando Auto Rover..."), JAEXEC("Já está em execução"), PRONTO("Pronto."),
	EXECMANOBRAS("Executando Manobras...");

	String t;

	private Status(String texto) {
		this.t = texto;
	}

	public String get() {
		return this.t;
	}

}
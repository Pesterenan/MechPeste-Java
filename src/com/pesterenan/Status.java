package com.pesterenan;

public enum Status {
	CONECTANDO("Conectando..."), CONECTADO("Conectado."), ERROCONEXAO("Erro na conexão!"),
	EXECSUICIDE("Executando Suicide Burn..."), EXECDECOLAGEM("Executando Decolagem Orbital..."),
	EXECROVER("Executando Auto Rover..."), JAEXEC("Já está em execução"), PRONTO("Pronto.");
	String t;

	private Status(String texto) {
		this.t = texto;
	}

	String get() {
		return this.t;
	}

}
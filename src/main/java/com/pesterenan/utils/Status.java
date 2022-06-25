package com.pesterenan.utils;

public enum Status {
	CONECTANDO("Conectando..."), CONECTADO("Conectado."),
	ERRO_CONEXAO("Erro na conexão. O jogo não está sendo executado."),
	ERRO_DECOLAGEM_ORBITAL("Erro ao executar a Decolagem Orbital!"),
	ERRO_POUSO_AUTO("Erro ao executar o Pouso Automático!"), ERRO_ROVER("Erro ao executar o Controle de Rover!"),
	ERRO_MANOBRAS("Erro ao executar a manobra!"), EXEC_POUSO_AUTO("Executando Suicide Burn..."),
	STATUS_DECOLAGEM_ORBITAL("Executando Decolagem Orbital..."),
	STATUS_POUSO_AUTOMATICO("Executando Pouso Automático..."), EXEC_ROVER("Executando Auto Rover..."),
	JA_EXECUTANDO("Já está em execução"), PRONTO("Pronto."), EXEC_MANOBRAS("Executando Manobras...");

	String t;

	private Status(String texto) {
		this.t = texto;
	}

	public String get() {
		return this.t;
	}

}
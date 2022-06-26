package com.pesterenan.utils;

public enum Status {
	CONNECTING("Conectando..."), CONNECTED("Conectado."),
	CONNECTION_ERROR("Erro na conexão. O jogo não está sendo executado."),
	ORBITAL_LIFTOFF_ERROR("Erro ao executar a Decolagem Orbital!"),
	AUTOMATIC_LAND_ERROR("Erro ao executar o Pouso Automático!"), ROVER_ERROR("Erro ao executar o Controle de Rover!"),
	MANEUVER_ERROR("Erro ao executar a manobra!"), EXEC_POUSO_AUTO("Executando Suicide Burn..."),
	ORBITAL_LIFTOFF_STATUS("Executando Decolagem Orbital..."),
	AUTOMATIC_LAND_STATUS("Executando Pouso Automático..."), EXECUTING_ROVER("Executando Auto Rover..."),
	ALREADY_EXECUTING("Já está em execução"), READY("Pronto."), EXECUTING_MANEUVERS("Executando Manobras...");

	String t;

	private Status(String text) {
		this.t = text;
	}

	public String get() {
		return this.t;
	}

}
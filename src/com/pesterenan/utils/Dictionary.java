package com.pesterenan.utils;

public enum Dictionary {
	MECHPESTE("MechPeste"), 
	CONNECT("Conectar"), 
	ERROR_CONNECTING("Erro ao se conectar ao jogo:\n\t"),
	ORBITAL_LIFTOFF("Decolagem Orbital"), 
	AUTOMATIC_LAND("Pouso Automático"), 
	AUTOMATIC_ROVER("Pilotar Rover"),
	MANEUVERS("Exec. Manobras"), 
	TELEMETRY("Telemetria"), 
	EXIT("Sair"), 
	TXT_PARAMETERS("Parâmetros:");

	final String t;

	private Dictionary(String text) {
		this.t = text;
	}

	final public String get() {
		return this.t;
	}

}
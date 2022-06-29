package com.pesterenan.utils;

public enum Dicionario {
	MECHPESTE("MechPeste"), 
	CONECTAR("Conectar"), 
	DECOLAGEM_ORBITAL("Decolagem Orbital"), 
	POUSO_AUTOMATICO("Pouso Automático"), 
	ROVER_AUTONOMO("Pilotar Rover"),
	MANOBRAS("Exec. Manobras"), 
	TELEMETRIA("Telemetria"), 
	SAIR("Sair"), 
	TXT_PARAMETROS("Parâmetros:");

	final String t;

	private Dicionario(String texto) {
		this.t = texto;
	}

	final public String get() {
		return this.t;
	}

}
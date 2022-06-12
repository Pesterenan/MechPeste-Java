package com.pesterenan.utils;

public enum Modulos {

	APOASTRO("Apoastro"), PERIASTRO("Periastro"), EXECUTAR("Executar"), AJUSTAR("Ajustar"), DIRECAO("Direção"),
	MODULO("Módulo"), FUNCAO("Função"), MODULO_DECOLAGEM("Executar Decolagem"), MODULO_MANOBRAS("Módulo Manobras"),
	MODULO_POUSO("Módulo Pouso"), MODULO_PILOTO("Módulo Piloto"), ALTITUDE_SOBREVOO("Altitude Sobrevoo"),
	MODULO_POUSO_SOBREVOAR("Sobrevoar");

	String t;

	Modulos(String t) {
		this.t = t;
	}

	public String get() {
		return this.t;
	}
}

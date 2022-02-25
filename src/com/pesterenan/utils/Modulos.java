package com.pesterenan.utils;

public enum Modulos {

	APOASTRO("Apoastro"), PERIASTRO("Periastro"), EXECUTAR("Executar"), AJUSTAR("Ajustar"), DIRECAO("Direção"),
	MODULO("Módulo"), FUNCAO("Função"), MODULO_DECOLAGEM("Executar Decolagem"), MODULO_MANOBRAS("Módulo Manobras");

	String t;

	Modulos(String t) {
		this.t = t;
	}

	public String get() {
		return this.t;
	}
}

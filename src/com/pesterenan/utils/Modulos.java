package com.pesterenan.utils;

public enum Modulos {

	APOASTRO("Apoastro"), 
	PERIASTRO("Periastro"), 
	EXECUTAR("Executar"), 
	AJUSTAR("Ajustar"), 
	DIRECAO("Direção"),
	MODULO("Módulo"), 
	FUNCAO("Função"), 
	MODULO_DECOLAGEM("Executar Decolagem"), 
	MODULO_MANOBRAS("Módulo Manobras"),
	MODULO_POUSO("Módulo Pouso"), 
	MODULO_ROVER("Módulo Rover"), 
	ALTITUDE_SOBREVOO("Altitude Sobrevoo"),
	MODULO_POUSO_SOBREVOAR("Sobrevoar"), 
	INCLINACAO("Inclinação"), 
	CIRCULAR("Circular"),
	QUADRATICA("Quadrática"),
	CUBICA("Cúbica"),
	SINUSOIDAL("Sinusoidal"), 
	EXPONENCIAL("Exponencial"), 
	ROLAGEM("Rolagem"), 
	USAR_ESTAGIOS("Usar Estágios"),
	ABRIR_PAINEIS("Abrir Painéis"), 
	AJUSTE_FINO("Ajuste Fino");

	String t;

	Modulos(String t) {
		this.t = t;
	}

	public String get() {
		return this.t;
	}
}

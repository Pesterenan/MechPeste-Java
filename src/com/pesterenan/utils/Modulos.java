package com.pesterenan.utils;

public enum Modulos {

	APOASTRO("Apoastro"),
	PERIASTRO("Periastro"),
	EXECUTAR("Executar"),
	AJUSTAR("Ajustar"),
	DIRECAO("Direção"),
	MODULO("Módulo"),
	FUNCAO("Função"),
	MODULO_DECOLAGEM("LIFTOFF"),
	MODULO_MANOBRAS("MANEUVER"),
	MODULO_POUSO("LANDING"),
	MODULO_ROVER("ROVER"),
	MODULO_TELEMETRIA("TELEMETRY"),
	MODULO_POUSO_SOBREVOAR("HOVER"),
	ALTITUDE_SOBREVOO("Altitude Sobrevoo"),
	INCLINACAO("Inclinação"),
	CIRCULAR("Circular"),
	QUADRATICA("Quadrática"),
	CUBICA("Cúbica"),
	SINUSOIDAL("Sinusoidal"),
	EXPONENCIAL("Exponencial"),
	ROLAGEM("Rolagem"),
	USAR_ESTAGIOS("Usar Estágios"),
	ABRIR_PAINEIS("Abrir Painéis"),
	AJUSTE_FINO("Ajuste Fino"),
	TIPO_ALVO_ROVER("Tipo de Alvo do Rover"),
	NAVE_ALVO("Nave alvo"),
	MARCADOR_MAPA("Marcador no mapa"),
	NOME_MARCADOR("Nome do marcador"),
	VELOCIDADE_MAX("Velocidade Máxima");

	final String t;

	Modulos(String t) {
		this.t = t;
	}

	public String get() {
		return this.t;
	}
}

package com.pesterenan.utils;

public enum Modulos {

	ABRIR_PAINEIS("Abrir Painéis"),
	AJUSTAR("Ajustar"),
	AJUSTE_FINO("Ajuste Fino"),
	ALTITUDE_SOBREVOO("Altitude Sobrevoo"),
	APOASTRO("Apoastro"),
	CIRCULAR("Circular"),
	CUBICA("Cúbica"),
	DIRECAO("Direção"),
	DISTANCIA_SEGURA("Distância Segura"),
	EXECUTAR("Executar"),
	EXPONENCIAL("Exponencial"),
	FUNCAO("Função"),
	INCLINACAO("Inclinação"),
	MARCADOR_MAPA("Marcador no mapa"),
	MAX_TWR("Max_TWR"),
	MODULE_MANEUVER("MANEUVER"),
	MODULO("Módulo"),
	MODULO_CRIAR_MANOBRAS("CRIAR_MANOBRAS"),
	MODULO_DECOLAGEM("LIFTOFF"),
	MODULO_DOCKING("DOCKING"),
	MODULO_POUSO("LANDING"),
	MODULO_POUSO_SOBREVOAR("HOVER"),
	MODULO_ROVER("ROVER"),
	MODULO_TELEMETRIA("TELEMETRY"),
	NAVE_ALVO("Nave alvo"),
	NOME_MARCADOR("Nome do marcador"),
	ORBITA_BAIXA("ÓRBITA BAIXA"),
	PERIASTRO("Periastro"),
	POUSAR("Pousar nave"),
	QUADRATICA("Quadrática"),
	RENDEZVOUS("Rendezvous"),
	ROLAGEM("Rolagem"),
	SINUSOIDAL("Sinusoidal"),
	SOBREVOO_POS_POUSO("SOBREVOO PÓS POUSO"),
	TIPO_ALVO_ROVER("Tipo de Alvo do Rover"),
	USAR_ESTAGIOS("Usar Estágios"),
	VELOCIDADE_MAX("Velocidade Máxima");

	final String t;

	Modulos(String t) {
		this.t = t;
	}

	public String get() {
		return this.t;
	}
}

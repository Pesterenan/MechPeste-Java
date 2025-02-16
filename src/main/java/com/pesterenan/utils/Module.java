package com.pesterenan.utils;

public enum Module {

	ADJUST("Ajustar"),
	APOAPSIS("Apoastro"),
	CIRCULAR("Circular"),
	CREATE_MANEUVER("CRIAR_MANOBRAS"),
	CUBIC("Cúbica"),
	DIRECTION("Direção"),
	DOCKING("DOCKING"),
	EXECUTE("Executar"),
	EXPONENCIAL("Exponencial"),
	FINE_ADJUST("Ajuste Fino"),
	FUNCTION("Função"),
	HOVERING("HOVER"),
	HOVER_AFTER_LANDING("SOBREVOO PÓS POUSO"),
	HOVER_ALTITUDE("Altitude Sobrevoo"),
	INCLINATION("Inclinação"),
	LAND("Pousar nave"),
	LANDING("LANDING"),
	LIFTOFF("LIFTOFF"),
	LOW_ORBIT("ÓRBITA BAIXA"),
	MANEUVER("MANEUVER"),
	MAP_MARKER("Marcador no mapa"),
	MARKER_NAME("Nome do marcador"),
	MAX_SPEED("Velocidade Máxima"),
	MAX_TWR("Max_TWR"),
	MODULO("Módulo"),
	OPEN_PANELS("Abrir Painéis"),
	PERIAPSIS("Periastro"),
	QUADRATIC("Quadrática"),
	RENDEZVOUS("Rendezvous"),
	ROLL("Rolagem"),
	ROVER("ROVER"),
	ROVER_TARGET_TYPE("Tipo de Alvo do Rover"),
	SAFE_DISTANCE("Distância Segura"),
	SINUSOIDAL("Sinusoidal"),
	STAGE("Usar Estágios"),
	TARGET_VESSEL("Nave alvo"),
	TELEMETRY("TELEMETRY");

	final String t;

	Module(String t) {
		this.t = t;
	}

	public String get() {
		return this.t;
	}
}

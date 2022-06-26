package com.pesterenan.utils;

public enum Modules {

	APOAPSIS("Apoastro"), 
	PERIAPSIS("Periastro"), 
	EXECUTE("Executar"), 
	AJUST("Ajustar"), 
	DIRECTION("Direção"),
	MODULE("Módulo"), 
	FUNCTION("Função"), 
	LIFTOFF_MODULE("Executar Decolagem"), 
	MANEUVER_MODULE("Módulo Manobras"),
	LANDING_MODULE("Módulo Pouso"), 
	PILOT_MODULE("Módulo Piloto"), 
	FLIGHT_ALTITUDE("Altitude Sobrevoo"),
	LANGING_FLIGHT_MODULE("Sobrevoar"), 
	INCLINATION("Inclinação"),
	CIRCLE("Circular"),
	QUADRATIC("Quadrática"),
	CUBIC("Cúbica"),
	SINUSOIDAL("Sinusoidal"), 
	EXPONENTIAL("Exponencial"), 
	ROLL("Rolagem");

	String t;

	Modules(String t) {
		this.t = t;
	}

	public String get() {
		return this.t;
	}
}

package com.pesterenan.utils;

import java.util.HashMap;
import java.util.Map;

public class Attributes {

    private static Map<String,Double> safeLowOrbitAltitudes = new HashMap<String,Double>();

    public Attributes() {
        safeLowOrbitAltitudes.put("Bop", 10_000.0);
        safeLowOrbitAltitudes.put("Dres", 20_000.0);
        safeLowOrbitAltitudes.put("Duna", 60_000.0);
        safeLowOrbitAltitudes.put("Eeloo", 20_000.0);
        safeLowOrbitAltitudes.put("Eve", 100_000.0);
        safeLowOrbitAltitudes.put("Gilly", 10_000.0);
        safeLowOrbitAltitudes.put("Ike", 20_000.0);
        safeLowOrbitAltitudes.put("Jool", 220_000.0);
        safeLowOrbitAltitudes.put("Kerbin", 80_000.0);
        safeLowOrbitAltitudes.put("Laythe", 80_000.0);
        safeLowOrbitAltitudes.put("Minmus", 10_000.0);
        safeLowOrbitAltitudes.put("Moho", 35_000.0);
        safeLowOrbitAltitudes.put("Mun", 10_000.0);
        safeLowOrbitAltitudes.put("Pol", 10_000.0);
        safeLowOrbitAltitudes.put("Sun", 350_000.0);
        safeLowOrbitAltitudes.put("Tylo", 40_000.0);
        safeLowOrbitAltitudes.put("Vall", 20_000.0);
    }

    public double getLowOrbitAltitude(String celestialBody) {
        return safeLowOrbitAltitudes.getOrDefault(celestialBody, 10_000.0);
    };
}

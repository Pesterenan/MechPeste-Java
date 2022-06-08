package com.pesterenan.utils;

public class Utilities {
	
	public static double linearInterpolation(double v0, double v1, double t) {
		return (1 - t) * v0 + t * v1;
	}

	public static double limitValue(double value, double min, double max) {
		return (value > max ? max : value < min ? min : value);
	}
}

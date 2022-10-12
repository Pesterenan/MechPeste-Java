package com.pesterenan.utils;

public class Utilities {

	public static double linearInterpolation(double start, double end, double value) {
		return (1.0 - value) * start + value * end;
	}

	public static double inverseLinearInterpolation(double start, double end, double value) {
		return (value - start) / (end - start);
	}

	public static double remap(double inputMin, double inputMax, double outputMin, double outputMax, double value,
							   boolean clampOutput) {
		double between = inverseLinearInterpolation(inputMin, inputMax, value);
		double remappedOutput = linearInterpolation(outputMin, outputMax, between);
		return clampOutput ? clamp(remappedOutput, outputMin, outputMax) : remappedOutput;
	}

	public static double clamp(double value, double minimum, double maximum) {
		return Math.max(Math.min(value, maximum), minimum);
	}

	// Easing functions
	public static double easeInCirc(double value) {
		return 1 - Math.sqrt(1 - Math.pow(clamp(value, 0, 1), 2));
	}

	public static double easeInSine(double value) {
		return 1 - Math.cos((value * Math.PI) / 2);
	}

	public static double easeInQuad(double value) {
		return Math.pow(value, 2);
	}

	public static double easeInCubic(double value) {
		return Math.pow(value, 3);
	}

	public static double easeInExpo(double value) {
		return value == 0 ? 0 : Math.pow(2, 10 * value - 10);
	}

}

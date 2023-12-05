package com.pesterenan.utils;

import com.pesterenan.resources.Bundle;

public class Utilities {

	public static double linearInterpolation(double start, double end, double value) {
		return (1.0 - value) * start + value * end;
	}

	public static Vector linearInterpolation(Vector start, Vector end, double value) {
		double x = linearInterpolation(start.x, end.x, value);
		double y = linearInterpolation(start.y, end.y, value);
		double z = linearInterpolation(start.z, end.z, value);
		return new Vector(x, y, z);
	}

	public static double inverseLinearInterpolation(double start, double end, double value) {
		return (value - start) / (end - start);
	}

	public static Vector inverseLinearInterpolation(Vector start, Vector end, double value) {
		double x = inverseLinearInterpolation(start.x, end.x, value);
		double y = inverseLinearInterpolation(start.y, end.y, value);
		double z = inverseLinearInterpolation(start.z, end.z, value);
		return new Vector(x, y, z);
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

	public static String convertToMetersMagnitudes(double meters) {
		String decimalPlaces = "%.2f"; //$NON-NLS-1$
		if (meters >= 1000000000) {
			return String.format(decimalPlaces + "Gm", meters / 1000000000); //$NON-NLS-1$
		} else if (meters >= 1000000) {
			return String.format(decimalPlaces + "Mm", meters / 1000000); //$NON-NLS-1$
		} else if (meters >= 1000) {
			return String.format(decimalPlaces + "km", meters / 1000); //$NON-NLS-1$
		} else {
			return String.format(decimalPlaces + "m", meters); //$NON-NLS-1$
		}
	}

	public String formatElapsedTime(Double totalSeconds) {
		int years = (totalSeconds.intValue() / 9201600);
		int days = (totalSeconds.intValue() / 21600) % 426;
		int hours = (totalSeconds.intValue() / 3600) % 6;
		int minutes = (totalSeconds.intValue() % 3600) / 60;
		int seconds = totalSeconds.intValue() % 60;
		return String.format(Bundle.getString("pnl_tel_lbl_date_template"), years, days, hours, minutes,
				seconds); // $NON-NLS-1$
	}
}

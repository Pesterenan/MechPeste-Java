package com.pesterenan.controllers;

import com.pesterenan.model.ActiveVessel;

public class Controller extends ActiveVessel implements Runnable {

	public Controller() {
		super();
	}

	public void run() {
		try {
			while (!Thread.interrupted()) {
				long currentTime = System.currentTimeMillis();
				if (currentTime > timer + 100) {
					recordTelemetryData();
					timer = currentTime;
				}
			}
		} catch (Exception ignored) {
		}
	}
}

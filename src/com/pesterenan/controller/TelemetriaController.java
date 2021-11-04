package com.pesterenan.controller;

import com.pesterenan.model.Nave;

import krpc.client.Connection;

public class TelemetriaController extends Nave implements Runnable {

	public TelemetriaController() {
		run();
	
	}

	public TelemetriaController(Connection con) {
		super(con);
	}

	@Override
	public void run() {
		int tempo = 0;
		while (tempo < 100) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(tempo);
			tempo++;
		}
	}

}

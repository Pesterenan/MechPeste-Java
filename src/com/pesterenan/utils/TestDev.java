package com.pesterenan.utils;

import com.pesterenan.resources.Bundle;
import com.pesterenan.views.StatusJPanel;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.Drawing;
import krpc.client.services.SpaceCenter;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestDev {

	private SpaceCenter.Vessel targetVessel;
	private SpaceCenter.ReferenceFrame pontoRefRover;
	private SpaceCenter.ReferenceFrame pontoRefSuperficie;
	private SpaceCenter.ReferenceFrame pontoRefOrbital;
	private Drawing drawing;
	private Drawing.Line steeringLine;
	private Connection connection;
	private SpaceCenter centroEspacial;
	private SpaceCenter.Vessel naveAtual;

	private List<Pair<Vector, Vector>> pontosDoCaminho = new ArrayList<>();
	private Vector roverPosition = new Vector();
	private Vector roverDirection = new Vector();
	private Vector targetPosition = new Vector();


	public TestDev() {
		initializeParameters();
	}

	public static void main(String[] args) throws RPCException, IOException, InterruptedException {
		TestDev td = new TestDev();
		td.setTarget();
		td.createPathToTarget();
//		td.testMethod();
	}

	private void testMethod() throws RPCException, InterruptedException {
		List<SpaceCenter.Fairing> fairings = naveAtual.getParts().getFairings();
		System.out.println(fairings.size() + "Tamanho da lista de fairings");
		if (fairings.size() > 0) {
			StatusJPanel.setStatus(Bundle.getString("status_jettisoning_shields"));
			for (SpaceCenter.Fairing f : fairings) {
				List<String> eventNames = f.getPart().getModules().get(0).getEvents();
				System.out.println("LISTA DE NOMES DE EVENTOS: " + eventNames);
				System.out.println(f.getJettisoned());
				if (f.getJettisoned()) {
					// Overly complicated way of getting the event from the button in the fairing
					// to jettison the fairing, since the jettison method doesn't work.
					f.getPart().getModules().get(0).triggerEvent(eventNames.get(0));
					Thread.sleep(100);
				}
			}
		}
	}

	private void createPathToTarget() throws RPCException, IOException {
		System.out.println("criando caminho");
		// PONTOREFSUPERFICIE: X: CIMA, Y: NORTE, Z: LESTE
		// criar uma lista de pontos, que tenham posicao e direcao juntos
		pontosDoCaminho.add(new Pair<>(roverPosition, roverDirection));
		// cada ponto parte do anterior, em direcao ao alvo, mas desviando de obstaculos a frente
		// saber a posicao do alvo
		targetPosition = new Vector(targetVessel.position(pontoRefSuperficie));
		// subtrair essa posicao do ponto atual
		Vector posicaoAtual = pontosDoCaminho.get(0).getValue0();
		Vector direcaoParaAlvo = targetPosition.subtract(posicaoAtual).normalize();
		// Direcao pra ser somada, do ponto do rover
		Vector direcao90Graus = new Vector(0.0, Math.sin(Math.toRadians(90)), Math.cos(Math.toRadians(90)));
		Vector direcao90GrausContra = new Vector(0.0, -Math.sin(Math.toRadians(90)), Math.cos(Math.toRadians(90)));
		System.out.println(direcaoParaAlvo);
		System.out.println(direcao90Graus);
		System.out.println((direcaoParaAlvo.sum(dirSurfToOrb(direcao90Graus))).normalize());
		// os pontos devem ficar na altura do rover
		Vector direcaoAtual = direcaoParaAlvo.sum(direcao90Graus).normalize();
		// depois de criado os pontos, gerar um poligono na tela
		drawLineBetweenPoints(posicaoAtual, direcaoParaAlvo.sum(direcao90Graus).normalize().multiply(10));
		drawLineBetweenPoints(posicaoAtual, direcaoParaAlvo.sum(direcao90GrausContra).normalize().multiply(10));
		drawLineBetweenPoints(posicaoAtual, direcaoParaAlvo.multiply(10));
	}


	private void drawLineBetweenPoints(Vector pointA, Vector pointB) throws RPCException, IOException {
		Drawing.Line line =
				drawing.addLine(posSurfToOrb(pointA).toTriplet(), posSurfToOrb(pointB).toTriplet(), pontoRefOrbital,
				                true
				               );
		line.setThickness(0.5f);
		line.setColor(new Triplet<>(1.0, 0.5, 0.0));
	}

	private void initializeParameters() {
		try {
			connection = Connection.newInstance("MechPeste - Pesterenan");
			centroEspacial = SpaceCenter.newInstance(connection);
			naveAtual = centroEspacial.getActiveVessel();
			drawing = Drawing.newInstance(connection);
			pontoRefRover = naveAtual.getReferenceFrame();
			pontoRefSuperficie = naveAtual.getSurfaceReferenceFrame();
			pontoRefOrbital = naveAtual.getOrbit().getBody().getReferenceFrame();

			roverPosition = new Vector(naveAtual.position(pontoRefSuperficie));
			roverDirection = new Vector(naveAtual.direction(pontoRefRover));
			System.out.println(roverDirection + "roverdir");
			System.out.println(new Vector(naveAtual.direction(pontoRefSuperficie)) + "supdir");
			System.out.println(transformDirection(roverDirection) + "rovtrans");

			steeringLine = drawing.addDirection(roverDirection.toTriplet(), pontoRefRover, 10, true);
			steeringLine.setColor(new Triplet<>(1.0, 0.0, 1.0));
			steeringLine.setThickness(0.4f);
		} catch (RPCException | IOException ignored) {
		}
	}

	private void setTarget() throws IOException, RPCException, InterruptedException {
		targetVessel = centroEspacial.getTargetVessel();
	}


	private Vector transformDirection(Vector vector) throws RPCException {
		return new Vector(centroEspacial.transformDirection(vector.toTriplet(), pontoRefRover, pontoRefSuperficie));
	}

	private Vector posSurfToRover(Vector vector) throws IOException, RPCException {
		return new Vector(centroEspacial.transformPosition(vector.toTriplet(), pontoRefSuperficie, pontoRefRover));
	}

	private Vector posRoverToSurf(Vector vector) throws IOException, RPCException {
		return new Vector(centroEspacial.transformPosition(vector.toTriplet(), pontoRefRover, pontoRefSuperficie));
	}

	private Vector dirSurfToOrb(Vector vector) throws IOException, RPCException {
		return new Vector(centroEspacial.transformDirection(vector.toTriplet(), pontoRefSuperficie, pontoRefOrbital));
	}

	private Vector dirRoverToSurf(Vector vector) throws IOException, RPCException {
		return new Vector(centroEspacial.transformDirection(vector.toTriplet(), pontoRefRover, pontoRefSuperficie));
	}

	private Vector posOrbToSurf(Vector vector) throws IOException, RPCException {
		return new Vector(centroEspacial.transformPosition(vector.toTriplet(), pontoRefOrbital, pontoRefSuperficie));
	}

	private Vector posSurfToOrb(Vector vector) throws IOException, RPCException {
		return new Vector(centroEspacial.transformPosition(vector.toTriplet(), pontoRefSuperficie, pontoRefOrbital));
	}
}

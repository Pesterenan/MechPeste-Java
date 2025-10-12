package com.pesterenan.controllers;

import com.pesterenan.model.ActiveVessel;
import com.pesterenan.resources.Bundle;
import com.pesterenan.utils.Attributes;
import com.pesterenan.utils.ControlePID;
import com.pesterenan.utils.Module;
import com.pesterenan.utils.Navigation;
import com.pesterenan.utils.Vector;
import com.pesterenan.views.MainGui;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Orbit;
import krpc.client.services.SpaceCenter.RCS;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;
import org.javatuples.Triplet;

public class ManeuverController extends Controller {

  enum Compare {
    INC,
    AP,
    PE
  }

  private enum RendezvousPhase {
    SETUP,
    CHECK_ANGULAR_DIFFERENCE,
    ADJUST_PERIAPSIS,
    ADJUST_APOAPSIS,
    ADJUST_UT,
    DONE
  }

  private static class RendezvousState {
    final Node maneuverNode;
    RendezvousPhase phase = RendezvousPhase.SETUP;
    double targetOrbitPosition;
    double maneuverAP;
    double maneuverPE;

    RendezvousState(Node maneuverNode) {
      this.maneuverNode = maneuverNode;
    }
  }

  public static final float CONST_GRAV = 9.81f;
  private ControlePID ctrlManeuver, ctrlRCS;

  private Navigation navigation;
  private boolean fineAdjustment;
  private double lowOrbitAltitude;
  private Stream<Float> headingErrorStream;
  private Stream<Float> pitchErrorStream;
  private Stream<Double> timeToNodeStream;

  private Stream<Float> rollErrorStream;

  private Stream<Triplet<Double, Double, Double>> remainingBurnStream;

  private double lastBurnDv = Double.MAX_VALUE;
  private final Map<String, String> commands;

  public ManeuverController(ActiveVessel vessel, Map<String, String> commands) {
    super(vessel);
    this.commands = commands;
    this.navigation = new Navigation(vessel.getConnectionManager(), vessel.getActiveVessel());
    initializeParameters();
  }

  @Override
  public void run() {
    try {
      calculateManeuver();
      if (!(commands.get(Module.FUNCTION.get()).equals(Module.RENDEZVOUS.get())
          || commands.get(Module.FUNCTION.get()).equals(Module.LOW_ORBIT.get())
          || commands.get(Module.FUNCTION.get()).equals(Module.ADJUST.get()))) {
        executeNextManeuver();
      }
    } catch (ClassCastException | InterruptedException e) {
      System.out.println("INFO: Interrompendo Controle de Manobras. Erro: " + e.getMessage());
      cleanup();
    }
  }

  public void calculateManeuver() {
    try {
      vessel.tuneAutoPilot();
      if (commands.get(Module.FUNCTION.get()).equals(Module.EXECUTE.get())) {
        return;
      }
      if (vessel.getActiveVessel().getSituation() == VesselSituation.LANDED
          || vessel.getActiveVessel().getSituation() == VesselSituation.SPLASHED) {
        throw new InterruptedException();
      }
      if (commands.get(Module.FUNCTION.get()).equals(Module.ADJUST.get())) {
        this.alignPlanesWithTargetVessel();
        return;
      }
      if (commands.get(Module.FUNCTION.get()).equals(Module.RENDEZVOUS.get())) {
        this.rendezvousWithTargetVessel();
        return;
      }
      if (commands.get(Module.FUNCTION.get()).equals(Module.LOW_ORBIT.get())) {
        biEllipticTransferToOrbit(
            lowOrbitAltitude, vessel.getActiveVessel().getOrbit().getTimeToPeriapsis());
        return;
      }
      double gravParameter = vessel.currentBody.getGravitationalParameter();
      double startingAltitutde = 0, timeUntilAltitude = 0;
      if (commands.get(Module.FUNCTION.get()).equals(Module.APOAPSIS.get())) {
        startingAltitutde = vessel.getActiveVessel().getOrbit().getApoapsis();
        timeUntilAltitude = vessel.getActiveVessel().getOrbit().getTimeToApoapsis();
      }
      if (commands.get(Module.FUNCTION.get()).equals(Module.PERIAPSIS.get())) {
        startingAltitutde = vessel.getActiveVessel().getOrbit().getPeriapsis();
        timeUntilAltitude = vessel.getActiveVessel().getOrbit().getTimeToPeriapsis();
      }

      double semiMajorAxis = vessel.getActiveVessel().getOrbit().getSemiMajorAxis();
      double currentOrbitalVelocity =
          Math.sqrt(gravParameter * ((2.0 / startingAltitutde) - (1.0 / semiMajorAxis)));
      double targetOrbitalVelocity =
          Math.sqrt(gravParameter * ((2.0 / startingAltitutde) - (1.0 / startingAltitutde)));
      double maneuverDeltaV = targetOrbitalVelocity - currentOrbitalVelocity;
      double[] deltaV = {maneuverDeltaV, 0, 0};
      createManeuver(timeUntilAltitude, deltaV);
    } catch (RPCException | InterruptedException e) {
      setCurrentStatus(Bundle.getString("status_maneuver_not_possible"));
    }
  }

  public void executeNextManeuver() throws InterruptedException {
    try {
      Node maneuverNode = vessel.getActiveVessel().getControl().getNodes().get(0);
      double burnTime = calculateBurnTime(maneuverNode);
      executeBurn(maneuverNode, burnTime);
    } catch (UnsupportedOperationException e) {
      System.err.println("Manobras ainda não desbloqueadas: " + e.getMessage());
      setCurrentStatus(Bundle.getString("status_maneuver_not_unlocked"));
    } catch (IndexOutOfBoundsException e) {
      System.err.println("Erro de index: " + e.getMessage());
      setCurrentStatus(Bundle.getString("status_maneuver_unavailable"));
    } catch (RPCException e) {
      System.err.println("Erro de Stream ou RPC: " + e.getMessage());
      setCurrentStatus(Bundle.getString("status_data_unavailable"));
    } catch (InterruptedException e) {
      System.err.println("Cancelando executar manobra");
      setCurrentStatus(Bundle.getString("status_couldnt_orient"));
      throw e;
    }
  }

  public void orientToManeuverNode(Node maneuverNode)
      throws RPCException, StreamException, InterruptedException {
    setCurrentStatus(Bundle.getString("status_orienting_ship"));
    vessel.ap.engage();
    vessel.ap.setReferenceFrame(maneuverNode.getReferenceFrame());

    // --- STAGE 1: ORIENT TO MANEUVER (PITCH/HEADING) ---
    try {
      setCurrentStatus(Bundle.getString("status_orienting_to_maneuver"));
      vessel.ap.setTargetDirection(new Triplet<>(0.0, 1.0, 0.0)); // Prograde in node's frame
      vessel.ap.setTargetRoll(Float.NaN); // Disable roll control

      final CountDownLatch directionLatch = new CountDownLatch(1);
      this.headingErrorStream = vessel.connection.addStream(vessel.ap, "getHeadingError");
      this.pitchErrorStream = vessel.connection.addStream(vessel.ap, "getPitchError");

      final Stream<Float> finalHeadingErrorStream = this.headingErrorStream;
      final Stream<Float> finalPitchErrorStream = this.pitchErrorStream;

      Runnable checkOrientation =
          () -> {
            try {
              if (directionLatch.getCount() > 0
                  && Math.abs(finalHeadingErrorStream.get()) < 1
                  && Math.abs(finalPitchErrorStream.get()) < 1) {
                directionLatch.countDown();
              }
            } catch (Exception e) {
              e.printStackTrace();
              directionLatch.countDown();
            }
          };

      this.headingErrorStream.addCallback(v -> checkOrientation.run());
      this.pitchErrorStream.addCallback(v -> checkOrientation.run());
      this.headingErrorStream.start();
      this.pitchErrorStream.start();

      if (!directionLatch.await(60, TimeUnit.SECONDS)) {
        System.err.println("Timeout waiting for direction stabilization.");
      }
    } finally {
      if (this.headingErrorStream != null) {
        this.headingErrorStream.remove();
      }
      if (this.pitchErrorStream != null) {
        this.pitchErrorStream.remove();
      }
    }

    // --- STAGE 2: STABILIZE ROLL ---
    try {
      setCurrentStatus(Bundle.getString("status_stabilizing_roll"));
      vessel.ap.setTargetRoll(0.0f);
      final CountDownLatch rollLatch = new CountDownLatch(1);
      this.rollErrorStream = vessel.connection.addStream(vessel.ap, "getRollError");
      final Stream<Float> finalRollErrorStream = this.rollErrorStream;
      final int[] rollCallbackTag = new int[1];
      rollCallbackTag[0] =
          finalRollErrorStream.addCallback(
              error -> {
                try {
                  if (Math.abs(error) < 1.0) {
                    rollLatch.countDown();
                    finalRollErrorStream.removeCallback(rollCallbackTag[0]);
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  rollLatch.countDown();
                }
              });
      this.rollErrorStream.start();
      if (!rollLatch.await(20, TimeUnit.SECONDS)) {
        System.err.println("Timeout waiting for roll stabilization.");
      }
    } finally {
      if (this.rollErrorStream != null) {
        this.rollErrorStream.remove();
      }
    }
  }

  public double calculateBurnTime(Node maneuverNode) {
    try {
      List<Engine> engines = vessel.getActiveVessel().getParts().getEngines();
      for (Engine engine : engines) {
        if (engine.getPart().getStage() == vessel.getActiveVessel().getControl().getCurrentStage()
            && !engine.getActive()) {
          engine.setActive(true);
        }
      }
    } catch (RPCException e) {
      System.err.println("Não foi possível ativar os motores." + e.getMessage());
    }
    double burnDuration = 0;
    try {
      double thrust = vessel.getActiveVessel().getAvailableThrust();
      double isp = vessel.getActiveVessel().getSpecificImpulse() * CONST_GRAV;
      double totalMass = vessel.getActiveVessel().getMass();
      double dryMass = totalMass / Math.exp(maneuverNode.getDeltaV() / isp);
      double burnRatio = thrust / isp;
      burnDuration = (totalMass - dryMass) / burnRatio;
    } catch (RPCException e) {
      System.err.println("Não foi possível calcular o tempo de queima." + e.getMessage());
    }
    setCurrentStatus("Tempo de Queima da Manobra: " + burnDuration + " segundos.");
    return burnDuration;
  }

  public void executeBurn(Node maneuverNode, double burnDuration) throws InterruptedException {
    try {
      // 1. PRE-WARP ORIENTATION
      orientToManeuverNode(maneuverNode);

      // 2. WARP AND COUNTDOWN
      // Wait until it's time to burn
      final CountDownLatch timeToBurnLatch = new CountDownLatch(1);
      timeToNodeStream = vessel.connection.addStream(maneuverNode, "getTimeTo");
      timeToNodeStream.addCallback(
          (time) -> {
            // Countdown for the last 5 seconds of warp
            double timeToWarp = time - (burnDuration / 2.0) - 30;
            if (timeToWarp > 0) {
              setCurrentStatus(String.format("Warping in: %.1f seconds...", timeToWarp));
              return;
            }
            // Countdown for ignition
            double timeToBurnStart = time - (burnDuration / 2.0);
            setCurrentStatus(
                String.format(Bundle.getString("status_maneuver_ignition_in"), timeToBurnStart));
            if (timeToBurnStart <= 0) {
              try {
                timeToBurnLatch.countDown();
                timeToNodeStream.remove();
              } catch (RPCException e) {
              }
            }
          });
      timeToNodeStream.start();

      double burnStartTime = maneuverNode.getTimeTo() - (burnDuration / 2.0) - 30;
      if (burnStartTime > 0) {
        setCurrentStatus(Bundle.getString("status_maneuver_warp"));
        vessel
            .getConnectionManager()
            .getSpaceCenter()
            .warpTo(
                (vessel.getConnectionManager().getSpaceCenter().getUT() + burnStartTime),
                100000,
                4);
      }

      // 2. ORIENT (AFTER WARP)
      orientToManeuverNode(maneuverNode);

      // 3. FINAL COUNTDOWN
      while (timeToBurnLatch.getCount() > 0) {
        if (Thread.interrupted()) throw new InterruptedException();
        if (timeToBurnLatch.await(100, TimeUnit.MILLISECONDS)) break;
      }

      // 4. EXECUTE THE BURN
      final CountDownLatch burnCompleteLatch = new CountDownLatch(1);
      remainingBurnStream =
          vessel.connection.addStream(
              maneuverNode, "remainingBurnVector", maneuverNode.getReferenceFrame());
      remainingBurnStream.addCallback(
          (burn) -> {
            try {
              double burnDvLeft = burn.getValue1();

              // SAFETY STOP: Check if dV is increasing
              if (burnDvLeft > lastBurnDv + 0.1) { // Using a 0.1m/s tolerance
                System.err.println("Maneuver failed: Delta-V increasing. Aborting burn.");
                vessel.throttle(0);
                burnCompleteLatch.countDown();
                return;
              }
              lastBurnDv = burnDvLeft; // Update last known dV

              if (burnDvLeft < (fineAdjustment ? 2 : 0.75)) {
                burnCompleteLatch.countDown();
                return;
              }
              navigation.aimAtManeuverNode(maneuverNode);
              vessel.throttle(ctrlManeuver.calculate(0, burnDvLeft / 100.0));
            } catch (Exception e) {
              e.printStackTrace();
              burnCompleteLatch.countDown();
            }
          });
      remainingBurnStream.start();

      setCurrentStatus(Bundle.getString("status_maneuver_executing"));
      burnCompleteLatch.await();
      vessel.throttle(0.0f);
      remainingBurnStream.remove();

      if (fineAdjustment) {
        adjustManeuverWithRCS(maneuverNode);
      }

      vessel.ap.setReferenceFrame(vessel.surfaceReferenceFrame);
      vessel.ap.disengage();
      vessel.getActiveVessel().getControl().setSAS(true);
      vessel.getActiveVessel().getControl().setRCS(false);
      maneuverNode.remove();
      setCurrentStatus(Bundle.getString("status_ready"));
    } catch (RPCException | StreamException e) {
      setCurrentStatus(Bundle.getString("status_data_unavailable"));
    } catch (InterruptedException e) {
      setCurrentStatus(Bundle.getString("status_maneuver_cancelled"));
      throw e;
    }
  }

  private void initializeParameters() {
    ctrlRCS = new ControlePID(vessel.getConnectionManager().getSpaceCenter(), 25);
    ctrlManeuver = new ControlePID(vessel.getConnectionManager().getSpaceCenter(), 25);
    ctrlManeuver.setPIDValues(1, 0.001, 0.1);
    ctrlManeuver.setOutput(0.1, 1.0);
    ctrlRCS.setOutput(0.5, 1.0);
    fineAdjustment = canFineAdjust(commands.get(Module.FINE_ADJUST.get()));
    try {
      lowOrbitAltitude = new Attributes().getLowOrbitAltitude(vessel.currentBody.getName());
    } catch (RPCException e) {
    }
  }

  private Node biEllipticTransferToOrbit(double targetAltitude, double timeToStart) {
    double[] totalDv = {0, 0, 0};
    try {
      Orbit currentOrbit = vessel.getActiveVessel().getOrbit();
      double startingRadius = currentOrbit.getApoapsis();
      double gravParameter = vessel.currentBody.getGravitationalParameter();
      double deltaV1 =
          Math.sqrt(2 * gravParameter / startingRadius) - Math.sqrt(gravParameter / startingRadius);
      double intermediateRadius = vessel.currentBody.getEquatorialRadius() + targetAltitude;
      double deltaV2 =
          Math.sqrt(gravParameter / intermediateRadius)
              - Math.sqrt(2 * gravParameter / intermediateRadius);
      double targetRadius = vessel.currentBody.getEquatorialRadius() + targetAltitude;
      double deltaV3 =
          Math.sqrt(2 * gravParameter / intermediateRadius)
              - Math.sqrt(gravParameter / intermediateRadius);
      double deltaV4 =
          Math.sqrt(gravParameter / targetRadius) - Math.sqrt(2 * gravParameter / targetRadius);
      totalDv[0] = deltaV1 + deltaV2 + deltaV3 + deltaV4;
    } catch (RPCException e) {
      System.err.println("ERRO: Cálculo de Transferência de Órbita" + e.getMessage());
    }
    return createManeuver(timeToStart, totalDv);
  }

  private void alignPlanesWithTargetVessel() throws InterruptedException, RPCException {
    final int[] utCallbackTag = new int[1];
    try {
      Vessel vesselObj = this.vessel.getActiveVessel();
      Orbit vesselOrbit = this.vessel.getActiveVessel().getOrbit();
      Orbit targetVesselOrbit =
          this.vessel.getConnectionManager().getSpaceCenter().getTargetVessel().getOrbit();
      if (vesselObj.getControl().getNodes().isEmpty()) {
        MainGui.newInstance().getCreateManeuverPanel().createManeuver();
      }
      java.util.List<Node> currentManeuvers = vesselObj.getControl().getNodes();
      Node currentManeuver = currentManeuvers.get(0);
      double[] incNodesUt = {
        vesselOrbit.uTAtTrueAnomaly(vesselOrbit.trueAnomalyAtAN(targetVesselOrbit)),
        vesselOrbit.uTAtTrueAnomaly(vesselOrbit.trueAnomalyAtDN(targetVesselOrbit))
      };
      boolean closestIsAN = incNodesUt[0] < incNodesUt[1];
      MainGui.newInstance()
          .getCreateManeuverPanel()
          .positionManeuverAt(closestIsAN ? "ascending" : "descending");

      ctrlManeuver.setTimeSample(25);

      final CountDownLatch latch = new CountDownLatch(1);
      utCallbackTag[0] =
          vessel.missionTime.addCallback(
              (ut) -> {
                try {
                  double currentInclination =
                      Math.toDegrees(
                          currentManeuver.getOrbit().relativeInclination(targetVesselOrbit));
                  if (currentInclination <= 0.05) {
                    latch.countDown();
                    return;
                  }
                  double ctrlOutput = ctrlManeuver.calculate(currentInclination * 100, 0);
                  currentManeuver.setNormal(
                      currentManeuver.getNormal() + (closestIsAN ? ctrlOutput : -ctrlOutput));
                } catch (Exception e) {
                  e.printStackTrace();
                  latch.countDown();
                }
              });
      latch.await(); // Wait until the alignment is done
    } catch (Exception err) {
      if (err instanceof InterruptedException) {
        throw (InterruptedException) err;
      }
      System.err.println("Error aligning planes: " + err);
    } finally {
      vessel.missionTime.removeCallback(utCallbackTag[0]);
    }
  }

  private void rendezvousWithTargetVessel() throws InterruptedException, RPCException {
    final int[] utCallbackTag = new int[1];
    try {
      List<Node> currentManeuvers = vessel.getActiveVessel().getControl().getNodes();
      Node lastManeuverNode;
      if (currentManeuvers.isEmpty()) {
        MainGui.newInstance().getCreateManeuverPanel().createManeuver();
        currentManeuvers = vessel.getActiveVessel().getControl().getNodes();
      } else {
        double lastManeuverNodeUT = 60 + currentManeuvers.get(currentManeuvers.size() - 1).getUT();
        MainGui.newInstance().getCreateManeuverPanel().createManeuver(lastManeuverNodeUT);
        currentManeuvers = vessel.getActiveVessel().getControl().getNodes();
      }
      lastManeuverNode = currentManeuvers.get(currentManeuvers.size() - 1);

      final CountDownLatch latch = new CountDownLatch(1);
      final RendezvousState state = new RendezvousState(lastManeuverNode);

      utCallbackTag[0] =
          vessel.missionTime.addCallback(
              ut -> {
                try {
                  updateRendezvousState(state, latch, utCallbackTag[0]);
                } catch (Exception e) {
                  e.printStackTrace();
                  System.err.println("Error in rendezvous update: " + e);
                  latch.countDown();
                }
              });
      latch.await();
    } catch (Exception err) {
      if (err instanceof InterruptedException) {
        throw (InterruptedException) err;
      }
      System.err.println("Error during rendezvous: " + err);
    } finally {
      vessel.missionTime.removeCallback(utCallbackTag[0]);
    }
  }

  private void updateRendezvousState(
      RendezvousState state, CountDownLatch latch, int callbackTag)
      throws RPCException, IOException {
    Orbit targetVesselOrbit =
        vessel.getConnectionManager().getSpaceCenter().getTargetVessel().getOrbit();
    ReferenceFrame currentBodyRefFrame =
        vessel.getActiveVessel().getOrbit().getBody().getNonRotatingReferenceFrame();

    switch (state.phase) {
      case SETUP:
        ctrlManeuver.setPIDValues(1, 0.001, 0.01);
        ctrlManeuver.setOutput(-100, 100);
        state.phase = RendezvousPhase.CHECK_ANGULAR_DIFFERENCE;
        break;

      case CHECK_ANGULAR_DIFFERENCE:
        double maneuverUT = state.maneuverNode.getUT();
        double angularDiff =
            calculatePhaseAngle(
                state.maneuverNode.getOrbit().positionAt(maneuverUT, currentBodyRefFrame),
                targetVesselOrbit.positionAt(maneuverUT, currentBodyRefFrame));
        if (angularDiff < 0.005) {
          state.phase = RendezvousPhase.DONE;
          break;
        }
        state.targetOrbitPosition =
            new Vector(targetVesselOrbit.positionAt(maneuverUT, currentBodyRefFrame)).magnitude();
        state.maneuverAP = state.maneuverNode.getOrbit().getApoapsis();
        state.maneuverPE = state.maneuverNode.getOrbit().getPeriapsis();

        if (state.targetOrbitPosition < state.maneuverPE) {
          state.phase = RendezvousPhase.ADJUST_PERIAPSIS;
        } else if (state.targetOrbitPosition > state.maneuverAP) {
          state.phase = RendezvousPhase.ADJUST_APOAPSIS;
        } else {
          state.phase = RendezvousPhase.ADJUST_UT;
        }
        break;

      case ADJUST_PERIAPSIS:
        if (Math.floor(state.targetOrbitPosition) == Math.floor(state.maneuverPE)) {
          state.phase = RendezvousPhase.ADJUST_UT;
          break;
        }
        state.maneuverNode.setPrograde(
            state.maneuverNode.getPrograde()
                + ctrlManeuver.calculate(
                    state.maneuverPE / state.targetOrbitPosition * 1000, 1000));
        state.maneuverPE = state.maneuverNode.getOrbit().getPeriapsis();
        break;

      case ADJUST_APOAPSIS:
        if (Math.floor(state.targetOrbitPosition) == Math.floor(state.maneuverAP)) {
          state.phase = RendezvousPhase.ADJUST_UT;
          break;
        }
        state.maneuverNode.setPrograde(
            state.maneuverNode.getPrograde()
                + ctrlManeuver.calculate(
                    state.maneuverAP / state.targetOrbitPosition * 1000, 1000));
        state.maneuverAP = state.maneuverNode.getOrbit().getApoapsis();
        break;

      case ADJUST_UT:
        double maneuverUT_adjust = state.maneuverNode.getUT();
        double angularDiff_adjust =
            calculatePhaseAngle(
                state.maneuverNode.getOrbit().positionAt(maneuverUT_adjust, currentBodyRefFrame),
                targetVesselOrbit.positionAt(maneuverUT_adjust, currentBodyRefFrame));
        state.maneuverNode.setUT(
            state.maneuverNode.getUT() + ctrlManeuver.calculate(-angularDiff_adjust * 100, 0));
        state.phase = RendezvousPhase.CHECK_ANGULAR_DIFFERENCE;
        break;

      case DONE:
        latch.countDown();
        break;
    }
  }

  private Node createManeuver(double laterTime, double[] deltaV) {
    Node maneuverNode = null;
    try {
      vessel
          .getActiveVessel()
          .getControl()
          .addNode(
              vessel.getConnectionManager().getSpaceCenter().getUT() + laterTime,
              (float) deltaV[0],
              (float) deltaV[1],
              (float) deltaV[2]);
      List<Node> currentNodes = vessel.getActiveVessel().getControl().getNodes();
      maneuverNode = currentNodes.get(currentNodes.size() - 1);
    } catch (UnsupportedOperationException | RPCException e) {
      setCurrentStatus(Bundle.getString("status_maneuver_not_possible"));
    }
    return maneuverNode;
  }

  private void cleanup() {
    try {
      if (headingErrorStream != null) headingErrorStream.remove();
      if (pitchErrorStream != null) pitchErrorStream.remove();
      if (rollErrorStream != null) rollErrorStream.remove();
      if (timeToNodeStream != null) timeToNodeStream.remove();
      if (remainingBurnStream != null) remainingBurnStream.remove();
      if (vessel.ap != null) vessel.ap.disengage();
      vessel.throttle(0);
    } catch (RPCException | NullPointerException e) {
      // ignore
    }
  }

  private void adjustManeuverWithRCS(Node maneuverNode)
      throws RPCException, StreamException, InterruptedException {
    setCurrentStatus("Fine tuning with RCS...");
    vessel.getActiveVessel().getControl().setRCS(true);
    final CountDownLatch rcsLatch = new CountDownLatch(1);

    Stream<Triplet<Double, Double, Double>> rcsStream = null;
    try {
      rcsStream =
          vessel.connection.addStream(
              maneuverNode, "remainingBurnVector", maneuverNode.getReferenceFrame());
      final Stream<Triplet<Double, Double, Double>> finalRcsStream = rcsStream;
      final int[] rcsCallbackTag = new int[1];

      rcsCallbackTag[0] =
          finalRcsStream.addCallback(
              (burn) -> {
                try {
                  double progradeDv = burn.getValue1();
                  if (progradeDv <= 0.1) {
                    rcsLatch.countDown();
                    finalRcsStream.removeCallback(rcsCallbackTag[0]);
                    return;
                  }
                  // Use the RCS PID controller to gently burn off remaining dV
                  vessel
                      .getActiveVessel()
                      .getControl()
                      .setForward((float) ctrlRCS.calculate(-progradeDv, 0));
                } catch (Exception e) {
                  e.printStackTrace();
                  rcsLatch.countDown();
                }
              });
      rcsStream.start();

      // Wait for RCS burn to complete, with a timeout
      if (!rcsLatch.await(60, TimeUnit.SECONDS)) {
        System.err.println("Timeout during RCS fine tuning.");
      }
    } finally {
      vessel.getActiveVessel().getControl().setForward(0);
      if (rcsStream != null) {
        rcsStream.remove();
      }
    }
  }

  private boolean canFineAdjust(String string) {
    if ("true".equals(string)) {
      try {
        List<RCS> rcsEngines = vessel.getActiveVessel().getParts().getRCS();
        if (rcsEngines.size() > 0) {
          for (RCS rcs : rcsEngines) {
            if (rcs.getHasFuel()) {
              return true;
            }
          }
        }
        return false;
      } catch (RPCException e) {
        System.err.println("ERRO: Ajuste fino de manobra." + e.getMessage());
      }
    }
    return false;
  }

  private double calculatePhaseAngle(
      Triplet<Double, Double, Double> startPos, Triplet<Double, Double, Double> endPos)
      throws RPCException {
    double targetPhaseAngle = 10;
    double angularDifference = 15;
    Vector startPosition = new Vector(startPos);
    Vector endPosition = new Vector(endPos);

    // Phase angle
    double dot = endPosition.dotProduct(startPosition);
    double det = endPosition.determinant(startPosition);
    targetPhaseAngle = Math.atan2(det, dot);

    double targetOrbit = endPosition.magnitude();

    double activeVesselSMA = vessel.getActiveVessel().getOrbit().getSemiMajorAxis();
    angularDifference =
        targetPhaseAngle
            + Math.PI
                * (1
                    - (1 / (2 * Math.sqrt(2)))
                        * Math.sqrt(Math.pow((activeVesselSMA / targetOrbit + 1), 3)));

    return Math.abs(angularDifference);
  }
}

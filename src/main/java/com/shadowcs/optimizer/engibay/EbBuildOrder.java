package com.shadowcs.optimizer.engibay;

import com.shadowcs.optimizer.engibay.build.EbAction;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Data
public class EbBuildOrder extends EbState {

    private double maxTime = 2.0 * 60.0 * 60.0 * 22.4;

    private int workersGoingOnGas;
    private int workersOnGas;
    private int workersGoingOnMinerals;
    private int workersOnMinerals;
    private int mulesOnMinerals;

    private int waits;
    private int badGenes;
    private int noop;

    /**
     * This is game time in simulation frames, 22.4 frames per second
     */
    private int currentFrame = 0;

    /**
     * This map contains a list of units that are in production
     */
    private final Int2IntOpenHashMap unitInProgressMap = new Int2IntOpenHashMap(100); // There are 204 unit definitions so we do slightly larger
    private final Int2IntOpenHashMap unitInUseMap = new Int2IntOpenHashMap(100); // There are 204 unit definitions so we do slightly larger

    /**
     * This map contains a list of upgrades that are in production
     */
    private final IntOpenHashSet upgradesInProgressMap = new IntOpenHashSet(20); // There are 122 upgrades so we do slightly larger just in case

    private final TreeMap<Integer, List<Runnable>> futureActions = new TreeMap<>();

    private List<EbAction> validActions = new ArrayList<>();

    public double supplyAvailableFuture() {
        return supplyAvailable() - (supplyUsedFuture() - supplyCapFuture());
    }

    public int basesFuture() {
        int bases = 0;

        for(int data: unitInProgressMap.keySet()) {
            if(techTree().unitMap().get(data).townhall()) {
                bases++;
            }
        }

        return bases + bases();
    }

    public double supplyUsedFuture() {

        double supply = 0.0;

        for(int data: unitInProgressMap.keySet()) {
            double supp = techTree().unitMap().get(data).supply();
            if(supp > 0) {
                supply += (supp * unitInProgressMap.getOrDefault(data, 0));;
            }
        }

        return supply;
    }

    public double supplyCapFuture() {

        double supply = 0.0;

        for(int data: unitInProgressMap.keySet()) {
            double supp = techTree().unitMap().get(data).supply();
            if(supp < 0) {
                supply -= (supp * unitInProgressMap.getOrDefault(data, 0));
            }
        }

        return Math.min(supply, EngineeringBay.MAX_SUPPLY);
    }

    public void addFutureAction(int frame, Runnable task) {
        futureActions.computeIfAbsent(currentFrame() + frame, k -> new ArrayList<>()).add(task);
    }

    /**
     * Execute all the actions between the current frame and the current frame + frame
     *
     * @param frame how many frames into the future are we simulating
     */
    public void executeFutureActions(int frame) {

        int lastFrame = currentFrame() + frame;

        while(getNextActionFrame() <= lastFrame) {
            executeNextAction();
        }

        collectResources(lastFrame - currentFrame());
        currentFrame(lastFrame);
    }

    public void executeNextAction() {

        var currentTasks = futureActions.pollFirstEntry();
        collectResources(currentTasks.getKey() - currentFrame());
        for (Runnable task: currentTasks.getValue()) {
            task.run();
        }
        currentFrame(currentTasks.getKey());
    }

    /**
     * This method will return the next frame that has actions or Integer.MAX_VALUE if there are no actions
     *
     * @return The next frame with actions or Integer.MAX_VALUE
     */
    public int getNextActionFrame() {
        return !futureActions.isEmpty() ? futureActions.firstKey(): Integer.MAX_VALUE;
    }

    public int getLastActionFrame() {
        return !futureActions.isEmpty() ? futureActions.lastKey(): Integer.MAX_VALUE;
    }

    public void collectResources(int delta) {
        // TODO: we may want to do a more complicated equation for resources but this should be good enough and its faster
        if(mulesOnMinerals > 0) {
            minerals(minerals() + (mulesOnMinerals * delta * Math.min(mulesOnMinerals, bases() * 8)) * EngineeringBay.MULE_MINERALS_PER_FRAME);
        }

        /*if (EngineeringBay.DEBUG) {
            System.out.println("Mining: " + workersOnMinerals);
            System.out.println("Mining: " + delta);
            System.out.println("Mining: " + Math.min(workersOnMinerals, bases() * 24));
            System.out.println("Mining: " + EngineeringBay.WORKER_MINERALS_PER_FRAME);
            System.out.println("Mining: " + (workersOnMinerals * delta * Math.min(workersOnMinerals, bases() * 24)) * EngineeringBay.WORKER_MINERALS_PER_FRAME + " IN " + delta);
        }*/

        minerals(minerals() + (delta * Math.min(workersOnMinerals, bases() * 24)) * EngineeringBay.WORKER_MINERALS_PER_FRAME);
        gas(gas() + (delta * Math.min(workersOnGas, bases() * 6)) * EngineeringBay.WORKER_GAS_PER_FRAME);
    }

    public static EbBuildOrder create(EbState state) {

        EbBuildOrder order = new EbBuildOrder();
        order.minerals(state.minerals()).gas(state.gas()).techTree(state.techTree());
        // order.currentFrame(state.currentFrame());
        order.unitCountMap().putAll(state.unitCountMap());
        order.upgradeSet().addAll(state.upgradeSet());

        return order;
    }
}

package com.shadowcs.optimizer.sc2data.genetics;

import com.google.gson.Gson;
import com.shadowcs.optimizer.genetics.Gene;
import com.shadowcs.optimizer.sc2data.models.TechTree;
import lombok.Data;

import java.util.*;

@Data
public class S2GameState implements Cloneable {

    private int maxTime;
    private int currentTime;

    private int minerals;
    private int gas;
    private S2MiningInfo miningInfo = new S2MiningInfo();
    private List<S2BaseInfo> baseInfo = new ArrayList<>();
    private int supply;
    private int totalSupply;

    private Set<Integer> upgrades = new HashSet<>(); // This is the upgrades we currently have
    private Map<Integer, Integer> unitsIdle = new HashMap<>(); // This is a count of the currently idle units

    private transient Map<Integer, Set<S2GeneAction>> inProgress = new HashMap<>();
    private transient Map<Integer, Integer> upgradeInProgress = new HashMap<>(); // This is a map of inprogress upgrades. It is upgrade id, frame done. We can do this because we can only have one of each upgrade

    private TechTree techTree;
    // TODO: store actions so we can have a decent build order...

    public boolean canPerformAction(S2GeneAction geneAction) {
        // Logic to check if an action can be performed based on the current state.
        // This might include checking if you have enough resources, if you have a unit that can perform the action, etc.

        // Check to make sure that we have the needed required parts
        var reqCond = havePrecondition(geneAction.required(), false);
        if(reqCond != null) {
            return correctBuildOrder(reqCond);
        }

        var borCond = havePrecondition(geneAction.borrowed(), true);
        if(borCond != null) {
            return correctBuildOrder(borCond);
        }

        var conCond = havePrecondition(geneAction.consumed(), true);
        if(conCond != null) {
            return correctBuildOrder(conCond);
        }

        return true;
    }

    public void processAction(S2GeneAction action) {
        if(canPerformAction(action)) {
            performAction(action);
        }


    }

    // TODO: adjust this to return a set of everything wrong instead of only one condition that is wrong
    private S2GeneAction.Condition havePrecondition(Set<S2GeneAction.Condition> conditions, boolean idle) {

        for(var check: conditions) {
            switch (check.type()) {
                case UNIT -> {
                    if((idle && unitsIdle.getOrDefault((int) check.data(), 0) > 0) || (!idle && !unitsIdle.containsKey((int) check.data()))) {
                        return check;
                    }
                }
                case RESEARCH -> {
                    if(!upgrades.contains((int) check.data())) {
                        return check;
                    }
                }
                case SUPPLY -> {
                    // We don't have enough supply either cap or current
                    if(supply < check.data() || totalSupply + check.data() > 200.0) {
                        return check;
                    }
                }
                case MINERAL -> {
                    if(minerals < check.data()) {
                        return check;
                    }
                }
                case GAS -> {
                    if(gas < check.data()) {
                        return check;
                    }
                }
                case ENERGY -> {
                    // TODO: need to figure out how to check this...
                }
                case TIME -> {
                    // We don't have enough time to make this unit
                    if(currentTime + check.data() > maxTime) {
                        return check;
                    }
                }
            }
        }

        return null;
    }

    private boolean correctBuildOrder(S2GeneAction.Condition condition) {
        // We have a condition that we need to make possible, does it just take time or do we need to add units to the build order
        // FIXME: currently we only wait for minerals or gas, everything else must be correct

        double delta = 0;
        switch (condition.type()) {
            case UNIT -> {
                return false;
            }
            case RESEARCH -> {
                return false;
            }
            case SUPPLY -> {
                if(totalSupply + condition.data() > 200.0) {
                    // We are at max supply so there is nothing we can do really atm
                    // TODO: investigate killing off our own units
                    return false;
                }

                // TODO: we need some supply added...
                return false;
            }
            case MINERAL -> {
                if(minerals < condition.data()) {
                    double temp = miningInfo.timeToGetMinerals(minerals - condition.data());
                    if(delta < temp) {
                        delta = temp;
                    }
                }
            }
            case GAS -> {
                if(gas < condition.data()) {
                    double temp = miningInfo.timeToGetGas(gas - condition.data());
                    if(delta < temp) {
                        delta = temp;
                    }
                }
            }
            case ENERGY -> {
                // TODO: need to figure out how to check this...
            }
            case TIME -> {
                // We cannot fix a time issue... at least not here, at this point its probably to late to do anything
                return false;
            }
        }

        return true;
    }

    /**
     * Search the set of actions we have available to get to the action we need. This is a depth first search starting
     * at a unit that we have and limited to a set number of steps we can take. This search also only cares about units
     * and not upgrades or resources
     */
    public List<S2GeneAction> DFBB(int startingUnit, int neededUnitId, int level) {

        // If its more than 13 steps away then we have a problem
        if(level > 13) {
            return null;
        }

        Set<List<S2GeneAction>> hold = new HashSet<>();
        for(var act: techTree.unitChildActionMap().getOrDefault(startingUnit, new HashSet<>())) {

            // Check if the action is what we actually want
            for(var prod: act.produced()) {
                if(prod.type() == S2GeneAction.ConditionType.UNIT) {
                    // We only care about the unit type
                    if((int) prod.data() == neededUnitId) {
                        List<S2GeneAction> value = new ArrayList<>();
                        value.add(act);
                        return value;
                    } else {
                        var data = DFBB((int) prod.data(), neededUnitId,level + 1);
                        if(data != null) {
                            data.add(0, act);
                            hold.add(data);
                        }
                    }
                }
            }
        }

        List<S2GeneAction> shortest = null;
        // get the shortest path to our needed unit from this point
        for(var test: hold) {
            if(shortest == null) {
                shortest = test;
            } else if(test.size() < shortest.size()) {
                shortest = test;
            }
        }

        return shortest;
    }

    public void performAction(S2GeneAction geneAction) {
        // Logic to update the game state based on the action.
        // This might include decrementing resources, creating a new unit, increasing the game tick, etc.

        // We already made sure we are able to perform the given action so we can just use it now.

        // If the produced item(s) are a research then we need to make sure we don't already have it
        for(var check: geneAction.produced()) {
            if(check.type() == S2GeneAction.ConditionType.RESEARCH && upgrades.contains((int) check.data())) {
                // We already have the research so this is a dead command that does nothing
                return;
            }
        }

        double timeDelta = 0;

        for(var consume: geneAction.consumed()) {
            switch (consume.type()) {
                case UNIT -> {
                    int count = unitsIdle.getOrDefault((int) consume.data(), 0) - 1;
                    if(count < 0) {
                        return;
                    } else if(count > 0) {
                        unitsIdle.put((int) consume.data(), count);
                    } else {
                        unitsIdle.remove((int) consume.data());
                    }
                }
                case SUPPLY -> {
                    supply -= consume.data();
                    totalSupply += consume.data();
                }
                case ENERGY -> {
                    // TODO: need to figure out how to check this...
                }
                case TIME -> {
                    // We don't have enough time to make this unit
                    timeDelta = consume.data();
                }
                case RESEARCH -> upgrades.remove((int) consume.data());
                case MINERAL -> minerals -= consume.data();
                case GAS -> gas -= consume.data();
            }
        }

        for(var consume: geneAction.borrowed()) {
            switch (consume.type()) {
                case UNIT -> {
                    int count = unitsIdle.getOrDefault((int) consume.data(), 0) - 1;
                    if(count < 0) {
                        return;
                    }

                    unitsIdle.put((int) consume.data(), count);
                }
                case SUPPLY -> {
                    supply -= consume.data();
                    totalSupply += consume.data();
                }
                case ENERGY -> {
                    // TODO: need to figure out how to check this...
                }
                case TIME -> {
                    // You can't borrow time...
                }
                case RESEARCH -> upgrades.remove((int) consume.data());
                case MINERAL -> minerals -= consume.data();
                case GAS -> gas -= consume.data();
            }
        }


        inProgress.putIfAbsent((int) timeDelta, new HashSet<>());
        inProgress.get((int) timeDelta).add(geneAction);
    }

    public void update(int delta) {

    }

    @Override
    public S2GameState clone() {
        var gson = new Gson();
        S2GameState clone = gson.fromJson(gson.toJson(this), S2GameState.class);
        return clone;
    }
}

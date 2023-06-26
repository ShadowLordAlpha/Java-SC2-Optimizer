package com.shadowcs.optimizer.engibay.old.action.unit;

import com.github.ocraft.s2client.protocol.data.Units;
import com.shadowcs.optimizer.engibay.old.EbBuildOrder;
import com.shadowcs.optimizer.engibay.EngineeringBay;
import com.shadowcs.optimizer.engibay.old.action.EbAction;
import com.shadowcs.optimizer.engibay.old.action.EbCondition;
import com.shadowcs.optimizer.engibay.old.action.EbConditionType;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class EbBuildAction implements EbAction{

    private int action;
    private String name;

    /**
     * What precondition must be present at the time of issuing the action
     */
    private Set<EbCondition> required = new HashSet<>();

    /**
     * What preconditions must be present and are used for the full duration of the action
     */
    private Set<EbCondition> borrowed = new HashSet<>();

    /**
     * What preconditions must be present and are consumed by the action
     */
    private Set<EbCondition> consumed = new HashSet<>();

    /**
     * What is created upon completion of the action
     */
    private Set<EbCondition> produced = new HashSet<>();

    public boolean isPossible(EbBuildOrder current) {
        for(var req: required) {
            if(!checkCondition(current, req, false)) {
                return false;
            }
        }

        for(var req: borrowed) {
            if(!checkCondition(current, req, false)) {
                return false;
            }
        }

        for(var req: consumed) {
            if(!checkCondition(current, req, false)) {
                return false;
            }
        }

        return true;
    }

    public boolean isValid(EbBuildOrder current) {

        for(var req: required) {
            if(!checkCondition(current, req, true)) {
                return false;
            }
        }

        for(var req: borrowed) {
            if(!checkCondition(current, req, true)) {
                return false;
            }
        }

        for(var req: consumed) {
            if(!checkCondition(current, req, true)) {
                return false;
            }
        }

        return true;
    }

    public void execute(EbBuildOrder current) {

        int time = 0;
        int supply = 0;
        Set<EbCondition> cons = new HashSet<>(consumed);
        cons.addAll(borrowed);

        if (EngineeringBay.DEBUG) {
            System.out.println("Starting " + name() + " @ " + Math.round(current.currentFrame() / 22.4) + " M: " + current.minerals() + " G; " + current.gas() + " S: " + current.supply());
        }

        for(var req: cons) {
            switch (req.type()) {
                case UNIT -> {
                    var count = current.unitCountMap().get((int) req.data()) - 1;
                    current.unitCountMap().put((int) req.data(), count);

                    // TODO: do we need to know of units in production?

                    if((int) req.data() == Units.ZERG_LARVA.getUnitTypeId()) {
                        current.addFutureAction(336, () -> {
                            if (EngineeringBay.DEBUG) {
                                System.out.println("Larva +1 @ " + Math.round(current.currentFrame() / 22.4));
                            }

                            // TODO: check larva count

                            var larva = current.unitCountMap().get(Units.ZERG_LARVA.getUnitTypeId());
                            current.unitCountMap().put(Units.ZERG_LARVA.getUnitTypeId(), larva + 1);
                        });
                    }
                }
                case SUPPLY -> {
                    supply = (int) req.data();
                    if(supply < 0) {
                        supply = 0;
                    }
                    current.activeSupply(current.activeSupply() + supply);
                }
                case MINERAL -> {
                    current.minerals(current.minerals() - req.data());
                }
                case GAS -> {
                    current.gas(current.gas() - req.data());
                }
                case ENERGY -> {
                    // something...
                }
                case TIME -> {
                    time = (int) req.data();
                }
            }
        }

        Set<EbCondition> added = new HashSet<>(produced);
        cons.addAll(borrowed);

        int finalSupply = supply;
        current.addFutureAction(time, () -> {

            current.activeSupply(current.activeSupply() - finalSupply);

            if (EngineeringBay.DEBUG) {
                System.out.println("Done " + name() + " @ " + Math.round(current.currentFrame() / 22.4) + " M: " + current.minerals() + " G; " + current.gas() + " S: " + current.supply());
            }

            for(var req: added) {
                switch (req.type()) {
                    case UNIT -> {
                        var count = current.unitCountMap().get((int) req.data()) + 1;
                        current.unitCountMap().put((int) req.data(), count);

                        if(current.techTree().unitMap().get((int) req.data()).worker()) {
                            current.workersOnMinerals(current.workersOnMinerals() + 1);
                        }
                    }
                    case RESEARCH -> {
                        current.upgradesInProgressMap().add((int) req.data());
                    }
                    case SUPPLY -> {
                        // we don't need to do anything here
                    }
                    case MINERAL -> {
                        current.minerals(current.minerals() + req.data());
                    }
                    case GAS -> {
                        current.gas(current.gas() + req.data());
                    }
                    case ENERGY -> {
                        // something...
                    }
                    case TIME -> {
                        // time doesn't matter here...
                    }
                }
            }
        });
    }

    /**
     * Check the condition to see if it is valid or not
     *
     * @param current The current state of the build order
     * @param check The condition that we are checking
     * @param checkProducing Check if we are producing the condition. This basically just means that we only need time
     *                       in order to produce instead of needing to build something
     * @return true iff the condition is satisfied.
     */
    private boolean checkCondition(EbBuildOrder current, EbCondition check, boolean checkProducing) {
        switch (check.type()) {
            case UNIT -> {
                if(checkProducing) {
                    return current.unitCountMap().containsKey((int) check.data()) || current.unitInProgressMap().containsKey((int) check.data());
                } else {
                    return (current.unitCountMap().get((int) check.data()) - current.unitInUseMap().get((int) check.data())) > 0;
                }
            }
            case RESEARCH -> {
                if(checkProducing) {
                    return current.upgradesMap().contains((int) check.data()) || current.upgradesInProgressMap().contains((int) check.data());
                } else {
                    return current.upgradesMap().contains((int) check.data());
                }
            }
            case SUPPLY -> {
                return current.supply() >= check.data();
            }
            case MINERAL -> {
                if(checkProducing) {
                    // We don't check mules here because that makes actually checking everything more complicated for if its valid
                    return current.workersOnMinerals() > 0;
                } else {
                    return current.minerals() >= check.data();
                }
            }
            case GAS -> {
                if(checkProducing) {
                    return current.workersOnGas() > 0;
                } else {
                    return current.gas() >= check.data();
                }
            }
            case ENERGY -> {
                // TODO: Actually figure out what to do with this...
                return false;
            }
            case TIME -> {
                // Time can always be considered fine and valid as its just waiting
                return true;
            }
        }

        return false;
    }

    @Override
    public int[] unitRequirements() {

        // we only care about units
        Set<EbCondition> temp = new HashSet<>(required.stream().filter(check -> check.type() == EbConditionType.UNIT).toList());
        temp.addAll(borrowed.stream().filter(check -> check.type() == EbConditionType.UNIT).toList());
        temp.addAll(consumed.stream().filter(check -> check.type() == EbConditionType.UNIT).toList());

        return temp.stream().mapToInt(c -> (int) c.data()).toArray();
    }

    @Override
    public int[] researchRequirements() {
        // we only care about units
        Set<EbCondition> temp = new HashSet<>(required.stream().filter(check -> check.type() == EbConditionType.RESEARCH).toList());
        temp.addAll(borrowed.stream().filter(check -> check.type() == EbConditionType.RESEARCH).toList());
        temp.addAll(consumed.stream().filter(check -> check.type() == EbConditionType.RESEARCH).toList());

        return temp.stream().mapToInt(c -> (int) c.data()).toArray();
    }
}

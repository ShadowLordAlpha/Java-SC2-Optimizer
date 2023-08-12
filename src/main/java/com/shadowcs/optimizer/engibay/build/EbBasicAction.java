package com.shadowcs.optimizer.engibay.build;

import com.github.ocraft.s2client.protocol.data.Units;
import com.shadowcs.optimizer.engibay.EbBuildOrder;
import com.shadowcs.optimizer.engibay.EngineeringBay;
import com.shadowcs.optimizer.engibay.build.macro.EbMineResourceAction;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * An action represents some action that the game is able to take. Note that Gas, Minerals, Food, and Energy are not
 * part of the requirements.
 *
 * @param name The name of what we are doing/making, this can be duplicated if we end up making the same building. This
 *             also doubles as what action we are doing for the action type
 * @param ability What ability if any are we using with this action. It may be a duplicate for some things and it may be
 *                INVALID (-1) as well
 * @param required What units conditions are required for the action to be taken but not used or consumed
 * @param borrowed What conditions are required for the duration of the action and then returned
 * @param consumed What conditions are consumed for the action
 * @param produced What does the action produce
 */
public record EbBasicAction(String name, int caster, String casterName, boolean upgrade, int created, String createdName, int ability, Set<EbCondition> required, Set<EbCondition> borrowed, Set<EbCondition> consumed, Set<EbCondition> produced) implements EbAction {

    /**
     * Is it possible for us to actually do this build order with the only possibly needed thing being to wait.
     *
     * @param candidate The current state of the build order
     * @return
     */
    @Override
    public boolean isValid(EbBuildOrder candidate) {

        // Because research just has to be a paint in the ass to do...
        for(var prod: produced) {
            if(prod.type() == EbConditionType.RESEARCH) {
                // Don't allow duplicate research
                if(candidate.upgradeSet().contains((int) prod.data()) || candidate.upgradesInProgressMap().contains((int) prod.data())) {
                    return false;
                }
            }
        }

        return checkFutureCondition(candidate, required) && checkFutureCondition(candidate, borrowed) && checkFutureCondition(candidate, consumed);
    }

    /**
     * Check the candidate to see if all the conditions that are needed are filled now or will be filled in the future.
     *
     * @param candidate The BuildOrder state to check
     * @param conditions The list of conditions to check
     *
     * @return true iff we will be able to execute this action according to the current state of the build order. This
     * is only based on the current state and changes in this state may affect if we are able to actually do the build
     * order or not.
     */
    private boolean checkFutureCondition(EbBuildOrder candidate, @Nullable Set<EbCondition> conditions) {

        if(conditions == null || conditions.isEmpty()) {
            return true;
        }

        for(var condition: conditions) {
            switch (condition.type()) {
                case UNIT -> {
                    if(candidate.unitCountMap().getOrDefault((int) condition.data(), 0) > 0 || candidate.unitInProgressMap().getOrDefault((int) condition.data(), 0) > 0) {
                        continue;
                    }
                    return false;
                }
                case RESEARCH -> {
                    if(candidate.upgradeSet().contains((int) condition.data()) || candidate.upgradesInProgressMap().contains((int) condition.data())) {
                        continue;
                    }
                    return false;
                }
                case SUPPLY -> {
                    // Check if we have the supply needed or if we have the needed supply in production
                    if(candidate.supplyAvailableFuture() >= condition.data()) {
                        continue;
                    }
                    return false;
                }
                case MINERAL -> {
                    // Check if we have the minerals we need or if we will eventually have the minerals we need
                    if(candidate.minerals() >= condition.data() || candidate.workersOnMinerals() > 0 || candidate.workersGoingOnMinerals() > 0) {
                        continue;
                    }
                    return false;
                }
                case GAS -> {
                    // Check if we have the gas we need, or if we will eventually have the gas we need
                    if(candidate.gas() >= condition.data() || candidate.workersOnGas() > 0 || candidate.workersGoingOnGas() > 0) {
                        continue;
                    }
                    return false;
                }
                case ENERGY -> {
                    // Energy really doesn't matter for the future as we just need the unit which is already checked
                }
                case TIME -> {
                    // Check if we will run out of time before this order can finish
                    if((candidate.currentFrame() + condition.data()) < candidate.maxTime()) {
                        continue;
                    }
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Are we able to execute the action now or do we need to wait?
     *
     * @param candidate The current state of the build order
     * @return 0 if we are able to execute now or the amount of time we need to wait until we can check again. NOTE:
     * this generally defaults to 23 but can be a much more exact time if programmed in. (I have not programmed it in as
     * this is much easier)
     */
    public int canExecute(EbBuildOrder candidate) {

        boolean canExecute = checkCondition(candidate, required, false) && checkCondition(candidate, borrowed, true) && checkCondition(candidate, consumed, true);
        return canExecute ? 0: 23;
    }

    /**
     *
     * @param candidate
     * @param conditions
     * @param available Do we need the unit available or just to exist (useful for knowing required vs borrowed differences)
     *                  This only affects Unit conditions
     * @return
     */
    private boolean checkCondition(EbBuildOrder candidate, @Nullable Set<EbCondition> conditions, boolean available) {

        if(conditions == null || conditions.isEmpty()) {
            return true;
        }

        for(var condition: conditions) {
            switch (condition.type()) {
                case UNIT -> {
                    int unitCount = candidate.unitCountMap().getOrDefault((int) condition.data(), 0);
                    if(unitCount > 0) {
                        // Do we need the unit to be available or just to exist
                        if(available) {
                            int unitInUseCount = candidate.unitInUseMap().getOrDefault((int) condition.data(), 0);
                            if((unitCount - unitInUseCount) > 0) {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                    return false;
                }
                case RESEARCH -> {
                    if(candidate.upgradeSet().contains((int) condition.data())) {
                        continue;
                    }
                    return false;
                }
                case SUPPLY -> {
                    // Check if we have the supply needed
                    if((candidate.supplyAvailable() - candidate.supplyUsedFuture()) >= condition.data()) {
                        continue;
                    }
                    return false;
                }
                case MINERAL -> {
                    // Check if we have the minerals we need
                    if(candidate.minerals() >= condition.data()) {
                        continue;
                    }
                    return false;
                }
                case GAS -> {
                    // Check if we have the gas we need
                    if(candidate.gas() >= condition.data()) {
                        continue;
                    }
                    return false;
                }
                case ENERGY -> {
                    // TODO: we will probably need to check this... somehow
                }
                case TIME -> {
                    // Time is already fully checked in the isValid method and no further check is needed
                }
            }
        }

        return true;
    }

    @Override
    public void execute(EbBuildOrder candidate) {

        // We already checked requirements, so we don't actually need to check them again.

        // Remove the borrowed things
        processAction(candidate, borrowed, false, false, false);
        int time = processAction(candidate, consumed, false, true, true);
        processAction(candidate, produced, false, false, true);

        candidate.addFutureAction(time, () -> {
            // Return borrowed things
            processAction(candidate, borrowed, true, false, false);
            processAction(candidate, produced, true, false, true);
        });
    }

    /**
     *
     * @param candidate
     * @param conditions
     * @param added
     * @param consumed
     * @param produced
     * @return
     */
    private int processAction(EbBuildOrder candidate, @Nullable Set<EbCondition> conditions, boolean added, boolean consumed, boolean produced) {

        if(conditions == null || conditions.isEmpty()) {
            return 0;
        }

        if(added && consumed) {
            throw new IllegalArgumentException("Conditions may not be added and consumed at the same time");
        }

        double multiple = added ? 1: -1;
        int time = 0;
        for(var condition: conditions) {
            switch (condition.type()) {
                case UNIT -> {
                    if(produced) {
                        if (added) {
                            candidate.unitInProgressMap().addTo((int) condition.data(), -1);
                            candidate.unitCountMap().addTo((int) condition.data(), 1);

                            if(Units.TERRAN_SCV.getUnitTypeId() == ((int) condition.data())
                                    || Units.ZERG_DRONE.getUnitTypeId() == ((int) condition.data())
                                    || Units.PROTOSS_PROBE.getUnitTypeId() == ((int) condition.data())) {

                                candidate.workersGoingOnMinerals(candidate.workersGoingOnMinerals() + 1);
                                candidate.addFutureAction(90, () -> {
                                    candidate.workersGoingOnMinerals(candidate.workersGoingOnMinerals() - 1);
                                    candidate.workersOnMinerals(candidate.workersOnMinerals() + 1);
                                });
                            } else if(Units.ZERG_EXTRACTOR.getUnitTypeId() == ((int) condition.data())
                                    || Units.PROTOSS_ASSIMILATOR.getUnitTypeId() == ((int) condition.data())
                                    || Units.TERRAN_REFINERY.getUnitTypeId() == ((int) condition.data())) {

                                // We automatically assign 3 workers
                                int count = Math.max(0, Math.min(3, candidate.workersOnMinerals() - 1));
                                candidate.workersOnMinerals(candidate.workersOnMinerals() - count);
                                candidate.workersGoingOnGas(candidate.workersGoingOnGas() + count);
                                candidate.addFutureAction(90, () -> {
                                    candidate.workersGoingOnGas(candidate.workersGoingOnGas() - count);
                                    candidate.workersOnGas(candidate.workersOnGas() + count);
                                });
                            }
                        } else if(consumed) {
                            candidate.unitCountMap().addTo((int) condition.data(), -1);

                            // Automatically start a new larva up for the one we just used
                            if(Units.ZERG_LARVA.getUnitTypeId() == ((int) condition.data())) {
                                candidate.unitInProgressMap().addTo((int) condition.data(), 1);
                                candidate.addFutureAction(336, () -> {
                                    if (EngineeringBay.DEBUG) {
                                        System.out.println("Larva +1 @ " + Math.round(candidate.currentFrame() / 22.4));
                                    }

                                    // TODO: check natural larva count per base
                                    candidate.unitInProgressMap().addTo((int) condition.data(), -1);
                                    var larva = candidate.unitCountMap().get(Units.ZERG_LARVA.getUnitTypeId());
                                    if(larva < (candidate.bases() * 3)) {
                                        candidate.unitCountMap().addTo(Units.ZERG_LARVA.getUnitTypeId(), 1);
                                    }
                                });
                            }
                        } else {
                            candidate.unitInProgressMap().addTo((int) condition.data(), 1);
                        }
                    } else {
                        // If we are using a worker we need to remove them from minerals or gas if no mineral one left
                        if(!added) {
                            if(Units.PROTOSS_PROBE.getUnitTypeId() == ((int) condition.data())
                                    || Units.ZERG_DRONE.getUnitTypeId() == ((int) condition.data())
                                    || Units.TERRAN_SCV.getUnitTypeId() == ((int) condition.data())) {

                                if(candidate.workersOnMinerals() > 0) {
                                    candidate.workersOnMinerals(candidate.workersOnMinerals() - 1);
                                } else if(candidate.workersOnGas() > 0) {
                                    candidate.workersOnGas(candidate.workersOnGas() - 1);
                                } else {
                                    System.out.println("ERROR: NO WORKER");
                                }
                            }
                        }

                        // Probes basically start back up right away
                        if(Units.PROTOSS_PROBE.getUnitTypeId() == ((int) condition.data())) {
                            if(!added) {
                                // Probes are only occupied for a little bit and not the full time
                                candidate.unitInUseMap().addTo((int) condition.data(), (int) multiple);
                                candidate.addFutureAction(90, () -> {
                                    candidate.unitInUseMap().addTo(Units.PROTOSS_PROBE.getUnitTypeId(), -1);

                                    candidate.workersGoingOnMinerals(candidate.workersGoingOnMinerals() + 1);
                                    candidate.addFutureAction(90, () -> {
                                        candidate.workersGoingOnMinerals(candidate.workersGoingOnMinerals() - 1);
                                        candidate.workersOnMinerals(candidate.workersOnMinerals() + 1);
                                    });
                                });
                            }
                        } else {
                            // Drones basically just die mostly but not always (extractor trick) and scvs are just in use the whole time
                            if(Units.TERRAN_SCV.getUnitTypeId() == ((int) condition.data()) || Units.ZERG_DRONE.getUnitTypeId() == ((int) condition.data())) {
                                if(added) {
                                    candidate.workersGoingOnMinerals(candidate.workersGoingOnMinerals() + 1);
                                    candidate.addFutureAction(90, () -> {
                                        candidate.workersGoingOnMinerals(candidate.workersGoingOnMinerals() - 1);
                                        candidate.workersOnMinerals(candidate.workersOnMinerals() + 1);
                                    });
                                }
                            }
                            candidate.unitInUseMap().addTo((int) condition.data(), (int) multiple);
                        }
                    }
                }
                case RESEARCH -> {
                    if(produced) {
                        if (added) {
                            candidate.upgradesInProgressMap().remove((int) condition.data());
                            candidate.upgradeSet().add((int) condition.data());
                        } else if(consumed) {
                            throw new IllegalArgumentException("RESEARCH cannot be Consumed");
                        } else {
                            candidate.upgradesInProgressMap().add((int) condition.data());
                        }
                    } else {
                        throw new IllegalArgumentException("RESEARCH cannot be Borrowed");
                    }
                }
                case SUPPLY -> {
                    // As this is automatically calculated we don't actually need to do anything here
                }
                case MINERAL -> {
                    // Remove the needed gas
                    // TODO: may want to add in some kind of check for borrowed resources as well for isValid check?
                    candidate.minerals(candidate.minerals() + (multiple * condition.data()));

                }
                case GAS -> {
                    // Remove the needed gas
                    // TODO: may want to add in some kind of check for borrowed resources as well for isValid check?
                    candidate.gas(candidate.gas() + (multiple * condition.data()));
                }
                case ENERGY -> {
                    // TODO: will probably need to do something about energy... need to figure this out
                    if(!produced) {
                        throw new IllegalArgumentException("ENERGY cannot be Borrowed");
                    }
                }
                case TIME -> {
                    if(produced) {
                        if(!added) {
                            time += condition.data();
                        } else {
                            throw new IllegalArgumentException("TIME cannot be Produced");
                        }
                    } else {
                        throw new IllegalArgumentException("TIME cannot be Borrowed");
                    }
                }
            }
        }

        return time;
    }
}

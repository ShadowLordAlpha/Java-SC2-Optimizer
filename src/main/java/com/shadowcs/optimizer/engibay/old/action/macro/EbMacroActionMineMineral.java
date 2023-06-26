package com.shadowcs.optimizer.engibay.old.action.macro;

import com.shadowcs.optimizer.engibay.old.EbBuildOrder;
import com.shadowcs.optimizer.engibay.EngineeringBay;
import lombok.extern.slf4j.Slf4j;

/**
 * Move workers from mining gas to mining minerals. The number is at maximum 3 if PULL_THREE_WORKERS is set otherwise it
 * is one.
 */
@Slf4j
public class EbMacroActionMineMineral implements EbMacroAction {

    @Override
    public String name() {
        return "Mine Minerals";
    }

    @Override
    public boolean isPossible(EbBuildOrder current) {
        // Valid and possible are identical for this action
        return isValid(current);
    }

    @Override
    public boolean isValid(EbBuildOrder current) {

        // We can only move workers from gas to minerals. Everything else should automatically go to minerals
        if(current.workersOnGas() > 0) {
            return true;
        }

        return false;
    }

    @Override
    public void execute(EbBuildOrder current) {

        int moving = EngineeringBay.PULL_THREE_WORKERS ? Math.min(current.workersOnGas(), 3): 1;

        current.workersOnGas(current.workersOnGas() - moving);

        current.addFutureAction(45, () -> {

            if(EngineeringBay.DEBUG) {
                log.debug("Mining: +{} Workers go Minerals", moving);
            }

            current.workersOnMinerals(current.workersOnMinerals() + moving);
        });
    }
}

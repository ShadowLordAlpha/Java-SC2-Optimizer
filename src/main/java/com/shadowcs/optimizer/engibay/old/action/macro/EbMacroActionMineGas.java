package com.shadowcs.optimizer.engibay.old.action.macro;

import com.github.ocraft.s2client.protocol.data.Units;
import com.shadowcs.optimizer.engibay.old.EbBuildOrder;
import com.shadowcs.optimizer.engibay.EngineeringBay;
import lombok.extern.slf4j.Slf4j;

/**
 * Move workers from mining minerals to mining gas. The number is at maximum 3 if PULL_THREE_WORKERS is set otherwise it
 * is one.
 */
@Slf4j
public class EbMacroActionMineGas implements EbMacroAction {

    @Override
    public String name() {
        return "Mine Gas";
    }

    @Override
    public boolean isPossible(EbBuildOrder current) {
        return isValid(current);
    }

    @Override
    public boolean isValid(EbBuildOrder current) {

        // We can only move workers from minerals to gas. Everything else should automatically go to minerals
        if(current.workersOnMinerals() == 0) {
            return false;
        }

        if(!current.unitCountMap().containsKey(Units.ZERG_EXTRACTOR.getUnitTypeId()) && !current.unitCountMap().containsKey(Units.PROTOSS_ASSIMILATOR.getUnitTypeId()) && !current.unitCountMap().containsKey(Units.TERRAN_REFINERY.getUnitTypeId())) {
            return false;
        }

        int count = current.unitCountMap().get(Units.ZERG_EXTRACTOR.getUnitTypeId()) + current.unitCountMap().get(Units.PROTOSS_ASSIMILATOR.getUnitTypeId()) + current.unitCountMap().get(Units.TERRAN_REFINERY.getUnitTypeId());

        return current.workersOnGas() < count * 3;
    }

    @Override
    public void execute(EbBuildOrder current) {

        int moving = EngineeringBay.PULL_THREE_WORKERS ? Math.min(current.workersOnMinerals(), 3): 1;

        current.workersOnMinerals(current.workersOnMinerals() - moving);
        current.workersOnGas(current.workersOnGas() + moving);

        // TODO: actually take time
        /*current.addFutureAction(45, () -> {

            if(EngineeringBay.DEBUG) {
                log.debug("Mining: +{} Workers to Gas", moving);
            }

            current.workersOnGas(current.workersOnGas() + moving);
        });*/
    }
}

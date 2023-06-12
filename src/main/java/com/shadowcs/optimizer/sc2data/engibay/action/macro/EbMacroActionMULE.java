package com.shadowcs.optimizer.sc2data.engibay.action.macro;

import com.github.ocraft.s2client.protocol.data.Units;
import com.shadowcs.optimizer.sc2data.engibay.EbBuildOrder;

public class EbMacroActionMULE implements EbMacroAction {

    @Override
    public String name() {
        return "Drop MULE";
    }

    @Override
    public boolean isPossible(EbBuildOrder current) {
        return isValid(current);
    }

    @Override
    public boolean isValid(EbBuildOrder current) {

        // TODO: we need to be able to track energy and cooldown of units
        // A mule costs 50 energy

        if(current.unitCountMap().containsKey(Units.TERRAN_ORBITAL_COMMAND.getUnitTypeId()) && current.mulesOnMinerals() < (current.bases() * 8)) {
            return true;
        }

        return false;
    }

    @Override
    public void execute(EbBuildOrder current) {

        // TODO: do we want to add the MULE as a unit we keep track of or just keep it as the mineral
        current.mulesOnMinerals(current.mulesOnMinerals() + 1);
        current.addFutureAction(1434, () -> current.mulesOnMinerals(current.mulesOnMinerals() - 1));
    }
}

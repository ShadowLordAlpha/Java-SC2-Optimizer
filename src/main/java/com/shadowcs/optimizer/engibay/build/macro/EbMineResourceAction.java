package com.shadowcs.optimizer.engibay.build.macro;

import com.github.ocraft.s2client.protocol.data.Units;
import com.shadowcs.optimizer.engibay.EbBuildOrder;
import com.shadowcs.optimizer.engibay.build.EbAction;

public record EbMineResourceAction(boolean gas) implements EbAction {

    @Override
    public boolean isValid(EbBuildOrder candidate) {

        int workerCount = candidate.unitCountMap().getOrDefault(Units.ZERG_DRONE.getUnitTypeId(), 0) + candidate.unitCountMap().getOrDefault(Units.TERRAN_SCV.getUnitTypeId(), 0) + candidate.unitCountMap().getOrDefault(Units.PROTOSS_PROBE.getUnitTypeId(), 0);
        if(gas) {
            int baseCount = candidate.basesFuture();
            int extractorCount = candidate.unitCountMap().getOrDefault(Units.ZERG_EXTRACTOR.getUnitTypeId(), 0)
                    + candidate.unitCountMap().getOrDefault(Units.TERRAN_REFINERY.getUnitTypeId(), 0)
                    + candidate.unitCountMap().getOrDefault(Units.PROTOSS_ASSIMILATOR.getUnitTypeId(), 0)
                    + candidate.unitCountMap().getOrDefault(Units.ZERG_EXTRACTOR_RICH.getUnitTypeId(), 0)
                    + candidate.unitCountMap().getOrDefault(Units.TERRAN_REFINERY_RICH.getUnitTypeId(), 0)
                    + candidate.unitCountMap().getOrDefault(Units.PROTOSS_ASSIMILATOR_RICH.getUnitTypeId(), 0)
                    + candidate.unitInProgressMap().getOrDefault(Units.ZERG_EXTRACTOR.getUnitTypeId(), 0)
                    + candidate.unitInProgressMap().getOrDefault(Units.TERRAN_REFINERY.getUnitTypeId(), 0)
                    + candidate.unitInProgressMap().getOrDefault(Units.PROTOSS_ASSIMILATOR.getUnitTypeId(), 0)
                    + candidate.unitInProgressMap().getOrDefault(Units.ZERG_EXTRACTOR_RICH.getUnitTypeId(), 0)
                    + candidate.unitInProgressMap().getOrDefault(Units.TERRAN_REFINERY_RICH.getUnitTypeId(), 0)
                    + candidate.unitInProgressMap().getOrDefault(Units.PROTOSS_ASSIMILATOR_RICH.getUnitTypeId(), 0);

            int current = candidate.workersOnGas() + candidate.workersGoingOnGas();
            return (workerCount - current) > 0 && current < Math.min(baseCount * 2, extractorCount);
        } else {
            int baseCount = candidate.basesFuture();
            int current = candidate.workersOnMinerals() + candidate.workersGoingOnMinerals();
            return (workerCount - current) > 0 && current < (baseCount * 3);
        }
    }

    @Override
    public int canExecute(EbBuildOrder candidate) {

        int workerCount = candidate.unitCountMap().getOrDefault(Units.ZERG_DRONE.getUnitTypeId(), 0) + candidate.unitCountMap().getOrDefault(Units.TERRAN_SCV.getUnitTypeId(), 0) + candidate.unitCountMap().getOrDefault(Units.PROTOSS_PROBE.getUnitTypeId(), 0);
        workerCount -= candidate.unitInUseMap().getOrDefault(Units.ZERG_DRONE.getUnitTypeId(), 0) + candidate.unitInUseMap().getOrDefault(Units.TERRAN_SCV.getUnitTypeId(), 0) + candidate.unitInUseMap().getOrDefault(Units.PROTOSS_PROBE.getUnitTypeId(), 0);

        if(gas) {
            int baseCount = candidate.bases();
            int extractorCount = candidate.unitCountMap().getOrDefault(Units.ZERG_EXTRACTOR.getUnitTypeId(), 0)
                    + candidate.unitCountMap().getOrDefault(Units.TERRAN_REFINERY.getUnitTypeId(), 0)
                    + candidate.unitCountMap().getOrDefault(Units.PROTOSS_ASSIMILATOR.getUnitTypeId(), 0)
                    + candidate.unitCountMap().getOrDefault(Units.ZERG_EXTRACTOR_RICH.getUnitTypeId(), 0)
                    + candidate.unitCountMap().getOrDefault(Units.TERRAN_REFINERY_RICH.getUnitTypeId(), 0)
                    + candidate.unitCountMap().getOrDefault(Units.PROTOSS_ASSIMILATOR_RICH.getUnitTypeId(), 0);

            int current = candidate.workersOnGas() + candidate.workersGoingOnGas();
            if((workerCount - current) > 0 && current < Math.min(baseCount * 2, extractorCount)) {
                return 0;
            }
        } else {
            int baseCount = candidate.bases();
            int current = candidate.workersOnMinerals() + candidate.workersGoingOnMinerals();
            if((workerCount - current) > 0 && current < (baseCount * 3)) {
                return 0;
            }
        }

        return 23;
    }

    @Override
    public void execute(EbBuildOrder candidate) {

        int workerCount = candidate.unitCountMap().getOrDefault(Units.ZERG_DRONE.getUnitTypeId(), 0) + candidate.unitCountMap().getOrDefault(Units.TERRAN_SCV.getUnitTypeId(), 0) + candidate.unitCountMap().getOrDefault(Units.PROTOSS_PROBE.getUnitTypeId(), 0);
        int current = candidate.workersOnMinerals() + candidate.workersGoingOnMinerals() + candidate.workersOnGas() + candidate.workersGoingOnGas();

        boolean allBusy = workerCount <= current;

        if(gas) {
            if(allBusy) {
                candidate.workersOnMinerals(candidate.workersOnMinerals() - 1);
            }
            candidate.workersGoingOnGas(candidate.workersGoingOnGas() + 1);
            candidate.addFutureAction(90, () -> {
                candidate.workersGoingOnGas(candidate.workersGoingOnGas() - 1);
                candidate.workersOnGas(candidate.workersOnGas() + 1);
            });
        } else {
            if(allBusy) {
                candidate.workersOnGas(candidate.workersOnGas() - 1);
            }
            candidate.workersGoingOnMinerals(candidate.workersGoingOnMinerals() + 1);
            candidate.addFutureAction(90, () -> {
                candidate.workersGoingOnMinerals(candidate.workersGoingOnMinerals() - 1);
                candidate.workersOnMinerals(candidate.workersOnMinerals() + 1);
            });
        }
    }

    @Override
    public String name() {
        if(gas) {
            return "Mine Gas";
        } else {
            return "Mine Mineral";
        }
    }
}

package com.shadowcs.optimizer.build.genetics.info;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import lombok.Data;

@Data
public class BuildUnitInfo {

    private final UnitType type;
    private final UnitType addon;
    private int units;
    private int busy;

    public int availableUnits() {
        if(addon == Units.TERRAN_REACTOR) {
            return units() - busy() / 2;
        } else {
            return units() - busy();
        }
    }
}

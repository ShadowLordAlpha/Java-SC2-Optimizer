package com.shadowcs.optimizer.build;

import com.github.ocraft.s2client.protocol.data.UnitType;
import lombok.Data;

@Data
public class BuildUnitInfo {

    private final UnitType type;
    private final UnitType addon;
    private int units;
    private int busy;
}

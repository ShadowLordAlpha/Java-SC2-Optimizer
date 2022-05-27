package com.shadowcs.optimizer.build.genetics;

import com.shadowcs.optimizer.sc2data.models.AbilityS2Data;
import com.shadowcs.optimizer.sc2data.models.UnitS2Data;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A Build order Gene represents one action taken during a build order
 */
@Data
@EqualsAndHashCode(cacheStrategy= EqualsAndHashCode.CacheStrategy.LAZY)
public class BuildOrderGene {

    /**
     * What unit is casting the ability.
     */
    private final UnitS2Data caster;

    /**
     * What ability are we using
     */
    private final AbilityS2Data ability;

    /**
     * Should we activate a chrono on the start of this unit?
     *
     * TODO, may need to not be final
     */
    private final boolean chrono;
}

package com.shadowcs.optimizer.build.genetics;

import com.shadowcs.optimizer.sc2data.models.AbilityS2Data;
import com.shadowcs.optimizer.sc2data.models.UnitS2Data;
import com.shadowcs.optimizer.sc2data.models.UpgradeS2Data;
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

    // These should basically always be the same for the same ability and are here as an optimization, as such they can
    // be ignored for the most part other than needing to be set
    @EqualsAndHashCode.Exclude private final UnitS2Data unitCreated;
    @EqualsAndHashCode.Exclude private final UpgradeS2Data upgradeResearched;

    /**
     * Should we activate a chrono on the start of this unit?
     *
     * TODO, may need to not be final
     */
    private final boolean chrono;
}

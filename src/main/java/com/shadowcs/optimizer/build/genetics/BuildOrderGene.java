package com.shadowcs.optimizer.build.genetics;

import com.shadowcs.optimizer.genetics.Gene;
import com.shadowcs.optimizer.sc2data.models.AbilityS2Data;
import com.shadowcs.optimizer.sc2data.models.UnitS2Data;
import com.shadowcs.optimizer.sc2data.models.UpgradeS2Data;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

/**
 * A Build order Gene represents one action taken during a build order
 */
@Data
@EqualsAndHashCode(cacheStrategy= EqualsAndHashCode.CacheStrategy.LAZY)
public class BuildOrderGene {

    /**
     * What unit is casting the ability.
     *
     * TODO: no longer needed? it may also cause issues if we keep it as we generally only have one gene. Maybe change this to a set instead
     */
    private final Gene caster;

    /**
     * These are possible alternates that can do the command, These are NEVER morph targets
     */
    private final Set<Gene> alternate = new HashSet<>();

    /**
     * What ability are we using
     *
     * TODO: I believe this is only ever one ability, I also don't think its needed anymore...
     */
    private final AbilityS2Data ability;

    // These should basically always be the same for the same ability and are here as an optimization, as such they can
    // be ignored for the most part other than needing to be set
    @EqualsAndHashCode.Exclude private final UnitS2Data unitCreated;
    @EqualsAndHashCode.Exclude private final UpgradeS2Data upgradeResearched;

    /**
     * Should we activate a chrono on the start of this unit?
     *
     * TODO, may need to not be final? may not want at all and simply make a sudo gene/command for buildings instead (that sounds like the better idea tbh)
     */
    private final boolean chrono;
}

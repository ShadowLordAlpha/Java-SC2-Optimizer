package com.shadowcs.optimizer.build;

import com.shadowcs.optimizer.sc2data.models.AbilityS2Data;
import lombok.Data;

/**
 * A Build order Gene represents one action taken during a build order
 */
@Data
public class BuildOrderGene {

    // TODO: we will need to eventually do something about chrono
    private AbilityS2Data ability;
}

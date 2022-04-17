package com.shadowcs.optimizer.build;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.shadowcs.optimizer.genetics.Chromosome;
import com.shadowcs.optimizer.genetics.Genetics;
import com.shadowcs.optimizer.sc2data.models.AbilityS2Data;
import com.shadowcs.optimizer.sc2data.models.UnitS2Data;
import com.shadowcs.optimizer.sc2data.models.UpgradeS2Data;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BuildOrderGenetics implements Genetics<BuildOrderGene> {

    private final BuildState state;
    private final LoadingCache<Integer, UnitS2Data> unitS2DataMap;
    private final LoadingCache<Integer, UnitS2Data> abilityToUnitS2DataMap;
    private final LoadingCache<Integer, UpgradeS2Data> upgradeS2Data;
    private final LoadingCache<Integer, AbilityS2Data> abilityS2Data;

    @Override
    public List<BuildOrderGene> available(Chromosome<BuildOrderGene> chromo, int index) {



        return null;
    }
}

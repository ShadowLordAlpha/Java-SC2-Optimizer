package com.shadowcs.optimizer.build.genetics;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.ocraft.s2client.protocol.data.Units;
import com.google.common.util.concurrent.AtomicDouble;
import com.shadowcs.optimizer.build.BuildOrderGene;
import com.shadowcs.optimizer.build.BuildState;
import com.shadowcs.optimizer.genetics.Chromosome;
import com.shadowcs.optimizer.genetics.Genetics;
import com.shadowcs.optimizer.pojo.Pair;
import com.shadowcs.optimizer.sc2data.models.AbilityS2Data;
import com.shadowcs.optimizer.sc2data.models.UnitS2Data;
import com.shadowcs.optimizer.sc2data.models.UpgradeS2Data;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@Data
@Slf4j
public class BuildOrderGenetics implements Genetics<BuildOrderGene> {

    private final BuildState state;
    private final LoadingCache<Integer, UnitS2Data> unitS2DataMap;
    private final LoadingCache<Integer, UnitS2Data> abilityToUnitS2DataMap;
    private final LoadingCache<Integer, UpgradeS2Data> upgradeS2Data;
    private final LoadingCache<Integer, AbilityS2Data> abilityS2Data;
    private final LoadingCache<Pair<Integer, Integer>, BuildOrderGene> abilityToOrder; // = Caffeine.newBuilder().build(key -> new BuildOrderGene().ability(abilityS2Data.get(key)));

    public BuildOrderGenetics(BuildState state, Set<UnitS2Data> units, Set<UpgradeS2Data> upgrades, Set<AbilityS2Data> abilities) {
        this.state = state;

        unitS2DataMap = Caffeine.newBuilder().build(key -> units.stream().filter(w -> w.id() == key).findFirst().orElse(null));
        abilityToUnitS2DataMap = Caffeine.newBuilder().build(key -> units.stream().filter(w -> w.buildAbility() == key).findFirst().orElse(null));

        upgradeS2Data = Caffeine.newBuilder().build(key -> upgrades.stream().filter(w -> w.id() == key).findFirst().orElse(null));
        abilityS2Data = Caffeine.newBuilder().build(key -> abilities.stream().filter(w -> w.id() == key).findFirst().orElse(null));

        abilityToOrder = Caffeine.newBuilder().build(key -> new BuildOrderGene(unitS2DataMap.get(key.first()), abilityS2Data.get(key.second()), false));
    }

    @Override
    public Set<BuildOrderGene> available(Chromosome<BuildOrderGene> chromo, int index) {

        Set<BuildOrderGene> genes = new HashSet<>();
        Set<Integer> tenologyUnit = new HashSet<>();
        tenologyUnit.add(0); // no tech needed
        AtomicDouble food = new AtomicDouble(0);

        // Unit ID, Count
        LoadingCache<Integer, Integer> unitCount = Caffeine.newBuilder().build(key -> 0);

        // These are the units we started with, we can't calculate what tech we have yet
        state.unitInfoMap().asMap().keySet().forEach(key -> {
            int count = unitCount.get(key.getUnitTypeId()) + 1;
            unitCount.put(key.getUnitTypeId(), count);

            UnitS2Data math = unitS2DataMap.get(key.getUnitTypeId());
            food.addAndGet(math.food());
        });

        // These are the commands we need to follow, because all of these should already be confirmed we can assume they
        // are correct. We need to take one command less than the index we are going to
        chromo.geneList().stream().limit(index).forEach(key -> {
            if (key != null && key.ability() != null) {
                UnitS2Data created = abilityToUnitS2DataMap.get(key.ability().id());
                UnitS2Data caster = key.caster();
                AbilityS2Data ability = key.ability();

                if (Pattern.compile(Pattern.quote("morph"), Pattern.CASE_INSENSITIVE).matcher(ability.name()).find() || caster.id() == Units.ZERG_DRONE.getUnitTypeId()) {
                    // Morphed units always use up whatever was morphed
                    int count = unitCount.get(caster.id()) - 1;
                    unitCount.put(caster.id(), count);
                    food.addAndGet(-caster.food());
                }

                int count = unitCount.get(created.id()) + 1;
                unitCount.put(created.id(), count);
                food.addAndGet(created.food());
            }
        });

        // Setup tech stuff
        Set<Integer> keySet = unitCount.asMap().keySet();
        keySet.forEach(key -> {
            if(unitCount.get(key) > 0) {
                UnitS2Data tech = unitS2DataMap.get(key);
                tenologyUnit.add(tech.id());
                tenologyUnit.addAll(tech.techAliases());
            } else {
                keySet.remove(key);
            }
        });

        // TODO: take addons into consideration as well, to make sure we don't make more then we can actually make...

        keySet.forEach(key -> {
            for(Integer ability: unitS2DataMap.get(key).abilities()) {

                UnitS2Data data = abilityToUnitS2DataMap.get(ability);
                if (data != null && tenologyUnit.contains(abilityToUnitS2DataMap.get(ability).techRequirement())) {
                    // Check that we have the food needed to make the thing
                    if (data.food() >= 0 || food.get() >= -data.food()) {
                        Pair<Integer, Integer> aKey = new Pair<>(key, ability);
                        genes.add(abilityToOrder.get(aKey));
                    }
                }
            }
        });

        return genes;
    }

    @Override
    public void validate(Chromosome<BuildOrderGene> chromo) {
        Set<BuildOrderGene> genes = new HashSet<>();
        Set<Integer> tenologyUnit = new HashSet<>();
        tenologyUnit.add(0); // no tech needed
        AtomicDouble food = new AtomicDouble(0);

        // Unit ID, Count
        LoadingCache<Integer, Integer> unitCount = Caffeine.newBuilder().build(key -> 0);

        // These are the units we started with, we can't calculate what tech we have yet
        state.unitInfoMap().asMap().keySet().forEach(key -> {
            int count = unitCount.get(key.getUnitTypeId()) + 1;
            unitCount.put(key.getUnitTypeId(), count);

            UnitS2Data math = unitS2DataMap.get(key.getUnitTypeId());
            food.addAndGet(math.food());
        });

        Set<Integer> keySet = unitCount.asMap().keySet();

        // We need to loop through each gene in the list
        boolean morphe = true;
        for(int i = 0; i < chromo.geneList().size(); i++) {

            // What tech do we have available, we may be able to calculate but we will need to use a map instead
            if(morphe) {
                tenologyUnit.clear();
                tenologyUnit.add(0);
                Set<Integer> keys = new HashSet<>();
                keySet.forEach(key -> {
                    if (unitCount.get(key) > 0) {
                        keys.add(key);
                        UnitS2Data tech = unitS2DataMap.get(key);
                        tenologyUnit.add(tech.id());
                        tenologyUnit.addAll(tech.techAliases());
                    }
                });

                // Get our actions that are currently valid, we need the full tech list though so this has to happen after
                genes.clear();
                keys.forEach(key -> {
                    for (Integer ability : unitS2DataMap.get(key).abilities()) {
                        // We only want cancel or build type abilities
                        UnitS2Data data = abilityToUnitS2DataMap.get(ability);
                        if (data != null && tenologyUnit.contains(data.techRequirement())) {
                            // Check that we have the food needed to make the thing
                            if (data.food() >= 0 || food.get() >= -data.food()) {
                                Pair<Integer, Integer> aKey = new Pair<>(key, ability);
                                genes.add(abilityToOrder.get(aKey));
                            }
                        }
                    }
                });
            }
            morphe = false;

            // Is this command valid? if not replace it with one of the others that is at random
            BuildOrderGene key = chromo.geneList().get(i);
            if(key == null || key.ability() == null || !genes.contains(key)) {
                key = new ArrayList<>(genes).get(ThreadLocalRandom.current().nextInt(genes.size()));
                chromo.geneList().set(i, key);
            }

            UnitS2Data created = abilityToUnitS2DataMap.get(key.ability().id());
            UnitS2Data caster = key.caster();
            AbilityS2Data ability = key.ability();

            if(Pattern.compile(Pattern.quote("morph"), Pattern.CASE_INSENSITIVE).matcher(ability.name()).find() || caster.id() == Units.ZERG_DRONE.getUnitTypeId()) {
                // Morphed units always use up whatever was morphed
                int count = unitCount.get(caster.id()) - 1;
                unitCount.put(caster.id(), count);
                food.addAndGet(- caster.food());

                if(count <= 0) {
                    // If this is set to true we need to recalculate most everything, luckily we don't need to do it much
                    morphe = true;
                }
            }

            int count = unitCount.get(created.id()) + 1;
            unitCount.put(created.id(), count);
            food.addAndGet(created.food());

            // We can bypass this if we are just going to recalculate everything anyway
            if(!morphe) {
                // Add our new unit to the tech map
                UnitS2Data tech = unitS2DataMap.get(created.id());
                tenologyUnit.add(tech.id());
                tenologyUnit.addAll(tech.techAliases());

                // Add our new units abilities to this map as well
                for (Integer abilityCheck : unitS2DataMap.get(tech.id()).abilities()) {
                    // We only want cancel or build type abilities
                    UnitS2Data data = abilityToUnitS2DataMap.get(abilityCheck);
                    if (data != null && tenologyUnit.contains(abilityToUnitS2DataMap.get(abilityCheck).techRequirement())) {
                        // Check that we have the food needed to make the thing
                        if (data.food() >= 0 || food.get() >= -data.food()) {
                            Pair<Integer, Integer> aKey = new Pair<>(tech.id(), abilityCheck);
                            genes.add(abilityToOrder.get(aKey));
                        }
                    }
                }
            }
        }
    }
}

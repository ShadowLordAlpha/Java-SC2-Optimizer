package com.shadowcs.optimizer.build.genetics;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.ocraft.s2client.protocol.data.Units;
import com.google.common.util.concurrent.AtomicDouble;
import com.shadowcs.optimizer.build.state.BuildState;
import com.shadowcs.optimizer.genetics.Chromosome;
import com.shadowcs.optimizer.genetics.Genetics;
import com.shadowcs.optimizer.pojo.LoadingHashMap;
import com.shadowcs.optimizer.pojo.Pair;
import com.shadowcs.optimizer.sc2data.models.AbilityS2Data;
import com.shadowcs.optimizer.sc2data.models.UnitS2Data;
import com.shadowcs.optimizer.sc2data.models.UpgradeS2Data;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
@Slf4j
public class BuildOrderGenetics implements Genetics<BuildOrderGene> {

    private static final Pattern pattern = Pattern.compile(Pattern.quote("morph"), Pattern.CASE_INSENSITIVE);
    private final BuildState state;
    private final Map<Integer, UnitS2Data> unitS2DataMap;
    private final Map<Integer, UnitS2Data> abilityToUnitS2DataMap;
    private final Map<Integer, UpgradeS2Data> upgradeS2Data;
    private final Map<Integer, AbilityS2Data> abilityS2Data;
    private final LoadingHashMap<Pair<Integer, Integer>, BuildOrderGene> abilityToOrder; // = Caffeine.newBuilder().build(key -> new BuildOrderGene().ability(abilityS2Data.get(key)));

    public BuildOrderGenetics(BuildState state, Map<Integer, UnitS2Data> unitS2DataMap, Map<Integer, UnitS2Data> abilityToUnitS2DataMap, Set<UpgradeS2Data> upgrades, Set<AbilityS2Data> abilities) {
        this.state = state;

        this.unitS2DataMap = unitS2DataMap;
        this.abilityToUnitS2DataMap = abilityToUnitS2DataMap;

        upgradeS2Data = new HashMap<>();
        upgradeS2Data.putAll(upgrades.stream().collect(Collectors.toMap(UpgradeS2Data::id, x -> x)));

        abilityS2Data = new HashMap<>();
        abilityS2Data.putAll(abilities.stream().collect(Collectors.toMap(AbilityS2Data::id, x -> x)));

        abilityToOrder = new LoadingHashMap<>(key -> new BuildOrderGene(unitS2DataMap.get(key.first()), abilityS2Data.get(key.second()), false));
    }

    /**
     * This should be fast enough as it "shouldn't" run nearly as often or as much as the 64+ times that validate runs,
     * with this one we also already assume that the previous commands are correct removing a lot of the need for
     * validation of commands.
     *
     * @param chromo
     * @param index
     * @return
     */
    @Override
    public Set<BuildOrderGene> available(Chromosome<BuildOrderGene> chromo, int index) {

        Set<BuildOrderGene> genes = new HashSet<>();
        Set<Integer> tenologyUnit = new HashSet<>();
        tenologyUnit.add(0); // no tech needed

        double food = 0;

        // Unit ID, Count
        LoadingHashMap<Integer, Integer> unitCount = new LoadingHashMap<>(key -> 0);

        // These are the units we started with, we can't calculate what tech we have yet
        for(var entry: state.unitInfoMap().entrySet()) {
            var entryUID = entry.getKey().getUnitTypeId();
            int count = unitCount.get(entryUID) + 1;
            unitCount.put(entryUID, count);

            UnitS2Data math = unitS2DataMap.get(entryUID);
            food += math.food() * count;
        }

        // These are the commands we need to follow, because all of these should already be confirmed we can assume they
        // are correct. We need to take one command less than the index we are going to
        for(int i = 0; i < index; i++) {
            var key = chromo.geneList().get(i);
            if (key != null && key.ability() != null) {
                UnitS2Data created = abilityToUnitS2DataMap.get(key.ability().id());
                UnitS2Data caster = key.caster();
                AbilityS2Data ability = key.ability();

                if (/*pattern.matcher(ability.name()).find()*/ability.name().contains("Morph") || caster.id() == Units.ZERG_DRONE.getUnitTypeId()) {
                    // Morphed units always use up whatever was morphed
                    int count = unitCount.get(caster.id()) - 1;
                    unitCount.put(caster.id(), count);
                    food -= caster.food();
                }

                int count = unitCount.get(created.id()) + 1;
                unitCount.put(created.id(), count);
                food += created.food();
            }
        }

        // Setup tech stuff
        Set<Integer> keySet = unitCount.keySet();
        keySet.removeIf(key -> {
            if(unitCount.get(key) > 0) {
                UnitS2Data tech = unitS2DataMap.get(key);
                tenologyUnit.add(tech.id());
                tenologyUnit.addAll(tech.techAliases());
                return false;
            } else {
                return true;
            }
        });

        // TODO: take addons into consideration as well, to make sure we don't make more then we can actually make...
        // TODO food max

        double finalFood = food;
        keySet.forEach(key -> {
            for(Integer ability: unitS2DataMap.get(key).abilities()) {

                UnitS2Data data = abilityToUnitS2DataMap.get(ability);
                if (data != null && tenologyUnit.contains(abilityToUnitS2DataMap.get(ability).techRequirement())) {
                    // Check that we have the food needed to make the thing
                    if (data.food() >= 0 || finalFood >= -data.food()) {
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

        LoadingHashMap<BuildOrderGene, AtomicInteger> genes = new LoadingHashMap<>(key -> new AtomicInteger(0));
        LoadingHashMap<Integer, AtomicInteger> tenologyUnit = new LoadingHashMap<>(key -> new AtomicInteger(0));

        // Unit ID, Count
        LoadingHashMap<Integer, Integer> unitCount = new LoadingHashMap<>(key -> 0);

        double food = 0;

        // These are the units we started with, we can't calculate what tech we have yet
        for(var entrySet: state.unitInfoMap().entrySet()) {
            int entryUID = entrySet.getKey().getUnitTypeId();
            int count = unitCount.get(entrySet.getKey().getUnitTypeId());
            for(var cTemp: entrySet.getValue()) {
                count += cTemp.units();
            }

            unitCount.put(entryUID, count);

            var unit = unitS2DataMap.get(entryUID);

            final int finalCount = count;
            tenologyUnit.get(unit.id()).addAndGet(finalCount);
            unit.techAliases().forEach(tech -> tenologyUnit.get(tech).addAndGet(finalCount));

            for (Integer ability : unit.abilities()) {
                // We only want cancel or build type abilities
                UnitS2Data data = abilityToUnitS2DataMap.get(ability);
                if (data != null) {
                    Pair<Integer, Integer> aKey = new Pair<>(unit.id(), ability);
                    genes.get(abilityToOrder.get(aKey)).addAndGet(count);
                }
            }

            food += unit.food() * count;
        }

        //long start = System.nanoTime();

        // We need to loop through each gene in the list
        for(int i = 0; i < chromo.geneList().size(); i++) {

            // Is this command valid? if not replace it with one of the others that is at random
            BuildOrderGene key = chromo.geneList().get(i);

            boolean ramoize = false;
            // Check if the command is valid
            if(key == null || key.ability() == null || !genes.containsKey(key)) {
                ramoize = true;
            } else {
                // The gene is in our list, now make sure its valid...
                UnitS2Data data = abilityToUnitS2DataMap.get(key.ability().id());
                if(genes.get(key).get() > 0 && data != null && (data.food() >= 0 || food >= Math.abs(data.food())) && (data.techRequirement() == 0 || tenologyUnit.get(data.techRequirement()).get() > 0)) {
                    // TODO: invert???
                } else {
                    ramoize = true;
                }
            }

            // We need to pick a valid gene
            if(ramoize) {
                // Get the current valid genes
                Set<BuildOrderGene> validGenes = new HashSet<>();

                for(BuildOrderGene gene: genes.keySet()) {
                    UnitS2Data data = abilityToUnitS2DataMap.get(gene.ability().id());
                    if(genes.get(gene).get() > 0 && data != null && (data.food() >= 0 || food >= Math.abs(data.food())) && (data.techRequirement() == 0 || tenologyUnit.get(data.techRequirement()).get() > 0)) {
                        validGenes.add(gene);
                    }
                }

                //log.info("new Gene stuff...");
                int index = ThreadLocalRandom.current().nextInt(validGenes.size());
                var itt = validGenes.iterator();
                for(int idx = 0; idx < index; idx++) {
                    itt.next();
                }
                key = itt.next();
                chromo.geneList().set(i, key);
            }

            UnitS2Data created = abilityToUnitS2DataMap.get(key.ability().id());
            UnitS2Data caster = key.caster();
            AbilityS2Data ability = key.ability();

            if(ability.name().contains("Morph") || caster.id() == Units.ZERG_DRONE.getUnitTypeId()) {
                // Morphed units always use up whatever was morphed
                int count = unitCount.get(caster.id()) - 1;
                unitCount.put(caster.id(), count);
                food -= caster.food();

                // Remove a count of what we are doing
                tenologyUnit.get(caster.id()).decrementAndGet();
                caster.techAliases().forEach(techk -> tenologyUnit.get(techk).decrementAndGet());

                for (Integer abilityId : caster.abilities()) {
                    Pair<Integer, Integer> aKey = new Pair<>(caster.id(), abilityId);
                    genes.get(abilityToOrder.get(aKey)).decrementAndGet();
                }
            }

            int count = unitCount.get(created.id()) + 1;
            unitCount.put(created.id(), count);
            food += created.food();

            UnitS2Data tech = unitS2DataMap.get(created.id());

            tenologyUnit.get(tech.id()).incrementAndGet();
            tech.techAliases().forEach(techk -> tenologyUnit.get(techk).incrementAndGet());

            for (Integer abilityId : created.abilities()) {
                Pair<Integer, Integer> aKey = new Pair<>(created.id(), abilityId);
                genes.get(abilityToOrder.get(aKey)).incrementAndGet();
            }

            //long start2 = System.nanoTime();
            //log.info("Loop times Area {} ", start2 - start);
        }

        //long start4 = System.nanoTime();
        //log.info("Finding Area Total {}", start4 - start);
    }
}

package com.shadowcs.optimizer.build.genetics;

import com.github.ocraft.s2client.protocol.data.Units;
import com.shadowcs.optimizer.build.genetics.info.BuildUnitInfo;
import com.shadowcs.optimizer.build.state.BuildState;
import com.shadowcs.optimizer.genetics.Chromosome;
import com.shadowcs.optimizer.genetics.Genetics;
import com.shadowcs.optimizer.pojo.LoadingHashMap;
import com.shadowcs.optimizer.sc2data.models.AbilityS2Data;
import com.shadowcs.optimizer.sc2data.models.UnitS2Data;
import com.shadowcs.optimizer.sc2data.models.UpgradeS2Data;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 *
 */
@Data
@Slf4j
public class BuildOrderGenetics implements Genetics<BuildOrderGene> {
    private final BuildState state;
    private final Map<Integer, UnitS2Data> unitS2DataMap;
    private final Map<Integer, UnitS2Data> abilityToUnitS2DataMap;
    private final Map<Integer, UpgradeS2Data> upgradeS2Data;
    private final Map<Integer, AbilityS2Data> abilityS2Data;
    private final LoadingHashMap<Long, BuildOrderGene> abilityToOrder;

    public BuildOrderGenetics(BuildState state, Map<Integer, UnitS2Data> unitS2DataMap, Map<Integer, UnitS2Data> abilityToUnitS2DataMap, Set<UpgradeS2Data> upgrades, Set<AbilityS2Data> abilities) {
        this.state = state;

        this.unitS2DataMap = unitS2DataMap;
        this.abilityToUnitS2DataMap = abilityToUnitS2DataMap;

        upgradeS2Data = new HashMap<>(upgrades.size() + 1, 1.0f);
        upgradeS2Data.putAll(upgrades.stream().collect(Collectors.toMap(UpgradeS2Data::id, x -> x)));

        abilityS2Data = new HashMap<>(abilities.size() + 1, 1.0f);
        abilityS2Data.putAll(abilities.stream().collect(Collectors.toMap(AbilityS2Data::id, x -> x)));

        abilityToOrder = new LoadingHashMap<>(key -> new BuildOrderGene(unitS2DataMap.get(((int)(key >> 32))), abilityS2Data.get(key.intValue()), abilityToUnitS2DataMap.get(key.intValue()), null, false));
    }

    /**
     * We assume that the chromosome up until index is correct and the only thing that we need to find out is the set of
     * actions that we are able to take at the index.
     *
     * @param chromo
     * @param index
     *
     * @return
     */
    @Override
    public Set<BuildOrderGene> available(Chromosome<BuildOrderGene> chromo, int index) {

        return availidate(new BuildOrderGeneticsState().init(), chromo, 0, index);
    }

    @Override
    public void validate(Chromosome<BuildOrderGene> chromo) {

        BuildOrderGeneticsState state = new BuildOrderGeneticsState().init();

        int count = chromo.geneList().size();
        for(int i = 0; i < count; i++) {
            Set<BuildOrderGene> genes = availidate(state, chromo, Math.max(i - 1, 0), i);

            if(!genes.contains(chromo.geneList().get(i))) {

                int index = ThreadLocalRandom.current().nextInt(genes.size());
                var itt = genes.iterator();
                for(int idx = 0; idx < index; idx++) {
                    itt.next();
                }
                var key = itt.next();

                chromo.geneList().set(i, key);
            }
        }
    }

    private Set<BuildOrderGene> availidate(BuildOrderGeneticsState bos, Chromosome<BuildOrderGene> chromo, int start, int end) {

        bos.processChromosome(chromo, start, end);

        bos.calculateAvailableAbilitySet();
        return bos.activeGenes();
    }

    @Data
    private class BuildOrderGeneticsState {

        // Unit ID and Count of units
        private Map<Integer, BuildUnitInfo> unitCount = new HashMap<>();

        private Set<Integer> technologyUnit = new HashSet<>(128);

        // What genes are currently allowed
        private Set<BuildOrderGene> genes = new HashSet<>(256);
        private Set<BuildOrderGene> activeGenes = new HashSet<>(256);

        // total food we have "available" for use
        private double availableFood = 0;

        // How much food have we used overall, must be a positive number
        private double usedFood = 0;

        private double abilityLowestFood = 0;
        private boolean abilityChanged = true;
        private boolean gasCreated = false;

        BuildOrderGeneticsState init() {

            unitCount.clear();
            technologyUnit.clear();

            availableFood = 0;
            usedFood = 0;

            // Insert BuildState values into lists, this is always correct as it is the current state of the game
            for(var entry: state.unitInfoMap().entrySet()) {
                var entryUID = entry.getKey().getUnitTypeId();

                int count = entry.getValue().units();

                unitCount.computeIfAbsent(entryUID, key -> new BuildUnitInfo(Units.from(key))).units(count).addonTechlab(entry.getValue().addonTechlab()).addonReactor(entry.getValue().addonReactor());

                UnitS2Data math = unitS2DataMap.get(entryUID);
                double foodCount = math.food() * count;
                availableFood += foodCount;
                if(math.food() < 0) { // If negative we have used some food up
                    usedFood -= foodCount;
                }
            }

            regenerateTechnologySet();
            regenerateAbilitySet();

            return this;
        }

        BuildOrderGeneticsState processChromosome(Chromosome<BuildOrderGene> chromo, int start, int index) {

            // Insert the Chromosome data for all the steps we care about
            for(int i = start; i < index; i++) {
                var bog = chromo.geneList().get(i);
                if(bog != null && bog.ability() != null) {
                    UnitS2Data created = bog.unitCreated();
                    UnitS2Data caster = bog.caster();
                    AbilityS2Data ability = bog.ability();

                    if (ability.name().contains("Morph") || ability.name().contains("Cancel") || caster.id() == Units.ZERG_DRONE.getUnitTypeId()) {
                        // Morphed units always use up whatever was morphed
                        var casterHold = unitCount.computeIfAbsent(caster.id(), key -> new BuildUnitInfo(Units.from(key)));
                        casterHold.units(unitCount.get(caster.id()).units() - 1); // We already must have a unit in order to cancel it
                        if(casterHold.units() == 0) {
                            // regenerateTechnologySet(); // unless tech dies we never need to do this
                            // regenerateAbilitySet();
                            removeAbilityUnit(casterHold);
                        }
                        availableFood -= caster.food();
                        if(caster.food() < 0) { // If negative we have used some food up
                            usedFood += caster.food();
                        }
                    }

                    //log.info("Created {}", created);
                    //log.info("Created check {} {} {}", created.food(), availableFood, usedFood);
                    var createdHold = unitCount.computeIfAbsent(created.id(), key -> new BuildUnitInfo(Units.from(key)));
                    if(createdHold.units() == 0) {
                        addAbilityUnit(createdHold);
                        addTechnologyUnit(createdHold);
                    }
                    createdHold.units(unitCount.get(created.id()).units() + 1);
                    availableFood += created.food();
                    if(created.food() < 0) { // If negative we have used some food up
                        usedFood -= created.food();
                    }
                    //log.info("After   check {} {} {}", created.food(), availableFood, usedFood);
                }
            }

            return this;
        }

        private void regenerateTechnologySet() {

            technologyUnit().clear();
            technologyUnit().add(0);

            // Calculate what tech we have access to
            // Because we do this after counting units up we are able to use a simple set as we only care about one more step and not several more
            // Overall this may make some things a little slower with the merged methods but its worth it for the simplicity of the overall code
            for(var key: unitCount().values()) {
                if (key.units() > 0) {
                    addTechnologyUnit(key);
                }
            }
        }

        private void addTechnologyUnit(BuildUnitInfo info) {
            UnitS2Data tech = unitS2DataMap.get(info.type().getUnitTypeId());
            technologyUnit.add(tech.id());
            technologyUnit.addAll(tech.techAliases());

            abilityChanged = true;
            // TODO: does this need the techlabs as well? probably TBH...
        }

        private void regenerateAbilitySet() {

            genes().clear();

            // TODO: take addons into consideration as well, to make sure we don't make more then we can actually make...
            // TODO food max;

            for(var key: unitCount().values()) {
                if(key.units() > 0) {
                    addAbilityUnit(key);
                }
            }
        }

        private void removeAbilityUnit(BuildUnitInfo info) {

            var unitKey = unitS2DataMap.get(info.type().getUnitTypeId());
            for (int ability : unitKey.abilities()) {

                UnitS2Data data = abilityToUnitS2DataMap.get(ability);
                if(data != null) {
                    abilityChanged = true;
                    long l = (((long) info.type().getUnitTypeId()) << 32) | (ability & 0xffffffffL);
                    var abil = abilityToOrder.get(l);
                    genes.remove(abil);
                    activeGenes.remove(abil);
                }
            }
        }

        private void addAbilityUnit(BuildUnitInfo info) {

            var unitKey = unitS2DataMap.get(info.type().getUnitTypeId());
            for (int ability : unitKey.abilities()) {

                UnitS2Data data = abilityToUnitS2DataMap.get(ability);
                if(data != null) {
                    //if(checkValid(data)) {
                        abilityChanged = true;
                        long l = (((long) info.type().getUnitTypeId()) << 32) | (ability & 0xffffffffL);
                        genes.add(abilityToOrder.get(l));
                    //}
                    // TODO: may need to move failures into another list... or something like that at least
                }
            }
        }

        void calculateAvailableAbilitySet() {

            // We have enough food for everything, so we don't need to worry about anything...
            //log.info("Food c {} {} {}", abilityLowestFood, availableFood(), abilityChanged);
            if(availableFood() >= abilityLowestFood && !abilityChanged) {
                return;
            }

            if(availableFood() < abilityLowestFood) {
                //log.info("Purging data");
                abilityLowestFood = 0;
                activeGenes().removeIf(test -> {
                    var created = test.unitCreated();
                    // Check that we have the food needed to make the thing
                    if (checkFood(created)) {
                        abilityLowestFood = Math.max(-created.food(), abilityLowestFood);
                        return false;
                    }

                    return true;
                });
            }

            // We have gained more food
            if(availableFood() > abilityLowestFood || abilityChanged) {
                // TODO: something in here...
                //log.info("adding data");

                // activeGenes(new HashSet<>(128));

                abilityLowestFood = 0;
                for(var test: genes) {
                    var created = test.unitCreated();
                    // Check that we have the food needed to make the thing
                    if (checkFood(created) && checkValid(created)) {
                        activeGenes.add(test);
                        abilityLowestFood = Math.max(-created.food(), abilityLowestFood);
                    }
                }

                abilityChanged = false;
            }
        }

        private boolean checkValid(UnitS2Data created) {
            return !BuildConstants.addon.contains(created.id()) && (technologyUnit().contains(created.techRequirement()));
        }

        private boolean checkFood(UnitS2Data created) {
            // log.info("Food {} {} {}", created.food(), availableFood(), usedFood());
            return created.food() >= 0 || (availableFood() >= -created.food() && (usedFood() + -created.food() <= 200));
        }
    }
}

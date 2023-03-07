package com.shadowcs.optimizer.build.genetics;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.shadowcs.optimizer.genetics.Gene;
import com.shadowcs.optimizer.random.XORShiftRandom;
import com.shadowcs.optimizer.sc2data.models.AbilityS2Data;
import com.shadowcs.optimizer.sc2data.models.UnitS2Data;
import com.shadowcs.optimizer.sc2data.models.UpgradeS2Data;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;

/**
 *
 */
@Data
@Slf4j
public class BuildOrderGenetics implements Function<Set<Gene>, Gene> {

    private final Random random = new XORShiftRandom();
    private final List<Gene> geneList = new ArrayList<>();


    /**
     * Generate the list of all Genes that we need for the given race(s). This will filter down the full lists to only
     * the ones we care about. Extra Genes for specific cases may also be created to make it easier on us later.
     *
     * @param units The list of all units in a game of SC2
     * @param upgrades The list of all upgrades in a game of SC2
     * @param abilities The list of all abilities in a game of SC2
     * @param race What specific races do we care about
     */
    public BuildOrderGenetics(Set<UnitS2Data> units, Set<UpgradeS2Data> upgrades, Set<AbilityS2Data> abilities, Race...race) {

        // Convert the array of races to a set to make it easier to use
        var validRace = new HashSet<>(List.of(race));

        // Because units are the only data object that has race we need to start with those
        var validUnits = new HashSet<>(units);
        validUnits.removeIf(unit -> !validRace.contains(unit.race()));

        // we now need to filter the abilities based on the units that we have
        Set<Integer> abilityIdSet = new HashSet<>();
        validUnits.forEach(u -> abilityIdSet.addAll(u.abilities()));
        var validAbilities = new HashSet<>(abilities);
        validAbilities.removeIf(ability -> !abilityIdSet.contains(ability.id()));

        // after that we are now able to do our upgrades...
        var validUpgrades = new HashSet<>(upgrades);
        validUpgrades.removeIf(upgrade -> {
            var abil = abilities.stream().filter(ad -> ad.id() == upgrade.buildAbility()).findFirst().orElse(null);
            if(abil == null) {
                return true;
            }

            if(validAbilities.stream().anyMatch(ab -> abil.id() == ab.id() || abil.generalId() == ab.id())) {
                validAbilities.add(abil);
                return false;
            }

            return true;
        });

        // Put all the valid IDs into a map for easy and faster lookup later?
        /*Map<Integer, AbilityS2Data> abilityIdMap = new HashMap<>();

        // Unit Genes
        validUnits.forEach(u -> {

        });

        // Upgrade Genes
        HashMap<UpgradeS2Data, Gene>;
        validUpgrades.forEach(u -> {
            new Gene();
        });*/

        if(log.isDebugEnabled()) {
            log.debug("=====< Valid Units >=====");
            validUnits.forEach(u -> log.debug(u.name()));
            log.debug("=====< Valid Upgrades >=====");
            validUpgrades.forEach(u -> log.debug(u.name()));
            log.debug("=====< Valid Abilities >=====");
            validAbilities.forEach(a -> log.debug(a.name()));
        }
    }

    /**
     * TODO: old, need to remove
     *
     * Generate the needed genes that are valid for us to make use of. Currently restricted to one race, in the future
     * we may want to expand this as zerg can capture units and that may be fun to use
     *
     * @param
     * @param abilityS2DataMap
     * @param abilityToUnitS2DataMap
     */
    public BuildOrderGenetics(Set<UnitS2Data> unitS2Data, Set<UpgradeS2Data> upgrades, Map<Integer, AbilityS2Data> abilityS2DataMap, Map<Integer, UnitS2Data> abilityToUnitS2DataMap, Race race) {

        // If the unit is not the correct race we don't care about it... that sounds so wrong and its only in text
        Set<UnitS2Data> care = new HashSet<>(unitS2Data);
        care.removeIf(data -> data.race() != race);

        // For every unit we care about make the initial gene object and store it so that we can modify it when we go
        // through the list again to generate the actual build tree.
        Gene nexus = null;
        Gene techlabBarracks = null;
        Gene techlabFactory = null;
        Gene techlabStarport = null;
        Map<UnitS2Data, Gene> unitGene = new HashMap<>();
        Set<Integer> abilityData = new HashSet<>();

        for(UnitS2Data unit: care) {

            // We need at least one basic gene per unit
            unitGene.put(unit, new Gene());
            if(unit.name().equalsIgnoreCase("nexus")) {
                nexus = unitGene.get(unit);
            } else if(unit.name().equalsIgnoreCase("BarracksTechLab")) {
                techlabBarracks = unitGene.get(unit);
            } else if(unit.name().equalsIgnoreCase("FactoryTechLab")) {
                techlabFactory = unitGene.get(unit);
            } else if(unit.name().equalsIgnoreCase("StarportTechLab")) {
                techlabStarport = unitGene.get(unit);
            }

            // For each ability within the unit
            // At this point this is specifically so we can generate the upgrade data
            abilityData.addAll(unit.abilities());
        }

        Set<UpgradeS2Data> careUpgrade = new HashSet<>(upgrades);
        careUpgrade.removeIf(data -> !abilityData.contains(data.buildAbility()) && !abilityData.contains(abilityS2DataMap.get(data.buildAbility()).generalId()));

        for(UpgradeS2Data upgrade: careUpgrade) {

            var ability = abilityS2DataMap.get(upgrade.buildAbility());
            if(ability.generalId() != 0) {
                // There is a general ability, we should use that one
                ability = abilityS2DataMap.get(ability.generalId());
            }

            UnitS2Data caster = null;
            for(UnitS2Data unit: care) {

                // For each ability within the unit
                // At this point this is specifically so we can generate the upgrade data
                for (int abil : unit.abilities()) {

                    if(ability.id() == abil) {
                        caster = unit;
                    }
                }
            }

            if(caster != null) {
                Set<Gene> need = new HashSet<>();
                need.add(unitGene.get(caster));

                BuildOrderGene bog = new BuildOrderGene(unitGene.get(caster), ability, null, upgrade, false);
                Gene upgradeGene = new Gene().data(bog);
                upgradeGene.needed().add(need);

                geneList.add(upgradeGene);
            }
        }

        // We now need to loop through and add the previous levels to the needed upgrades
        for(var gene: geneList) {

            BuildOrderGene bog = gene.data();
            UpgradeS2Data us2d = bog.upgradeResearched();
            if(us2d != null) {

                String lookfor = null;
                if (us2d.name().toLowerCase().contains("level2")) {
                    lookfor = us2d.name().replace('2', '1');
                } else if(us2d.name().toLowerCase().contains("level3")) {
                    lookfor = us2d.name().replace('3', '2');
                }

                if(lookfor != null) {
                    for(var gene2: geneList) {
                        BuildOrderGene bog2 = gene2.data();
                        UpgradeS2Data us2d2 = bog.upgradeResearched();
                        if(us2d2 != null && us2d2.name().equalsIgnoreCase(lookfor)) {
                            // We will always need the previous level gene so just go ahead and add it to all lists
                            gene.needed().forEach(l -> l.add(gene2));
                        }
                    }
                }
            }
        }


        // For every unit we care about
        for(UnitS2Data unit: care) {

            // For each ability within the unit
            for(int ability: unit.abilities()) {

                // For each unit
                var created = abilityToUnitS2DataMap.get(ability);
                var aData = abilityS2DataMap.get(ability);
                if(created != null) {

                    Gene gene = unitGene.get(created);

                    if(gene.data() == null) {
                        Gene creator = unitGene.get(unit);

                        BuildOrderGene bog = new BuildOrderGene(creator, aData, created, null, false);
                        bog.alternate().add(creator);

                        gene.data(bog);
                    } else {
                        Gene creator = unitGene.get(unit);

                        BuildOrderGene bog = gene.data();
                        bog.alternate().add(creator);
                    }



                    switch (created.name().toLowerCase()) {
                        case "thor":
                            created.requireAttached(true);
                            created.techRequirement(Units.TERRAN_ARMORY.getUnitTypeId());
                            break;
                        case "ghost":
                            created.requireAttached(true);
                            created.techRequirement(Units.TERRAN_GHOST_ACADEMY.getUnitTypeId());
                            break;
                        case "battlecruiser":
                            created.requireAttached(true);
                            created.techRequirement(Units.TERRAN_FUSION_CORE.getUnitTypeId());
                            break;
                    }

                    Set<Gene> needed = new HashSet<>();
                    needed.add(unitGene.get(unit));

                    if(created.requireAttached()) {

                        if(created.techRequirement() == 5) {
                            created.techRequirement(0);
                        }

                        // We need a techlab of some kind to build this unit

                        if(unit.name().equalsIgnoreCase("Factory")) {
                            needed.add(techlabFactory);
                        } else if(unit.name().equalsIgnoreCase("Starport")) {
                            needed.add(techlabStarport);
                        } else if(unit.name().equalsIgnoreCase("Barracks")) {
                            needed.add(techlabBarracks);
                        }
                    }

                    if(created.techRequirement() != 0) {
                        for(var testGene: unitGene.keySet()) {
                            if(testGene.id() == created.techRequirement() || testGene.techAliases().contains(created.techRequirement())) {
                                Set<Gene> alias = new HashSet<>(needed);
                                alias.add(unitGene.get(testGene));
                                gene.needed().add(alias);
                            }
                        }
                    } else {
                        gene.needed().add(needed);
                    }

                    geneList.add(gene);
                }
            }
        }
    }

    @Override
    public Gene apply(Set<Gene> genes) {
        return geneList.get(random.nextInt(geneList().size()));
    }
}

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

                BuildOrderGene bog = new BuildOrderGene(caster, ability, null, upgrade, false);
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

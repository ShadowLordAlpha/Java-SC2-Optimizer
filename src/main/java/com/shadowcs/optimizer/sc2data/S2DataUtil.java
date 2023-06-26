package com.shadowcs.optimizer.sc2data;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.google.gson.Gson;
import com.shadowcs.optimizer.engibay.old.action.EbAction;
import com.shadowcs.optimizer.genetics.Gene;
import com.shadowcs.optimizer.engibay.old.action.EbCondition;
import com.shadowcs.optimizer.engibay.old.action.EbConditionType;
import com.shadowcs.optimizer.engibay.old.action.unit.EbBuildAction;
import com.shadowcs.optimizer.sc2data.models.Ability;
import com.shadowcs.optimizer.sc2data.models.TechTree;
import com.shadowcs.optimizer.sc2data.models.Unit;
import com.shadowcs.optimizer.sc2data.models.Upgrade;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@UtilityClass
public class S2DataUtil {

    public Set<EbAction> generateActions(TechTree tree, Race...race) {

        Map<Integer, Ability> abilityMap = tree.abilityMap();
        tree.ability().forEach(ability -> abilityMap.put(ability.id(), ability));

        Map<Integer, Unit> unitMap = tree.unitMap();
        tree.unit().forEach(unit -> unitMap.put(unit.id(), unit));

        Map<Integer, Upgrade> upgradeMap = new HashMap<>();
        tree.upgrade().forEach(upgrade -> upgradeMap.put(upgrade.id(), upgrade));

        Set<EbAction> actionSet = new HashSet<>();

        // Generate our initial actions

        tree.unit().stream().filter(unit -> Arrays.stream(race).anyMatch(r -> unit.race().equalsIgnoreCase(r.name()))).forEach(unit -> {
            System.out.println("Unit: " + unit.name() + " " + unit.race());
        });

        // TODO: generate the special actions

        // Now we generate the needed requirements

        return actionSet;
    }




    // TODO: review below for if we need anymore or if we do not

    public List<Gene> generateGenes(TechTree tree, Race...race) {

        List<Gene> geneSet = new ArrayList<>();

        Map<Integer, Ability> abilityMap = tree.abilityMap();
        tree.ability().forEach(ability -> abilityMap.put(ability.id(), ability));

        Map<Integer, Unit> unitMap = tree.unitMap();
        tree.unit().forEach(unit -> unitMap.put(unit.id(), unit));

        Map<Integer, Upgrade> upgradeMap = new HashMap<>();
        tree.upgrade().forEach(upgrade -> upgradeMap.put(upgrade.id(), upgrade));

        Map<Integer, Gene> abilityGeneMap = new HashMap<>();
        Map<Integer, Gene> researchGeneMap = new HashMap<>();
        Map<Integer, Set<Gene>> unitGeneMap = new HashMap<>();

        // Filter to only units we care about, specifically using a races worker as the entry point and doing a depth
        // search for the unit IDs
        Set<Integer> unitIdList = new HashSet<>();
        tree.unit().forEach(unit -> {
            // For terran we also care about the TechLab and Reactor even though its really annoying to create those

            if(Arrays.stream(race).noneMatch(r -> r.name().equalsIgnoreCase(unit.race()))) {
                return;
            }

            if(unit.worker() || unit.addon() || unit.id() == Units.ZERG_LARVA.getUnitTypeId()) {
                getAbilityUnits(unitIdList, unit, abilityMap, unitMap);
            }

            // TODO: there are a few extra buildings that we may want to be able to create, will need to modify actions
        });

        // Only units can use abilities so we only need to loop through them and get the ability data to create our genes
        tree.unit().forEach(unit -> {

            if(!unitIdList.contains(unit.id())) {
                return;
            }

            // System.out.println("Unit: " + unitMap.get(unit.id()).name() + " - " + unitMap.get(unit.id()));

            // We always ignore the burrow types of units, we don't need to know about them for build orders
            if(unit.name().toLowerCase().contains("burrow")) {
                return;
            }

            // TODO: figure out how to filter to only units that are valid in mp

            unit.abilities().forEach(ability -> {

                var action = abilityMap.get(ability.ability());

                if(action.target() instanceof Map<?,?>) {
                    var target = (Map<?,?>) action.target();
                    String key = (String) target.keySet().iterator().next();
                    var data = (Map<?,?>) target.get(key);

                    var abilityData = abilityMap.get(ability.ability());
                    var s2action = new EbBuildAction().action(abilityData.id()).name(abilityData.name());

                    ability.requirements().forEach(requirement -> {
                        // We don't really care about the addonTo because we don't actually use it... its fine to have though
                        if(requirement.addonTo() != 0) {
                            s2action.consumed().add(new EbCondition(EbConditionType.UNIT, requirement.addonTo()));
                        }

                        if(requirement.building() != 0) {
                            s2action.required().add(new EbCondition(EbConditionType.UNIT, requirement.building()));
                        }

                        if(requirement.addon() != 0) {
                            // TODO: check this to make sure its the specific one instead of the general one
                            s2action.required().add(new EbCondition(EbConditionType.UNIT, requirement.addon()));
                        }

                        if(requirement.upgrade() != 0) {
                            s2action.required().add(new EbCondition(EbConditionType.RESEARCH, requirement.upgrade()));
                        }
                    });

                    if(!key.equalsIgnoreCase("Research")) { // if research
                        // If we are any of the morph types we need to calculate some things
                        //System.out.println("Ability: " + key + " : " + abilityData.name());

                        var produced = unitMap.get((int) (double) data.get("produces"));

                        int reduceMin = 0;
                        int reduceGas = 0;
                        double reduceSupply = 0;

                        if(key.toLowerCase().contains("morph")) {
                            reduceMin = unit.minerals();
                            reduceGas = unit.gas();
                            reduceSupply = unit.supply();

                            s2action.consumed().add(new EbCondition(EbConditionType.UNIT, unit.id()));
                        } else {
                            s2action.borrowed().add(new EbCondition(EbConditionType.UNIT, unit.id()));
                        }

                        double supply = produced.supply() - reduceSupply;
                        if(supply > 0) {
                            s2action.consumed().add(new EbCondition(EbConditionType.SUPPLY, supply));
                        }

                        if(supply < 0) {
                            s2action.produced().add(new EbCondition(EbConditionType.SUPPLY, -1 * supply));
                        }

                        if(produced.time() > 0) {
                            s2action.consumed().add(new EbCondition(EbConditionType.TIME, produced.time()));
                        }

                        int min = produced.minerals() - reduceMin;
                        if(min > 0) {
                            s2action.consumed().add(new EbCondition(EbConditionType.MINERAL, min));
                        }

                        int gas = produced.gas() - reduceGas;
                        if(gas > 0) {
                            s2action.consumed().add(new EbCondition(EbConditionType.GAS, gas));
                        }

                        if(abilityData.energyCost() > 0) {
                            s2action.consumed().add(new EbCondition(EbConditionType.ENERGY, abilityData.energyCost()));
                        }

                        s2action.produced().add(new EbCondition(EbConditionType.UNIT, produced.id()));
                        // Some units produce double, this adds a second copy of them to the list
                        if(produced.id() == Units.ZERG_ZERGLING.getUnitTypeId() && unit.id() != Units.ZERG_ZERGLING_BURROWED.getUnitTypeId()) {
                            s2action.produced().add(new EbCondition(EbConditionType.UNIT, produced.id()));
                        }

                        tree.unitGeneMap().putIfAbsent(produced.id(), new HashSet<>());
                        tree.unitGeneMap().get(produced.id()).add(s2action);

                        tree.unitChildActionMap().putIfAbsent(unit.id(), new HashSet<>());
                        tree.unitChildActionMap().get(unit.id()).add(s2action);
                    } else {
                        //System.out.println("Ability RESEARCH: " + abilityData.name());

                        var produced = upgradeMap.get((int) (double) data.get("upgrade"));

                        s2action.borrowed().add(new EbCondition(EbConditionType.UNIT, unit.id()));

                        if(produced.cost().time() > 0) {
                            s2action.consumed().add(new EbCondition(EbConditionType.TIME, produced.cost().time()));
                        }

                        if(produced.cost().minerals() > 0) {
                            s2action.consumed().add(new EbCondition(EbConditionType.MINERAL, produced.cost().minerals()));
                        }

                        if(produced.cost().gas() > 0) {
                            s2action.consumed().add(new EbCondition(EbConditionType.GAS, produced.cost().gas()));
                        }

                        s2action.produced().add(new EbCondition(EbConditionType.RESEARCH, produced.id()));

                        tree.upgradeGeneMap().put(produced.id(), s2action);
                    }

                    geneSet.add(new Gene().data(s2action));
                }
            });
        });

        return geneSet;
    }

    private void getAbilityUnits(Set<Integer> knownUnits, Unit unit, Map<Integer, Ability> abilityMap, Map<Integer, Unit> unitMap) {

        // If we know the unit already we can assume that we have already processed that unit
        if(knownUnits.contains(unit.id())) {
            return;
        } else {
            knownUnits.add(unit.id());
        }

        unit.abilities().forEach(ability -> {
            var action = abilityMap.get(ability.ability());

            if(action.target() instanceof Map<?,?>) {
                var target = (Map<?,?>) action.target();
                String key = (String) target.keySet().iterator().next();
                var data = (Map<?,?>) target.get(key);
                if(!key.equalsIgnoreCase("Research")) { // if research
                    getAbilityUnits(knownUnits, unitMap.get((int) (double) data.get("produces")), abilityMap, unitMap);
                }
            }
        });
    }

    public TechTree loadData() {

        return new Gson().fromJson(readFromFile("./data/optimizer/89720/data.json"), TechTree.class);
    }

    public String readFromFile(String file) {

        Path path = Paths.get(file);

        try {
            byte[] encoded = Files.readAllBytes(path);
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

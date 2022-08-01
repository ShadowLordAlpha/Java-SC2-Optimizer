package com.shadowcs.optimizer.sc2data.generator;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.protocol.data.*;
import com.github.ocraft.s2client.protocol.game.Race;
import com.google.gson.GsonBuilder;
import com.shadowcs.optimizer.sc2data.models.AbilityS2Data;
import com.shadowcs.optimizer.sc2data.models.CostS2Data;
import com.shadowcs.optimizer.sc2data.models.UnitS2Data;
import com.shadowcs.optimizer.sc2data.models.UpgradeS2Data;
import lombok.experimental.UtilityClass;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class CollectData {

    public Set<AbilityS2Data> collectAbilityBuildData(Set<UnitS2Data> unData, S2Agent agent) {
        Set<Integer> usedAbility = new HashSet<>();
        unData.forEach(unit -> {
            usedAbility.add(unit.buildAbility());
            usedAbility.addAll(unit.abilities());
        });

        Set<AbilityS2Data> abilityS2Data = new HashSet<>();
        var map = new ConcurrentHashMap<>(agent.observation().getAbilityData(true));

        // Not sure if this is needed but should be fine to leave in
        map.values().forEach(unit -> {
            if(unit.getRemapsToAbility().isPresent() && unit.getRemapsToAbility().get() != Abilities.INVALID) {
                if(usedAbility.contains(unit.getAbility().getAbilityId())) {
                    usedAbility.add(unit.getRemapsToAbility().get().getAbilityId());
                } else if(usedAbility.contains(unit.getRemapsToAbility().get().getAbilityId())) {
                    usedAbility.add(unit.getAbility().getAbilityId());
                }
            }
        });

        map.values().forEach(value -> {

            if(value.isAvailable() && !value.getAbility().getTargets().isEmpty() && usedAbility.contains(value.getAbility().getAbilityId())) {
                var temp = new AbilityS2Data();
                temp.id(value.getAbility().getAbilityId());
                temp.pName(value.getAbility().toString());
                temp.name((value.getLinkName() + "" + value.getFriendlyName().orElse(value.getButtonName().orElse(""))).trim());
                temp.generalId(value.getRemapsToAbility().orElse(Abilities.INVALID).getAbilityId());
                for(Target target: value.getAbility().getTargets()) {
                    temp.target().add(target.name());
                }

                abilityS2Data.add(temp);
            }
        });

        String unitData = new GsonBuilder().setPrettyPrinting().create().toJson(abilityS2Data);
        writeToFile(unitData, "./data/optimizer/" + agent.control().proto().getBaseBuild() + "/ability_data.json");

        return abilityS2Data;
    }

    public Set<UpgradeS2Data> collectUpgradeBuildData(S2Agent agent) {
        Set<UpgradeS2Data> upgradeTypeData = new HashSet<>();
        var map = new ConcurrentHashMap<>(agent.observation().getUpgradeData(true));
        map.values().forEach(value -> {

            if(value.getAbility().orElse(Abilities.INVALID) != Abilities.INVALID) {
                var temp = new UpgradeS2Data();
                temp.id(value.getUpgrade().getUpgradeId());
                temp.pName(value.getUpgrade().toString());
                temp.name(value.getName());
                temp.cost(new CostS2Data().minerals(value.getMineralCost().orElse(0)).vespene(value.getVespeneCost().orElse(0)).buildTime(value.getResearchTime().orElse(0.0f)));
                temp.buildAbility(value.getAbility().orElse(Abilities.INVALID).getAbilityId());

                upgradeTypeData.add(temp);
            }
        });

        String unitData = new GsonBuilder().setPrettyPrinting().create().toJson(upgradeTypeData);
        writeToFile(unitData, "./data/optimizer/" + agent.control().proto().getBaseBuild() + "/upgrade_data.json");

        return upgradeTypeData;
    }

    public Set<UnitS2Data> collectUnitBuildData(S2Agent agent) {

        Set<Integer> aliasUnits = new HashSet<>();
        Set<UnitS2Data> unitTypeData = new HashSet<>();
        var map = new ConcurrentHashMap<>(agent.observation().getUnitTypeData(true));
        map.values().forEach(value -> {
            if(value.isAvailable()
                    && value.getRace().orElse(Race.NO_RACE) != Race.NO_RACE
                    && !value.getTechAliases().isEmpty()) {
                value.getTechAliases().forEach(ut -> aliasUnits.add(ut.getUnitTypeId()));
            }
        });

        map.values().forEach(value -> {
            // We only currently care about these units, though we will probably want to know about all of them eventually
            if(value.isAvailable()
                    && value.getRace().orElse(Race.NO_RACE) != Race.NO_RACE
                    && value.getAbility().orElse(Abilities.INVALID) != Abilities.INVALID
                    && (!value.getUnitType().getAbilities().isEmpty() || aliasUnits.contains(value.getUnitType().getUnitTypeId()))) {
                var temp = new UnitS2Data();
                temp.id(value.getUnitType().getUnitTypeId());
                temp.pName(value.getUnitType().toString());
                temp.name(value.getName());
                temp.cost(new CostS2Data().minerals(value.getMineralCost().orElse(0)).vespene(value.getVespeneCost().orElse(0)).buildTime(value.getBuildTime().orElse(0.0f)));
                temp.buildAbility(value.getAbility().orElse(Abilities.INVALID).getAbilityId());
                temp.race(value.getRace().orElse(Race.NO_RACE));
                temp.food(value.getFoodProvided().orElse(0.0f) - value.getFoodRequired().orElse(0.0f));
                temp.hasMinerals(value.isHasMinerals()).hasVespene(value.isHasVespene());

                var abilities = new HashSet<Integer>();
                for(Ability ability: value.getUnitType().getAbilities()) {
                    // De-Generalize addon abilities... its already a pain without them getting generalized...
                    if(ability == Abilities.BUILD_TECHLAB) {
                        if(value.getUnitType() == Units.TERRAN_BARRACKS) {
                            abilities.add(Abilities.BUILD_TECHLAB_BARRACKS.getAbilityId());
                        } else if(value.getUnitType() == Units.TERRAN_FACTORY) {
                            abilities.add(Abilities.BUILD_TECHLAB_FACTORY.getAbilityId());
                        } else if(value.getUnitType() == Units.TERRAN_STARPORT) {
                            abilities.add(Abilities.BUILD_TECHLAB_STARPORT.getAbilityId());
                        }
                    } else if(ability == Abilities.BUILD_REACTOR) {
                        if(value.getUnitType() == Units.TERRAN_BARRACKS) {
                            abilities.add(Abilities.BUILD_REACTOR_BARRACKS.getAbilityId());
                        } else if(value.getUnitType() == Units.TERRAN_FACTORY) {
                            abilities.add(Abilities.BUILD_REACTOR_FACTORY.getAbilityId());
                        } else if(value.getUnitType() == Units.TERRAN_STARPORT) {
                            abilities.add(Abilities.BUILD_REACTOR_STARPORT.getAbilityId());
                        }
                    } else {
                        abilities.add(ability.getAbilityId());
                    }
                }
                temp.abilities(abilities);

                var techs = new HashSet<Integer>();
                for(UnitType unit: value.getTechAliases()) {
                    techs.add(unit.getUnitTypeId());
                }
                temp.techAliases(techs);
                temp.unitAlias(value.getUnitAlias().orElse(Units.INVALID).getUnitTypeId());

                var techUnit = value.getTechRequirement().orElse(Units.INVALID);
                temp.techRequirement(techUnit.getUnitTypeId());

                temp.requireAttached(value.isRequireAttached());

                unitTypeData.add(temp);
            }
        });

        String unitData = new GsonBuilder().setPrettyPrinting().create().toJson(unitTypeData);
        writeToFile(unitData, "./data/optimizer/" + agent.control().proto().getBaseBuild() + "/unit_data.json");

        return unitTypeData;
    }

    public static void writeToFile(String data, String file) {

        Path path = Paths.get(file);
        byte[] strToBytes = data.getBytes();

        try {
            Files.createDirectories(path.getParent());
            // Files.createFile(path);
            Files.write(path, strToBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.shadowcs.optimizer.sc2data;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.google.gson.Gson;
import com.shadowcs.optimizer.genetics.Gene;
import com.shadowcs.optimizer.sc2data.genetics.GeneAction;
import com.shadowcs.optimizer.sc2data.models.Ability;
import com.shadowcs.optimizer.sc2data.models.TechTree;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@UtilityClass
public class S2DataUtil {

    public List<Gene> generateGenes(TechTree tree, Race...race) {

        List<Gene> geneSet = new ArrayList<>();

        Map<Integer, Ability> abilityMap = new HashMap<>();
        tree.ability().forEach(ability -> abilityMap.put(ability.id(), ability));

        Map<Integer, Gene> abilityGeneMap = new HashMap<>();
        Map<Integer, Gene> researchGeneMap = new HashMap<>();
        Map<Integer, Set<Gene>> unitGeneMap = new HashMap<>();

        // Only units can use abilities so we only need to loop through them and get the ability data to create our genes
        tree.unit().forEach(unit -> {

            unit.abilities().forEach(ability -> {
                if(Arrays.stream(race).noneMatch(r -> r.name().equalsIgnoreCase(unit.race()))) {
                    return;
                }

                var action = abilityMap.get(ability.ability());

                if(action.target() instanceof Map<?,?>) {
                    var geneAction = new GeneAction().unitId(unit.id()).actionId(action.id());
                    var gene = new Gene().data(geneAction);
                    abilityGeneMap.put(action.id(), gene);
                    geneSet.add(gene);

                    var target = (Map<?,?>) action.target();
                    String key = (String) target.keySet().iterator().next();
                    var data = (Map<?,?>) target.get(key);
                    if(key.equalsIgnoreCase("Research")) { // if research
                        //System.out.println(data);
                        geneAction.compResearchId((int) (double) data.get("upgrade"));
                        researchGeneMap.put((int) (double) data.get("upgrade"), gene);
                    } else { // else unit
                        //System.out.println(data);
                        geneAction.compUnitId((int) (double) data.get("produces"));

                        unitGeneMap.putIfAbsent((int) (double) data.get("produces"), new HashSet<>());
                        unitGeneMap.get((int) (double) data.get("produces")).add(gene);
                    }
                }
            });
        });

        // we now need to loop through again... because we need to define requirements
        tree.unit().forEach(unit -> {
            if(Arrays.stream(race).noneMatch(r -> r.name().equalsIgnoreCase(unit.race()))) {
                return;
            }

            var units = unitGeneMap.get(unit.id());
            if(units != null) {
                unit.abilities().forEach(ability -> {
                    var gene = abilityGeneMap.get(ability.ability());
                    if (gene != null) {

                        //System.out.println(unit.name() + units.size() + "(" + abilityMap.get(ability.ability()).name() + ") - " + ability.requirements());
                        units.forEach(creation -> {
                            Set<Gene> recSet = new HashSet<>();
                            recSet.add(creation);
                            ability.requirements().forEach(requirement -> {
                                if(requirement.upgrade() != null && requirement.upgrade() != 0) {
                                    recSet.add(researchGeneMap.get(requirement.upgrade()));
                                }

                                try {
                                    if (requirement.building() != null && requirement.building() != 0) {
                                        // TODO: check if there are several of this building
                                        if(unitGeneMap.get(requirement.building()).size() > 1) {
                                            System.out.println("More than one we need to handle..." + requirement.building());
                                        }
                                        recSet.add(unitGeneMap.get(requirement.building()).iterator().next());
                                    }
                                } catch (Exception e) {
                                    //e.printStackTrace();
                                }

                                try {
                                    if (requirement.addon() != null && requirement.addon() != 0) {
                                        if(requirement.addon() == 5) {
                                            if(unit.id() == Units.TERRAN_BARRACKS.getUnitTypeId()) {
                                                recSet.add(unitGeneMap.get(Units.TERRAN_BARRACKS_TECHLAB.getUnitTypeId()).iterator().next());
                                            } else if(unit.id() == Units.TERRAN_FACTORY.getUnitTypeId()) {
                                                recSet.add(unitGeneMap.get(Units.TERRAN_FACTORY_TECHLAB.getUnitTypeId()).iterator().next());
                                            } else if(unit.id() == Units.TERRAN_STARPORT.getUnitTypeId()) {
                                                recSet.add(unitGeneMap.get(Units.TERRAN_STARPORT_TECHLAB.getUnitTypeId()).iterator().next());
                                            }
                                        } else {
                                            // TODO: check if there are several ways to make this building
                                            if(unitGeneMap.get(requirement.addon()).size() > 1) {
                                                System.out.println("More than one we addon need to handle..." + requirement.addon());
                                            }

                                            recSet.add(unitGeneMap.get(requirement.addon()).iterator().next());
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("ERROR ADDON: " + requirement.addon() + " IN " + unit.name() + " - " + abilityMap.get(ability.ability()).name());
                                }
                            });
                        });

                    }
                });
            } else {
                System.out.println("NULL: " + unit.name());
            }
        });

        return geneSet;
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

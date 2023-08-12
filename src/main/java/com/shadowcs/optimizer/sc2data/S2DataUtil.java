package com.shadowcs.optimizer.sc2data;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.google.gson.Gson;
import com.shadowcs.optimizer.engibay.EbState;
import com.shadowcs.optimizer.engibay.build.EbAction;
import com.shadowcs.optimizer.engibay.build.EbBasicAction;
import com.shadowcs.optimizer.engibay.build.EbCondition;
import com.shadowcs.optimizer.engibay.build.EbConditionType;
import com.shadowcs.optimizer.nydusnetwork.breathfirst.BFS;
import com.shadowcs.optimizer.nydusnetwork.breathfirst.BFSNode;
import com.shadowcs.optimizer.sc2data.models.Ability;
import com.shadowcs.optimizer.sc2data.models.TechTree;
import com.shadowcs.optimizer.sc2data.models.Unit;
import com.shadowcs.optimizer.sc2data.models.Upgrade;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@UtilityClass
@Slf4j
public class S2DataUtil {

    private static final Set<Integer> unitOverridSet = new HashSet<>(asList(
            Units.TERRAN_MULE.getUnitTypeId(),
            Units.TERRAN_REACTOR.getUnitTypeId(),
            Units.TERRAN_TECHLAB.getUnitTypeId(),
            Units.ZERG_LARVA.getUnitTypeId()
    ));

    /**
     * We use a filter to reduce the search space of our problem for the GA. This allows us to reduce the complexity of
     * the problem and as such increase the efficiency of the GA
     *
     * @param actions The full set of actions we need to limit and attempt to reduce
     * @param init Our starting point
     * @param goal our end goal
     * @param override These actions are added no matter what to our returned list, they are not duplicated if they
     *                 would have already been returned in our action list. This does not mean that the action itself is
     *                 usable just that it is in the list of possible actions
     *
     * @return A list of valid actions that we can take
     */
    public static List<EbAction> filterActions(Set<EbAction> actions, EbState init, EbState goal, EbAction...override) {

        System.out.println("Starting Action Space: " + actions.size());
        log.debug("Starting Action Space: {}", actions.size());

        Set<EbAction> actionList = new HashSet<>(List.of(override));

        var nodeGraph = generateTechGraph(actions);

        Set<BFSNode<EbAction>> start = new HashSet();

        nodeGraph.forEach(node -> {
            if(node.node() instanceof EbBasicAction) {
                if(((EbBasicAction) node.node()).upgrade()) {
                    // upgrades are not considered valid start positions to simplify the graph
                    if(init.upgradeSet().contains(((EbBasicAction) node.node()).caster())) {
                        start.add(node);
                    }
                } else {
                    // We should probably be able to create more of a unit we need TBH... mainly needed for morph type commands though
                    if(init.unitCountMap().keySet().contains(((EbBasicAction) node.node()).caster())) {
                        start.add(node);
                    }
                }
            }
        });

        Set<BFSNode<EbAction>> end = new HashSet();
        nodeGraph.forEach(node -> {
            if(node.node() instanceof EbBasicAction) {
                if(((EbBasicAction) node.node()).upgrade()) {
                    // upgrades are not considered valid start positions to simplify the graph
                    if(goal.upgradeSet().contains(((EbBasicAction) node.node()).created())) {
                        System.out.println("UPGRADE: " + node.node().name());
                        end.add(node);
                    }
                } else {
                    // We should probably be able to create more of a unit we need TBH... mainly needed for morph type commands though
                    if(goal.unitCountMap().keySet().contains(((EbBasicAction) node.node()).created())) {
                        end.add(node);
                    }
                }
            }
        });

        int lastRun;
        Set<EbAction> tempList = new HashSet<>();

        do {
            lastRun = tempList.size();
            var paths = BFS.bfs(start, end, true);

            System.out.println("Paths: " + paths);

            paths.forEach(path -> {
                System.out.println(path);
                path.forEach(node -> {
                    tempList.add(node.node());

                    if (node.node() instanceof EbBasicAction) {
                        // Check to make sure our start has all the required parts
                        ((EbBasicAction) node.node()).required().forEach(condition -> {
                            switch (condition.type()) {
                                case UNIT -> {
                                    if (init.unitCountMap().get((int) condition.data()) <= 0) {
                                        findNode(nodeGraph, end, (int) condition.data(), false);
                                    }
                                }
                                case RESEARCH -> {
                                    if (!init.upgradeSet().contains((int) condition.data())) {
                                        findNode(nodeGraph, end, (int) condition.data(), true);
                                    }
                                }
                                case GAS -> {
                                    findNode(nodeGraph, end, Units.ZERG_EXTRACTOR.getUnitTypeId(), false);
                                    findNode(nodeGraph, end, Units.TERRAN_REFINERY.getUnitTypeId(), false);
                                    findNode(nodeGraph, end, Units.PROTOSS_ASSIMILATOR.getUnitTypeId(), false);
                                }
                            }
                        });

                        // Now we add in the borrowed and consumed parts as we need those and more may make us faster
                        ((EbBasicAction) node.node()).borrowed().forEach(condition -> {
                            switch (condition.type()) {
                                case UNIT -> {
                                    findNode(nodeGraph, end, (int) condition.data(), false);
                                }
                                case RESEARCH -> {
                                    findNode(nodeGraph, end, (int) condition.data(), true);
                                }
                                case GAS -> {
                                    findNode(nodeGraph, end, Units.ZERG_EXTRACTOR.getUnitTypeId(), false);
                                    findNode(nodeGraph, end, Units.TERRAN_REFINERY.getUnitTypeId(), false);
                                    findNode(nodeGraph, end, Units.PROTOSS_ASSIMILATOR.getUnitTypeId(), false);
                                }
                            }
                        });

                        ((EbBasicAction) node.node()).consumed().forEach(condition -> {
                            switch (condition.type()) {
                                case UNIT -> {
                                    findNode(nodeGraph, end, (int) condition.data(), false);
                                }
                                case RESEARCH -> {
                                    findNode(nodeGraph, end, (int) condition.data(), true);
                                }
                                case GAS -> {
                                    findNode(nodeGraph, end, Units.ZERG_EXTRACTOR.getUnitTypeId(), false);
                                    findNode(nodeGraph, end, Units.TERRAN_REFINERY.getUnitTypeId(), false);
                                    findNode(nodeGraph, end, Units.PROTOSS_ASSIMILATOR.getUnitTypeId(), false);
                                }
                            }
                        });
                    }
                });
            });

            System.out.println("Last size: " + lastRun);
            System.out.println("Paths size: " + tempList.size());
        } while(lastRun != tempList.size());

        System.out.println("Paths size: " + tempList.size());
        tempList.forEach(action -> System.out.println(action.name()));

        actionList.addAll(tempList);
        // actionList.addAll(actions);

        System.out.println("Optimized Action Space: " + actionList.size());
        log.debug("Optimized Action Space: {}", actionList.size());
        return new ArrayList<>(actionList);
    }

    private void findNode(Set<BFSNode<EbAction>> nodeGraph, Set<BFSNode<EbAction>> end, int id, boolean upgrade) {
        nodeGraph.forEach(node -> {
            if(node.node() instanceof EbBasicAction && id == ((EbBasicAction) node.node()).created() && ((EbBasicAction) node.node()).upgrade() == upgrade) {
                System.out.println(((EbBasicAction) node.node()).casterName() + " - " + node.node().name());
                if(upgrade) {
                    // upgrades are not considered valid start positions to simplify the graph
                    end.add(node);
                } else {
                    // We should probably be able to create more of a unit we need TBH... mainly needed for morph type commands though
                    end.add(node);
                }
            }
        });
    }

    private Set<BFSNode<EbAction>> generateTechGraph(Set<EbAction> actions) {

        // Action is wrong, should be the unit or upgrade instead, our actions should only be the edges instead
        Set<BFSNode<EbAction>> nodeSet = new HashSet<>();

        // Generate all the nodes we need from the actions we can take
        actions.forEach(action -> {
            BFSNode<EbAction> node = new BFSNode<>(action);
            nodeSet.add(node);
        });

        // Now that we have all the nodes we need we can go ahead and generate all the edges we need
        nodeSet.forEach(node -> {
            if(node.node() instanceof EbBasicAction) {

                // The node is represented by what it creates, not what casts it
                int created = ((EbBasicAction) node.node()).created();

                nodeSet.forEach(graphFind -> {
                    if (graphFind.node() instanceof EbBasicAction) {
                        int caster = ((EbBasicAction) graphFind.node()).caster();
                        if (caster == created) {
                            node.neighbors().add(graphFind);
                        }
                    }
                });
            }
        });

        return nodeSet;
    }

    public Set<EbAction> generateActions(TechTree tree, Race...race) {

        Map<Integer, Ability> abilityMap = tree.abilityMap();
        tree.ability().forEach(ability -> abilityMap.put(ability.id(), ability));

        Map<Integer, Unit> unitMap = tree.unitMap();
        tree.unit().forEach(unit -> unitMap.put(unit.id(), unit));

        Map<Integer, Upgrade> upgradeMap = new HashMap<>();
        tree.upgrade().forEach(upgrade -> upgradeMap.put(upgrade.id(), upgrade));

        Set<EbAction> actionSet = new HashSet<>();

        // Generate our initial actions
        Map<Unit, Set<Ability>> unitProductionMap = new HashMap<>();

        var raceUnits = tree.unit().stream().filter(unit -> Arrays.stream(race).anyMatch(r -> unit.race().equalsIgnoreCase(r.name()))).collect(Collectors.toSet());
        var raceAbilities = tree.ability().stream().filter(ability -> unitHasAbility(raceUnits, ability)).collect(Collectors.toSet());

        var producibleUnits = new HashSet<>(raceUnits);
        var producibleAbilities = new HashSet<>(raceAbilities);

        int size = 0;
        while (size != producibleUnits.size()) {
            size = producibleUnits.size();

            producibleUnits.removeIf(unit -> !abilityMakesUnit(producibleAbilities, unit));
            producibleAbilities.removeIf(ability -> !unitHasAbility(producibleUnits, ability));
        }

        var raceUpgrades = tree.upgrade().stream().filter(u -> abilityMakesUpgrade(producibleAbilities, u)).collect(Collectors.toSet());

        //producibleUnits.forEach(u -> System.out.println("Unit: " + u.name()));
        //producibleAbilities.forEach(u -> System.out.println("Ability: " + u.name()));
        //raceUpgrades.forEach(u -> System.out.println("Upgrade: " + u.name()));

        producibleUnits.forEach(u -> {

            //System.out.println("Unit: " + u.name());

            u.abilities().forEach(a -> {

                var ability = abilityMap.get(a.ability());
                if(ability.target() instanceof Map<?, ?> target) {
                    String key = (String) target.keySet().iterator().next();
                    var data = (Map<?, ?>) target.get(key);

                    boolean upgrade = key.equalsIgnoreCase("Research");
                    String produce = upgrade ? "upgrade" : "produces";
                    int prodId = (int) (double) data.get(produce);

                    //System.out.println("    Ability: " + ability.name());
                    //System.out.println("        Requirements: " + key);

                    Set<EbCondition> required = new HashSet<>();
                    for(var requirement: a.requirements()) {
                        if(requirement.addonTo() != 0) {
                            var un = unitMap.get(requirement.addonTo());
                            required.add(new EbCondition(EbConditionType.UNIT, un.name(), un.id()));
                            //System.out.println("        Requirements: " + un.name());
                        }
                        if(requirement.addon() != 0) {
                            var un = unitMap.get(requirement.addon());
                            required.add(new EbCondition(EbConditionType.UNIT, un.name(), un.id()));
                            //System.out.println("        Requirements: " + un.name());
                        }
                        if(requirement.building() != 0) {
                            var un = unitMap.get(requirement.building());
                            required.add(new EbCondition(EbConditionType.UNIT, un.name(), un.id()));
                            //System.out.println("        Requirements: " + un.name());
                        }

                        if(requirement.upgrade() != 0) {
                            var up = upgradeMap.get(requirement.upgrade());
                            required.add(new EbCondition(EbConditionType.RESEARCH, up.name(), up.id()));
                            //System.out.println("        Requirements: " + up.name());
                        }
                    }

                    double gas;
                    double mineral;
                    double time;
                    double supply = 0;
                    boolean structure = false;
                    String createdName = "";

                    Set<EbCondition> produced = new HashSet<>();
                    if(upgrade) {
                        var up = upgradeMap.get(prodId);
                        gas = up.cost().gas();
                        mineral = up.cost().minerals();
                        time = up.cost().time();
                        createdName = up.name();
                        produced.add(new EbCondition(EbConditionType.RESEARCH, up.name(), up.id()));
                        //System.out.println("        Requirements: Gas " + up.cost().gas());
                        //System.out.println("        Requirements: Minerals " + up.cost().minerals());
                        //System.out.println("        Requirements: Time " + up.cost().time());
                        //System.out.println("        Upgrade: " + up.name());
                    } else {
                        var un = unitMap.get(prodId);
                        structure = un.isStructure();
                        gas = un.gas();
                        mineral = un.minerals();
                        supply = un.supply();
                        time = un.time();
                        createdName = un.name();
                        produced.add(new EbCondition(EbConditionType.UNIT, un.name(), un.id()));
                        if(un.id() == Units.ZERG_ZERGLING.getUnitTypeId()) {
                            supply = 1;
                            produced.add(new EbCondition(EbConditionType.UNIT, un.name(), un.id()));
                        }
                        //System.out.println("        Requirements: Gas " + un.gas());
                        //System.out.println("        Requirements: Minerals " + un.minerals());
                        //System.out.println("        Requirements: Supply " + un.supply());
                        //System.out.println("        Requirements: Time " + un.time());
                        //System.out.println("        Unit: " + un.name());
                    }

                    Set<EbCondition> borrowed = new HashSet<>();
                    Set<EbCondition> consumed = new HashSet<>();
                    if(key.toUpperCase().contains("MORPH") || (u.id() == Units.ZERG_DRONE.getUnitTypeId() && structure)) {
                        gas -= u.gas();
                        mineral -= u.minerals();
                        if(structure) {
                            supply = 0; // Structures never use supply, assume other morphs have the correct supply already set for now because its dumb and I have not fixed it yet
                        } else if(u.id() == Units.ZERG_LARVA.getUnitTypeId()) {
                            // Supply should stay as what it was...
                        } else {
                            supply = u.supply() - supply;
                        }


                        consumed.add(new EbCondition(EbConditionType.UNIT, u.name(), u.id()));
                    } else {
                        borrowed.add(new EbCondition(EbConditionType.UNIT, u.name(), u.id()));
                    }

                    if(mineral > 0) {
                        consumed.add(new EbCondition(EbConditionType.MINERAL, "mineral", mineral));
                    }
                    if(gas > 0) {
                        consumed.add(new EbCondition(EbConditionType.GAS, "gas", gas));
                    }
                    if(supply > 0) {
                        consumed.add(new EbCondition(EbConditionType.SUPPLY, "supply", supply));
                    }
                    if(time > 0) {
                        consumed.add(new EbCondition(EbConditionType.TIME, "time", time));
                    }

                    actionSet.add(new EbBasicAction(ability.name(), u.id(), u.name(), upgrade, prodId, createdName, ability.id(), required, borrowed, consumed, produced));
                }
            });
        });

        // TODO: things with this
        // actionSet.forEach(as -> System.out.println(as));


        return actionSet;
    }

    /**
     * Check if there is a unit in the given set that has the ability.
     *
     * @param raceUnits The unit set we have to work with
     * @param ability The ability we are looking for
     * @return true iff there is a unit that has the ability we are looking for
     */
    private boolean unitHasAbility(Set<Unit> raceUnits, Ability ability) {

        for(var unit: raceUnits) {
            for(var unitAbility: unit.abilities()) {
                if(Objects.equals(unitAbility.ability(), ability.id())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if there is an ability in the given set that creates the needed unit
     *
     * @param raceAbilities The ability se we have to work with
     * @param unit The Unit we are looking for
     * @return true if there is an ability that produces the unit we are looking for or the unit is in one we want no
     * matter what.
     */
    private boolean abilityMakesUnit(Set<Ability> raceAbilities, Unit unit) {

        // There are some units that we want as they are auto generated or generated from other commands that we have to
        //  make exceptions for
        if(unitOverridSet.contains(unit.id())) {
            return true;
        }

        // These are not used units
        if(unit.id() == Units.ZERG_INFESTOR_TERRAN.getUnitTypeId()) {
            return false;
        }

        for(var ability: raceAbilities) {
            if(ability.target() instanceof Map<?, ?> target) {
                String key = (String) target.keySet().iterator().next();
                if(!key.equalsIgnoreCase("Research")) {
                    var data = (Map<?, ?>) target.get(key);
                    int prodId = (int) (double) data.get("produces");
                    if(unit.id() == prodId) {
                        // We found the ability so we can return right away
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Check if there is an ability in the given set that creates the needed upgrade.
     *
     * @param raceAbilities The ability set we have to work with
     * @param upgrade The upgrade we are looking for
     * @return true iff there is an ability in the set that produces the upgrade we are looking for
     */
    private boolean abilityMakesUpgrade(Set<Ability> raceAbilities, Upgrade upgrade) {

        for(var ability: raceAbilities) {
            if(ability.target() instanceof Map<?, ?> target) {
                String key = (String) target.keySet().iterator().next();
                if(key.equalsIgnoreCase("Research")) {
                    var data = (Map<?, ?>) target.get(key);
                    int prodId = (int) (double) data.get("upgrade");
                    if(upgrade.id() == prodId) {
                        // We found the ability so we can return right away
                        return true;
                    }
                }
            }
        }

        return false;
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

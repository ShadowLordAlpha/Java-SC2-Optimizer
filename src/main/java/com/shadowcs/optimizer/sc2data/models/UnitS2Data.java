package com.shadowcs.optimizer.sc2data.models;

import com.github.ocraft.s2client.protocol.data.UnitAttribute;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.game.Race;
import lombok.Data;

import java.util.Set;

/**
 * TODO: This is currently only what we need for a build optimized, we will need more info for a combat analyzer
 */
@Data
public class UnitS2Data {
    private int id; // Our Unit ID
    private String pName;
    private String name;
    private CostS2Data cost; // The cost to build this unit (morph units are odd)
    private int buildAbility;
    private Race race;
    private float food;
    private boolean hasVespene;
    private boolean hasMinerals;
    private Set<Integer> abilities;

    private Set<Integer> techAliases; // Other units that satisfy the same tech requirement.
    private int unitAlias;        // The morphed variant of this unit.
    private int techRequirement;  // Structure required to build this unit. (Or any with the same tech_alias)
    private boolean requireAttached;   // Whether tech_requirement is an add-on.
}

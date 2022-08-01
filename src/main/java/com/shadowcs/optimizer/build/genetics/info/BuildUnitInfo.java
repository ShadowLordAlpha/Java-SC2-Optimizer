package com.shadowcs.optimizer.build.genetics.info;

import com.github.ocraft.s2client.protocol.data.UnitType;
import lombok.Data;

/**
 * BuildUnitInfo keeps track of specific unit info as well as addons for the units. all addon type units are included
 * within the main batch of units in order to keep them all in the same object.
 */
@Data
public class BuildUnitInfo {

    /**
     * What type of unit are we keeping track of
     */
    private final UnitType type;

    /**
     * How many total of this unit do we have (reactors do not do anything to this number)
     */
    private int units;

    /**
     * How many units do we have busy. Reactors DO affect this number so you could only have 2 units but have 4 busy
     */
    private int busy;

    /**
     * How many techlabs to we have that are attached to units of this kind
     */
    private int addonTechlab;

    /**
     * How many techlabs are busy? this is also reflected in the busy number but as a techlab can do its own things we
     * need to keep track of them
     */
    private int addonTechlabBusy;

    /**
     * How many of this unit have attached reactors
     */
    private int addonReactor;

    /**
     * How many "units" do we have available, reactor addons do affect this number
     *
     * @return the number of units + the number of reactors on this unit type - the number of busy units
     */
    public int availableUnits() {
        return (units() + addonReactor()) - busy();
    }

    /**
     * do we have at least one of this unit and at least one busy
     * @return
     */
    public boolean inUse() {
        return units > 0 && busy > 0;
    }

    /**
     * How many of this unit do not have an addon
     * @return
     */
    public int countNoAddon() {
        return units() - addonReactor() - addonTechlab();
    }
}

package com.shadowcs.optimizer.build.genetics.info;

import com.shadowcs.optimizer.build.genetics.BuildConstants;
import lombok.Data;

/**
 * {@link EnergyInfo} Keeps track of a unit's energy for using different abilities or spells.
 */
@Data
public class EnergyInfo {

    /**
     * The current amount of energy in the Nexus, a new Nexus always starts with 50 Energy
     */
    private float energy = BuildConstants.startingEnergy;

    /**
     * The time in frames until the unit has the needed amount of energy
     *
     * @param needed The total amount of energy needed
     * @return The number of frames until we have the needed amount of energy
     */
    public float timeToGetEnergy(float needed) {
        float remaining = needed - energy;
        if(remaining <= 0) {
            return 0;
        } else if(needed > BuildConstants.maxEnergy) {
            return Float.POSITIVE_INFINITY;
        }

        return remaining * BuildConstants.energyPerFrameNeeded;
    }

    /**
     * Simulate and set the energy amount.
     *
     * @param dt The change in time in frames
     */
    public void simulateEnergy(float dt) {
        energy = Math.min(BuildConstants.maxEnergy, energy + (dt * BuildConstants.energyPerFrame));
    }
}

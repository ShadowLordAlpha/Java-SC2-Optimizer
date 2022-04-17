package com.shadowcs.optimizer.sc2data.models;

import lombok.Data;

/**
 * The cost of a unit is a one off price that must be paid for the unit. Because it must be a one off price this does
 * not include the supply cost of a unit.
 */
@Data
public class CostS2Data {
    private int minerals;
    private int vespene;
    private float buildTime;
}

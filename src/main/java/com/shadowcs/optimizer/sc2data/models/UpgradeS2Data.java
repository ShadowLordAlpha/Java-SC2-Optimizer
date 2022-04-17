package com.shadowcs.optimizer.sc2data.models;

import lombok.Data;

@Data
public class UpgradeS2Data {
    private int id;
    private String pName;
    private String name;
    private CostS2Data cost; // The cost to build this unit (morph units are odd)
    private int buildAbility;
}

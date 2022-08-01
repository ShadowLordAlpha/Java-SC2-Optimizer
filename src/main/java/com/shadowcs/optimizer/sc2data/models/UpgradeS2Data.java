package com.shadowcs.optimizer.sc2data.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(cacheStrategy= EqualsAndHashCode.CacheStrategy.LAZY, onlyExplicitlyIncluded = true)
public class UpgradeS2Data {
    @EqualsAndHashCode.Include private int id;
    private String pName;
    private String name;
    private CostS2Data cost; // The cost to build this unit (morph units are odd)
    private int buildAbility;
}

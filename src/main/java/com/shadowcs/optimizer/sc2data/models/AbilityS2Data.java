package com.shadowcs.optimizer.sc2data.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(cacheStrategy= EqualsAndHashCode.CacheStrategy.LAZY, onlyExplicitlyIncluded = true)
public class AbilityS2Data {
    @EqualsAndHashCode.Include private int id;
    private String pName;
    private String name;
    private int generalId;
    private Set<String> target = new HashSet<>();
}

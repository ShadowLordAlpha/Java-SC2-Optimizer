package com.shadowcs.optimizer.sc2data.models;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class AbilityS2Data {
    private int id;
    private String pName;
    private String name;
    private int generalId;
    private Set<String> target = new HashSet<>();
}

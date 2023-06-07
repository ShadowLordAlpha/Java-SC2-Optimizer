package com.shadowcs.optimizer.sc2data.genetics;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class S2GeneAction {

    private int action;

    /**
     * What precondition must be present at the time of issuing the action
     */
    private Set<Condition> required = new HashSet<>();

    /**
     * What preconditions must be present and are used for the full duration of the action
     */
    private Set<Condition> borrowed = new HashSet<>();

    /**
     * What preconditions must be present and are consumed by the action
     */
    private Set<Condition> consumed = new HashSet<>();

    /**
     * What is created upon completion of the action
     */
    private Set<Condition> produced = new HashSet<>();

    @Data
    public static class Condition {
        private double data;
        private ConditionType type;
    }

    public static enum ConditionType {
        UNIT,
        RESEARCH,
        SUPPLY,
        MINERAL,
        GAS,
        ENERGY,
        TIME
    }
}

package com.shadowcs.optimizer.sc2data.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Ability_ {
    private Integer ability;
    private List<Requirement> requirements = new ArrayList<>();
}

package com.shadowcs.optimizer.sc2data.models.old;

import lombok.Data;

import java.util.ArrayList;

@Data
public class AbilityLink {
    public int ability;
    public ArrayList<Requirement> requirements;
}

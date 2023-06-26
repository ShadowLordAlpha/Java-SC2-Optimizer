package com.shadowcs.optimizer.sc2data.models;

import com.google.gson.annotations.SerializedName;
import com.shadowcs.optimizer.engibay.old.action.EbAction;
import lombok.Data;

import java.util.*;

@Data
public class TechTree {
    @SerializedName("Ability") private List<Ability> ability = new ArrayList<>();
    @SerializedName("Unit") private List<Unit> unit = new ArrayList<>();
    @SerializedName("Upgrade") private List<Upgrade> upgrade = new ArrayList<>();

    private transient Map<Integer, Ability> abilityMap = new HashMap<>();
    private transient Map<Integer, Unit> unitMap = new HashMap<>();
    private transient Map<Integer, Set<EbAction>> unitGeneMap = new HashMap<>();
    private transient Map<Integer, EbAction> upgradeGeneMap = new HashMap<>();

    private transient Map<Integer, Set<EbAction>> unitChildActionMap = new HashMap<>();
}

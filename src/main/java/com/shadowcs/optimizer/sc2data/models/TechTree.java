package com.shadowcs.optimizer.sc2data.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TechTree {
    @SerializedName("Ability") private List<Ability> ability = new ArrayList<>();
    @SerializedName("Unit") private List<Unit> unit = new ArrayList<>();
    @SerializedName("Upgrade") private List<Upgrade> upgrade = new ArrayList<>();
}

package com.shadowcs.optimizer.sc2data.models.old;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;

@Data
public class Root {
    @SerializedName("Ability") private ArrayList<Ability> ability;
    @SerializedName("Unit") private ArrayList<Unit> unit;
    @SerializedName("Upgrade") private ArrayList<Upgrade> upgrade;
}


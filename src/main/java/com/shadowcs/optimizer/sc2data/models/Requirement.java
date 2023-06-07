package com.shadowcs.optimizer.sc2data.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Requirement {
    @SerializedName("addon_to") private int addonTo;
    private int upgrade;
    private int building;
    private int addon;
}

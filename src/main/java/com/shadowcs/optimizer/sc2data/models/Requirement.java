package com.shadowcs.optimizer.sc2data.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Requirement {
    @SerializedName("addon_to") private Integer addonTo;
    private Integer upgrade;
    private Integer building;
    private Integer addon;
}

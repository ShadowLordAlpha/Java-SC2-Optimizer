package com.shadowcs.optimizer.sc2data.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Ability {
    private Integer id;
    private String name;
    @SerializedName("cast_range") private Double castRange;
    @SerializedName("energy_cost") private Integer energyCost;
    @SerializedName("allow_minimap") private Boolean allowMinimap;
    @SerializedName("allow_autocast") private Boolean allowAutocast;
    private List<Object> effect = new ArrayList<>();
    private List<Object> buff = new ArrayList<>();
    private Integer cooldown;
    private Object target;
    @SerializedName("remaps_to_ability_id") private Integer remapsToAbilityId;
}

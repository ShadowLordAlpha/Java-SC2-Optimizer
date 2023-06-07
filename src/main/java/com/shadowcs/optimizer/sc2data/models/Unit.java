package com.shadowcs.optimizer.sc2data.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Unit {
    private Integer id;
    private String name;
    private String race;
    private Double supply;
    private Integer cargoSize;
    @SerializedName("max_health") private Double maxHealth;
    private Double armor;
    private Double sight;
    private Double speed;
    private Double speedCreepMul;
    private List<String> attributes = new ArrayList<>();
    private Integer size;
    private Double radius;
    private Boolean acceptsAddon;
    private Boolean needsPower;
    private Boolean needsCreep;
    @SerializedName("needs_geyser") private boolean needsGeyser;
    private Boolean isStructure;
    @SerializedName("is_addon") private boolean addon;
    @SerializedName("is_worker") private boolean worker;
    @SerializedName("is_townhall") private boolean townhall;
    private Integer minerals;
    private Integer gas;
    private Double time;
    private Double maxShield;
    private Boolean isFlying;
    private List<Weapon> weapons = new ArrayList<>();
    private List<Ability_> abilities = new ArrayList<>();
    private Double maxEnergy;
    private Integer startEnergy;
    private Integer normalMode;
    private Integer cargoCapacity;
    private Double detectionRange;
    private Double powerRadius;
}

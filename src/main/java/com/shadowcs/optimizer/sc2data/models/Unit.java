package com.shadowcs.optimizer.sc2data.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Unit {
    private int id;
    private String name;
    private String race;
    private double supply;
    private int cargoSize;
    @SerializedName("max_health") private double maxHealth;
    private double armor;
    private double sight;
    private double speed;
    @SerializedName("speed_creep_mul") private double speedCreepMul;
    private List<String> attributes = new ArrayList<>();
    private int size;
    private double radius;
    @SerializedName("accepts_addon") private boolean acceptsAddon;
    private Boolean needsPower;
    private Boolean needsCreep;
    @SerializedName("needs_geyser") private boolean needsGeyser;
    @SerializedName("is_structure") private boolean isStructure;
    @SerializedName("is_addon") private boolean addon;
    @SerializedName("is_worker") private boolean worker;
    @SerializedName("is_townhall") private boolean townhall;
    private int minerals;
    private int gas;
    private double time;
    private Double maxShield;
    @SerializedName("is_flying") private boolean isFlying;
    private List<Weapon> weapons = new ArrayList<>();
    private List<Ability_> abilities = new ArrayList<>();
    private Double maxEnergy;
    private Integer startEnergy;
    @SerializedName("normal_mode") private int normalMode;
    private Integer cargoCapacity;
    @SerializedName("detection_range") private double detectionRange;
    private Double powerRadius;
}

package com.shadowcs.optimizer.sc2data.models;

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
    private Double maxHealth;
    private Double armor;
    private Double sight;
    private Double speed;
    private Double speedCreepMul;
    private List<String> attributes = new ArrayList<String>();
    private Integer size;
    private Double radius;
    private Boolean acceptsAddon;
    private Boolean needsPower;
    private Boolean needsCreep;
    private Boolean needsGeyser;
    private Boolean isStructure;
    private Boolean isAddon;
    private Boolean isWorker;
    private Boolean isTownhall;
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

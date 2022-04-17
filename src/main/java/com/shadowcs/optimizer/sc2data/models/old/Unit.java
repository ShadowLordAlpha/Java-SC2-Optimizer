package com.shadowcs.optimizer.sc2data.models.old;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Unit {
    public ArrayList<AbilityLink> abilities;
    public boolean accepts_addon;
    public double armor;
    public ArrayList<String> attributes;
    public int cargo_size;
    public int gas;
    public int id;
    public boolean is_addon;
    public boolean is_flying;
    public boolean is_structure;
    public boolean is_townhall;
    public boolean is_worker;
    public double max_health;
    public double max_shield;
    public int minerals;
    public String name;
    public boolean needs_creep;
    public boolean needs_geyser;
    public boolean needs_power;
    public String race;
    public double radius;
    public double sight;
    public int size;
    public double speed;
    public double speed_creep_mul;
    public double supply;
    public double time;
    public ArrayList<Weapon> weapons;
    public double max_energy;
    public int start_energy;
    public int normal_mode;
    public int cargo_capacity;
    public double detection_range;
}

package com.shadowcs.optimizer.sc2data.models.old;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Ability {
    public boolean allow_autocast;
    public boolean allow_minimap;
    public ArrayList<Object> buff;
    public double cast_range;
    public int cooldown;
    public ArrayList<Object> effect;
    public int energy_cost;
    public int id;
    public String name;
    public Object target;
}

package com.shadowcs.optimizer.sc2data.models.old;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Weapon {
    public int attacks;
    public ArrayList<Bonus> bonuses;
    public double cooldown;
    public double damage_per_hit;
    public int damage_splash;
    public double range;
    public String target_type;
}

package com.shadowcs.optimizer.sc2data.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Weapon {
    private String targetType;
    private Double damagePerHit;
    private Integer damageSplash;
    private Integer attacks;
    private Double range;
    private Double cooldown;
    private List<Bonuse> bonuses = new ArrayList<Bonuse>();
}

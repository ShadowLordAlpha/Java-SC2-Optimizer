package com.shadowcs.optimizer;

import com.shadowcs.optimizer.sc2data.S2DataUtil;
import com.shadowcs.optimizer.sc2data.models.TechTree;

public class Test {

    public static void main(String...args) {
        TechTree data = S2DataUtil.loadData();

        S2DataUtil.generateGenes(data);

        System.out.println(data.ability().size());
        System.out.println(data.unit().size());
        System.out.println(data.upgrade().size());
        data.ability().forEach(u -> {
            System.out.println("Unit: (" + u.name() + ")" + u.target().getClass().getSimpleName());
        });
    }
}

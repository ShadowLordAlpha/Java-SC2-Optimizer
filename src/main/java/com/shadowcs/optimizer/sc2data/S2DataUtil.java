package com.shadowcs.optimizer.sc2data;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.shadowcs.optimizer.genetics.Gene;
import com.shadowcs.optimizer.sc2data.models.TechTree;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class S2DataUtil {

    public Set<Gene> generateGenes(TechTree tree) {

        Set<Gene> geneSet = new HashSet<>();

        for(var ability: tree.ability()) {
            if(ability.target() instanceof Map<?,?>) {
                LinkedTreeMap target = (LinkedTreeMap) ability.target();
                System.out.println("Wanted: " + ability.name() + " Type: " + target.keySet() + target.values());
            }
        }

        System.out.println();

        return geneSet;
    }

    public TechTree loadData() {

        return new Gson().fromJson(readFromFile("./data/optimizer/89720/data.json"), TechTree.class);
    }

    public String readFromFile(String file) {

        Path path = Paths.get(file);

        try {
            byte[] encoded = Files.readAllBytes(path);
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

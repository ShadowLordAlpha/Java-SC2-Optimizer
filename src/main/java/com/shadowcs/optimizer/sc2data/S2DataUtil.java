package com.shadowcs.optimizer.sc2data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shadowcs.optimizer.sc2data.models.AbilityS2Data;
import com.shadowcs.optimizer.sc2data.models.UnitS2Data;
import com.shadowcs.optimizer.sc2data.models.UpgradeS2Data;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class S2DataUtil {

    public Set<UnitS2Data> loadUnitData() {

        Type listType = new TypeToken<Set<UnitS2Data>>(){}.getType();
        return new Gson().fromJson(readFromFile("./data/optimizer/87702/unit_data.json"), listType);
    }

    public Set<AbilityS2Data> loadAbilityData() {
        Type listType = new TypeToken<Set<AbilityS2Data>>(){}.getType();
        return new Gson().fromJson(readFromFile("./data/optimizer/87702/ability_data.json"), listType);
    }

    public Set<UpgradeS2Data> loadUpgradeData() {
        Type listType = new TypeToken<Set<UpgradeS2Data>>(){}.getType();
        return new Gson().fromJson(readFromFile("./data/optimizer/87702/upgrade_data.json"), listType);
    }

    public String readResDataFile() {

        // for static access, uses the class name directly
        InputStream is = S2DataUtil.class.getClassLoader().getResourceAsStream("com/shadowcs/optimizer/sc2data/data.json");

        return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
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

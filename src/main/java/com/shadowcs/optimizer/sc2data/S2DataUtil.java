package com.shadowcs.optimizer.sc2data;

import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@UtilityClass
public class S2DataUtil {



    public String readResDataFile() {

        // for static access, uses the class name directly
        InputStream is = S2DataUtil.class.getClassLoader().getResourceAsStream("com/shadowcs/optimizer/sc2data/data.json");

        return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
    }

    public void readFromFile(String file) {

        Path path = Paths.get(file);

        try {
            String read = Files.readAllLines(path).get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

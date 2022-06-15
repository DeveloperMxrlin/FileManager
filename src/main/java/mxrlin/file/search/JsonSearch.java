/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.search;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mxrlin.file.FileManager;
import mxrlin.file.misc.ObjectType;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonSearch extends DefaultSearch{

    @Override
    public Map<?, ?> getMapWithHighestPoints(Map<String, String> allMaps, File file, List<Entry> changedValues) {

        Gson gson = new Gson();
        JsonObject jsonObject;
        try {
            jsonObject = gson.fromJson(new BufferedReader(new FileReader(file)), JsonObject.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        Map<String, Object> configurationKeys = entrySetToMap(jsonObject.entrySet());

        return getMapWithHighestPointsEasy(allMaps, configurationKeys, changedValues);
    }

    private Map<String, Object> entrySetToMap(Set<Map.Entry<String, JsonElement>> entrySet){

        Map<String, Object> map = new HashMap<>();

        for(Map.Entry<String, JsonElement> entry : entrySet){
            map.put(entry.getKey(),
                    ObjectType.getStringAsObjectTypeObject(
                            ObjectType.getTypeOfString(
                                    entry.getValue().getAsString()),
                            entry.getValue().getAsString()
                    ));
        }

        return map;

    }
}

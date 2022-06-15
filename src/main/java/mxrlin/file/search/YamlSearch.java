/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.search;

import com.google.common.collect.Maps;
import mxrlin.file.FileManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class YamlSearch extends DefaultSearch {

    /*

            needed Values:
             - File pluginDirectory
             - File editedFile

            returns:
             - HashMap with Values of editedFile

            aim of algorithm

            getMainClass of Plugin -> search for HashMaps + get Methods from onEnable (ASM) -> go
            in methods of ASM, get Class of method, search for HashMaps + more deep

            all HashMaps in a Map<ClassName(String), FieldName(String)>,
            get Maps in foreach, get their values
            get points in direction with file
            highest points -> winner
            winner -> return

             */

    @Override
    public Map<?, ?> getMapWithHighestPoints(Map<String, String> allMaps, File file, List<Entry> changedValues) {

        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        Map<String, Object> configurationKeys = yamlConfiguration.getValues(true);

        return getMapWithHighestPointsEasy(allMaps, configurationKeys, changedValues);
    }

}

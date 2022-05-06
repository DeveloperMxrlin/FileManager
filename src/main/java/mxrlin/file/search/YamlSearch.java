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

        String fileName = file.getName().split("\\.")[0];
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        Map<String, Object> configurationKeys = yamlConfiguration.getValues(true);

        double basic = 1/((double) configurationKeys.size()-(double) changedValues.size());
        double pointsEachKey = basic*512;
        double pointsEachVal = basic*128;

        int changedValuesWithNewKey = 0;
        int changedValuesWithOldKey = 0;
        for(Entry entry : changedValues){
            if(entry.isNewEntry()) changedValuesWithNewKey++;
            else changedValuesWithOldKey++;
        }

        double maxPoints = ( pointsEachKey * ( configurationKeys.size() - changedValuesWithNewKey ) )
                + ( pointsEachVal * ( configurationKeys.size() - changedValuesWithOldKey ) );
        double minPoints = maxPoints * 0.3;

        FileManager.getInstance().debug("basic " + basic);
        FileManager.getInstance().debug("ptseachkey " + pointsEachKey);
        FileManager.getInstance().debug("ptseachval " + pointsEachVal);

        FileManager.getInstance().debug("Max Points: " + maxPoints);
        FileManager.getInstance().debug("Min Points: " + minPoints);

        FileManager.getInstance().debug("ChangedValsWithNewKey " + changedValuesWithNewKey);

        double highestPoints = 0;
        Map<?, ?> winnerMap = null;

            for(Map.Entry<String, String> entry : allMaps.entrySet()){

                try{

                    Map<?, ?> map = getMapByClassAndFieldName(entry.getKey(), entry.getValue());

                    FileManager.getInstance().debug(entry.getKey() + "#" + entry.getValue() + " Map is " + (map == null ? "null" : "not null"));
                    if(map == null) continue;

                    double points = 0D;

                    for(String configKey : configurationKeys.keySet()){

                        for(Object mapKey : map.keySet()){

                            String mapKeyStr = String.valueOf(mapKey);
                            if(mapKeyStr.equalsIgnoreCase(configKey)){
                                points += pointsEachKey;
                                FileManager.getInstance().debug(entry.getKey() + "#" + entry.getValue() + ": same Key (" + configKey + "=" + mapKeyStr + "), ++ " + pointsEachKey);
                                if(configurationKeys.get(configKey).equals(map.get(mapKeyStr))){
                                    FileManager.getInstance().debug(entry.getKey() + "#" + entry.getValue() + ": same value ++ " + pointsEachVal);
                                    points += pointsEachVal;
                                }
                                break;
                            }

                        }

                    }

                    FileManager.getInstance().debug(entry.getKey() + "#" + entry.getValue() + " HashMap points: " + points);

                    if(points > highestPoints){
                        FileManager.getInstance().debug("Bigger then current highest (" + points + ">" + highestPoints + ")");
                        highestPoints = points;
                        winnerMap = map;
                    }

                }catch (Exception e){
                    FileManager.getInstance().debug("Failed to get points of the HashMap in " + entry.getKey() + "#" + entry.getValue() + ". Reason: " + e.getMessage());
                }

            }

        if(highestPoints < minPoints) return null;

        return winnerMap;
    }

}

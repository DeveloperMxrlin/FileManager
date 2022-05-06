package mxrlin.file.search;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface Search {

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

    Plugin getPluginOfDirectory(File pluginDirectory);

    Map<String, String> getAllHashMaps(Plugin plugin) throws Exception;

    Map<?, ?> getMapWithHighestPoints(Map<String, String> allMaps, File file, List<Entry> changedValues);

    public class Entry {

        private String key;
        private Object value;
        private boolean isNewEntry;

        public Entry(String key, Object value, boolean isNewEntry) {
            this.key = key;
            this.value = value;
            this.isNewEntry = isNewEntry;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public boolean isNewEntry() {
            return isNewEntry;
        }

    }

}

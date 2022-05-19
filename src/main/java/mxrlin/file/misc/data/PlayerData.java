/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.misc.data;

import java.util.HashMap;
import java.util.Map;

public class PlayerData {

    private Map<String, Object> data;

    public Map<String, Object> changedValues; // used for file

    public PlayerData(){
        this.data = new HashMap<>();
        this.changedValues = new HashMap<>();
    }

    public boolean keyIsValid(String key){
        return key.split(":").length == 1;
    }

    public void addData(String key, Object obj){
        data.remove(key.toLowerCase());
        data.put(key.toLowerCase(), obj);
    }

    public void remKey(String key){
        data.remove(key.toLowerCase());
    }

    public boolean keyIsSet(String key){
        return data.containsKey(key.toLowerCase());
    }

    public <V> V getData(String key){
        return (V) data.get(key.toLowerCase());
    }

    public void addDataIfKeyNotSet(String key, Object obj){
        if(!keyIsSet(key.toLowerCase())) addData(key.toLowerCase(), obj);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public SaveType getNextSaveType(SaveType type){
        switch (type){
            case SEARCH_RELOAD:
                return SaveType.SEARCH;
            case SEARCH:
                return SaveType.RELOAD;
            case RELOAD:
                return SaveType.NOTHING;
            case NOTHING:
                return SaveType.SEARCH_RELOAD;
            default:
                return null;
        }
    }

    public InformationType getNextInformationType(InformationType type){
        switch (type){
            case ALL:
                return InformationType.NOTHING;
            case NOTHING:
                return InformationType.ALL;
            default:
                return null;
        }
    }

    public static class Prefixes {

        public static final String FILE_MANAGER = "general:";
        public static final String DIRECTORY_WATCHER = "dir:";
        public static final String YAML_EDITOR = "yaml:";
        public static final String ENTRY_CREATOR = "entrycreator:";

    }

    public static enum SaveType {

        SEARCH_RELOAD,
        SEARCH,
        RELOAD,
        NOTHING;

    }

    public static enum InformationType {

        ALL,
        NOTHING;

    }

}

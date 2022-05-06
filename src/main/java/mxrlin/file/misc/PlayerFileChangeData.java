package mxrlin.file.misc;

import java.io.File;
import java.util.Map;

public class PlayerFileChangeData {

    public Map<String, Object> changedValues;
    private SaveType saveType;
    private InformationType informationType;
    private File file;

    public PlayerFileChangeData(Map<String, Object> changedValues, SaveType saveType, InformationType informationType, File file) {
        this.changedValues = changedValues;
        this.saveType = saveType;
        this.informationType = informationType;
        this.file = file;
    }

    public SaveType getSaveType() {
        return saveType;
    }

    public void setSaveType(SaveType saveType) {
        this.saveType = saveType;
    }

    public InformationType getInformationType() {
        return informationType;
    }

    public void setInformationType(InformationType informationType) {
        this.informationType = informationType;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public SaveType getNextSaveType(){
        switch (saveType){
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

    public InformationType getNextInformationType(){
        switch (informationType){
            case ALL:
                return InformationType.NOTHING;
            case NOTHING:
                return InformationType.ALL;
            default:
                return null;
        }
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

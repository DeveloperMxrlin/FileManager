/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.inventorys.editor;

import mxrlin.file.inventorys.editor.file.FileEditor;
import mxrlin.file.inventorys.editor.file.JarEditor;
import mxrlin.file.inventorys.editor.file.JsonEditor;
import mxrlin.file.inventorys.editor.file.YamlEditor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EditorManager {

    public static final EditorManager INSTANCE = new EditorManager();

    private final Map<String, FileEditor> editorMap = new HashMap<>();

    private EditorManager(){
        loadEditorMap();
    }

    private void loadEditorMap(){
        editorMap.put("yml", new YamlEditor());
        editorMap.put("jar", new JarEditor());
        editorMap.put("json", new JsonEditor());
    }

    public FileEditor getEditorForFile(File file){

        String[] fileNameSplit = file.getName().split("\\.");
        String ending = fileNameSplit[fileNameSplit.length-1];

        if(ending == null || ending.isEmpty()) return null;
        if(!editorMap.containsKey(ending)) return null;

        try {
            return editorMap.get(ending).getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;

    }

    public void addOwnFileEditor(String ending, FileEditor editor, boolean overwriteExisting){
        if(editorMap.containsKey(ending) && !overwriteExisting) return;
        if(editorMap.containsKey(ending))
            editorMap.remove(ending);
        editorMap.put(ending, editor);
    }

}

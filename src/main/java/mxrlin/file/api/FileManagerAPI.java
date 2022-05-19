/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.api;

import fr.minuskube.inv.SmartInventory;
import mxrlin.file.inventorys.DirectoryInventory;
import mxrlin.file.inventorys.editor.EditorManager;
import mxrlin.file.inventorys.editor.file.FileEditor;

import java.io.File;

public class FileManagerAPI {

    public static final FileManagerAPI INSTANCE = new FileManagerAPI();

    private FileManagerAPI(){}

    public void addFileEditor(String ending, FileEditor editor){
        EditorManager.INSTANCE.addOwnFileEditor(ending, editor, true);
    }

    public SmartInventory getInventoryForFile(File file){
        if(file.isDirectory()){
            return getInventoryForDir(file);
        }
        FileEditor fileEditor = EditorManager.INSTANCE.getEditorForFile(file);
        return fileEditor.getInventory(file);
    }

    public SmartInventory getInventoryForDir(File dir){
        if(dir.isFile()){
            return getInventoryForFile(dir);
        }
        return DirectoryInventory.getDirectoryInventory(dir);
    }

}

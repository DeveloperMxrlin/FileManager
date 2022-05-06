/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.inventorys.editor.file;

import fr.minuskube.inv.SmartInventory;

import java.io.File;

public interface FileEditor {

    SmartInventory getInventory(File file);

}

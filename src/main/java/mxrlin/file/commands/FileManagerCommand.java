/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.commands;

import mxrlin.file.FileManager;
import mxrlin.file.api.FileManagerAPI;
import mxrlin.file.inventorys.DirectoryInventory;
import mxrlin.file.misc.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.FileSystems;

public class FileManagerCommand implements CommandExecutor {

    private static final File startingDir = new File("plugins");

    public static boolean isStartingDir(File dir){
        if(dir.isFile()) return false;
        return dir.equals(startingDir);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("§cNo Player!");
            return true;
        }

        Player player = (Player) sender;
        if(!player.hasPermission("filemanager.use")){
            sender.sendMessage("§cYou don't have the required permission to execute that command!");
            return true;
        }

        if(args.length == 0){
            DirectoryInventory.getDirectoryInventory(startingDir).open(player);
            return true;
        }

        String str = Utils.stringArrToString(args);
        String separator = File.separator;
        String filePath = str
                .replace(" ", separator)
                .replace("//", separator)
                .replace("/", separator)
                .replace("\\", separator);

        if(filePath.startsWith("plugins" + separator)) filePath = filePath.substring(6 + separator.length());

        File file = new File(startingDir, filePath);
        if(!file.exists()){
            DirectoryInventory.getDirectoryInventory(startingDir).open(player);
            player.sendMessage("§cThere was no file found called \"plugins\\" + filePath + "\".");
            return true;
        }
        if(file.isFile()) FileManagerAPI.INSTANCE.getInventoryForFile(file).open(player);
        else DirectoryInventory.getDirectoryInventory(file).open(player);

        return true;
    }
}

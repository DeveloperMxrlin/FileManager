/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.commands;

import mxrlin.file.inventorys.DirectoryInventory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class FileManagerCommand implements CommandExecutor {
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

        DirectoryInventory.getDirectoryInventory(new File("plugins")).open(player);

        // TODO: 26.05.2022 open directory inventory with more args 

        return true;
    }
}

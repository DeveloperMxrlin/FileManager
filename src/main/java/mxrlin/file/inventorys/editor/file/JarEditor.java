/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.inventorys.editor.file;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryListener;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import mxrlin.file.FileManager;
import mxrlin.file.inventorys.DirectoryInventory;
import mxrlin.file.misc.Utils;
import mxrlin.file.misc.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.IOException;

public class JarEditor implements FileEditor {
    @Override
    public SmartInventory getInventory(File file) {

        try {
            if(!Utils.isSubDirectory(FileManager.pluginDirectory, file)){
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String title = "§8" + file.getPath();
        title = title.replace("\\", "§0/§8");
        title = DirectoryInventory.shortTitle(title);

        return SmartInventory.builder()
                .manager(FileManager.getInstance().getManager())
                .size(4, 9)
                .title(title)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents inventoryContents) {

                        Plugin plugin = getPlugin(file);
                        PluginDescriptionFile descriptionFile = plugin.getDescription();

                        inventoryContents.set(0, 3, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                                .setDisplayname("§7Main Class Path").setLore("§8§m-----", "§a" + descriptionFile.getMain())
                                .build()));
                        inventoryContents.set(0, 4, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                                .setDisplayname("§7Plugin Name").setLore("§8§m-----", "§a" + descriptionFile.getName())
                                .build()));
                        inventoryContents.set(0, 5, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                                .setDisplayname("§7Plugin Version").setLore("§8§m-----", "§a" + descriptionFile.getVersion())
                                .build()));
                        inventoryContents.set(1, 0, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                                .setDisplayname("§7Description").setLore("§8§m-----", "§a" + descriptionFile.getDescription())
                                .build()));
                        inventoryContents.set(1, 1, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                                .setDisplayname("§7API-Version").setLore("§8§m-----", "§a" + descriptionFile.getAPIVersion())
                                .build()));
                        inventoryContents.set(1, 2, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                                .setDisplayname("§7Load").setLore("§8§m-----", "§a" + descriptionFile.getLoad())
                                .build()));
                        inventoryContents.set(1, 3, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                                .setDisplayname("§7Author(s)").setLore("§8§m-----", "§a" + descriptionFile.getAuthors())
                                .build()));
                        inventoryContents.set(1, 4, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                                .setDisplayname("§7Website").setLore("§8§m-----", "§a" + descriptionFile.getWebsite())
                                .build()));
                        inventoryContents.set(1, 5, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                                .setDisplayname("§7Depend").setLore("§8§m-----", "§a" + descriptionFile.getDepend())
                                .build()));
                        inventoryContents.set(1, 6, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                                .setDisplayname("§7Prefix").setLore("§8§m-----", "§a" + descriptionFile.getPrefix())
                                .build()));
                        inventoryContents.set(1, 7, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                                .setDisplayname("§7Soft Depend").setLore("§8§m-----", "§a" + descriptionFile.getSoftDepend())
                                .build()));
                        inventoryContents.set(1, 8, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                                .setDisplayname("§7Load Before").setLore("§8§m-----", "§a" + descriptionFile.getLoadBefore())
                                .build()));
                        inventoryContents.set(2, 4, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                                .setDisplayname("§7Libraries").setLore("§8§m-----", "§a" + descriptionFile.getLibraries())
                                .build()));
                        inventoryContents.set(3, 2, ClickableItem.empty(new ItemBuilder(Material.COMMAND_BLOCK)
                                .setDisplayname("§7Commands").setLore("§8§m-----", "§a" + descriptionFile.getCommands())
                                .build()));
                        inventoryContents.set(3, 6, ClickableItem.empty(new ItemBuilder(Material.BOOK)
                                .setDisplayname("§7Permissions").setLore("§8§m-----", "§a" + descriptionFile.getPermissions())
                                .build()));

                    }

                    @Override
                    public void update(Player player, InventoryContents inventoryContents) {

                    }
                })
                .listener(new InventoryListener<>(InventoryCloseEvent.class, inventoryCloseEvent -> {

                    if(!DirectoryInventory.updatingInventorys.contains(inventoryCloseEvent.getPlayer().getUniqueId())){

                        File parentDirectory = file.getParentFile();
                        Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> DirectoryInventory.getDirectoryInventory(parentDirectory).open((Player) inventoryCloseEvent.getPlayer()), 2);

                    }

                })).build();
    }

    private Plugin getPlugin(File file){

        String fileName = file.getName().split("\\.")[0];
        Plugin pluginFileName = Bukkit.getPluginManager().getPlugin(fileName);
        if(pluginFileName != null){
            return pluginFileName;
        }

        int possiblePoints = fileName.length() + 10;
        double minPoints = possiblePoints * 0.75;

        int highestPoints = 0;
        Plugin highestPointsPlugin = null;

        for(Plugin plugin : Bukkit.getPluginManager().getPlugins()){

            PluginDescriptionFile descriptionFile = plugin.getDescription();
            if(fileName.equalsIgnoreCase(descriptionFile.getName())){
                return plugin;
            }

            int points = plugin.getName().length() == fileName.length() ? 10 : 0;
            if(fileName.length() >= plugin.getName().length()){
                for(int i = 0; i < fileName.toCharArray().length; i++){
                    if(i > plugin.getName().toCharArray().length) break;

                    char c = fileName.charAt(i);
                    char c1 = plugin.getName().charAt(i);
                    if(c == c1){
                        points++;
                    }
                }
            }else {
                for(int i = 0; i < plugin.getName().toCharArray().length; i++){
                    if(i > fileName.toCharArray().length) break;

                    char c = fileName.charAt(i);
                    char c1 = plugin.getName().charAt(i);
                    if(c == c1){
                        points++;
                    }
                }
            }

            if(points > highestPoints){
                highestPoints = points;
                highestPointsPlugin = plugin;
            }

        }

        if(highestPoints > minPoints){
            return highestPointsPlugin;
        }

        return null;

    }

}

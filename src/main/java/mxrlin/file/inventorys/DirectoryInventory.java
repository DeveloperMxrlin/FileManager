/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.inventorys;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryListener;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import mxrlin.file.FileManager;
import mxrlin.file.inventorys.editor.EditorManager;
import mxrlin.file.inventorys.editor.file.FileEditor;
import mxrlin.file.misc.item.ItemBuilder;
import mxrlin.file.misc.item.Skull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class DirectoryInventory {

    public static final int MAX_TITLE_LENGTH = 30;

    public static String shortTitle(String title){
        if(title.length() <= MAX_TITLE_LENGTH) return title;

        title = "..." + title.substring(title.length()-(MAX_TITLE_LENGTH-3));

        return title;

    }

    public static List<UUID> updatingInventorys = new ArrayList<>();

    public static SmartInventory getDirectoryInventory(File directory){
        if(!directory.isDirectory()) return null;
        if(directory.listFiles() == null) return null;

        String title = "§8" + directory.getPath();
        title = title.replace("\\", "§0/§8");
        title = shortTitle(title);

        return SmartInventory.builder()
                .manager(FileManager.getInstance().getManager())
                .size(6, 9)
                .title(title)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents inventoryContents) {

                        inventoryContents.fillBorders(ClickableItem.empty(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayname(" ").build()));

                        Pagination pagination = inventoryContents.pagination();

                        List<ClickableItem> items = new ArrayList<>();

                        for(File file : directory.listFiles()){
                            items.add(ClickableItem.of(new ItemBuilder(getMaterialFile(file)).setDisplayname("§7"+file.getName())
                                            .setLore(getLoreFile(file)).build(),
                                    inventoryClickEvent -> {
                                        ItemStack itemStack = inventoryClickEvent.getCurrentItem();
                                        String fileName = itemStack.getItemMeta().getDisplayName().replace("§7", "");
                                        File clickedFile = new File(directory, fileName);
                                        if(!clickedFile.exists()) {
                                            System.err.println("The Clicked File doesn't exist!");
                                            return;
                                        }

                                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

                                        if(clickedFile.isDirectory() && clickedFile.listFiles() != null){
                                            updatingInventorys.add(player.getUniqueId());
                                            getDirectoryInventory(clickedFile).open(player);
                                            updatingInventorys.remove(player.getUniqueId());
                                            return;
                                        }

                                        FileEditor editor = EditorManager.INSTANCE.getEditorForFile(clickedFile);
                                        if(editor != null){
                                            SmartInventory inventory = editor.getInventory(clickedFile);
                                            if(inventory == null) {
                                                player.sendMessage("§cWhilst trying to open \"" + clickedFile.getName() + "\" the FileEditor had an error.");
                                                return;
                                            }
                                            updatingInventorys.add(player.getUniqueId());
                                            inventory.open(player);
                                            updatingInventorys.remove(player.getUniqueId());
                                        }else{
                                            player.sendMessage("§cThere is no FileEditor for \"" + clickedFile.getName() + "\" registered.");
                                        }

                                    }));
                        }

                        Collections.sort(items, new Comparator<ClickableItem>() {
                            // priority list: (higher is better)
                            // 3 .jar
                            // 2 directory
                            // 1 file
                            @Override
                            public int compare(ClickableItem click1, ClickableItem click2) {
                                switch (click1.getItem().getType()){
                                    case NETHER_STAR:
                                        // -1 or 0
                                        switch (click2.getItem().getType()){
                                            case NETHER_STAR:
                                                return 0;
                                            case BIRCH_WOOD:
                                            case PAPER:
                                                return -1;
                                            default:
                                                break;
                                        }
                                        break;
                                    case BIRCH_WOOD:
                                        switch (click2.getItem().getType()){
                                            case NETHER_STAR:
                                                return 1;
                                            case BIRCH_WOOD:
                                                return 0;
                                            case PAPER:
                                                return -1;
                                            default:
                                                break;
                                        }
                                        // -1, 0 or 1
                                        break;
                                    case PAPER:
                                        switch (click2.getItem().getType()){
                                            case PAPER:
                                                return 0;
                                            case NETHER_STAR:
                                            case BIRCH_WOOD:
                                                return 1;
                                            default:
                                                break;
                                        }
                                        // 0 or 1
                                        break;
                                    default:
                                        break;
                                }
                                return 0;
                            }
                        });

                        pagination.setItems(items.toArray(new ClickableItem[0]));
                        pagination.setItemsPerPage(4*7);

                        pagination.addToIterator(inventoryContents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1)
                                .blacklist(2, 0).blacklist(3, 0).blacklist(4, 0).blacklist(5, 0)
                                .blacklist(1, 8).blacklist(2, 8).blacklist(3, 8).blacklist(4, 8));

                        inventoryContents.set(5, 3, ClickableItem.of(new ItemBuilder(Skull.OAK_WOOD_ARROW_LEFT).setDisplayname("§7Previous Page").build(), inventoryClickEvent -> {
                            getDirectoryInventory(directory).open(player, pagination.previous().getPage());
                        }));

                        inventoryContents.set(5, 5, ClickableItem.of(new ItemBuilder(Skull.OAK_WOOD_ARROW_RIGHT).setDisplayname("§7Next Page").build(), inventoryClickEvent -> {
                            getDirectoryInventory(directory).open(player, pagination.next().getPage());
                        }));

                    }

                    @Override
                    public void update(Player player, InventoryContents inventoryContents) {

                    }
                })
                .listener(new InventoryListener<>(InventoryCloseEvent.class, inventoryCloseEvent -> {

                    if(directory.getName().equalsIgnoreCase("plugins")) return;

                    File parentDirectory = directory.getParentFile();

                    if(!updatingInventorys.contains(inventoryCloseEvent.getPlayer().getUniqueId()))
                        Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> getDirectoryInventory(parentDirectory).open((Player) inventoryCloseEvent.getPlayer()), 2);

                }))
                .build();
    }

    private static Material getMaterialFile(File file){

        // file = paper
        // directory = LOG
        // jar = nether star

        if(file.isDirectory()) return Material.BIRCH_WOOD;
        if(file.isFile()) {
            String[] fileNameSplit = file.getName().split("\\.");
            if(fileNameSplit[fileNameSplit.length-1].equalsIgnoreCase("jar")){
                return Material.NETHER_STAR;
            }
            return Material.PAPER;
        }
        return Material.BARRIER;
    }

    private static String[] getLoreFile(File file){

        /*

        file = Type: File\n                 Click to see and edit the values of that File!
        directory = Type: Directory\n       Click to see the Files in that Directory!
        jar = Type: Executable Java File\n  This is a plugin!

         */

        if(file.isDirectory()) {
            if(file.listFiles() == null) return new String[]{"§8§m-----","§7Type: §aDirectory", "§cThat Directory is empty!"};
            return new String[]{"§8§m-----","§7Type: §aDirectory", "§7Click to see the Files in that Directory!"};
        }
        if(file.isFile()) {
            String[] fileNameSplit = file.getName().split("\\.");
            if(fileNameSplit[fileNameSplit.length-1].equalsIgnoreCase("jar")){
                return new String[]{"§8§m-----", "§7Type: §aExecutable Java File", "§7This is a plugin!"};
            }
            return new String[]{"§8§m-----", "§7Type: §aFile", "§7Click to see and edit the values of that File!"};
        }
        return new String[]{"§8§m-----", "§7Type: §c§m" + file.getName().split("\\.")[0]}; // -> Not supported

    }

}

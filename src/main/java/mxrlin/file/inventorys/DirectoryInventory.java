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
import mxrlin.file.commands.FileManagerCommand;
import mxrlin.file.inventorys.editor.EditorManager;
import mxrlin.file.inventorys.editor.file.FileEditor;
import mxrlin.file.misc.ObjectType;
import mxrlin.file.misc.data.PlayerData;
import mxrlin.file.misc.item.ItemBuilder;
import mxrlin.file.misc.item.LineBuilder;
import mxrlin.file.misc.item.Skull;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

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

                        ClickableItem glass = ClickableItem.empty(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayname(" ").build());

                        inventoryContents.fillBorders(glass);
                        inventoryContents.fillColumn(7, glass);
                        inventoryContents.fillRow(5, glass);

                        Pagination pagination = inventoryContents.pagination();

                        List<ClickableItem> items = new ArrayList<>();

                        for(File file : directory.listFiles()){
                            items.add(ClickableItem.of(new ItemBuilder(getMaterialFile(file)).setDisplayname("§7"+file.getName())
                                            .setLore(getLoreFile(file)).build(),
                                    inventoryClickEvent -> {

                                PlayerData data = FileManager.getInstance().getPlayerData(inventoryClickEvent.getWhoClicked().getUniqueId());

                                        ItemStack itemStack = inventoryClickEvent.getCurrentItem();
                                        String fileName = itemStack.getItemMeta().getDisplayName().replace("§7", "");
                                        File clickedFile = new File(directory, fileName);

                                        data.addData(PlayerData.Prefixes.DIRECTORY_WATCHER + "selfile", clickedFile);

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
                        pagination.setItemsPerPage(4*6);

                        pagination.addToIterator(inventoryContents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1)
                                .blacklist(2, 0).blacklist(3, 0).blacklist(4, 0).blacklist(5, 0)
                                .blacklist(1, 8).blacklist(2, 8).blacklist(3, 8).blacklist(4, 8)
                                .blacklist(1, 7).blacklist(2, 7).blacklist(3, 7).blacklist(4, 7));

                        inventoryContents.set(5, 2, ClickableItem.of(new ItemBuilder(Skull.OAK_WOOD_ARROW_LEFT).setDisplayname("§7Previous Page").build(), inventoryClickEvent -> {
                            getDirectoryInventory(directory).open(player, pagination.previous().getPage());
                        }));

                        inventoryContents.set(5, 3, ClickableItem.of(new ItemBuilder(Skull.OAK_WOOD_ARROW_RIGHT).setDisplayname("§7Next Page").build(), inventoryClickEvent -> {
                            getDirectoryInventory(directory).open(player, pagination.next().getPage());
                        }));

                        inventoryContents.set(5, 5, ClickableItem.of(new ItemBuilder(Skull.LIME_PLUS).setDisplayname("§7Create File").build(), inventoryClickEvent -> {



                            updatingInventorys.add(player.getUniqueId());
                            player.closeInventory();
                            updatingInventorys.remove(player.getUniqueId());
                            new AnvilGUI.Builder()
                                    .onClose(player1 -> {
                                        if(!updatingInventorys.contains(player1.getUniqueId())){
                                            player1.sendMessage("§cYou closed the inventory and the input won't be saved.");
                                            player1.playSound(player1.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                        }else updatingInventorys.remove(player1.getUniqueId());
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> getDirectoryInventory(directory).open(player1), 5);
                                    })
                                    .onComplete((player1, s) -> {

                                        File file = new File(directory, s);

                                        if(file.exists()) player1.sendMessage("§cThere is already a file existing with the name \"" + s + "\"!");
                                        else{

                                            try {
                                                file.createNewFile();
                                                player1.sendMessage("§7You successfully created a new File with the name \"" + file.getName() + "\"!");
                                            } catch (IOException e) {
                                                FileManager.getInstance().getLogger().log(Level.SEVERE, "While trying to create a file something wasn't working: " + e.getMessage());
                                                player1.sendMessage("§cCouldn't create new file. Please try again later.");
                                            }

                                        }

                                        updatingInventorys.add(player1.getUniqueId());
                                        return AnvilGUI.Response.close();
                                    })
                                    .text(" ")
                                    .title("§7Create the file")
                                    .plugin(FileManager.getInstance())
                                    .itemLeft(new ItemBuilder(Material.NAME_TAG).setDisplayname("§7File Name").build())
                                    .open(player);

                        }));

                    }

                    @Override
                    public void update(Player player, InventoryContents inventoryContents) {

                        PlayerData data = FileManager.getInstance().getPlayerData(player.getUniqueId());
                        if(!data.keyIsSet(PlayerData.Prefixes.DIRECTORY_WATCHER + "selfile")){
                            inventoryContents.set(1, 8, ClickableItem.empty(new ItemBuilder(Material.BARRIER).setDisplayname("Select a File").build()));
                            inventoryContents.set(2, 8, ClickableItem.empty(new ItemBuilder(Material.BARRIER).setDisplayname("Select a File").build()));
                            inventoryContents.set(3, 8, ClickableItem.empty(new ItemBuilder(Material.BARRIER).setDisplayname("Select a File").build()));
                            inventoryContents.set(4, 8, ClickableItem.empty(new ItemBuilder(Material.BARRIER).setDisplayname("Select a File").build()));
                            return;
                        }

                        File clickedFile = data.getData(PlayerData.Prefixes.DIRECTORY_WATCHER + "selfile");

                        inventoryContents.set(1, 8, ClickableItem.of(new ItemBuilder(Material.NAME_TAG)
                                .setDisplayname("§7Rename " + (clickedFile.isFile() ? "File" : "Directory"))
                                .setLore(new LineBuilder()
                                        .addLine("§8§m-----")
                                        .addLine("§7Rename the current " + (clickedFile.isFile() ? "File" : "Directory"))
                                        .addLine("§8§m-----").addLine("§7Current " + (clickedFile.isFile() ? "File" : "Directory") + "'s Name:", "§7")
                                        .addLine("§8> §7" + clickedFile.getName(), "§7")).build(), inventoryClickEvent -> {

                            if(!clickedFile.exists()){
                                player.sendMessage("§cThe clicked file seems to be deleted! Try to update the inventory.");
                                return;
                            }

                            data.remKey(PlayerData.Prefixes.DIRECTORY_WATCHER + "selfile");
                            updatingInventorys.add(player.getUniqueId());
                            player.closeInventory();
                            updatingInventorys.remove(player.getUniqueId());
                            new AnvilGUI.Builder()
                                    .onClose(player1 -> {
                                        if(!updatingInventorys.contains(player1.getUniqueId())){
                                            player1.sendMessage("§cYou closed the inventory and the input won't be saved.");
                                            player1.playSound(player1.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                        }else updatingInventorys.remove(player1.getUniqueId());
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> getDirectoryInventory(clickedFile.getParentFile()).open(player1), 5);
                                    })
                                    .onComplete((player1, s) -> {

                                        if(!clickedFile.exists()) player1.sendMessage("§cThe clicked file seems to be deleted, so it can't rename it.");
                                        else{

                                            File parent = clickedFile.getParentFile();

                                            File newFile = new File(parent, s);
                                            if(newFile.exists()) player1.sendMessage("§cYou can't rename it to \"" + s + "\" because that file already exists.");

                                            boolean success = clickedFile.renameTo(newFile);
                                            if(success){
                                                player1.sendMessage("§7You successfully renamed the file to \"" + s + "\"");
                                            }else{
                                                player1.sendMessage("§7Renaming the file didn't work. Please try again later.");
                                            }
                                        }

                                        updatingInventorys.add(player1.getUniqueId());
                                        return AnvilGUI.Response.close();
                                    })
                                    .text(clickedFile.getName())
                                    .title("§7Rename the file")
                                    .plugin(FileManager.getInstance())
                                    .itemLeft(new ItemBuilder(Material.NAME_TAG).setDisplayname("§7" + clickedFile.getName()).build())
                                    .open(player);

                        }));

                        inventoryContents.set(2, 8, ClickableItem.of(new ItemBuilder(Skull.OAK_WOOD_ARROW_RIGHT_UP)
                                .setDisplayname("§7Open " + (clickedFile.isFile() ? "File" : "Directory"))
                                .setLore(new LineBuilder()
                                        .addLine("§8§m-----")
                                        .addLine("§7Open the current " + (clickedFile.isFile() ? "File" : "Directory"))
                                        .addLine("§8§m-----").addLine("§7Current " + (clickedFile.isFile() ? "File" : "Directory") + "'s Name:", "§7")
                                        .addLine("§8> §7" + clickedFile.getName(), "§7"))
                                .build(), inventoryClickEvent -> {

                            if(!clickedFile.exists()) {
                                player.sendMessage("§cThe clicked file seems to be deleted! Try to update the inventory.");
                                return;
                            }

                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

                            if(clickedFile.isDirectory() && clickedFile.listFiles() != null){
                                updatingInventorys.add(player.getUniqueId());
                                data.remKey(PlayerData.Prefixes.DIRECTORY_WATCHER + "selfile");
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
                                data.remKey(PlayerData.Prefixes.DIRECTORY_WATCHER + "selfile");
                                inventory.open(player);
                                updatingInventorys.remove(player.getUniqueId());
                            }else{
                                player.sendMessage("§cThere is no FileEditor for \"" + clickedFile.getName() + "\" registered.");
                            }

                        }));

                        inventoryContents.set(3, 8, ClickableItem.of(new ItemBuilder(Skull.GARBAGE_CAN)
                                .setDisplayname("§7Delete File")
                                .setLore(new LineBuilder()
                                        .addLine("§8§m-----")
                                        .addLine("§7Delete the current " + (clickedFile.isFile() ? "File" : "Directory"))
                                        .addLine("§8§m-----").addLine("§7Current " + (clickedFile.isFile() ? "File" : "Directory") + "'s Name:", "§7")
                                        .addLine("§8> §7" + clickedFile.getName(), "§7")).build(), inventoryClickEvent -> {
                            if(!clickedFile.exists()){
                                player.sendMessage("§cThe clicked file seems to be deleted already!");
                                return;
                            }

                            data.remKey(PlayerData.Prefixes.DIRECTORY_WATCHER + "selfile");
                            boolean bool = clickedFile.delete();
                            if(bool) player.sendMessage("§7You successfully deleted the file \""+clickedFile.getName()+"\"!");
                            else player.sendMessage("§cCouldn't delete the file. Try again later.");

                            updateInv(player, directory);
                        }));
                        
                        inventoryContents.set(4, 8, ClickableItem.of(new ItemBuilder(Skull.MOVEMENT)
                                .setDisplayname("§7Move to")
                                .setLore(new LineBuilder()
                                        .addLine("§8§m-----")
                                        .addLine("§7Move the current " + (clickedFile.isFile() ? "File" : "Directory") + " to an other directory", "§7")
                                        .addLine("§8§m-----")
                                        .addLine("§7Current " + (clickedFile.isFile() ? "File" : "Directory") + "'s Name:", "§7")
                                        .addLine("§8> §7" + clickedFile.getName(), "§7")).build(), inventoryClickEvent -> {
                            // TODO: 26.05.2022 move file to other dir 
                            player.sendMessage(FileManager.getInstance().getImplementationError());
                        }));

                    }
                })
                .listener(new InventoryListener<>(InventoryCloseEvent.class, inventoryCloseEvent -> {

                    if(FileManagerCommand.isStartingDir(directory)) return;

                    File parentDirectory = directory.getParentFile();

                    if(!updatingInventorys.contains(inventoryCloseEvent.getPlayer().getUniqueId())){
                        Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> getDirectoryInventory(parentDirectory).open((Player) inventoryCloseEvent.getPlayer()), 2);
                        FileManager.getInstance().getPlayerData((Player) inventoryCloseEvent.getPlayer()).remKey(PlayerData.Prefixes.DIRECTORY_WATCHER + "selfile");
                    }

                }))
                .build();
    }

    private static void updateInv(Player player, File dir){

        if(!dir.isDirectory()) return;

        updatingInventorys.add(player.getUniqueId());
        player.closeInventory();
        FileManager.getInstance().getPlayerData(player).remKey(PlayerData.Prefixes.DIRECTORY_WATCHER + "selfile");
        updatingInventorys.remove(player.getUniqueId());

        getDirectoryInventory(dir).open(player);

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

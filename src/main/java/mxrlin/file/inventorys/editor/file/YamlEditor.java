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
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import mxrlin.file.FileManager;
import mxrlin.file.inventorys.DirectoryInventory;
import mxrlin.file.inventorys.editor.misc.EntryCreator;
import mxrlin.file.inventorys.editor.misc.ListEditor;
import mxrlin.file.misc.*;
import mxrlin.file.misc.Timer;
import mxrlin.file.misc.data.PlayerData;
import mxrlin.file.misc.item.ItemBuilder;
import mxrlin.file.misc.item.LineBuilder;
import mxrlin.file.misc.item.Skull;
import mxrlin.file.search.Search;
import mxrlin.file.search.YamlSearch;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

public class YamlEditor implements FileEditor {

    private Map<File, SmartInventory> createdInventoryForFile;

    public YamlEditor(){
        createdInventoryForFile = new HashMap<>();
    }

    @Override
    public SmartInventory getInventory(File file) {

        if(createdInventoryForFile.containsKey(file)){
            return createdInventoryForFile.get(file);
        }

        String[] fileNameSplit = file.getName().split("\\.");
        if(!fileNameSplit[fileNameSplit.length-1].equalsIgnoreCase("yml"))
            return null;
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        Set<String> keys = yamlConfiguration.getKeys(true);

        String title = "§8" + file.getPath();
        title = title.replace("\\", "§0/§8");
        title = DirectoryInventory.shortTitle(title);

        SmartInventory inventory = SmartInventory.builder()
                .manager(FileManager.getInstance().getManager())
                .size(6, 9)
                .title(title)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents inventoryContents) {

                        PlayerData data = FileManager.getInstance().getPlayerData(player);
                        data.addDataIfKeyNotSet(PlayerData.Prefixes.YAML_EDITOR + "file", file);
                        data.addDataIfKeyNotSet(PlayerData.Prefixes.YAML_EDITOR + "informationtype", PlayerData.InformationType.ALL);
                        data.addDataIfKeyNotSet(PlayerData.Prefixes.YAML_EDITOR + "savetype", PlayerData.SaveType.SEARCH_RELOAD);

                        ClickableItem glass = ClickableItem.empty(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayname(" ").build());
                        inventoryContents.fillColumn(7, glass);
                        inventoryContents.fillRow(4, glass);
                        inventoryContents.fillRow(5, glass);

                        inventoryContents.set(0, 8, glass);

                        Pagination pagination = inventoryContents.pagination();

                        List<ClickableItem> items = new ArrayList<>();
                        for(String key : data.changedValues.keySet()){
                            if(!keys.contains(key)) keys.add(key);
                        }

                        for(String key : keys){

                            Object val = yamlConfiguration.get(key);
                            if(val instanceof ConfigurationSection) continue;

                            if(data.changedValues.containsKey(key)) val = data.changedValues.get(key);

                            ObjectType type = ObjectType.getType(val);
                            Object finalVal = val;

                            PlayerData.InformationType informationType = data.getData(PlayerData.Prefixes.YAML_EDITOR + "informationtype");

                            ItemStack item;

                            switch (informationType){
                                case ALL:
                                    List<String> comments = yamlConfiguration.getComments(key);
                                    item = new ItemBuilder(Skull.getCustomHead(type.getHeadId()))
                                            .setDisplayname("§7" + key)
                                            .setLore(new LineBuilder()
                                                    .addLine("§8§m-----")
                                                    .addLineIgnoringMaxLength("§8> §7Type: " + type.getDisplayName())
                                                    .addLine("§8> §7Value: " + yamlConfiguration.get(key), "          §7")
                                                    .addLine("§8> §7Comments: " + (comments.isEmpty() ? "None" : ""))
                                                    .addListAsLine(comments, "  §8- §7", ""))
                                            .build();
                                    break;
                                case NOTHING:
                                    item = new ItemBuilder(Material.PAPER).setDisplayname("§7" + key).build();
                                    break;
                                default:
                                    item = new ItemBuilder(Material.PAPER).setDisplayname("?").build();
                                    break;
                            }

                            items.add(ClickableItem.of(item, inventoryClickEvent -> {

                                // TODO: 19.05.2022 add entrywatcher 

                                if(type == ObjectType.NOT_SUPPORTED) {
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                    return;
                                }
                                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if(type == ObjectType.LIST){
                                    ListEditor editor = new ListEditor((List<Object>) finalVal,(inventoryCloseEvent, list) -> {

                                        Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () ->
                                                getInventory(file).open(player), 2);
                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                        data.changedValues.put(key, list);

                                    });
                                    DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                                    editor.getInventory().open(player);
                                    DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                                    return;
                                }
                                DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                                player.closeInventory();
                                DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                                new AnvilGUI.Builder()
                                        .onClose(player1 -> {
                                            if(!DirectoryInventory.updatingInventorys.contains(player1.getUniqueId())){
                                                player1.sendMessage("§cYou closed the inventory and the value won't be saved.");
                                                player1.playSound(player1.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                                Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> getInventory(file).open(player1), 5);
                                            }else DirectoryInventory.updatingInventorys.remove(player1.getUniqueId());
                                        })
                                        .onComplete((player1, s) -> {
                                            Object finalValue = null;
                                            try{
                                                finalValue = ObjectType.getStringAsObjectTypeObject(type, s);
                                            }catch (Exception e){
                                                player1.sendMessage("§7Couldn't convert string to required value type. (" + type.getDisplayName() + ")");
                                            }
                                            if(finalValue == null){
                                                player1.playSound(player1.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                            }else{
                                                data.changedValues.put(key, finalValue);
                                                player1.sendMessage("§7You set the value from the key §a\"" + key + "\" §7in §a\"" + file.getName() + "\" §7to §a\"" + s + "\"");
                                                player1.playSound(player1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                            }
                                            DirectoryInventory.updatingInventorys.add(player1.getUniqueId());
                                            Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> getInventory(file).open(player1), 5);
                                            return AnvilGUI.Response.close();
                                        })
                                        .text(String.valueOf(finalVal))
                                        .title("§7Set a value")
                                        .plugin(FileManager.getInstance())
                                        .itemLeft(new ItemBuilder(Material.PAPER).setDisplayname("§7" + key).build())
                                        .open(player);
                            }));

                        }

                        pagination.setItems(items.toArray(new ClickableItem[0]));
                        pagination.setItemsPerPage(28);

                        pagination.addToIterator(inventoryContents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0)
                                .blacklist(0, 7).blacklist(0, 8)
                                .blacklist(1, 7).blacklist(1, 8)
                                .blacklist(2, 7).blacklist(2, 8));

                        PlayerData.InformationType type = data.getData(PlayerData.Prefixes.YAML_EDITOR + "informationtype");
                        PlayerData.InformationType nextInfo = data.getNextInformationType(type);

                        inventoryContents.set(1, 8, ClickableItem.of(new ItemBuilder(Material.OAK_SIGN)
                                        .setDisplayname("§7Change Information Type")
                                        .setLore(new LineBuilder()
                                                .setStaticPrefix(Utils.textToSpaces(" -> ") + "§7")
                                                .addLine("§8§m-----")
                                            .addLine("§7Current: " + type)
                                            .addLine(" §7-> " + type.getDescription())
                                            .addLine("§8§m-----")
                                            .addLine("§7Next: " + nextInfo)
                                            .addLine(" §7-> " + nextInfo.getDescription()))
                                .build(), inventoryClickEvent -> {
                            data.addData(PlayerData.Prefixes.YAML_EDITOR + "informationtype", nextInfo);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                        }));

                        PlayerData.SaveType saveType = data.getData(PlayerData.Prefixes.YAML_EDITOR + "savetype");
                        PlayerData.SaveType next = data.getNextSaveType(saveType);

                        inventoryContents.set(2, 8, ClickableItem.of(new ItemBuilder(Skull.STRUCTURE_BLOCK_SAVE)
                                        .setDisplayname("§7Change Save Type")
                                        .setLore(new LineBuilder()
                                                .setStaticPrefix(Utils.textToSpaces(" -> ") + "§7")
                                                .addLine("§8§m-----")
                                               .addLine("§7Current: " + saveType)
                                               .addLine(" §7-> " + saveType.getDescription())
                                               .addLine("§8§m-----")
                                               .addLine("§7Next: " + next)
                                               .addLine(" §7-> " + next.getDescription()))
                                .build(), inventoryClickEvent -> {
                            data.addData(PlayerData.Prefixes.YAML_EDITOR + "savetype", next);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                        }));

                        File currentFile = data.getData(PlayerData.Prefixes.YAML_EDITOR + "file");
                        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");

                        try {
                            inventoryContents.set(3, 8, ClickableItem.empty(new ItemBuilder(Skull.OAK_WOOD_QUESTION_MARK)
                                    .setDisplayname("§7File Information")
                                                    .setLore(new LineBuilder()
                                                            .addLine("§8§m-----")
                                                            .addLine("§8> §7Last Edit: " + format.format(currentFile.lastModified()))
                                                            .addLineIgnoringMaxLength("§8> §7Path: " + currentFile.getAbsolutePath())
                                                            .addLine("§8> §7Type: Yaml")
                                                            .addLine("§8> §7Keys: " + (keys.size()-1) + "x")
                                                            .addLine("§8> §7Size: " + Utils.humanReadableByteCountSI(Files.size(file.toPath()))))
                                    .build()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        inventoryContents.set(5, 8, ClickableItem.of(new ItemBuilder(Skull.LIME_PLUS)
                                                .setDisplayname("§7Create an Entry")
                                                .setLore("§8§m-----", "§7Add a new Entry!")
                                        .build(),
                                inventoryClickEvent -> {
                                    EntryCreator creator = new EntryCreator(entry -> {

                                        Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> getInventory(file).open(player));

                                        if(entry.getKey().isEmpty() || entry.getValue() == null){
                                            player.sendMessage("§7The Key or the Value is not set!");
                                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                            return;
                                        }

                                        if(yamlConfiguration.isSet(entry.getKey()) || data.changedValues.containsKey(entry.getKey())){
                                            player.sendMessage("§7The key §c\"" + entry.getKey() + "\" §7is already set! Use the replace function instead of creating a new one!");
                                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                            return;
                                        }

                                        data.changedValues.put(entry.getKey(), entry.getValue());
                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                                    });

                                    DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                                    player.closeInventory();
                                    DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                                    creator.getInventory().open(player);
                        }));

                        int pageCount = (int)Math.ceil((double)items.size() / (double)28);

                        if(pageCount <= 0){ // -> only one site -> no need for page buttons
                            return;
                        }

                        if(!pagination.isFirst()){
                            inventoryContents.set(5, 2, ClickableItem.of(new ItemBuilder(Skull.OAK_WOOD_ARROW_LEFT)
                                            .setDisplayname("§7Previous Page")
                                    .build(), inventoryClickEvent -> {
                                DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                                getInventory(file).open(player, pagination.previous().getPage());
                                DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                            }));
                            inventoryContents.set(5, 1, ClickableItem.of(new ItemBuilder(Skull.GOLDEN_ARROW_LEFT)
                                    .setDisplayname("§7First Page")
                                    .build(), inventoryClickEvent -> {
                                DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                                getInventory(file).open(player, 0);
                                DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                            }));
                        }

                        if(!pagination.isLast()){
                            inventoryContents.set(5, 4, ClickableItem.of(new ItemBuilder(Skull.OAK_WOOD_ARROW_RIGHT)
                                            .setDisplayname("§7Next Page")
                                    .build(), inventoryClickEvent -> {
                                DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                                getInventory(file).open(player, pagination.next().getPage());
                                DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                            }));
                            inventoryContents.set(5, 5, ClickableItem.of(new ItemBuilder(Skull.GOLDEN_ARROW_RIGHT)
                                            .setDisplayname("§7Last Page")
                                    .build(), inventoryClickEvent -> {
                                DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                                getInventory(file).open(player, pageCount-1);
                                DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                            }));
                        }

                        inventoryContents.set(5, 3, ClickableItem.empty(new ItemBuilder(Material.OAK_SIGN)
                                        .setDisplayname("§7" + pagination.getPage() + "§8/§7" + (pageCount-1))
                                .build()));

                    }

                    @Override
                    public void update(Player player, InventoryContents inventoryContents) {

                        PlayerData data = FileManager.getInstance().getPlayerData(player.getUniqueId());

                        Pagination pagination = inventoryContents.pagination();

                        List<ClickableItem> items = new ArrayList<>();

                        for(String key : keys){

                            Object val = yamlConfiguration.get(key);
                            if(val instanceof ConfigurationSection) continue;

                            if(data.changedValues.containsKey(key)) val = data.changedValues.get(key);

                            ObjectType type = ObjectType.getType(val);
                            Object finalVal = val;

                            PlayerData.InformationType informationType = data.getData(PlayerData.Prefixes.YAML_EDITOR + "informationtype");

                            ItemStack item;

                            switch (informationType){
                                case ALL:
                                    List<String> comments = yamlConfiguration.getComments(key);
                                    item = new ItemBuilder(Skull.getCustomHead(type.getHeadId()))
                                            .setDisplayname("§7" + key)
                                            .setLore(new LineBuilder()
                                                    .addLine("§8§m-----")
                                                    .addLineIgnoringMaxLength("§8> §7Type: " + type.getDisplayName())
                                                    .addLine("§8> §7Value: " + yamlConfiguration.get(key), "          §7")
                                                    .addLine("§8> §7Comments: " + (comments.isEmpty() ? "None" : ""))
                                                    .addListAsLine(comments, "  §8- §7", ""))
                                            .build();
                                    break;
                                case NOTHING:
                                    item = new ItemBuilder(Material.PAPER).setDisplayname("§7" + key).build();
                                    break;
                                default:
                                    item = new ItemBuilder(Material.PAPER).setDisplayname("?").build();
                                    break;
                            }

                            items.add(ClickableItem.of(item, inventoryClickEvent -> {

                                if(type == ObjectType.NOT_SUPPORTED) {
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                    return;
                                }
                                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if(type == ObjectType.LIST){
                                    ListEditor editor = new ListEditor((List<Object>) finalVal,(inventoryCloseEvent, list) -> {

                                        Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () ->
                                                getInventory(file).open(player), 2);
                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                        data.changedValues.put(key, list);

                                    });
                                    DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                                    editor.getInventory().open(player);
                                    DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                                    return;
                                }
                                DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                                player.closeInventory();
                                DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                                new AnvilGUI.Builder()
                                        .onClose(player1 -> {
                                            if(!DirectoryInventory.updatingInventorys.contains(player1.getUniqueId())){
                                                player1.sendMessage("§cYou closed the inventory and the value won't be saved.");
                                                player1.playSound(player1.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                                Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> getInventory(file).open(player1), 5);
                                            }else DirectoryInventory.updatingInventorys.remove(player1.getUniqueId());
                                        })
                                        .onComplete((player1, s) -> {
                                            Object finalValue = null;
                                            try{
                                                finalValue = ObjectType.getStringAsObjectTypeObject(type, s);
                                            }catch (Exception e){
                                                player1.sendMessage("§7Couldn't convert string to required value type. (" + type.getDisplayName() + ")");
                                            }
                                            if(finalValue == null){
                                                player1.playSound(player1.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                            }else{
                                                data.changedValues.put(key, finalValue);
                                                player1.sendMessage("§7You set the value from the key §a\"" + key + "\" §7in §a\"" + file.getName() + "\" §7to §a\"" + s + "\"");
                                                player1.playSound(player1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                            }
                                            DirectoryInventory.updatingInventorys.add(player1.getUniqueId());
                                            Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> getInventory(file).open(player1), 5);
                                            return AnvilGUI.Response.close();
                                        })
                                        .text(String.valueOf(finalVal))
                                        .title("§7Set a value")
                                        .plugin(FileManager.getInstance())
                                        .itemLeft(new ItemBuilder(Material.PAPER).setDisplayname("§7" + key).build())
                                        .open(player);

                            }));

                        }

                        pagination.setItems(items.toArray(new ClickableItem[0]));
                        pagination.setItemsPerPage(28);

                        pagination.addToIterator(inventoryContents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0)
                                .blacklist(0, 7).blacklist(0, 8)
                                .blacklist(1, 7).blacklist(1, 8)
                                .blacklist(2, 7).blacklist(2, 8));

                        PlayerData.InformationType type = data.getData(PlayerData.Prefixes.YAML_EDITOR + "informationtype");
                        PlayerData.InformationType nextInfo = data.getNextInformationType(type);

                        inventoryContents.set(1, 8, ClickableItem.of(new ItemBuilder(Material.OAK_SIGN)
                                .setDisplayname("§7Change Information Type")
                                .setLore(new LineBuilder()
                                        .setStaticPrefix(Utils.textToSpaces(" -> ") + "§7")
                                        .addLine("§8§m-----")
                                        .addLine("§7Current: " + type)
                                        .addLine(" §7-> " + type.getDescription())
                                        .addLine("§8§m-----")
                                        .addLine("§7Next: " + nextInfo)
                                        .addLine(" §7-> " + nextInfo.getDescription()))
                                .build(), inventoryClickEvent -> {
                            data.addData(PlayerData.Prefixes.YAML_EDITOR + "informationtype", nextInfo);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                        }));

                        PlayerData.SaveType saveType = data.getData(PlayerData.Prefixes.YAML_EDITOR + "savetype");
                        PlayerData.SaveType next = data.getNextSaveType(saveType);

                        inventoryContents.set(2, 8, ClickableItem.of(new ItemBuilder(Skull.STRUCTURE_BLOCK_SAVE)
                                .setDisplayname("§7Change Save Type")
                                .setLore(new LineBuilder()
                                        .setStaticPrefix(Utils.textToSpaces(" -> ") + "§7")
                                        .addLine("§8§m-----")
                                        .addLine("§7Current: " + saveType)
                                        .addLine(" §7-> " + saveType.getDescription())
                                        .addLine("§8§m-----")
                                        .addLine("§7Next: " + next)
                                        .addLine(" §7-> " + next.getDescription()))
                                .build(), inventoryClickEvent -> {
                            data.addData(PlayerData.Prefixes.YAML_EDITOR + "savetype", next);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                        }));

                    }
                })
                .listener(new InventoryListener<>(InventoryCloseEvent.class, inventoryCloseEvent -> {

                    if(!DirectoryInventory.updatingInventorys.contains(inventoryCloseEvent.getPlayer().getUniqueId())){

                        Player player = (Player) inventoryCloseEvent.getPlayer();
                        File parentDirectory = file.getParentFile();
                        Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> DirectoryInventory.getDirectoryInventory(parentDirectory).open((Player) inventoryCloseEvent.getPlayer()), 2);

                        PlayerData data = FileManager.getInstance().getPlayerData(player);

                        if(!data.changedValues.isEmpty()){
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                            boolean search = false;
                            boolean reload = false;

                            PlayerData.SaveType type = data.getData(PlayerData.Prefixes.YAML_EDITOR + "savetype");

                            switch (type){
                                case SEARCH_RELOAD:
                                    search = true;
                                    reload = true;
                                    break;
                                case SEARCH:
                                    search = true;
                                    break;
                                case RELOAD:
                                    reload = true;
                                    break;
                                default:
                                    break;
                            }

                            long ms = save(data.changedValues, file, search, reload);
                            player.sendMessage("§7You saved the File " + file.getName() + "! The process took " + ms + "ms.");

                        }

                        data.remKey(PlayerData.Prefixes.YAML_EDITOR + "file");

                    }


                }))
                .build();

        createdInventoryForFile.put(file, inventory);
        return inventory;

    }
    
    private long save(Map<String, Object> changed, File file, boolean searchForMap, boolean reloadPlugin){
        
        if(changed.isEmpty()) return -1L;

        Timer timer = new Timer();
        timer.start();

        FileManager.getInstance().debug("Initialization of Saving the File \"" + file.getAbsolutePath() + "\"");
        FileManager.getInstance().debug("Values: changed=" +changed + ",searchForMap=" + searchForMap + ",reloadPlugin=" + reloadPlugin);
        
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);

        List<Search.Entry> changedValues = new ArrayList<>();
        for(String key : changed.keySet()){
            changedValues.add(new Search.Entry(key, changed.get(key), !yamlConfiguration.isSet(key)));
        }
        
        for(String key : changed.keySet()){
            yamlConfiguration.set(key, changed.get(key));
        }

        boolean saveSuccess = true;
        
        try {
            yamlConfiguration.save(file);
        } catch (IOException e) {
            saveSuccess = false;
            e.printStackTrace();
        }

        if(saveSuccess){

            FileManager.getInstance().debug("Successfully saved file with overwritten values");

            YamlSearch yamlSearch = new YamlSearch();
            
            File pluginDir = null;
            File current = file;
            
            while (pluginDir == null) {
                if(current.getParentFile().getName().equals("plugins")){
                    pluginDir = current;
                }else current = current.getParentFile();
            }

            Plugin plugin = yamlSearch.getPluginOfDirectory(pluginDir);
            if(plugin == null){
                FileManager.getInstance().debug("Couldn't find Plugin of pluginDirectory \"" + pluginDir.getAbsolutePath() + "\"");
                return timer.stopAndTime();
            }

            FileManager.getInstance().debug("Found the plugin for pluginDirectory: " + plugin.getName());

            if(searchForMap){

                FileManager.getInstance().debug("Starting to search for a HashMap in the Plugin...");

                try {

                    Map<?, ?> map = yamlSearch.getMapWithHighestPoints(
                            yamlSearch.getAllHashMaps(
                                    plugin),
                            file,
                            changedValues);

                    if(map == null){

                        if(reloadPlugin){
                            PluginUtil.reload(plugin);
                            FileManager.getInstance().debug("No HashMap Found, trying to reload the plugin...");
                            return -1L;
                        }

                    }else{

                        FileManager.getInstance().debug("Found a HashMap! Replacing Values...");
                        replaceValues((Map<String, Object>) map, changed);

                    }

                } catch (Exception e) {
                    FileManager.getInstance().debug("Error while looking for the map! (" + e.getMessage() + ")");
                }

            }else if(reloadPlugin){
                FileManager.getInstance().debug("Reloading the plugin...");
                PluginUtil.reload(plugin);
            }
            
        }

        return timer.stopAndTime();

    }

    private void replaceValues(Map<String, Object> map, Map<String, Object> changedValues){

        for(Object obj : map.keySet()){
            String str = String.valueOf(obj);
            for(String key : changedValues.keySet()){
                if(str.equalsIgnoreCase(key)){
                    map.replace(str, changedValues.get(key));
                }
            }
        }

    }

}

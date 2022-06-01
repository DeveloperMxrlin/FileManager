/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.api;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import mxrlin.file.FileManager;
import mxrlin.file.inventorys.DirectoryInventory;
import mxrlin.file.inventorys.editor.EditorManager;
import mxrlin.file.inventorys.editor.file.FileEditor;
import mxrlin.file.inventorys.editor.misc.EntryCreator;
import mxrlin.file.misc.Utils;
import mxrlin.file.misc.data.PlayerData;
import mxrlin.file.misc.item.ItemBuilder;
import mxrlin.file.misc.item.LineBuilder;
import mxrlin.file.misc.item.Skull;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.io.File;

public abstract class FileEditorHelper {

    private final String type;
    private final String ending;

    protected FileEditorHelper(String type, String ending){

        this.type = type;
        this.ending = ending;

    }

    abstract InventoryProvider inventory(File file);

    void addFileEditor(){
        EditorManager.INSTANCE.addOwnFileEditor(ending, getEditor(null), true);
    }

    InventoryProvider entriesToInventory(ClickableItem[] items, ClickableItem fileInformationItem, EntryCreator entryCreator, File file, String playerDataPrefix){
        if(!playerDataPrefix.endsWith(":")) playerDataPrefix += ":";
        String finalPlayerDataPrefix = playerDataPrefix;
        return new InventoryProvider() {
            @Override
            public void init(Player player, InventoryContents inventoryContents) {

                PlayerData data = FileManager.getInstance().getPlayerData(player);
                data.addDataIfKeyNotSet(finalPlayerDataPrefix + "file", file);
                data.addDataIfKeyNotSet(finalPlayerDataPrefix + "informationtype", PlayerData.InformationType.ALL);
                data.addDataIfKeyNotSet(finalPlayerDataPrefix + "savetype", PlayerData.SaveType.SEARCH_RELOAD);

                ClickableItem glass = ClickableItem.empty(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayname(" ").build());
                inventoryContents.fillColumn(7, glass);
                inventoryContents.fillRow(4, glass);
                inventoryContents.fillRow(5, glass);

                inventoryContents.set(0, 8, glass);

                Pagination pagination = inventoryContents.pagination();

                pagination.setItems(items);
                pagination.setItemsPerPage(28);

                pagination.addToIterator(inventoryContents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0)
                        .blacklist(0, 7).blacklist(0, 8)
                        .blacklist(1, 7).blacklist(1, 8)
                        .blacklist(2, 7).blacklist(2, 8));

                PlayerData.InformationType type = data.getData(finalPlayerDataPrefix + "informationtype");
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
                    data.addData(finalPlayerDataPrefix + "informationtype", nextInfo);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                PlayerData.SaveType saveType = data.getData(finalPlayerDataPrefix + "savetype");
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
                    data.addData(finalPlayerDataPrefix + "savetype", next);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                inventoryContents.set(3, 8, fileInformationItem);

                inventoryContents.set(5, 8, ClickableItem.of(new ItemBuilder(Skull.LIME_PLUS)
                                .setDisplayname("§7Create an Entry")
                                .setLore("§8§m-----", "§7Add a new Entry!")
                                .build(),
                        inventoryClickEvent -> {
                            DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                            player.closeInventory();
                            DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                            entryCreator.getInventory().open(player);
                        }));

                int pageCount = (int)Math.ceil((double)items.length / (double)28);

                if(pageCount <= 0){ // -> only one site -> no need for page buttons
                    return;
                }

                if(!pagination.isFirst()){
                    inventoryContents.set(5, 2, ClickableItem.of(new ItemBuilder(Skull.OAK_WOOD_ARROW_LEFT)
                            .setDisplayname("§7Previous Page")
                            .build(), inventoryClickEvent -> {
                        DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                        getEditor(file).getInventory(file).open(player, pagination.previous().getPage());
                        DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                    }));
                    inventoryContents.set(5, 1, ClickableItem.of(new ItemBuilder(Skull.GOLDEN_ARROW_LEFT)
                            .setDisplayname("§7First Page")
                            .build(), inventoryClickEvent -> {
                        DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                        getEditor(file).getInventory(file).open(player, 0);
                        DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                    }));
                }

                if(!pagination.isLast()){
                    inventoryContents.set(5, 4, ClickableItem.of(new ItemBuilder(Skull.OAK_WOOD_ARROW_RIGHT)
                            .setDisplayname("§7Next Page")
                            .build(), inventoryClickEvent -> {
                        DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                        getEditor(file).getInventory(file).open(player, pagination.next().getPage());
                        DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                    }));
                    inventoryContents.set(5, 5, ClickableItem.of(new ItemBuilder(Skull.GOLDEN_ARROW_RIGHT)
                            .setDisplayname("§7Last Page")
                            .build(), inventoryClickEvent -> {
                        DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                        getEditor(file).getInventory(file).open(player, pageCount-1);
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

                pagination.setItems(items);
                pagination.setItemsPerPage(28);

                pagination.addToIterator(inventoryContents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0)
                        .blacklist(0, 7).blacklist(0, 8)
                        .blacklist(1, 7).blacklist(1, 8)
                        .blacklist(2, 7).blacklist(2, 8));

                PlayerData.InformationType type = data.getData(finalPlayerDataPrefix + "informationtype");
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
                    data.addData(finalPlayerDataPrefix + "informationtype", nextInfo);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                PlayerData.SaveType saveType = data.getData(finalPlayerDataPrefix + "savetype");
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
                    data.addData(finalPlayerDataPrefix + "savetype", next);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

            }
        };
    }

    FileEditor getEditor(File file){

        String title = "";
        if(file != null){
            title = "§8" + file.getPath();
            title = title.replace("\\", "§0/§8");
            title = DirectoryInventory.shortTitle(title);
        }

        return new FileEditor() {
            @Override
            public SmartInventory getInventory(File file) {
                return SmartInventory.builder()
                        .provider(inventory(file))
                        .manager(FileManager.getInstance().getManager())
                        .title("")
                        .size(6, 9)
                        .build();
            }
        };
    }

    public String getType() {
        return type;
    }

    public String getEnding() {
        return ending;
    }

}

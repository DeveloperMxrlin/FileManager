/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.inventorys.editor.misc;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryListener;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import mxrlin.file.FileManager;
import mxrlin.file.inventorys.DirectoryInventory;
import mxrlin.file.misc.ObjectType;
import mxrlin.file.misc.item.ItemBuilder;
import mxrlin.file.misc.item.Skull;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;

public class EntryCreator {

    private Entry currentEntry;
    private Consumer<Entry> consumerOnClose;

    private Map<UUID, String> currentInvState; // TODO: 22.04.2022 add both maps to playerdata (do a map in playerdata with entry you can do custom)
    private Map<UUID, ObjectType> setValueType;

    private final ClickableItem glass = ClickableItem.empty(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayname(" ").build());

    public EntryCreator(Consumer<Entry> consumerOnClose) {

        this.consumerOnClose = consumerOnClose;
        this.currentEntry = new Entry("", null);

        this.currentInvState = new HashMap<>();
        this.setValueType = new HashMap<>();

    }

    public SmartInventory getInventory() {
        return SmartInventory.builder()
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents inventoryContents) {

                        if(!DirectoryInventory.updatingInventorys.contains(player.getUniqueId())){
                            currentInvState.remove(player.getUniqueId());
                            currentInvState.put(player.getUniqueId(), "");
                        }else DirectoryInventory.updatingInventorys.remove(player.getUniqueId());

                        String state = currentInvState.get(player.getUniqueId());

                        inventoryContents.fill(glass);

                        if (state == "") {
                            applyBasicState(player, inventoryContents);
                        }else if(state == "setval"){
                            applySetValState(player, inventoryContents);
                        }

                    }

                    @Override
                    public void update(Player player, InventoryContents inventoryContents) {

                        String state = currentInvState.get(player.getUniqueId());

                        if (state == "") {
                            inventoryContents.fill(glass);
                            applyBasicState(player, inventoryContents);
                        }else if(state == "setval"){
                            inventoryContents.fill(glass);
                            applySetValState(player, inventoryContents);
                        }

                    }

                    private void applyBasicState(Player player, InventoryContents inventoryContents) {
                        inventoryContents.set(1, 2, ClickableItem.of(new ItemBuilder(Material.NAME_TAG)
                                .setDisplayname("§7Key")
                                .setLore("§8§m-----",
                                        "§7Currently: " + (currentEntry.getKey() == null || currentEntry.getKey().isEmpty() ? "§c?" : "§a" + currentEntry.getKey()))
                                .build(), inventoryClickEvent -> {

                            DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                            player.closeInventory();
                            DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                            new AnvilGUI.Builder()
                                    .onClose(player1 -> {
                                        if(!DirectoryInventory.updatingInventorys.contains(player1.getUniqueId())){
                                            player1.sendMessage("§cYou closed the inventory and the input won't be saved.");
                                            player1.playSound(player1.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                            Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> getInventory().open(player1), 5);
                                        }else DirectoryInventory.updatingInventorys.remove(player1.getUniqueId());
                                    })
                                    .onComplete((player1, s) -> {
                                        player1.playSound(player1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                        currentEntry.key = s;
                                        DirectoryInventory.updatingInventorys.add(player1.getUniqueId());
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> getInventory().open(player1), 5);
                                        return AnvilGUI.Response.close();
                                    })
                                    .text("")
                                    .title("§7Set the key of the entry")
                                    .plugin(FileManager.getInstance())
                                    .itemLeft(new ItemBuilder(Material.NAME_TAG).setDisplayname("§7Key").build())
                                    .open(player);

                        }));

                        inventoryContents.set(1, 6, ClickableItem.of(new ItemBuilder(Material.OAK_SIGN)
                                .setDisplayname("§7Value")
                                .setLore("§8§m-----",
                                        "§7Currently: " + (currentEntry.getValue() == null ? "§c?" : "§a" + currentEntry.getValue()))
                                .build(), inventoryClickEvent -> {
                            currentInvState.replace(player.getUniqueId(), "setval");
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                        }));
                    }

                    private void applySetValState(Player player, InventoryContents inventoryContents){
                        Pagination pagination = inventoryContents.pagination();

                        List<ClickableItem> items = new ArrayList<>();

                        for(ObjectType type : ObjectType.values()){

                            ItemStack item = new ItemBuilder(Skull.getCustomHead(type.getHeadId()))
                                    .setDisplayname("§7" + type.getDisplayName())
                                    .setLore("§8§m-----", "§7" + type.getDescription())
                                    .build();

                            items.add(ClickableItem.of(item, inventoryClickEvent -> {

                                setValueType.put(player.getUniqueId(), type);
                                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

                            }));

                        }

                        pagination.setItems(items.toArray(new ClickableItem[0]));
                        pagination.setItemsPerPage(3);
                        pagination.addToIterator(inventoryContents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 2));

                        inventoryContents.set(1, 1, ClickableItem.of(new ItemBuilder(Skull.OAK_WOOD_ARROW_LEFT)
                                .setDisplayname("§7Previous Page")
                                .build(), inventoryClickEvent -> {
                            DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                            getInventory().open(player, pagination.previous().getPage());
                            DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                        }));

                        inventoryContents.set(1, 5, ClickableItem.of(new ItemBuilder(Skull.OAK_WOOD_ARROW_RIGHT)
                                .setDisplayname("§7Next Page")
                                .build(), inventoryClickEvent -> {
                            DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                            getInventory().open(player, pagination.next().getPage());
                            DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                        }));

                        ItemStack itemStack = new ItemBuilder((setValueType.containsKey(player.getUniqueId()) ?
                                Skull.getCustomHead(setValueType.get(player.getUniqueId()).getHeadId()).getSkull() : new ItemStack(Material.BARRIER)))
                                .setDisplayname("§7Currently selected:")
                                .setLore("§8> " + (setValueType.containsKey(player.getUniqueId()) ? "§7" + setValueType.get(player.getUniqueId()).getDisplayName() : "§c?"))
                                .build();
                        inventoryContents.set(2, 3, ClickableItem.empty(itemStack));

                        inventoryContents.set(1, 7, ClickableItem.of(new ItemBuilder(Material.OAK_SIGN)
                                .setDisplayname("§7Set Value")
                                .build(), inventoryClickEvent -> {

                            if(!setValueType.containsKey(player.getUniqueId())){
                                player.sendMessage("§7Select a data type before doing that!");
                                return;
                            }

                            ObjectType type = setValueType.get(player.getUniqueId());

                            if(type == ObjectType.NOT_SUPPORTED){
                                player.sendMessage("§cCan't set the value with an unsupported type!");
                                return;
                            }
                            if(type == ObjectType.LIST){
                                player.sendMessage(FileManager.getInstance().getImplementationError()); // TODO: 22.04.2022 implement
                                return;
                            }

                            DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                            player.closeInventory();
                            DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                            new AnvilGUI.Builder()
                                    .onClose(player1 -> {
                                        if(!DirectoryInventory.updatingInventorys.contains(player1.getUniqueId())){
                                            player1.sendMessage("§cYou closed the inventory and the input won't be saved.");
                                            player1.playSound(player1.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                            DirectoryInventory.updatingInventorys.add(player1.getUniqueId());
                                            Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> getInventory().open(player1), 5);
                                        }
                                    })
                                    .onComplete((player1, s) -> {

                                        Object obj = null;

                                        try{
                                            obj = ObjectType.getStringAsObjectTypeObject(type, s);
                                        }catch (Exception ignored){} // ignore because error message is handling on "obj == null"

                                        if(obj == null){
                                            player1.playSound(player1.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                            player1.sendMessage("§cThat doesn't work as an " + type.getDisplayName() + "!");
                                        }else{
                                            currentEntry.value = obj;
                                            player1.playSound(player1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                        }
                                        DirectoryInventory.updatingInventorys.add(player1.getUniqueId());
                                        currentInvState.replace(player.getUniqueId(), "");
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> getInventory().open(player1), 5);
                                        return AnvilGUI.Response.close();
                                    })
                                    .text("")
                                    .title("§7Set the value of the entry")
                                    .plugin(FileManager.getInstance())
                                    .itemLeft(new ItemBuilder(Material.NAME_TAG).setDisplayname("§7Key").build())
                                    .open(player);

                        }));
                    }
                })
                .manager(FileManager.getInstance().getManager())
                .title("Create a value")
                .size(3, 9)
                .listener(new InventoryListener<>(InventoryCloseEvent.class, inventoryCloseEvent -> {

                    Player player = (Player) inventoryCloseEvent.getPlayer();

                    if(!DirectoryInventory.updatingInventorys.contains(player.getUniqueId())){

                        String state = currentInvState.get(player.getUniqueId());
                        if(state == "setval"){
                            setValueType.remove(player.getUniqueId());
                            currentInvState.replace(player.getUniqueId(), "");
                            Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> getInventory().open(player), 2);
                            return;
                        }

                        consumerOnClose.accept(currentEntry);
                        currentInvState.remove(player.getUniqueId());
                    }
                }))
                .build();
    }

    public class Entry {

        private String key;
        private Object value;

        public Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

    }

}

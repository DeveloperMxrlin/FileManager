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
import mxrlin.file.misc.item.ItemBuilder;
import mxrlin.file.misc.ObjectType;
import mxrlin.file.misc.item.Skull;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ListEditor {

    private List<Object> list;
    private final BiConsumer<InventoryCloseEvent, List<?>> consumer;

    public ListEditor(List<Object> list, BiConsumer<InventoryCloseEvent, List<?>> consumerOnClose){
        this.list = list;
        this.consumer = consumerOnClose;
    }

    public SmartInventory getInventory(){

        return SmartInventory.builder()
                .manager(FileManager.getInstance().getManager())
                .size(6,9)
                .title("§7Edit the List")
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents inventoryContents) {

                        inventoryContents.fillBorders(ClickableItem.empty(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayname(" ").build()));

                        Pagination pagination = inventoryContents.pagination();

                        List<ClickableItem> items = new ArrayList<>();

                        if(!list.isEmpty()){
                            ObjectType type = ObjectType.getType(list.get(0));
                            int current = 1;
                            for(Object obj : list){

                                int finalCurrent = current;
                                items.add(ClickableItem.of(new ItemBuilder(Skull.getCustomHead(type.getHeadId()))
                                        .setDisplayname("§7" + obj).setLore("§8§m-----", "§7Click to remove the value!", "§7Type: §a" + type).build(), inventoryClickEvent -> {

                                    list.remove(finalCurrent);
                                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

                                }));

                                current++;
                            }
                        }

                        pagination.setItems(items.toArray(new ClickableItem[0]));
                        pagination.setItemsPerPage(4*7);

                        pagination.addToIterator(inventoryContents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1)
                                .blacklist(2, 0).blacklist(3, 0).blacklist(4, 0).blacklist(5, 0)
                                .blacklist(1, 8).blacklist(2, 8).blacklist(3, 8).blacklist(4, 8));

                        inventoryContents.set(5, 3, ClickableItem.of(new ItemBuilder(Skull.OAK_WOOD_ARROW_LEFT).setDisplayname("§7Previous Page").build(), inventoryClickEvent -> {
                            DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                            getInventory().open(player, pagination.previous().getPage());
                            DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                        }));

                        inventoryContents.set(5, 5, ClickableItem.of(new ItemBuilder(Skull.OAK_WOOD_ARROW_RIGHT).setDisplayname("§7Next Page").build(), inventoryClickEvent -> {
                            DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                            getInventory().open(player, pagination.next().getPage());
                            DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                        }));

                        inventoryContents.set(5, 4, ClickableItem.of(new ItemBuilder(Skull.LIME_PLUS).setDisplayname("§7Add Value").build(), inventoryClickEvent -> {

                            ObjectType type = null;
                            if(list.isEmpty()){
                                type = ObjectType.NOT_SUPPORTED;
                            }else type = ObjectType.getType(list.get(0));

                            DirectoryInventory.updatingInventorys.add(player.getUniqueId());
                            player.closeInventory();
                            DirectoryInventory.updatingInventorys.remove(player.getUniqueId());
                            ObjectType finalType = type;
                            new AnvilGUI.Builder()
                                    .onClose(player1 -> {
                                        if(!DirectoryInventory.updatingInventorys.contains(player1.getUniqueId())){
                                            player.sendMessage("§7You closed the inventory and the value won't be saved.");
                                        }else DirectoryInventory.updatingInventorys.remove(player1.getUniqueId());
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(FileManager.getInstance(), () -> getInventory().open(player), 2);
                                    })
                                    .onComplete((player1, s) -> {

                                        if(list.isEmpty()){

                                            ObjectType objectType = ObjectType.getTypeOfString(s);

                                            list.add(ObjectType.getStringAsObjectTypeObject(objectType, s));

                                            player1.sendMessage("§7Successfully added a new value to the List!");
                                            player1.playSound(player1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                                        }else{
                                            Object finalValue = null;

                                            try{
                                                finalValue = ObjectType.getStringAsObjectTypeObject(finalType, s);
                                            }catch (Exception e){
                                                player1.sendMessage("§7Couldn't convert string to required value type. (" + finalType.getDisplayName() + ")");
                                            }
                                            if(finalValue == null){
                                                player1.playSound(player1.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                            }else{
                                                player1.sendMessage("§7Successfully added a new value to the List!");
                                                player1.playSound(player1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                                list.add(finalValue);
                                            }
                                        }

                                        DirectoryInventory.updatingInventorys.add(player1.getUniqueId());
                                        return AnvilGUI.Response.close();
                                    })
                                    .text("Value")
                                    .itemLeft(new ItemBuilder(Skull.getCustomHead(type.getHeadId())).build())
                                    .title("§7Set a new Value in List")
                                    .plugin(FileManager.getInstance())
                                    .open(player);


                        }));

                    }

                    @Override
                    public void update(Player player, InventoryContents inventoryContents) {
                        Pagination pagination = inventoryContents.pagination();

                        List<ClickableItem> items = new ArrayList<>();

                        if(!list.isEmpty()){
                            ObjectType type = ObjectType.getType(list.get(0));
                            int current = 0;
                            for(Object obj : list){

                                final int finalCurrent = current;
                                items.add(ClickableItem.of(new ItemBuilder(Skull.getCustomHead(type.getHeadId()))
                                        .setDisplayname("§7" + obj).setLore("§8§m-----", "§7Click to remove the value!", "§7Type: §a" + type).build(), inventoryClickEvent -> {

                                    list.remove(finalCurrent);
                                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

                                }));

                                current++;
                            }
                        }

                        pagination.setItems(items.toArray(new ClickableItem[0]));
                        pagination.setItemsPerPage(4*7);

                        pagination.addToIterator(inventoryContents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1)
                                .blacklist(2, 0).blacklist(3, 0).blacklist(4, 0).blacklist(5, 0)
                                .blacklist(1, 8).blacklist(2, 8).blacklist(3, 8).blacklist(4, 8));

                    }
                })
                .listener(new InventoryListener<>(InventoryCloseEvent.class, inventoryCloseEvent -> {
                    if(!DirectoryInventory.updatingInventorys.contains(inventoryCloseEvent.getPlayer().getUniqueId()))
                        consumer.accept(inventoryCloseEvent, list);
                }))
                .build();

    }

}

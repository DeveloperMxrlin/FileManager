/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.misc.item;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private ItemStack itemStack;

    public ItemBuilder(Skull head){
        this(head.getSkull());
    }

    public ItemBuilder(Material material){
        this(material, 1);
    }

    public ItemBuilder(Material material, int amount){
        this(new ItemStack(material, amount));
    }

    public ItemBuilder(ItemStack itemStack){
        this.itemStack = itemStack;
    }

    public ItemBuilder setDisplayname(String displayname){
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(displayname);
        itemStack.setItemMeta(meta);
        return this;
    }

    private LineBuilder stringListToLoreBuilder(List<String> lore){
        LineBuilder loreBuilder = new LineBuilder();
        lore.forEach(s -> loreBuilder.addLineIgnoringMaxLength(s));
        return loreBuilder;
    }

    /**
     * @deprecated Use {@link ItemBuilder#setLore(LineBuilder)} instead
     */
    @Deprecated
    public ItemBuilder setLore(String... lore){
        return setLore(Arrays.asList(lore));
    }

    /**
     * @deprecated Use {@link ItemBuilder#setLore(LineBuilder)} instead
     */
    @Deprecated
    public ItemBuilder setLore(List<String> lore){
        LineBuilder loreBuilder = stringListToLoreBuilder(lore);
        setLore(loreBuilder);
        return this;
    }

    public ItemBuilder setLore(LineBuilder builder){
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(builder.build());
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level){
        ItemMeta meta = itemStack.getItemMeta();
        meta.addEnchant(enchantment, level, true);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addItemFlags(ItemFlag... flags){
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(flags);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setAmount(int amount){
        itemStack.setAmount(amount);
        return this;
    }

    public ItemStack build(){
        return itemStack;
    }

}

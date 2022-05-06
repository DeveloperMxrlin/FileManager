/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.misc.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public class Skull {

    public static final Skull OAK_LOG_ARROW_RIGHT = new Skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzE1NDQ1ZGExNmZhYjY3ZmNkODI3ZjcxYmFlOWMxZDJmOTBjNzNlYjJjMWJkMWVmOGQ4Mzk2Y2Q4ZTgifX19");
    public static final Skull OAK_LOG_ARROW_LEFT = new Skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWVkNzg4MjI1NzYzMTdiMDQ4ZWVhOTIyMjdjZDg1ZjdhZmNjNDQxNDhkY2I4MzI3MzNiYWNjYjhlYjU2ZmExIn19fQ==");

    public static final Skull OAK_WOOD_QUESTION_MARK = new Skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19");
    public static final Skull OAK_WOOD_FORWARD = new Skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmEzYjhmNjgxZGFhZDhiZjQzNmNhZThkYTNmZTgxMzFmNjJhMTYyYWI4MWFmNjM5YzNlMDY0NGFhNmFiYWMyZiJ9fX0=");
    public static final Skull OAK_WOOD_ARROW_RIGHT = new Skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19");
    public static final Skull OAK_WOOD_ARROW_LEFT = new Skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==");

    public static final Skull LIME_PLUS = new Skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA1NmJjMTI0NGZjZmY5OTM0NGYxMmFiYTQyYWMyM2ZlZTZlZjZlMzM1MWQyN2QyNzNjMTU3MjUzMWYifX19");
    public static final Skull RED_MINUS = new Skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGU0YjhiOGQyMzYyYzg2NGUwNjIzMDE0ODdkOTRkMzI3MmE2YjU3MGFmYmY4MGMyYzViMTQ4Yzk1NDU3OWQ0NiJ9fX0=");

    public static final Skull STRUCTURE_BLOCK_SAVE = new Skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODk0MjgyN2Q0MzRmYWE4MWU4NGZkMDY4ODJmOWExNTUzY2I3YzIwYTExM2Q1NzM0ZjM3ZWY0ODBlMGMyYTU5YSJ9fX0=");

    public static final Skull GOLDEN_ARROW_LEFT = new Skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZhNjA1ZTI1ZjRmYzJjZWE1YTc2NmQ3OWE4YmZhMjkwMzEzZTQ1ZDhmNWU5NTdkOTU4YTBmMzNmY2IxNiJ9fX0=");
    public static final Skull GOLDEN_ARROW_RIGHT = new Skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWE5MmFkNDU2Zjc2ZWM3Y2FhMzU5NTkyMmQ1ZmNjMzhkY2E5MjhkYzY3MTVmNzUyZTc0YzhkZGRlMzQ0ZSJ9fX0=");

    public static Skull getCustomHead(String skullId){
        return new Skull(skullId);
    }

    private final String id;

    private Skull(String skullId){
        id = skullId;
    }

    public String getId() {
        return id;
    }

    public ItemStack getSkull(){
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (id.isEmpty())
            return head;
        SkullMeta headMeta = (SkullMeta)head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", id));
        try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (IllegalArgumentException|NoSuchFieldException|SecurityException|IllegalAccessException exception) {
            exception.printStackTrace();
        }
        head.setItemMeta((ItemMeta)headMeta);
        return head;
    }

    @Deprecated
    public static ItemStack getSkullFromOwner(String owner) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta)head.getItemMeta();
        headMeta.setOwner(owner);
        head.setItemMeta((ItemMeta)headMeta);
        return head;
    }

    public static ItemStack getSkullFromOwner(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta)head.getItemMeta();
        headMeta.setOwningPlayer((OfflinePlayer)player);
        head.setItemMeta((ItemMeta)headMeta);
        return head;
    }

}

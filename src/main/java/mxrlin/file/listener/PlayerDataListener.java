/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.listener;

import mxrlin.file.FileManager;
import mxrlin.file.misc.data.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDataListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        FileManager.getInstance().data.put(event.getPlayer().getUniqueId(), new PlayerData());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        FileManager.getInstance().data.remove(event.getPlayer().getUniqueId());
    }

}

/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.updater;

import com.google.gson.JsonObject;
import mxrlin.file.misc.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.Level;

public class UpdateCheck {

    private int spigotID;
    private Plugin plugin;

    private boolean updateAvailable;

    public UpdateCheck(Plugin plugin, int spigotID){
        this.plugin = plugin;
        this.spigotID = spigotID;

        this.updateAvailable = false;
    }

    public void requestUpdateCheck(){

        String url = "https://api.spigotmc.org/legacy/update.php?resource=" + spigotID;
        Consumer<String> check = s -> {
            updateAvailable = !s.equalsIgnoreCase(plugin.getDescription().getVersion());
        };

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try(InputStream inputStream = new URL(url).openStream(); Scanner scanner = new Scanner(inputStream)){
                if(scanner.hasNext()){
                    check.accept(scanner.next());
                }
            }catch (Exception e){
                plugin.getLogger().log(Level.WARNING, "Couldn't check for a new version: " + e.getMessage());
            }
        });

    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

}

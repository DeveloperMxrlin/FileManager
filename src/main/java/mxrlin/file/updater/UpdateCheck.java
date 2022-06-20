/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.updater;

import com.google.gson.JsonObject;
import com.sun.glass.ui.MenuItem;
import mxrlin.file.misc.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.security.auth.callback.Callback;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.logging.Level;

public class UpdateCheck {

    private int spigotID;
    private Plugin plugin;

    private String latestVersion;

    public UpdateCheck(Plugin plugin, int spigotID){
        this.plugin = plugin;
        this.spigotID = spigotID;

        this.latestVersion = "";
    }

    public void requestUpdateCheck(){

        String url = "https://api.spigotmc.org/legacy/update.php?resource=" + spigotID;

        try(InputStream inputStream = new URL(url).openStream(); Scanner scanner = new Scanner(inputStream)){
            if(scanner.hasNext()){
                latestVersion = scanner.next();
            }
        }catch (Exception e){
            plugin.getLogger().log(Level.WARNING, "Couldn't check for a new version: " + e.getMessage());
        }

    }

    public boolean isUpdateAvailable() {
        if(latestVersion.isEmpty()) requestUpdateCheck();
        return !plugin.getDescription().getVersion().equalsIgnoreCase(latestVersion);
    }

    public String getLatestVersion() {
        return latestVersion;
    }

}

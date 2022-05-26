/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file;

import fr.minuskube.inv.InventoryManager;
import mxrlin.file.commands.FileManagerCommand;
import mxrlin.file.listener.PlayerDataListener;
import mxrlin.file.misc.Metrics;
import mxrlin.file.misc.data.PlayerData;
import mxrlin.file.updater.Downloader;
import mxrlin.file.updater.UpdateCheck;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class FileManager extends JavaPlugin {

    // TODO: 19.05.2022 add more editor: json, txt, properties (MC-Server), worldwatcher PRIORITY #2
    // TODO: 21.05.2022 change start directory to the server directory PRIORITY #2.1
    // TODO: 23.05.2022 more options for directorywatcher: add file, rename file, del file #PRIORITY #1

    public static final int     SPIGOT_ID       = 102079;
    public static final String  RESOURCE_LINK   = "https://www.spigotmc.org/resources/filemanager.102079/";

    private static FileManager instance;

    private InventoryManager manager;
    private boolean debug;

    private List<String> implementationErrorMessages;

    private Metrics metrics;

    public Map<UUID, PlayerData> data;

    private boolean checkUpdate;
    private boolean downloadUpdate;

    @Override
    public void onEnable() {

        getLogger().log(Level.INFO, "Booting FileManager up...");

        instance = this;

        data = new HashMap<>();

        getLogger().log(Level.INFO, "Loading Config...");

        loadConfig();

        getLogger().log(Level.INFO, "Checking for Updates...");
        getLogger().log(Level.INFO, "UpdateChecker Response:");

        checkUpdate();

        getLogger().log(Level.INFO, "========");
        getLogger().log(Level.INFO, "Initializing InventoryManager...");

        manager = new InventoryManager(this);
        manager.init();

        getLogger().log(Level.INFO, "Initializing Implementation Error Messages...");

        implementationErrorMessages = new ArrayList<>();
        getImplementationError();

        getLogger().log(Level.INFO, "Initializing bStats Metrics for the plugin...");

        metrics = new Metrics(this, 15053);

        getLogger().log(Level.INFO, "Adding Commands and Listeners...");

        Bukkit.getPluginManager().registerEvents(new PlayerDataListener(), this);

        getCommand("filemanager").setExecutor(new FileManagerCommand());

        getLogger().log(Level.INFO, "FileManager successfully booted up!");

    }

    private void loadConfig(){

        if(!getDataFolder().exists()) getDataFolder().mkdir();

        File config = new File(getDataFolder(), "config.yml");
        if(!config.exists()) {
            try {
                config.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(config);

        configuration.options().copyDefaults(true);
        configuration.options().parseComments(true);

        configuration.options().setHeader(Arrays.asList("Configuration for FileManager",
                "",
                "FileManager is a plugin, made for server owners, to not have the need to edit their files by opening the",
                "file, editing and then restarting the server.",
                "With FileManager this is done ingame! You can edit all your entries ingame, and the saving is done",
                "automatically! With your agreement the plugin even searches for the entries and replaces them in the working",
                "plugin! No need to restart the server or anything."));

        configuration.addDefault("debug", false);
        configuration.addDefault("update.check", true);
        configuration.addDefault("update.download", true);

        try {
            configuration.save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }

        debug = configuration.getBoolean("debug");
        checkUpdate = configuration.getBoolean("update.check");
        downloadUpdate = configuration.getBoolean("update.download");

    }

    private void checkUpdate(){

        if(checkUpdate){
            UpdateCheck check = new UpdateCheck(this, SPIGOT_ID);

            if(check.isUpdateAvailable()){

                if(downloadUpdate){

                    File dest = new File(getDataFolder() + "/Plugin");
                    Downloader downloader = new Downloader(this, SPIGOT_ID, dest);
                    try {
                        downloader.download();
                        getLogger().log(Level.INFO, "Successfully downloaded the latest version of the plugin in " + dest.getAbsolutePath());
                    } catch (Exception e) {
                        getLogger().log(Level.INFO, "Failed to download the latest version of the plugin! Error: " + e.getMessage());
                    }

                }else{

                    getLogger().log(Level.INFO, "A new Update is available for the Plugin!");
                    getLogger().log(Level.INFO, "Download it now here: " + RESOURCE_LINK);

                }

            }else{

                getLogger().log(Level.INFO, "No update is available. You're on the newest Version!");

            }

        }else {

            getLogger().log(Level.INFO, "You deactivated checking for updates.");

        }

    }

    @Override
    public void onDisable() {

    }

    public static FileManager getInstance() {
        return instance;
    }

    public InventoryManager getManager() {
        return manager;
    }

    public boolean isDebug() {
        return debug;
    }

    public void debug(String message){
        if(!isDebug()) return;
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement lastStackTrace = stackTraceElements[2];
        // [class.method(class.java:line)] message
        getLogger().log(Level.INFO,
                "[" + lastStackTrace.getClassName() + "." +
                lastStackTrace.getMethodName() + "(" +
                lastStackTrace.getFileName() + ":" +
                lastStackTrace.getLineNumber() + ")" + "] " + message);
    }

    public String getImplementationError(){ // When something isn't implemented
        if(implementationErrorMessages.isEmpty()){
            implementationErrorMessages.add("§7The Developer hasn't added this feature yet.");
            implementationErrorMessages.add("§7Coming soon! \uD83D\uDC40"); // 👀
            implementationErrorMessages.add("§7What a fault Developer, he didn't add that feature!");
            implementationErrorMessages.add("§7This is just a placeholder for now, but later here will be something for sure!");
        }
        Random random = new Random();
        return implementationErrorMessages.get(random.nextInt(implementationErrorMessages.size()));
    }

    public PlayerData getPlayerData(UUID uuid){
        return data.get(uuid);
    }

    public PlayerData getPlayerData(Player player){
        return data.get(player.getUniqueId());
    }

}

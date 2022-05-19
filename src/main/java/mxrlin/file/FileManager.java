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

    // TODO: 19.05.2022 add more editor: json,

    /**
     * Plugin Idea:
     * View and edit .yml + .json + .txt Files in a Bukkit Inventory
     */

    private static FileManager instance;

    private InventoryManager manager;
    private boolean debug;

    private List<String> implementationErrorMessages;

    private Metrics metrics;

    public Map<UUID, PlayerData> data;

    @Override
    public void onEnable() {

        instance = this;

        data = new HashMap<>();

        loadConfig();

        manager = new InventoryManager(this);
        manager.init();

        implementationErrorMessages = new ArrayList<>();
        getImplementationError();

        metrics = new Metrics(this, 15053);
        // TODO: 07.05.2022 maybe add custom charts

        Bukkit.getPluginManager().registerEvents(new PlayerDataListener(), this);

        getCommand("filemanager").setExecutor(new FileManagerCommand());

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

        try {
            configuration.save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }

        debug = configuration.getBoolean("debug");

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

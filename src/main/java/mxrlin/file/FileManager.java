/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

public class FileManager extends JavaPlugin {

    // TODO: 19.05.2022 add more editor: txt, properties (MC-Server), worldwatcher PRIORITY #2
    // TODO: 21.05.2022 change start directory to the server directory PRIORITY #2.1
    // TODO: 02.06.2022 File -> FileEditor -> File PRIORITY #3
    // TODO: 02.06.2022 Inventory -> File -> FileEditor PRIORITY #4
    // TODO: 02.06.2022 Replace "move file to other dir" with "copy file data" and "paste file data" PRIORITY #2
    // TODO: 15.06.2022 description for update checker

    public static final int     SPIGOT_ID       = 102079;
    public static final String  RESOURCE_LINK   = "https://www.spigotmc.org/resources/filemanager.102079/";

    public static File pluginDirectory;
    public static File serverDirectory;

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

        instance = this;

        pluginDirectory = getDataFolder().getParentFile();
        serverDirectory = new File(pluginDirectory.getAbsolutePath()).getParentFile();

        data = new HashMap<>();

        loadConfig();

        getLogger().log(Level.INFO, "Trying to check for updates...");

        checkUpdate();

        manager = new InventoryManager(this);
        manager.init();

        implementationErrorMessages = new ArrayList<>();
        getImplementationError();

        metrics = new Metrics(this, 15053);

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
            check.requestUpdateCheck();
            getLogger().log(Level.INFO, "Current: " + getDescription().getVersion() + " - Latest: " + check.getLatestVersion());

            if(check.isUpdateAvailable()){

                getLogger().log(Level.INFO, "There is a new update available!");

                String title = getLatestUpdateTitle();
                getLogger().log(Level.INFO, "Update Title: \"" + title + "\"");

                if(downloadUpdate){

                    File dest = new File(getDataFolder() + "/Plugin");
                    Downloader downloader = new Downloader(this, SPIGOT_ID, dest);

                    getLogger().log(Level.INFO, "Trying to download the plugin into " + dest.getAbsolutePath());

                    try {
                        downloader.download();
                        getLogger().log(Level.INFO, "Successfully downloaded the latest version of the plugin in " + dest.getAbsolutePath());
                    } catch (Exception e) {
                        getLogger().log(Level.INFO, "Failed to download the latest version of the plugin! Error: " + e.getMessage());
                    }

                }else{

                    getLogger().log(Level.INFO, "Download it now here: " + RESOURCE_LINK);

                }

            }else{

                getLogger().log(Level.INFO, "No update is available. You're on the newest Version!");

            }

        }else {

            getLogger().log(Level.INFO, "You deactivated checking for updates.");

        }

    }

    private String getLatestUpdateTitle(){
        StringBuilder json = new StringBuilder();

        try {
            URL url = new URL("https://api.spiget.org/v2/resources/102079/updates");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            if(conn.getResponseCode() == 200) {
                Scanner scan = new Scanner(url.openStream());
                while(scan.hasNext()) {
                    String temp = scan.nextLine();
                    json.append(temp);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        Gson gson = new Gson();
        JsonArray array = gson.fromJson(json.toString(), JsonArray.class);

        JsonObject latestUpdate = (JsonObject) array.get(array.size()-1);
        JsonElement title = latestUpdate.get("title");

        return title.getAsString();
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

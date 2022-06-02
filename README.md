![FileManager Logo](https://i.postimg.cc/7ZgqpDQP/File-Manager-Logo.png "FileManager Logo")
[
  ![test](https://img.shields.io/bstats/players/15053?color=yellow&style=for-the-badge "Click to see stats!")
](https://bstats.org/plugin/bukkit/FileManager/15053 "Click to see stats!") 
[
  ![test](https://img.shields.io/bstats/servers/15053?color=yellow&style=for-the-badge "Click to see stats!")
](https://bstats.org/plugin/bukkit/FileManager/15053 "Click to see stats!")
[
  ![test](https://img.shields.io/discord/968876186304393257?color=yellow&label=Discord&style=for-the-badge "Click to join the discord!")
](https://discord.gg/2ajfpDvn2b "Click to join the discord!")
# FileManager

File Manager is a Spigot plugin, made for server owners, to not have the need to edit their files, by opening the file, editing, and then restarting the server. With FileManager this is done ingame! You can edit your entries all ingame, and the saving is done automatically! With your agreement the plugin even searches for the entries and replaces them in the working Plugin! No need to restart the server or anything.

## Links
* Join the [Discord](https://discord.gg/2ajfpDvn2b) for support, updates and news.
* Download the plugin at the [SpigotMC Ressource](https://www.spigotmc.org/resources/filemanager.102079/).

## Libraries
* [SmartInvs](https://www.spigotmc.org/resources/smartinvs-advanced-inventory-api.42835/ "Click to go to SmartInvs!")
* [AnvilGUI](https://github.com/WesJD/AnvilGUI "Click to go to AnvilGUI!")
* [ASM](https://asm.ow2.io "Click to go to ASM!")
* [Gson](https://en.wikipedia.org/wiki/Gson "Click to learn more about Gson!")
* [OkHttp](https://square.github.io/okhttp/ "Click to learn more about OkHttp!")

## API
To use the included API of FileManager, first implement the plugin.

To implement the plugin you can either use maven:
```
  <dependency>
	    <groupId>com.github.DeveloperMxrlin</groupId>
	    <artifactId>FileManager</artifactId>
	    <version>VERSION</version>
	</dependency>
```
Or you're using gradle:
```
  dependencies {
	        implementation 'com.github.DeveloperMxrlin:FileManager:VERSION'
	}
```
For both methods you will have to add [JitPack](https://jitpack.io) to your repository.

After implemented, use the method `FileManagerAPI#INSTANCE` to get all available methods that are working for the API.

Create own FileEditor by either implementing the FileEditor Interface or extending the FileEditorHelper Class.

FileEditorHelper Code Example:

```
public class Test extends FileEditorHelper {

    protected Test() {
        super("json");
    }

    @Override
    protected InventoryProvider inventory(File file) {
        List<ClickableItem> items = new ArrayList<>();
        // add your items / entries of the file

        return entriesToInventory(items.toArray(new ClickableItem[0]),
                ClickableItem.empty(new ItemStack(Material.PAPER)), // add a information item that shows all information about the file
                new EntryCreator((entry, player) -> {
                    // do something with the entry
                }),
                file,
                "jsonTest:");
    }

    @Override
    protected Consumer<InventoryCloseEvent> closeInventory() {
        return inventoryCloseEvent -> {
            // do something on close
            // f.e save the file, open new inventory...
        };
    }
}
```

And then you just have to add this into your enable part: `new JsonTest().addFileEditor();`

## Statistics

* 30 Different Files with Code
* 5300 Lines of Code
* 230 KB of Code

## License
This Repository uses a **MIT License**.

*[Click here](../blob/master/LICENSE) to read the entire license.*

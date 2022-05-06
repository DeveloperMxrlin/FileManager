/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.search;

import mxrlin.file.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public abstract class DefaultSearch implements Search {
    @Override
    public Plugin getPluginOfDirectory(File pluginDirectory) {

        String fileName = pluginDirectory.getName();
        Plugin pluginFileName = Bukkit.getPluginManager().getPlugin(fileName);
        if(pluginFileName != null){
            return pluginFileName;
        }

        double possiblePoints = fileName.length() + 10;
        double minPoints = possiblePoints * 0.75;

        int highestPoints = 0;
        Plugin highestPointsPlugin = null;

        for(Plugin plugin : Bukkit.getPluginManager().getPlugins()){

            PluginDescriptionFile descriptionFile = plugin.getDescription();
            if(fileName.equalsIgnoreCase(descriptionFile.getName())){
                FileManager.getInstance().debug("getPluginOfDirectory: \"" + pluginDirectory.getAbsolutePath() + "\": " + plugin.getName());
                return plugin;
            }

            int points = plugin.getName().length() == fileName.length() ? 10 : 0;
            if(fileName.length() >= plugin.getName().length()){
                for(int i = 0; i < fileName.toCharArray().length; i++){
                    if(i >= plugin.getName().toCharArray().length) break;

                    char c = fileName.charAt(i);
                    char c1 = plugin.getName().charAt(i);
                    if(c == c1){
                        points++;
                    }
                }
            }else {
                for(int i = 0; i < plugin.getName().toCharArray().length; i++){
                    if(i >= fileName.toCharArray().length) break;

                    char c = fileName.charAt(i);
                    char c1 = plugin.getName().charAt(i);
                    if(c == c1){
                        points++;
                    }
                }
            }

            if(points > highestPoints){
                highestPoints = points;
                highestPointsPlugin = plugin;
            }

        }

        if(highestPoints > minPoints){
            FileManager.getInstance().debug("getPluginOfDirectory: \"" + pluginDirectory.getAbsolutePath() + "\": " + highestPointsPlugin.getName());
            return highestPointsPlugin;
        }

        FileManager.getInstance().debug("getPluginOfDirectory: \"" + pluginDirectory.getAbsolutePath() + "\": null");
        return null;
    }

    private static File getJarOfPlugin(Plugin plugin) throws Exception {
        Method getPlugin = JavaPlugin.class.getDeclaredMethod("getFile");
        getPlugin.setAccessible(true);

        File pluginJar = (File)getPlugin.invoke(plugin);

        getPlugin.setAccessible(false);
        return pluginJar;
    }

    private Map<Map.Entry<File, String>, InputStream> inputStreamMap = new HashMap<>();

    private InputStream getFileOfJar(File jar, String fileName) throws Exception {

        Map.Entry<File, String> mapEntry = new Map.Entry<File, String>() {
            @Override
            public File getKey() {
                return jar;
            }

            @Override
            public String getValue() {
                return fileName;
            }

            @Override
            public String setValue(String value) {
                return fileName;
            }
        };
        if(inputStreamMap.containsKey(mapEntry)){
            return inputStreamMap.get(mapEntry);
        }

        ZipInputStream stream = new ZipInputStream(jar.toURI().toURL().openStream());
        while (true){

            ZipEntry entry = stream.getNextEntry();
            if(entry == null) break; // no more entries

            if(!entry.getName().endsWith(".class")) continue;

            FileManager.getInstance().debug(fileName + " == " + entry.getName() + " ? " + fileName.equals(entry.getName()));

            if(fileName.equals(entry.getName())
                    || (!fileName.contains("$") && entry.getName().contains("$") && fileName.equalsIgnoreCase(entry.getName().substring(entry.getName().lastIndexOf("$"))))){
                return new ZipFile(jar).getInputStream(entry);
            }

        }

        return null;

    }

    @Override
    public Map<String, String> getAllHashMaps(Plugin plugin) throws Exception {

        FileManager.getInstance().debug("Starting to try getting all Maps from " + plugin.getName());
        FileManager.getInstance().debug("Main Class: " + plugin.getDescription().getMain());

        File jar = getJarOfPlugin(plugin);
        FileManager.getInstance().debug(jar.getAbsolutePath());

        InputStream main = getFileOfJar(jar, plugin.getDescription().getMain().replace(".", "/") + ".class");
        FileManager.getInstance().debug("main " + (main == null ? "null" : "!null"));

        List<String> classesStr = getClasses(jar,
                    new ClassReader(main),
                    "onEnable",
                    5);
        classesStr = removeDuplicates(classesStr);

        FileManager.getInstance().debug("All classes found, with a depth of 5: " + classesStr);

        Map<String, String> map = new HashMap<>();

        FileManager.getInstance().debug("Looping through all classes...");
        for(String className : classesStr){
            Class<?> clazz = Class.forName(className);
            if(clazz == null) continue;

            Field[] fields = clazz.getDeclaredFields();
            for(Field field : fields){
                Class<?> type = field.getType();
                if(Map.class.isAssignableFrom(type)){
                    FileManager.getInstance().debug("Found a Map with the FieldName " + field.getName() + " in " + clazz.getName());
                    map.put(className, field.getName());
                }
            }

        }

        return map;
    }

    private List<String> getClasses(File pluginJar, ClassReader startClass, String startMethod, int depth){
        return getClasses(pluginJar, startClass, startMethod, depth, 0, new ArrayList<>());
    }
    private List<String> getClasses(File pluginJar, ClassReader startClass, String startMethod, int depth, int currentDepth, List<String> current){

        FileManager.getInstance().debug("Getting Classes called in the Method " + startMethod + " in " + startClass.getClassName());

        startClass.accept(new ClassVisitor(262144) {

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

                if(name.equals(startMethod)){

                    FileManager.getInstance().debug("Found method " + startMethod + "!");

                    return new MethodVisitor(262144) {

                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {

                            if(owner.startsWith("java") || owner.startsWith("org/bukkit")) return; // dont go through java classes

                            FileManager.getInstance().debug("Method in method " + startMethod + " found: " + name + " in " + owner);

                            try {

                                if(!current.contains(owner.replace("/", "."))){

                                    FileManager.getInstance().debug("Method's class not in list, adding...");

                                    current.add(owner.replace("/", "."));

                                    if(currentDepth < depth){
                                        current.addAll(getClasses(pluginJar,
                                                new ClassReader(
                                                        getFileOfJar(pluginJar,
                                                                owner + ".class"
                                                        )),
                                                name, depth, currentDepth+1, current));
                                        FileManager.getInstance().debug("CurrentDepth < depth = Getting more methods that are in the found method " + name + ".");
                                    }

                                }else{
                                    FileManager.getInstance().debug("Method's class already in list");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }

                    };

                }

                return null;
            }

        }, ClassReader.EXPAND_FRAMES);

        return current;

    }

    private static <T> List<T> removeDuplicates(List<T> list){

        List<T> newList = new ArrayList<>();
        for(T t : list){
            boolean alreadyInNewList = false;
            for(T t1 : newList){
                if(t1.equals(t)){
                    alreadyInNewList = true;
                    break;
                }
            }
            if(!alreadyInNewList){
                newList.add(t);
            }
        }

        return newList;

    }

    @Override
    public abstract Map<?, ?> getMapWithHighestPoints(Map<String, String> allMaps, File file, List<Entry> changedValues);

    protected Map<?, ?> getMapByClassAndFieldName(String clazzName, String fieldName) throws Exception {
        Class<?> clazz = Class.forName(clazzName);
        Field field = clazz.getDeclaredField(fieldName);

        boolean fieldAccess = field.isAccessible();
        if(!fieldAccess) field.setAccessible(true);

        Constructor<?> constructor = clazz.getDeclaredConstructor(null);
        boolean constructorAccess = constructor.isAccessible();

        if(!constructorAccess) constructor.setAccessible(true);

        Map<?, ?> map = (Map<?, ?>) field.get(constructor.newInstance(null));

        if(!constructorAccess) constructor.setAccessible(false);
        if(!fieldAccess) field.setAccessible(false);

        return map;
    }

}

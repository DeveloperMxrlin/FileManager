/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.misc;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import mxrlin.file.FileManager;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;
import java.util.logging.Level;

public class Utils {

    public static String humanReadableByteCountSI(long bytes) { // https://stackoverflow.com/questions/3758606/how-can-i-convert-byte-size-into-a-human-readable-format-in-java
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    public static String[] split(String str, int maxLength, String regex){

        if(regex.equalsIgnoreCase("def")) regex = "\\s+";
        if(str.length() <= maxLength) return new String[]{str};

        List<String> splitStr = new ArrayList<>();
        String[] words = str.split(regex);
        StringBuilder currentLine = new StringBuilder();
        int i = 0;

        for(String word : words){

            int length = currentLine.length() + word.length() + 1;

            if(length > maxLength){
                splitStr.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }else{
                currentLine.append((i == 0 ? "" : " ")).append(word);
            }
            i++;

        }

        if(!currentLine.toString().isEmpty()){
            splitStr.add(currentLine.toString());
        }

        return splitStr.toArray(new String[0]);
    }

    public static String textToSpaces(String text){
        StringBuilder builder = new StringBuilder();
        for(char c : text.toCharArray()){
            builder.append(" ");
        }
        return builder.toString();
    }

    public static String stringArrToString(String[] arr){
        return stringArrToString(arr, 0);
    }

    public static String stringArrToString(String[] arr, int start){
        return stringArrToString(arr, start, arr.length);
    }

    public static String stringArrToString(String[] arr, int start, int end){
        StringBuilder builder = new StringBuilder();

        if(arr.length < end) end = arr.length;
        for(int i = start; i < end; i++){
            builder.append(arr[i]);
            if(i != (end-1)){
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    public static boolean isSubDirectory(File base, File child)
            throws IOException {
        base = base.getCanonicalFile();
        child = child.getCanonicalFile();

        File parentFile = child;
        while (parentFile != null) {
            if (base.equals(parentFile)) {
                return true;
            }
            parentFile = parentFile.getParentFile();
        }
        return false;
    }

    public static Map<String, Object> parseJsonFileToMap(File file){
        Gson gson = new Gson();
        try {
            Map<String, Object>[] maps = gson.fromJson(Files.newBufferedReader(file.toPath()), new TypeToken<Map<String, Object>[]>(){}.getType());
            Map<String, Object> map = new HashMap<>();
            for(Map<String, Object> m : maps){
                map.putAll(m);
            }
            return map;
        } catch (IOException e) {
            FileManager.getInstance().getLogger().log(Level.SEVERE, "Failed to get json Object from File \"" + file.getAbsolutePath() + "\": " + e.getMessage());
        }
        return null;
    }

}

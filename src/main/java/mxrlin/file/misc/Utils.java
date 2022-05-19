/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.misc;

import org.bukkit.ChatColor;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;

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

    public static String[] split(String str, int maxLength){
        if(str.length() <= maxLength) return new String[]{str};
        int lines = (int) Math.ceil((double) str.length() / maxLength);
        String[] splittedStr = new String[lines];

        int numBefore = 0;

        for(int i = 0; i < splittedStr.length; i++){

            int numFrom = numBefore;
            int numUntil = numBefore + maxLength;

            if(numUntil > str.length()){
                numUntil = str.length();
            }

            splittedStr[i] = str.substring(numFrom, numUntil);
            numBefore = numUntil;

            if(numUntil >= str.length()) break;

        }

        return splittedStr;
    }

    public static String textToSpaces(String text){
        StringBuilder builder = new StringBuilder();
        for(char c : text.toCharArray()){
            builder.append(" ");
        }
        return builder.toString();
    }

}

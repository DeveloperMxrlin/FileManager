/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.misc.item;

import mxrlin.file.misc.Utils;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LineBuilder {

    public static final int DEF_MAX_LENGTH_PER_LINE = 32;

    private int maxLength;
    private List<String> lines;

    public LineBuilder(){
        this(DEF_MAX_LENGTH_PER_LINE);
    }

    public LineBuilder(int maxLength){
        this.maxLength = maxLength;
        lines = new ArrayList<>();
    }

    public LineBuilder addListAsLine(List<String> list, String prefix, String suffix){
        for(String str : list){
            addLine(prefix + str + suffix, Utils.textToSpaces(ChatColor.stripColor(prefix) + "§7"));
        }
        return this;
    }

    public LineBuilder addLineIgnoringMaxLength(String str){
        lines.add(str);
        return this;
    }

    public LineBuilder addLine(String str){
        return addLine(str, "");
    }

    public LineBuilder addLine(String str, String prefixForNewLine){
        if(str.length() <= maxLength) addLineIgnoringMaxLength(str);
        else {
            String[] split = Utils.split(str, maxLength);
            for(int i = 0; i < split.length; i++){
                String s = split[i];
                if(i != 0) lines.add(prefixForNewLine + s);
                else lines.add(s);
            }
        }
        return this;
    }

    /**
     * Add multiple Lines at once to the Lore Builder
     * @param str The String Array to add to the Lines
     * @deprecated Foreach and use {@link LineBuilder#addLine(String)} instead
     */
    @Deprecated
    public LineBuilder addLines(String[] str){
        Collections.addAll(lines, str);
        return this;
    }

    public int getLines(){
        return lines.size();
    }

    public List<String> build() {
        return lines;
    }

}

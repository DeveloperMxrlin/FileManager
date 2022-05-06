/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.misc;

import java.util.Arrays;
import java.util.List;

public enum ObjectType {

    STRING("String", "A String is a Text.", String.class, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2U0MWM2MDU3MmM1MzNlOTNjYTQyMTIyODkyOWU1NGQ2Yzg1NjUyOTQ1OTI0OWMyNWMzMmJhMzNhMWIxNTE3In19fQ=="),
    INTEGER("Integer", "An Integer is a full number like '1', '2', etc.", Integer.class, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDYxNzhhZDUxZmQ1MmIxOWQwYTM4ODg3MTBiZDkyMDY4ZTkzMzI1MmFhYzZiMTNjNzZlN2U2ZWE1ZDMyMjYifX19"),
    DOUBLE("Double", "A Double is a number with a decimals and double precision.", Double.class, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzE5M2RjMGQ0YzVlODBmZjlhOGEwNWQyZmNmZTI2OTUzOWNiMzkyNzE5MGJhYzE5ZGEyZmNlNjFkNzEifX19"),
    FLOAT("Float", "A float is a single-precision decimal point number.", Float.class, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjE4M2JhYjUwYTMyMjQwMjQ4ODZmMjUyNTFkMjRiNmRiOTNkNzNjMjQzMjU1OWZmNDllNDU5YjRjZDZhIn19fQ=="),
    LIST("List<?>", List.class, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzE5ZjUwYjQzMmQ4NjhhZTM1OGUxNmY2MmVjMjZmMzU0MzdhZWI5NDkyYmNlMTM1NmM5YWE2YmIxOWEzODYifX19"),
    BOOLEAN("Boolean", "A Boolean is a primitive data type that can only be true or false.", Boolean.class, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTBjMWI1ODRmMTM5ODdiNDY2MTM5Mjg1YjJmM2YyOGRmNjc4NzEyM2QwYjMyMjgzZDg3OTRlMzM3NGUyMyJ9fX0="),
    NOT_SUPPORTED("Not Supported", null, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19");

    private String displayName;
    private Class<?> clazz;
    private String headId;
    private String description;

    ObjectType(String displayName, String description, Class<?> clazz, String headId){
        this.displayName = displayName;
        this.description = description;
        this.clazz = clazz;
        this.headId = headId;
    }

    ObjectType(String displayName, Class<?> clazz, String headId){
        this.displayName = displayName;
        this.description = "No Description provided.";
        this.clazz = clazz;
        this.headId = headId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getHeadId() {
        return headId;
    }

    public static ObjectType getType(Object obj){

        for(ObjectType type : values()){
            if(obj.getClass()==type.getClazz() ||
                    Arrays.stream(obj.getClass().getInterfaces()).anyMatch(aClass -> aClass == type.getClazz())){
                return type;
            }
        }

        return ObjectType.NOT_SUPPORTED;

    }

    public static Object getStringAsObjectTypeObject(ObjectType objectType, String str){
        switch (objectType){
            case STRING:
                return str;
            case BOOLEAN:
                return Boolean.parseBoolean(str);
            case INTEGER:
                return Integer.parseInt(str);
            case FLOAT:
                return Float.parseFloat(str);
            case DOUBLE:
                return Double.parseDouble(str);
            case LIST:
            case NOT_SUPPORTED:
            default:
                break;
        }
        return null;
    }

}

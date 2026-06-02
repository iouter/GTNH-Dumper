package com.iouter.gtnhdumper.common.serializer;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cpw.mods.fml.relauncher.ReflectionHelper;

@SuppressWarnings("rawtypes")
public class NBTTagCompoundSerializer implements JsonSerializer<NBTTagCompound> {

    private static final Field tagMapField = ReflectionHelper
        .findField(NBTTagCompound.class, "tagMap", "field_74784_a");

    @Override
    public JsonElement serialize(NBTTagCompound src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return null;
        }
        if (src.func_150296_c()
            .isEmpty()) {
            return null;
        }
        Map map = getTagMap(src);
        return context.serialize(map, Map.class);
    }

    public static Map getTagMap(NBTTagCompound nbtTagCompound) {
        try {
            return (Map) tagMapField.get(nbtTagCompound);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

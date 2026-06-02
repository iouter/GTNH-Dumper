package com.iouter.gtnhdumper.common.serializer;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SafeDoubleSerializer implements JsonSerializer<Double> {

    @Override
    public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }

        if (src.isNaN()) {
            return new JsonPrimitive("NaN");
        } else if (src.isInfinite()) {
            if (src > 0) {
                return new JsonPrimitive("Infinity");
            } else {
                return new JsonPrimitive("-Infinity");
            }
        }
        return new JsonPrimitive(src);
    }
}

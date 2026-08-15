package com.patex.forever.utils;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;

/**
 * Created by Alexey on 29.07.2017.
 */
public class BooleanJson {
    public static class Serializer extends ValueSerializer {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializationContext serializers) {
            gen.writeRawValue(value.toString());
        }
    }

    public static class Deserializer extends ValueDeserializer {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) {
            return p.getValueAsBoolean();
        }
    }
}

package org.comic_con.museum.fcb.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import org.comic_con.museum.fcb.controllers.responses.Views;
import org.comic_con.museum.fcb.models.User;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class JSONUtil {
    private static ObjectMapper om;
    
    private static class ISO8601InstantSerializer extends StdSerializer<Instant> {
        private static final DateTimeFormatter format = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    
        protected ISO8601InstantSerializer(Class<Instant> t) {
            super(t);
        }
    
        @Override
        public void serialize(Instant instant, JsonGenerator json, SerializerProvider serializer) throws IOException {
            json.writeString(format.format(instant.atZone(ZoneId.of("Z"))));
        }
    }
    
    static {
        om = new ObjectMapper();
        om.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        
        SimpleModule serializers = new SimpleModule();
        serializers.addSerializer(Instant.class, new ISO8601InstantSerializer(null));
        om.registerModule(serializers);
    }
    
    public static ObjectWriter withView(Class<? extends Views> viewClass) {
        return om.writerWithView(viewClass);
    }
    
    public static String toString(Object o) throws JsonProcessingException {
        return om.writeValueAsString(o);
    }
    
    public static String toString(Object o, Class<? extends Views> viewClass) throws JsonProcessingException {
        return withView(viewClass).writeValueAsString(o);
    }
    
    public static String toString(Object o, User user) throws JsonProcessingException {
        return toString(o, Views.byPrincipal(user));
    }
}

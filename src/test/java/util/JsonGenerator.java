package util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JsonGenerator {
    public static class JsonKV {
        public final String key;
        public final JsonNode value;
        
        private JsonKV(String key, JsonNode value) {
            this.key = key;
            this.value = value;
        }
    }
    
    public static JsonKV p(String key, JsonNode value) {
        return new JsonKV(key, value);
    }
    
    public static ObjectNode o(JsonKV... children) {
        Map<String, JsonNode> kvs = new HashMap<>();
        for (JsonKV kv : children) {
            kvs.put(kv.key, kv.value);
        }
        return new ObjectNode(JsonNodeFactory.instance, kvs);
    }
    
    public static ArrayNode a(JsonNode... children) {
        return new ArrayNode(JsonNodeFactory.instance, Arrays.asList(children));
    }
    
    public static ValueNode v(int val) { return new IntNode(val); }
    public static ValueNode v(long val) { return new LongNode(val); }
    public static ValueNode v(float val) { return new FloatNode(val); }
    public static ValueNode v(double val) { return new DoubleNode(val); }
    public static ValueNode v(boolean val) { return BooleanNode.valueOf(val); }
    public static ValueNode v(short val) { return new ShortNode(val); }
    public static ValueNode v(String val) {
        if (val == null) {
            return NullNode.getInstance();
        }
        return new TextNode(val);
    }
}

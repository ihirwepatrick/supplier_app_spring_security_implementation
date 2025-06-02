package rca.restapi.year2.year2ADemo.Config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class SwaggerRequestParameterConfig {

    /**
     * Custom deserializer to handle String values that might come as arrays from Swagger UI
     */
    public static class StringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (p.isExpectedStartArrayToken()) {
                // If the value is an array, get the first element
                List<String> list = p.readValueAs(List.class);
                return list.isEmpty() ? null : list.get(0).toString();
            }
            // Otherwise, treat as normal string
            return p.getValueAsString();
        }
    }

    /**
     * Custom deserializer to handle Set<String> values that might come in different formats from Swagger UI
     */
    public static class SetDeserializer extends JsonDeserializer<Set<String>> {
        @Override
        public Set<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            Set<String> result = new HashSet<>();
            
            // Handle different incoming formats
            if (p.isExpectedStartArrayToken()) {
                // If it's already an array, convert each element to string
                List<Object> list = p.readValueAs(List.class);
                for (Object item : list) {
                    result.add(item.toString());
                }
            } else {
                // If it's a single value, add it as the only element
                result.add(p.getValueAsString());
            }
            
            return result;
        }
    }
} 
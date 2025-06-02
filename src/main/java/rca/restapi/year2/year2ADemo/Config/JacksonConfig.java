package rca.restapi.year2.year2ADemo.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import rca.restapi.year2.year2ADemo.Models.Admin;
import rca.restapi.year2.year2ADemo.Models.Supplier;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();
        
        // Register mixins for Admin and Supplier to handle roles field
        objectMapper.addMixIn(Admin.class, AdminMixin.class);
        objectMapper.addMixIn(Supplier.class, SupplierMixin.class);
        
        // Register Java 8 time module
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
        objectMapper.registerModule(javaTimeModule);
        
        return objectMapper;
    }
    
    // Mixin for Admin class
    abstract static class AdminMixin {
        @JsonDeserialize(using = SwaggerRequestParameterConfig.SetDeserializer.class)
        private Set<String> roles;
    }
    
    // Mixin for Supplier class
    abstract static class SupplierMixin {
        @JsonDeserialize(using = SwaggerRequestParameterConfig.SetDeserializer.class)
        private Set<String> roles;
    }
} 
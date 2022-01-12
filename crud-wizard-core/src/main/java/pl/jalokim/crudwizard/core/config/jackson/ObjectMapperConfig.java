package pl.jalokim.crudwizard.core.config.jackson;

import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getClassForName;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;

@Configuration
public class ObjectMapperConfig {

    public static final AtomicReference<ObjectMapper> OBJECT_MAPPER_INSTANCE_REF = new AtomicReference<>(createObjectMapper());

    public static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false);
        objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer());
        javaTimeModule.addDeserializer(OffsetDateTime.class, new OffsetDateTimeDeserializer());
        objectMapper.registerModule(javaTimeModule);
        objectMapper.registerModule(new StringBlankToNullModule());
        objectMapper.addMixIn(Page.class, PageMixIn.class);
        SimpleModule module = new SimpleModule();
        module.addSerializer(Enum.class, new EnumToLowerCaseSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

    @Bean
    @Primary
    public ObjectMapper serializingObjectMapper() {
        return OBJECT_MAPPER_INSTANCE_REF.get();
    }

    public static ObjectMapper getInstance() {
        return OBJECT_MAPPER_INSTANCE_REF.get();
    }

    @SuppressWarnings("unchecked")
    public static <T> T rawJsonToObject(String rawJson, String realClassName) {
        return (T) Optional.ofNullable(realClassName)
            .map(className -> rawJsonToObject(rawJson, getClassForName(className)))
            .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T rawJsonToObject(String nullableRawJson, Class<?> realClass) {
        return (T) Optional.ofNullable(nullableRawJson)
            .map(rawJson -> mapRawJsonToObject(rawJson, realClass))
            .orElse(null);
    }

    @SneakyThrows
    private static Object mapRawJsonToObject(String rawJson, Class<?> realClass) {
        return ObjectMapperConfig.getInstance().readValue(rawJson, realClass);
    }

    public static String objectToRawJson(Object rawObject) {
        return Optional.ofNullable(rawObject)
            .map(ObjectMapperConfig::writeValueAsString)
            .orElse(null);
    }

    @SneakyThrows
    private static String writeValueAsString(Object object) {
        return ObjectMapperConfig.getInstance().writeValueAsString(object);
    }
}

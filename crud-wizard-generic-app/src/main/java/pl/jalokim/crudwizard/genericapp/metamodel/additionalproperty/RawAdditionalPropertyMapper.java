package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getClassForName;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyDto;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class RawAdditionalPropertyMapper {

    @Autowired
    private ObjectMapper objectMapper;

    public AdditionalPropertyDto additionalPropertyToDto(AdditionalPropertyEntity additionalPropertyEntity) {
        String valueRealClassName = additionalPropertyEntity.getValueRealClassName();
        return AdditionalPropertyDto.builder()
            .id(additionalPropertyEntity.getId())
            .name(additionalPropertyEntity.getName())
            .valueRealClassName(valueRealClassName)
            .rawJson(additionalPropertyEntity.getRawJson())
            .valueAsObject(rawJsonToObject(additionalPropertyEntity))
            .build();
    }

    private Object rawJsonToObject(AdditionalPropertyEntity additionalPropertyEntity) {
        String nullableRealClass = additionalPropertyEntity.getValueRealClassName();
        return Optional.ofNullable(nullableRealClass)
            .map(valueRealClassName -> readValue(additionalPropertyEntity, valueRealClassName))
            .orElse(null);
    }

    @SneakyThrows
    private Object readValue(AdditionalPropertyEntity additionalPropertyEntity, String valueRealClassName) {
        return objectMapper.readValue(additionalPropertyEntity.getRawJson(), getClassForName(valueRealClassName));
    }

    private String objectToRawJson(AdditionalPropertyDto additionalPropertyDto) {
        return Optional.ofNullable(additionalPropertyDto.getRealValue())
            .map(this::writeValueAsString)
            .orElse(null);
    }

    @SneakyThrows
    private String writeValueAsString(Object object) {
        return objectMapper.writeValueAsString(object);
    }

    public AdditionalPropertyEntity additionalPropertyToEntity(AdditionalPropertyDto additionalPropertyDto) {
        String valueRealClassName = additionalPropertyDto.getValueRealClassName();
        return AdditionalPropertyEntity.builder()
            .id(additionalPropertyDto.getId())
            .name(additionalPropertyDto.getName())
            .valueRealClassName(valueRealClassName)
            .rawJson(objectToRawJson(additionalPropertyDto))
            .build();
    }
}

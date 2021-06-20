package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyDto;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class RawAdditionalPropertyMapper {

    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    public AdditionalPropertyDto additionalPropertyToDto(AdditionalPropertyEntity additionalPropertyEntity) {
        String valueRealClassName = additionalPropertyEntity.getValueRealClassName();
        return AdditionalPropertyDto.builder()
            .id(additionalPropertyEntity.getId())
            .name(additionalPropertyEntity.getName())
            .valueRealClassName(valueRealClassName)
            .value(objectMapper.readValue(additionalPropertyEntity.getJsonValue(), MetadataReflectionUtils.getClassForName(valueRealClassName)))
            .build();
    }

    @SneakyThrows
    public AdditionalPropertyEntity additionalPropertyToEntity(AdditionalPropertyDto additionalPropertyDto) {
        String valueRealClassName = additionalPropertyDto.getValueRealClassName();
        return AdditionalPropertyEntity.builder()
            .id(additionalPropertyDto.getId())
            .name(additionalPropertyDto.getName())
            .valueRealClassName(valueRealClassName)
            .jsonValue(objectMapper.writeValueAsString(additionalPropertyDto.getValue()))
            .build();
    }
}

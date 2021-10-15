package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getClassForName;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyDto;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;

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
            .rawJson(additionalPropertyEntity.getRawJson())
            .valueAsObject(objectMapper.readValue(additionalPropertyEntity.getRawJson(), getClassForName(valueRealClassName)))
            .build();
    }

    @SneakyThrows
    public AdditionalPropertyEntity additionalPropertyToEntity(AdditionalPropertyDto additionalPropertyDto) {
        String valueRealClassName = additionalPropertyDto.getValueRealClassName();
        return AdditionalPropertyEntity.builder()
            .id(additionalPropertyDto.getId())
            .name(additionalPropertyDto.getName())
            .valueRealClassName(valueRealClassName)
            .rawJson(objectMapper.writeValueAsString(additionalPropertyDto.getRealValue()))
            .build();
    }
}

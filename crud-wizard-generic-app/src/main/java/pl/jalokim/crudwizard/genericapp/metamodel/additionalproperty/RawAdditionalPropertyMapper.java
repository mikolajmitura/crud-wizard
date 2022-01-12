package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.rawJsonToObject;

import org.mapstruct.Mapper;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class RawAdditionalPropertyMapper {

    public AdditionalPropertyDto additionalPropertyToDto(AdditionalProperty additionalPropertyEntity) {
        String valueRealClassName = additionalPropertyEntity.getValueRealClassName();
        return AdditionalPropertyDto.builder()
            .name(additionalPropertyEntity.getName())
            .valueRealClassName(valueRealClassName)
            .rawJson(additionalPropertyEntity.getRawJson())
            .build();
    }

    public AdditionalProperty additionalPropertyToEntity(AdditionalPropertyDto additionalPropertyDto) {
        String valueRealClassName = additionalPropertyDto.getValueRealClassName();
        return AdditionalProperty.builder()
            .name(additionalPropertyDto.getName())
            .valueRealClassName(valueRealClassName)
            .rawJson(additionalPropertyDto.getRawJson())
            .build();
    }

    public AdditionalPropertyMetaModel additionalPropertyToModel(AdditionalProperty additionalPropertyEntity) {
        String valueRealClassName = additionalPropertyEntity.getValueRealClassName();
        return AdditionalPropertyMetaModel.builder()
            .name(additionalPropertyEntity.getName())
            .valueRealClassName(valueRealClassName)
            .rawJson(additionalPropertyEntity.getRawJson())
            .valueAsObject(rawJsonToObject(additionalPropertyEntity.getRawJson(), valueRealClassName))
            .build();
    }
}

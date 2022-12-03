package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import lombok.Value;

@Value
public class ClassMetaModelConverterDefinition {

    String beanName;
    ClassMetaModelConverter<Object, Object> converter;
}

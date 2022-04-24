package pl.jalokim.crudwizard.core.metamodels;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MapperMetaModel extends WithAdditionalPropertiesMetaModel {

    Long id;
    Object mapperInstance;
    String className;
    String beanName;
    String methodName;
    BeanMethodMetaModel methodMetaModel;

    MapperType mapperType;
    String mapperName;
    ClassMetaModel sourceClassMetaModel;
    ClassMetaModel targetClassMetaModel;
}

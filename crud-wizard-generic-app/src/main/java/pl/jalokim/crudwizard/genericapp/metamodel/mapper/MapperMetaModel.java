package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import pl.jalokim.crudwizard.genericapp.metamodel.MetaModelState;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodMetaModel;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MapperMetaModel extends WithAdditionalPropertiesMetaModel {

    Long id;
    Object mapperInstance;
    BeanAndMethodMetaModel methodMetaModel;

    MapperType mapperType;
    String mapperName;
    // TODO #1 check that sourceClassMetaModel and targetClassMetaModel exists in GENERATED mappers and in this from beans.
    ClassMetaModel sourceClassMetaModel;
    ClassMetaModel targetClassMetaModel;
    MetaModelState state;
}

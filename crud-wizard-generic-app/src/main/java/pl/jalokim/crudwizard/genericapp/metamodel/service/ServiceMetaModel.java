package pl.jalokim.crudwizard.genericapp.metamodel.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodMetaModel;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder(toBuilder = true)
public class ServiceMetaModel extends WithAdditionalPropertiesMetaModel {

    Long id;

    /**
     * real instance of service
     */
    Object serviceInstance;

    BeanAndMethodMetaModel serviceBeanAndMethod;

    // TODO script will be loaded to serviceInstance in lazy way and cached then
    String serviceScript;
}

package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQueryProvider;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EndpointResponseMetaModel extends WithAdditionalPropertiesMetaModel {

    public static final EndpointResponseMetaModel EMPTY = EndpointResponseMetaModel.builder().build();

    Long id;
    ClassMetaModel classMetaModel;
    Integer successHttpCode;
    MapperMetaModel mapperMetaModel;
    DataStorageQueryProvider queryProvider;
}

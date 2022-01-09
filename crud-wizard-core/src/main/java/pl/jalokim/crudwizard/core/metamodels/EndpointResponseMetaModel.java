package pl.jalokim.crudwizard.core.metamodels;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryProvider;

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

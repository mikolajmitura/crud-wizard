package pl.jalokim.crudwizard.genericapp.metamodel.service;

import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.ParentMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classes.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.mapping.MapperMetaModel;

@EqualsAndHashCode(callSuper = true)
@Value
public class DataStorageConnectorMetaModel extends ParentMetaModel {

    Long id;
    DataStorageMetaModel dataStorageMetaModel;
    MapperMetaModel mapperMetaModel;
    ClassMetaModel classMetaModelInDataStorage;
}

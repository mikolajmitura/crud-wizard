package pl.jalokim.crudwizard.genericapp.metamodel.service;

import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.classes.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.mapping.MapperMetaModel;

@Value
public class DataStorageConnectorMetaModel {

    Long dataStorageId;
    MapperMetaModel mapperMetaModel;

    Long existingDataStorageMetaModelId;

    ClassMetaModel dataStorageMetaModel;
}

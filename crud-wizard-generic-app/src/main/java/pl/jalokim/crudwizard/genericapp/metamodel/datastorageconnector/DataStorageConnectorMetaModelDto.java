package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider.QueryProviderDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Jacksonized
@SuperBuilder(toBuilder = true)
public class DataStorageConnectorMetaModelDto extends WithAdditionalPropertiesDto {

    Long id;
    @Valid
    DataStorageMetaModelDto dataStorageMetaModel;
    @Valid
    MapperMetaModelDto mapperMetaModelForPersist;
    @Valid
    MapperMetaModelDto mapperMetaModelForQuery;
    @Valid
    ClassMetaModelDto classMetaModelInDataStorage;

    @Size(min = 3, max = 100)
    String nameOfQuery;

    @Valid
    QueryProviderDto queryProvider;
}

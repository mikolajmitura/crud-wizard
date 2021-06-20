package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class DataStorageConnectorMetaModelDto extends AdditionalPropertyMetaModelDto {

    Long id;
    DataStorageMetaModelDto dataStorageMetaModel;
    MapperMetaModelDto mapperMetaModel;
    ClassMetaModelDto classMetaModelInDataStorage;
}

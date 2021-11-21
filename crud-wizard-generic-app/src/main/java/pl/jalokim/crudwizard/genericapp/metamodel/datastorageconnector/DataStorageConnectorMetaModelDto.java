package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyDto;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DataStorageConnectorMetaModelDto extends AdditionalPropertyMetaModelDto {

    Long id;
    @Valid
    DataStorageMetaModelDto dataStorageMetaModel;
    @Valid
    MapperMetaModelDto mapperMetaModelForReturn;
    @Valid
    MapperMetaModelDto mapperMetaModelForQuery;
    @Valid
    ClassMetaModelDto classMetaModelInDataStorage;

    @Builder.Default
    List<AdditionalPropertyDto> additionalProperties = new ArrayList<>();
}

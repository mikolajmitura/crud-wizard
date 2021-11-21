package pl.jalokim.crudwizard.genericapp.metamodel.url;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDto;

@Data
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
// TODO #38 should be used instead of pathParams in EndpointMetaModelDto
//  and should be mapped to DataStorageMetaForUrlModel
public class PathVariableMetaModelDto {

    String pathVariableName;
    String pathVariableRealClassName;
    DataStorageMetaModelDto dataStorageMetaModel;
    ClassMetaModelDto classMetaModelInDataStorage;
}

package pl.jalokim.crudwizard.genericapp.metamodel.url;

import javax.validation.Valid;
import javax.validation.constraints.Size;
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

    @Size(min = 3, max = 250)
    String pathVariableName;

    @Size(min = 3, max = 250)
    String pathVariableRealClassName;

    @Valid
    DataStorageMetaModelDto dataStorageMetaModel;

    @Valid
    ClassMetaModelDto classMetaModelInDataStorage;
}

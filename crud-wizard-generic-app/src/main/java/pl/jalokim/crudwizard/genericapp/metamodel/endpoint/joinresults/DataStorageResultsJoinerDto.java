package pl.jalokim.crudwizard.genericapp.metamodel.endpoint.joinresults;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;
import pl.jalokim.crudwizard.genericapp.datastorage.query.ObjectsJoinerVerifier;

@Data
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DataStorageResultsJoinerDto {

    Long id;

    @NotEmpty
    @Size(min = 3, max = 100)
    String leftNameOfQueryResult;

    @NotEmpty
    @Size(min = 1, max = 250)
    String leftPath;

    @NotEmpty
    @ClassExists(expectedOfType = ObjectsJoinerVerifier.class)
    @Size(min = 3, max = 250)
    String joinerVerifierClassName;

    @NotEmpty
    @Size(min = 3, max = 100)
    String rightNameOfQueryResult;

    @NotEmpty
    @Size(min = 1, max = 250)
    String rightPath;
}

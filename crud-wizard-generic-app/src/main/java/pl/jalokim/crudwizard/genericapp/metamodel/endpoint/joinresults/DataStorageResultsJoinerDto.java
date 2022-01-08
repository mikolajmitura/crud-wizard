package pl.jalokim.crudwizard.genericapp.metamodel.endpoint.joinresults;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.datastorage.query.ObjectsJoinerVerifier;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;

@Data
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DataStorageResultsJoinerDto {

    Long id;

    @NotNull
    String leftNameOfQueryResult;
    @NotNull
    String leftPath;
    @NotNull
    @ClassExists(expectedOfType = ObjectsJoinerVerifier.class)
    String joinerVerifierClassName;
    @NotNull
    String rightNameOfQueryResult;
    @NotNull
    String rightPath;
}

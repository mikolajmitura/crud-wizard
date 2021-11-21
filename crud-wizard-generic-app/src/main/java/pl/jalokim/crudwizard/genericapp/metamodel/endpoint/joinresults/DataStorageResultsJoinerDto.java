package pl.jalokim.crudwizard.genericapp.metamodel.endpoint.joinresults;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

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
    String joinerVerifierClassName;
    @NotNull
    String rightNameOfQueryResult;
    @NotNull
    String rightPath;
}

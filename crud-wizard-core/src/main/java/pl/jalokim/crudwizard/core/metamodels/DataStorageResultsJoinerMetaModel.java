package pl.jalokim.crudwizard.core.metamodels;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.datastorage.query.ObjectsJoinerVerifier;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DataStorageResultsJoinerMetaModel {

    String leftNameOfQueryResult;
    String leftPath;
    ObjectsJoinerVerifier<Object, Object> joinerVerifierInstance;
    String rightNameOfQueryResult;
    String rightPath;
}

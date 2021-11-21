package pl.jalokim.crudwizard.core.metamodels;

import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.datastorage.query.ObjectsJoinerVerifier;

@Value
@Builder(toBuilder = true)
public class DataStorageResultsJoinerMetaModel {

    String leftNameOfQueryResult;
    String leftPath;
    ObjectsJoinerVerifier<Object, Object> joinerVerifierInstance;
    String rightNameOfQueryResult;
    String rightPath;
}

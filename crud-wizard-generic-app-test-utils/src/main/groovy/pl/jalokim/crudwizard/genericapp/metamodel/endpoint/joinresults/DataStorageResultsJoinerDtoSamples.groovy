package pl.jalokim.crudwizard.genericapp.metamodel.endpoint.joinresults

import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query.EqObjectsJoiner

class DataStorageResultsJoinerDtoSamples {

    static DataStorageResultsJoinerDto sampleJoinerDto(String leftQueryName, String leftPath,
        String rightQueryName, String rightPath) {

        DataStorageResultsJoinerDto.builder()
            .leftNameOfQueryResult(leftQueryName)
            .leftPath(leftPath)
            .joinerVerifierClassName(EqObjectsJoiner.canonicalName)
            .rightNameOfQueryResult(rightQueryName)
            .rightPath(rightPath)
            .build()
    }
}

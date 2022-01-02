package pl.jalokim.crudwizard.genericapp.rest.samples.mapper;

import static pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage.DEFAULT_DS_NAME;

import java.util.Map;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;

public class GetByIdFromFewDsMapper {

    Object mapPersonsResultsToOne(GenericMapperArgument genericMapperArgument) {
        Map<String, Object> mappingContext = genericMapperArgument.getMappingContext();
        return Map.of(
            "firstDb", mappingContext.get(DEFAULT_DS_NAME),
            "secondDb", mappingContext.get("second-db"),
            "thirdDb", mappingContext.get("third-db")
        );
    }

}

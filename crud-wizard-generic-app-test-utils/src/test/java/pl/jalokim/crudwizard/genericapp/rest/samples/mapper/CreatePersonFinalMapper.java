package pl.jalokim.crudwizard.genericapp.rest.samples.mapper;

import static pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage.DEFAULT_DS_NAME;

import java.util.Map;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;

public class CreatePersonFinalMapper {

    public Map<String, Object> returnIdFromDefaultDs(GenericMapperArgument genericMapperArgument) {
        return Map.of("personId", genericMapperArgument.getMappingContext().get(DEFAULT_DS_NAME),
            "personDocumentUUID", genericMapperArgument.getMappingContext().get("third-db")) ;
    }
}

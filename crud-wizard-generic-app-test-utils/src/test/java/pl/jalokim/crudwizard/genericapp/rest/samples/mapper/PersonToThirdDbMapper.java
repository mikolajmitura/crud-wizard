package pl.jalokim.crudwizard.genericapp.rest.samples.mapper;

import static pl.jalokim.crudwizard.core.utils.ValueExtractorFromPath.getValueFromPath;
import static pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage.DEFAULT_DS_NAME;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;

@RequiredArgsConstructor
public class PersonToThirdDbMapper {

    private final PersonDocumentInThirdDbIdMapper personDocumentInThirdDbIdMapper;

    @SuppressWarnings("unchecked")
    Map<String, Object> personToThirdDbMapperCreate(GenericMapperArgument genericMapperArgument) {
        Map<String, Object> thirdDbPerson = new HashMap<>();
        Map<String, Object> sourceObject = (Map<String, Object>) genericMapperArgument.getSourceObject();
        Map<String, Object> mappingContext = genericMapperArgument.getMappingContext();

        // this below will useful for fetch current uuid when is update of object
        thirdDbPerson.put("uuid", personDocumentInThirdDbIdMapper.mapToUuid(genericMapperArgument));

        thirdDbPerson.put("documentValue", getValueFromPath(sourceObject, "document.value"));
        thirdDbPerson.put("firstDbId", mappingContext.get(DEFAULT_DS_NAME));
        return thirdDbPerson;
    }
}

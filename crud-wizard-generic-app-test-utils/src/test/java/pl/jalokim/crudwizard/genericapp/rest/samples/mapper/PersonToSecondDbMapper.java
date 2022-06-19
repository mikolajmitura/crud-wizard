package pl.jalokim.crudwizard.genericapp.rest.samples.mapper;

import static pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage.DEFAULT_DS_NAME;
import static pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQuery.buildSelectFromAndWhere;
import static pl.jalokim.crudwizard.genericapp.datastorage.query.RealExpression.isEqualsTo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import pl.jalokim.crudwizard.core.utils.ValueExtractorFromPath;
import pl.jalokim.crudwizard.genericapp.datastorage.DataStorage;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;

@AllArgsConstructor
public class PersonToSecondDbMapper {

    private final MetaModelContextService metaModelContextService;

    @SuppressWarnings("unchecked")
    Object personToSecondDbMapperCreate(GenericMapperArgument genericMapperArgument) {
        DataStorage secondDb = metaModelContextService.getDataStorageByName("second-db");
        List<Object> foundEntities = secondDb.findEntities(buildSelectFromAndWhere(
            metaModelContextService.getClassMetaModelByName("personSecondDb"),
            isEqualsTo("firstDbId", genericMapperArgument.getPathVariables().get("userId"))));

        Map<String, Object> secondDbPerson = new HashMap<>();
        Map<String, Object> sourceObject = (Map<String, Object>) genericMapperArgument.getSourceObject();
        Map<String, Object> mappingContext = genericMapperArgument.getMappingContext();

        if (foundEntities.size() == 1) {
            secondDbPerson.put("uuid", ValueExtractorFromPath.getValueFromPath(foundEntities.get(0), "uuid"));
        }

        secondDbPerson.put("name", sourceObject.get("name"));
        secondDbPerson.put("surname", sourceObject.get("surname"));
        secondDbPerson.put("firstDbId", mappingContext.get(DEFAULT_DS_NAME));
        return secondDbPerson;
    }
}

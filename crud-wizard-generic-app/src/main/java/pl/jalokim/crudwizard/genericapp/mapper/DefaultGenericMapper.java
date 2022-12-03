package pl.jalokim.crudwizard.genericapp.mapper;

import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getFullClassName;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.genericapp.config.GenericMapper;
import pl.jalokim.crudwizard.genericapp.config.GenericMethod;
import pl.jalokim.crudwizard.genericapp.service.results.JoinedResultsRow;
import pl.jalokim.crudwizard.genericapp.service.results.JoinedResultsRowMapper;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@RequiredArgsConstructor
@GenericMapper
public class DefaultGenericMapper {

    private final JoinedResultsRowMapper joinedResultsRowMapper;

    @GenericMethod
    @SuppressWarnings("unchecked")
    public Object mapToTarget(GenericMapperArgument mapperArgument) {
        if (mapperArgument.getTargetMetaModel() != null) {
            if (mapperArgument.getTargetMetaModel().isTheSameMetaModel(mapperArgument.getSourceMetaModel())) {
                // TODO #1 should be deep copy or just pass it???
                return mapperArgument.getSourceObject();
            }
            // it returns list of maps where every map contains results from data storages.
            if (mapperArgument.getSourceObject() instanceof JoinedResultsRow) {
                JoinedResultsRow joinedResultsRow = (JoinedResultsRow) mapperArgument.getSourceObject();
                return joinedResultsRowMapper.mapToObject(mapperArgument.getTargetMetaModel(), joinedResultsRow);
            }

            if (mapperArgument.getSourceMetaModel() == null) {
                // simple work around for return first created id from DS or from all
                return getFirstCreatedIdOrAllCreateResults(mapperArgument);
            }
        }
        if (mapperArgument.getTargetMetaModel() == null && mapperArgument.getSourceMetaModel() == null) {
            // simple workaround for return mapped id for querying
            return mapperArgument.getSourceObject();
        }

        // TODO #1 should map from one meta model to another meta model.
        // another is auto mapping the same fields with names and the same types or with auto conversion from one to another.
        throw new UnsupportedOperationException("Not supported mapping yet!");
    }

    @SuppressWarnings("unchecked")
    private Object getFirstCreatedIdOrAllCreateResults(GenericMapperArgument mapperArgument) {
        Map<String, Object> resultsFrom = (Map<String, Object>) mapperArgument.getSourceObject();
        if (resultsFrom.values().size() == 1) {
            return Elements.elements(resultsFrom.values())
                .filter(value -> mapperArgument.getTargetMetaModel().getRealClass() == null ||
                    MetadataReflectionUtils.isTypeOf(value, mapperArgument.getTargetMetaModel().getRealClass()))
                .findFirst()
                .orElseThrow(() -> {
                    Object foundValue = resultsFrom.values().iterator().next();
                    return new IllegalArgumentException("expected class: " +
                        mapperArgument.getTargetMetaModel().getRealClass().getCanonicalName() +
                        " as endpoint type but given was value: " + foundValue +
                        " with type: " + getFullClassName(foundValue));
                });
        }
        return resultsFrom;
    }
}

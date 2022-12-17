package pl.jalokim.crudwizard.genericapp.mapper.defaults;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.config.GenericMethod;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.service.results.JoinedResultsRow;
import pl.jalokim.crudwizard.genericapp.service.results.JoinedResultsRowMapper;

@RequiredArgsConstructor
@Component
public class DefaultFinalJoinedRowOrDefaultMapper implements BaseGenericMapper {

    private final JoinedResultsRowMapper joinedResultsRowMapper;
    private final DefaultGenericMapper defaultGenericMapper;

    @GenericMethod
    @Override
    @SuppressWarnings("unchecked")
    public Object mapToTarget(GenericMapperArgument mapperArgument) {
        if (mapperArgument.getSourceObject() instanceof JoinedResultsRow) {
            JoinedResultsRow joinedResultsRow = (JoinedResultsRow) mapperArgument.getSourceObject();
            return joinedResultsRowMapper.mapToObject(mapperArgument.getTargetMetaModel(), joinedResultsRow);
        }
        // when context mapping for get one object
        if (mapperArgument.getSourceObject() instanceof Map) {
            Map<String, Object> resultsFrom = (Map<String, Object>) mapperArgument.getSourceObject();
            if (resultsFrom.values().size() == 1) {
                return DefaultFinalGetIdAfterSaveMapper.getFirstElementOrAllFromContext(mapperArgument);
            }
        }
        return defaultGenericMapper.mapToTarget(mapperArgument);
    }
}

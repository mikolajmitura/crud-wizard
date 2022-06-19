package pl.jalokim.crudwizard.genericapp.service.results;

import static pl.jalokim.utils.reflection.InvokableReflectionUtils.getValueOfField;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@Component
public class JoinedResultsRowMapper {

    public Object mapToObject(ClassMetaModel targetMetaModel, JoinedResultsRow joinedResultsRow) {
        Map<String, Object> finalResult = new HashMap<>();
        Map<String, Object> joinedResultsByDsQueryName = joinedResultsRow.getJoinedResultsByDsQueryName();
        // TODO #11 build final results based on targetMetaModel and resolve mapping field collision somehow...
        //  resolving of field collision should be done via passed CWML script
        for (Object value : joinedResultsByDsQueryName.values()) {
            if (value instanceof Map) {
                Map<String, Object> dsResultMap = (Map<String, Object>) value;
                for (var entry : dsResultMap.entrySet()) {
                    finalResult.putIfAbsent(entry.getKey(), entry.getValue());
                }
            } else {
                for (Field field : MetadataReflectionUtils.getAllFields(value.getClass())) {
                    finalResult.computeIfAbsent(field.getName(), (fieldName) -> getValueOfField(value, fieldName));
                }
            }
        }
        return finalResult;
    }
}

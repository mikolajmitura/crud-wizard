package pl.jalokim.crudwizard.genericapp.mapper;

import java.util.Map;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

@Value
@Builder(toBuilder = true)
public class GenericMapperArgument {

    ClassMetaModel sourceMetaModel;
    Object sourceObject;
    ClassMetaModel targetMetaModel;
    Map<String, String> headers;
    Map<String, Object> pathVariables;
    Map<String, Object> requestParams;
    /**
     *  results of current mapping from other data storages run earlier than current mapper
     */
    Map<String, Object> mappingContext;

}

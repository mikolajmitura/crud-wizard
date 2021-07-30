package pl.jalokim.crudwizard.genericapp.mapper;

import java.util.Map;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

@Value
@Builder
public class MapperArgument {

    ClassMetaModel sourceMetaModel;
    Map<String, Object> sourceObject;
    ClassMetaModel targetMetaModel;

}

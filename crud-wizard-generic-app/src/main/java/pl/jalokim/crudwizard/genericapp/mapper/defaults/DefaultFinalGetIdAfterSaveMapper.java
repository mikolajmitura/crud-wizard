package pl.jalokim.crudwizard.genericapp.mapper.defaults;

import java.util.Map;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.config.GenericMethod;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@Component
public class DefaultFinalGetIdAfterSaveMapper implements BaseGenericMapper {

    @GenericMethod
    @Override
    public Object mapToTarget(GenericMapperArgument mapperArgument) {
        return getFirstElementOrAllFromContext(mapperArgument);
    }

    @SuppressWarnings("unchecked")
    static Object getFirstElementOrAllFromContext(GenericMapperArgument mapperArgument) {
        Map<String, Object> resultsFrom = (Map<String, Object>) mapperArgument.getSourceObject();
        if (resultsFrom.values().size() == 1) {
            return Elements.elements(resultsFrom.values())
                .filter(value -> mapperArgument.getTargetMetaModel().isGenericModel() ||
                    MetadataReflectionUtils.isTypeOf(value, mapperArgument.getTargetMetaModel().getRealClass()))
                .findFirst()
                .orElseThrow(() -> {
                    Object foundValue = resultsFrom.values().iterator().next();
                    return new IllegalArgumentException("expected type: " +
                        mapperArgument.getTargetMetaModel().getTypeDescription() +
                        " as endpoint type but given was value: " + foundValue +
                        " with type: " + mapperArgument.getSourceMetaModel().getTypeDescription());
                });
        }
        return resultsFrom;
    }
}

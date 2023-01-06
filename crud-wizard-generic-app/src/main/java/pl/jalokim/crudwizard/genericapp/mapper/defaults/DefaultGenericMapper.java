package pl.jalokim.crudwizard.genericapp.mapper.defaults;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.mapper.conversion.GenericObjectsConversionService;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@Component
@RequiredArgsConstructor
public class DefaultGenericMapper implements BaseGenericMapper {

    private final GenericObjectsConversionService genericObjectsConversionService;

    @Override
    public Object mapToTarget(GenericMapperArgument mapperArgument) {
        ClassMetaModel targetMetaModel = mapperArgument.getTargetMetaModel();
        ClassMetaModel sourceMetaModel = mapperArgument.getSourceMetaModel();
        if (targetMetaModel != null && sourceMetaModel != null) {
            if (targetMetaModel.isTheSameMetaModel(sourceMetaModel)) {
                return mapperArgument.getSourceObject();
            }

            var converterDefinition = genericObjectsConversionService
                .findConverterDefinition(sourceMetaModel, targetMetaModel);

            if (converterDefinition != null) {
                return converterDefinition.getConverter().convert(mapperArgument.getSourceObject());
            }
            throw new IllegalArgumentException("cannot convert from:" +
                sourceMetaModel.getTypeDescription() + " to: " + targetMetaModel.getTypeDescription()) {
            };
        }
        throw new IllegalArgumentException("conversion problem value: " + mapperArgument.getSourceObject());
    }
}

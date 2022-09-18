package pl.jalokim.crudwizard.genericapp.metamodel.mapper.validation;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;
import pl.jalokim.crudwizard.core.validation.javax.base.PropertyPath;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.FieldMetaModelExtractor;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.PropertiesOverriddenMappingDto;
import pl.jalokim.utils.collection.CollectionUtils;

@Component
@RequiredArgsConstructor
public class TargetAssignPathsValidator implements BaseConstraintValidator<TargetAssignPaths, MapperConfigurationDto> {

    private final ClassMetaModelMapper classMetaModelMapper;

    @Override
    public boolean isValidValue(MapperConfigurationDto mapperConfigurationDto, ConstraintValidatorContext context) {
        AtomicBoolean isValid = new AtomicBoolean(true);
        ClassMetaModelDto targetMetaModelDto = mapperConfigurationDto.getTargetMetaModel();

        if (CollectionUtils.isNotEmpty(mapperConfigurationDto.getPropertyOverriddenMapping())
            && targetMetaModelDto != null) {

            var targetMetaModel = classMetaModelMapper.toModelFromDto(targetMetaModelDto);

            elements(mapperConfigurationDto.getPropertyOverriddenMapping())
                .map(PropertiesOverriddenMappingDto::getTargetAssignPath)
                .forEachWithIndex((index, path) -> {
                    try {
                        FieldMetaModelExtractor.extractFieldMetaModel(targetMetaModel, path);
                    } catch (TechnicalException ex) {
                        customMessage(context, ex.getMessage(), PropertyPath.builder()
                            .addNextPropertyAndIndex("propertyOverriddenMapping", index)
                            .addNextProperty("targetAssignPath")
                            .build());
                        isValid.set(false);
                    }
                });
        }
        return isValid.get();
    }
}

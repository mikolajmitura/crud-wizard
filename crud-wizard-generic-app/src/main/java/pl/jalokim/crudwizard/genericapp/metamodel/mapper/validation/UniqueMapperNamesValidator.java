package pl.jalokim.crudwizard.genericapp.metamodel.mapper.validation;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsExternalPlaceholder;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.validation.ConstraintValidatorContext;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;
import pl.jalokim.crudwizard.core.validation.javax.base.PropertyPath;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto;

public class UniqueMapperNamesValidator implements BaseConstraintValidator<UniqueMapperNames, MapperMetaModelDto> {

    @Override
    public boolean isValidValue(MapperMetaModelDto mapperMetaModelDto, ConstraintValidatorContext context) {
        if (mapperMetaModelDto.getMapperGenerateConfiguration() != null) {
            MapperGenerateConfigurationDto mapperGenerateConfiguration = mapperMetaModelDto.getMapperGenerateConfiguration();
            MapperConfigurationDto rootConfiguration = mapperGenerateConfiguration.getRootConfiguration();

            AtomicBoolean isValid = new AtomicBoolean(true);
            if (mapperGenerateConfiguration.getRootConfiguration() != null &&
                !Objects.equals(mapperMetaModelDto.getMapperName(), rootConfiguration.getName())) {
                isValid.set(false);
                customMessage(context, createMessagePlaceholder("UniqueMapperNamesValidator.root.names.should.be.the.same",
                    wrapAsExternalPlaceholder("mapperGenerateConfiguration.rootConfiguration.name")), "name");
            }

            elements(mapperGenerateConfiguration.getSubMappersAsMethods())
                .forEachWithIndex((index, method) -> {
                    var foundMethods = elements(mapperGenerateConfiguration.getSubMappersAsMethods())
                        .filter(nextMethod -> Objects.equals(nextMethod.getName(), method.getName()))
                        .asList();
                    if (foundMethods.size() > 1) {
                        isValid.set(false);
                        customMessage(context,
                            "{UniqueMapperNamesValidator.not.unique.method.name}",
                            PropertyPath.builder()
                                .addNextProperty("mapperGenerateConfiguration")
                                .addNextPropertyAndIndex("subMappersAsMethods", index)
                                .build()
                        );
                    }
                });

            return isValid.get();
        }

        return true;
    }
}

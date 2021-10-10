package pl.jalokim.crudwizard.genericapp.validation.validator;

import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getAllChildClassesForClass;

import java.util.Map;
import javax.validation.ConstraintValidator;
import javax.validation.constraints.Size;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.TypeMetadata;

/**
 * This is proxy validator for javax validators in package org.hibernate.validator.internal.constraintvalidators.bv.size from
 *
 * groupId=org.hibernate.validator
 * artifactId=hibernate-validator
 *
 *
 * It can validate size of collections, arrays, map texts based on CharSequence.
 */
public class SizeValidator extends JavaxProxyDataValidator<Size, Object> {

    private static final Integer DEFAULT_MIN_VALUE = 0;
    private static final Integer DEFAULT_MAX_VALUE = Integer.MAX_VALUE;

    public static final String VALIDATOR_KEY_SIZE = "SIZE";
    public static final String SIZE_MAX_ARG = "max";
    public static final String SIZE_MIN_ARG = "min";

    private static final Map<Class<?>, Class<?>> JAVAX_SIZE_VALIDATORS_BY_CLASS;

    static {
        JAVAX_SIZE_VALIDATORS_BY_CLASS = elements(getAllChildClassesForClass(ConstraintValidator.class,
            "org.hibernate.validator.internal.constraintvalidators.bv.size",
            false
        ))
            .map(MetadataReflectionUtils::getTypeMetadataFromClass)
            .asMap(typeMetadata ->
                    typeMetadata.getTypeMetaDataForParentClass(ConstraintValidator.class)
                        .getGenericTypes().get(1).getRawType(),
                TypeMetadata::getRawType);
    }

    @Override
    public String validatorName() {
        return VALIDATOR_KEY_SIZE;
    }

    @Override
    public Map<Class<?>, Class<?>> getValidatorsByType() {
        return JAVAX_SIZE_VALIDATORS_BY_CLASS;
    }

    @Override
    public Map<String, Object> messagePlaceholderArgs(ValidationSessionContext validationContext) {
        Integer expectedMinSize = validationContext.getValidatorArgumentOrDefault(SIZE_MIN_ARG, DEFAULT_MIN_VALUE);
        Integer expectedMaxSize = validationContext.getValidatorArgumentOrDefault(SIZE_MAX_ARG, DEFAULT_MAX_VALUE);

        return Map.of(
            "min", expectedMinSize,
            "max", expectedMaxSize
        );
    }
}

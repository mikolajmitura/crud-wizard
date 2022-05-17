package pl.jalokim.crudwizard.genericapp.validation.generic;

import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isTypeOf;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.AdditionalValidatorsMetaModel;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.core.metamodels.PropertyPath;
import pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModel;
import pl.jalokim.crudwizard.genericapp.groovy.GroovyScriptInvoker;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;
import pl.jalokim.crudwizard.genericapp.validation.ValidatorModelContext;
import pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@Component
@SuppressWarnings("unchecked")
public class GenericValidator {

    public void validate(Object translatedObject, ClassMetaModel classMetaModel) {
        validate(translatedObject, classMetaModel, AdditionalValidatorsMetaModel.empty());
    }

    public void validate(Object translatedObject, ClassMetaModel classMetaModel, AdditionalValidatorsMetaModel additionalValidators) {
        if (translatedObject == null && classMetaModel == null) {
            return;
        }
        ValidationSessionContext validationContext = new ValidationSessionContext();

        validate(PropertyPathMetaModel.builder()
            .objectToValidate(translatedObject)
            .classMetaModel(classMetaModel)
            .validationContext(validationContext)
            .currentPath(PropertyPath.createRoot())
            .currentAdditionalValidators(additionalValidators)
            .build());

        validationContext.throwExceptionWhenErrorsOccurred();
    }

    private void validate(PropertyPathMetaModel propertyPathMetaModel) {
        ClassMetaModel classMetaModel = propertyPathMetaModel.getClassMetaModel();
        Object objectToValidate = propertyPathMetaModel.getObjectToValidate();

        Class<?> metaModelRealClass = classMetaModel.getRealClass() == null ? Map.class : classMetaModel.getRealClass();
        if (objectToValidate != null && !classMetaModel.isGenericMetamodelEnum() && !isTypeOf(objectToValidate, metaModelRealClass)) {
            String messageFormat = "Expected metamodel class: %s, but give was: %s, field path: %s";
            throw new IllegalStateException(String.format(messageFormat,
                metaModelRealClass.getCanonicalName(),
                objectToValidate.getClass().getCanonicalName(),
                propertyPathMetaModel.getCurrentPath().buildFullPath()
            ));
        }

        validate(propertyPathMetaModel, metaModelRealClass);
    }

    private void validate(PropertyPathMetaModel propertyPathMetaModel, Class<?> metaModelRealClass) {
        ClassMetaModel classMetaModel = propertyPathMetaModel.getClassMetaModel();
        FieldMetaModel fieldMetaModel = propertyPathMetaModel.getFieldMetaModel();

        Object objectToValidate = propertyPathMetaModel.getObjectToValidate();
        ValidationSessionContext validationContext = propertyPathMetaModel.getValidationContext();

        validateCurrentNode(propertyPathMetaModel, metaModelRealClass, classMetaModel, fieldMetaModel, objectToValidate, validationContext);
        validateComposedObject(propertyPathMetaModel, classMetaModel, objectToValidate);
    }

    private void validateComposedObject(PropertyPathMetaModel propertyPathMetaModel, ClassMetaModel classMetaModel, Object objectToValidate) {
        if (objectToValidate != null) {
            if (MetadataReflectionUtils.isArrayType(objectToValidate.getClass())) {
                Object[] array = (Object[]) objectToValidate;
                validateElements(propertyPathMetaModel, classMetaModel, elements(array));
            }

            if (MetadataReflectionUtils.isCollectionType(objectToValidate.getClass())) {
                Collection<?> collection = (Collection<?>) objectToValidate;
                validateElements(propertyPathMetaModel, classMetaModel, elements(collection));
            }

            if (objectToValidate instanceof Map) {
                Map<?, ?> objectToValidateAsMap = (Map<?, ?>) objectToValidate;
                List<FieldMetaModel> fields = classMetaModel.fetchAllFields();
                // TODO how to get fields when has generic fields ???
                for (FieldMetaModel field : fields) {
                    String fieldName = field.getFieldName();
                    Object mapFieldValue = objectToValidateAsMap.get(field.getFieldName());

                    PropertyPath propertyPathByFieldName = propertyPathMetaModel.getCurrentPath()
                        .nextWithName(fieldName);

                    var additionalValidators = propertyPathMetaModel.getCurrentAdditionalValidators()
                        .getByPropertyPath(propertyPathByFieldName);

                    validate(propertyPathMetaModel.toBuilder()
                        .objectToValidate(mapFieldValue)
                        .fieldMetaModel(field)
                        .currentPath(propertyPathByFieldName)
                        .currentAdditionalValidators(additionalValidators)
                        .build());
                }
            }
        }
    }

    private void validateCurrentNode(PropertyPathMetaModel propertyPathMetaModel, Class<?> metaModelRealClass, ClassMetaModel classMetaModel,
        FieldMetaModel fieldMetaModel, Object objectToValidate, ValidationSessionContext validationContext) {
        var allValidators = elements(classMetaModel.getValidators())
            .concat(Optional.ofNullable(fieldMetaModel)
                .map(FieldMetaModel::getValidators)
                .orElse(List.of())
            )
            .concat(Optional.ofNullable(propertyPathMetaModel.getCurrentAdditionalValidators())
                .map(AdditionalValidatorsMetaModel::getValidatorsMetaModel)
                .orElse(List.of()))
            .asList();

        for (ValidatorMetaModel validator : allValidators) {
            Object validatorInstance = validator.getValidatorInstance();
            if (validatorInstance == null) {
                GroovyScriptInvoker.invokeScript(validator.getValidatorScript());
            } else {
                DataValidator<Object> dataValidator = (DataValidator<Object>) validatorInstance;
                validationContext.newValidatorContext(validator, classMetaModel, propertyPathMetaModel.getCurrentPath());
                List<Class<?>> typesToValidate = dataValidator.getTypesToValidate();
                boolean canValidate = dataValidator.canValidate(metaModelRealClass);

                if (!canValidate) {
                    String typesToValidateAsText = elements(typesToValidate)
                        .map(Class::getCanonicalName)
                        .asConcatText(", ");

                    String message = String.format("Cannot validate object with type: %s "
                            + "with validator with class: %s which can validate types: %s",
                        metaModelRealClass.getCanonicalName(),
                        dataValidator.getClass().getCanonicalName(),
                        typesToValidateAsText
                    );
                    throw new IllegalArgumentException(message);
                }

                boolean validationResult = dataValidator.isValid(objectToValidate, validationContext);
                ValidatorModelContext currentValidatorContext = validationContext.getCurrentValidatorContext();
                currentValidatorContext.setValidationPassed(validationResult);
                validationContext.fetchValidatorContextResult();
            }
        }
    }

    private void validateElements(PropertyPathMetaModel propertyPathMetaModel, ClassMetaModel classMetaModel, Elements<?> elements) {
        elements.forEachWithIndex((index, value) -> {
            PropertyPath propertyPathByIndex = propertyPathMetaModel.getCurrentPath()
                .nextWithIndex(index);

            var additionalValidators = propertyPathMetaModel.getCurrentAdditionalValidators()
                .getByPropertyPath(propertyPathByIndex);

            validate(propertyPathMetaModel.toBuilder()
                .currentPath(propertyPathByIndex)
                .classMetaModel(classMetaModel.getGenericTypes().get(0))
                .objectToValidate(value)
                .currentAdditionalValidators(additionalValidators)
                .build()
            );
        });
    }
}

package pl.jalokim.crudwizard.genericapp.validation;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.constants.Constants.DOT;
import static pl.jalokim.utils.constants.Constants.EMPTY;

import java.util.Map;
import java.util.Optional;
import lombok.Data;
import pl.jalokim.crudwizard.core.exception.CustomValidationException;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.PropertyPath;
import pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModel;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;
import pl.jalokim.crudwizard.genericapp.validation.result.ValidationResult;
import pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator;
import pl.jalokim.utils.collection.CollectionUtils;
import pl.jalokim.utils.string.StringUtils;

@Data
public class ValidationSessionContext {

    private final ValidationResult validationResult = new ValidationResult();
    private ValidatorModelContext currentValidatorContext;

    public void addNextMessage(PropertyPath propertyPath, String translationKey) {
        addNextMessage(propertyPath, createMessagePlaceholder(translationKey));
    }

    public void addNextMessage(PropertyPath propertyPath, String translationKey, Map<String, Object> arguments) {
        addNextMessage(propertyPath, createMessagePlaceholder(translationKey, arguments));
    }

    public void addNextMessage(PropertyPath propertyPath, String translationKey, Object... arguments) {
        addNextMessage(propertyPath, createMessagePlaceholder(translationKey, arguments));
    }

    public void addNextMessage(PropertyPath propertyPath, MessagePlaceholder messagePlaceholder) {
        addNextMessage(propertyPath.buildFullPath(), messagePlaceholder);
    }

    public void addNextMessage(String propertyPathAsText, String translationKey) {
        addNextMessage(propertyPathAsText, createMessagePlaceholder(translationKey));
    }

    public void addNextMessage(String propertyPathAsText, MessagePlaceholder messagePlaceholder) {
        String translatedValue = messagePlaceholder.translateMessage();
        String fullProperty = concatPaths(currentValidatorContext.getCurrentPath().buildFullPath(), propertyPathAsText);
        currentValidatorContext.addEntry(fullProperty, messagePlaceholder.getMainPlaceholder(), translatedValue);
    }

    public void newValidatorContext(ValidatorMetaModel currentValidatorMetaModel, ClassMetaModel validatedClassMetaModel, PropertyPath currentPath) {
        currentValidatorContext = new ValidatorModelContext(currentValidatorMetaModel, validatedClassMetaModel, currentPath);
    }

    public void fetchValidatorContextResult() {
        boolean validationPassed = currentValidatorContext.isValidationPassed();
        if (!validationPassed) {
            if (currentValidatorContext.hasCustomMessages()) {
                currentValidatorContext.getCurrentEntries().forEach(validationResult::addEntry);
            } else {
                PropertyPath currentPath = currentValidatorContext.getCurrentPath();
                ValidatorMetaModel validatorMetaModel = currentValidatorContext.getCurrentValidatorMetaModel();
                DataValidator<?> validatorInstance = (DataValidator<?>) validatorMetaModel.getValidatorInstance();

                String translationKey = validatorMetaModel.getMessagePlaceholder();
                Map<String, Object> messagePlaceholders = validatorInstance.messagePlaceholderArgs(this);
                String translatedMessage = createMessagePlaceholder(translationKey, messagePlaceholders).translateMessage();
                validationResult.addEntry(currentPath.buildFullPath(), translationKey, translatedMessage);
            }
        }
    }

    public void throwExceptionWhenErrorsOccurred() {
        if (hasErrors()) {
            throw new CustomValidationException(getValidationResult().getMessage(),
                elements(getValidationResult().getEntries()).asSet());
        }
    }

    public boolean hasErrors() {
        return CollectionUtils.isNotEmpty(validationResult.getEntries());
    }

    public ValidatorMetaModel getValidatorModelContext() {
        return currentValidatorContext.getCurrentValidatorMetaModel();
    }

    @SuppressWarnings("unchecked")
    public <T> T getValidatorArgument(String name) {
        return (T) currentValidatorContext.getMessagePlaceholders().get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValidatorArgumentOrDefault(String name, T defaultValue) {
        return (T) Optional.ofNullable(currentValidatorContext.getMessagePlaceholders().get(name))
            .orElse(defaultValue);
    }

    static String concatPaths(String firstPathAsText, String secondPathAsText) {
        String concatenationValue = EMPTY;
        if (StringUtils.isBlank(firstPathAsText) || secondPathAsText.trim().startsWith("[")) {
            concatenationValue = EMPTY;
        } else if (StringUtils.isNotBlank(firstPathAsText) && StringUtils.isNotBlank(secondPathAsText)) {
            concatenationValue = DOT;
        }

        return StringUtils.concat(firstPathAsText, concatenationValue, secondPathAsText);
    }
}

package pl.jalokim.crudwizard.test.utils.validation

import static pl.jalokim.crudwizard.core.exception.handler.ErrorWithMessagePlaceholderMapper.convertToErrorsDto

import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import pl.jalokim.crudwizard.core.exception.CustomValidationException
import pl.jalokim.crudwizard.core.exception.ErrorWithPlaceholders
import pl.jalokim.crudwizard.core.rest.response.converter.ConstraintViolationToErrorConverter
import pl.jalokim.crudwizard.core.rest.response.error.ErrorDto

class ValidatorWithConverter {

    private final Validator validator

    ValidatorWithConverter(Validator validator) {
        this.validator = validator
    }

    static ValidatorWithConverter createValidatorWithConverter(Object... validatorsDependencies) {
        new ValidatorWithConverter(TestingConstraintValidatorFactory.createTestingValidator(validatorsDependencies))
    }

    List<ErrorDto> validateAndReturnErrors(Object objectTarget, Class<?>... groups) {
        Set<ConstraintViolation<Object>> violations = validator.validate(objectTarget, groups)
        return violations.collect {ConstraintViolationToErrorConverter.toErrorDto(it)}
    }

    static List<ErrorDto> errorsFromViolationException(ConstraintViolationException constraintViolationException) {
        Set<ConstraintViolation<?>> violations = constraintViolationException.constraintViolations
        return violations.collect {ConstraintViolationToErrorConverter.toErrorDto(it)}
    }

    static List<ErrorDto> errorsFromViolationException(CustomValidationException customValidationException) {
        return errorsFromViolationException(customValidationException.getErrors())
    }

    static List<ErrorDto> errorsFromViolationException(Collection<ErrorWithPlaceholders> errorsDtoWithPlaceholder) {
        return convertToErrorsDto(errorsDtoWithPlaceholder as Set) as List
    }

    Validator getValidator() {
        return validator
    }

    ConstraintViolationToErrorConverter getConverter() {
        return converter
    }
}

package pl.jalokim.crudwizard.test.utils.validation

import javax.validation.ConstraintViolation
import javax.validation.Validator
import pl.jalokim.crudwizard.core.rest.response.converter.ConstraintViolationToErrorConverter
import pl.jalokim.crudwizard.core.rest.response.error.ErrorDto

class ValidatorWithConverter {

    private final ConstraintViolationToErrorConverter converter = new ConstraintViolationToErrorConverter()
    private final Validator validator

    ValidatorWithConverter(Validator validator) {
        this.validator = validator
    }

    static ValidatorWithConverter createValidatorWithConverter(Object... validatorsDependencies) {
        new ValidatorWithConverter(TestingConstraintValidatorFactory.createTestingValidator(validatorsDependencies))
    }

    List<ErrorDto> validateAndReturnErrors(Object objectTarget, Class<?>... groups) {
        Set<ConstraintViolation<Object>> violations = validator.validate(objectTarget, groups)
        return violations.collect {converter.toErrorDto(it)}
    }

    Validator getValidator() {
        return validator
    }

    ConstraintViolationToErrorConverter getConverter() {
        return converter
    }
}

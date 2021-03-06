package pl.jalokim.crudwizard.test.utils.validation

import pl.jalokim.crudwizard.core.rest.response.error.ErrorDto

class ValidationErrorsAssertion {

    static boolean assertValidationResults(Collection<ErrorDto> foundErrors, Collection<ErrorDto> expectedErrors) {
        expectedErrors.forEach {
            assert foundErrors.contains(it)
        }
        foundErrors.forEach {
            assert expectedErrors.contains(it)
        }
        return foundErrors.size() == expectedErrors.size()
    }
}

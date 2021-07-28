package pl.jalokim.crudwizard.core.validation.javax.groups

import static pl.jalokim.crudwizard.core.exception.handler.SimpleDummyDto.emptySimpleDummyDto
import static pl.jalokim.crudwizard.core.exception.handler.SimpleDummyDto.validCreateSimpleDummyDto
import static pl.jalokim.crudwizard.core.exception.handler.SimpleDummyDto.validUpdateSimpleDummyDto
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults

import javax.validation.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.core.exception.handler.DummyService
import pl.jalokim.crudwizard.test.utils.DummyBaseIntegrationControllerSpec
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter

class ValidatedInServiceInterceptorIT extends DummyBaseIntegrationControllerSpec {

    @Autowired
    private DummyService dummyService

    def "should not throw validation exception when call create dummy with @Validated"() {
        when:
        dummyService.create(validCreateSimpleDummyDto())

        then:
        noExceptionThrown()
    }

    def "should throw validation exception when call dummy service method with @Validated"() {
        when:
        dummyService.create(emptySimpleDummyDto())

        then:
        ConstraintViolationException tx = thrown()
        def foundErrors = ValidatorWithConverter.errorsFromViolationException(tx)
        assertValidationResults(foundErrors, [
            errorEntry("someText", notNullMessage())
        ])
    }

    def "should throw validation exception when call dummy service method with @Validated(Update.class)"() {
        when:
        dummyService.update(12, emptySimpleDummyDto())

        then:
        ConstraintViolationException tx = thrown()
        def foundErrors = ValidatorWithConverter.errorsFromViolationException(tx)
        assertValidationResults(foundErrors, [
            errorEntry("id", notNullMessage()),
            errorEntry("someText", notNullMessage())
        ])
    }

    def "should not throw validation exception when call update dummy with @Validated"() {
        when:
        dummyService.update(23, validUpdateSimpleDummyDto())

        then:
        noExceptionThrown()
    }
}

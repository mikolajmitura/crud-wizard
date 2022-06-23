package pl.jalokim.crudwizard.core.validation.javax

import static pl.jalokim.crudwizard.core.exception.EntityNotFoundException.EXCEPTION_CONCRETE_MESSAGE_PROPERTY_KEY
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter

import org.springframework.jdbc.core.JdbcTemplate
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeExistIdDto
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter

class IdExistsValidatorTest extends UnitTestSpec {

    private JdbcTemplate jdbcTemplate = Mock()

    private ValidatorWithConverter validatorWithConverter = createValidatorWithConverter(jdbcTemplate)

    def "entity with given ids exists"() {
        given:
        def dto = new SomeExistIdDto(15, "uuidValue")
        def invokedStatements = []

        jdbcTemplate.queryForObject(_ as String, _ as Class<?>) >> {args ->
            invokedStatements.add(args[0])
            return 1
        }

        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(dto)

        then:
        foundErrors == []
        invokedStatements.size() == 2
        invokedStatements.contains("select count(SOME_ID) from with_long_id_table where SOME_ID = 15")
        invokedStatements.contains("select count(UUID) from some_table_name where UUID = 'uuidValue'")
    }

    def "entity with given ids does not exist"() {
        given:
        def dto = new SomeExistIdDto(17, "uuid_Value")
        def invokedStatements = []

        jdbcTemplate.queryForObject(_ as String, _ as Class<?>) >> {args ->
            invokedStatements.add(args[0])
            return 0
        }

        def expectedErrors = [
            errorEntry("someId",
                createMessagePlaceholder(EXCEPTION_CONCRETE_MESSAGE_PROPERTY_KEY,
                17, "with_long_id_table").translateMessage()),
            errorEntry("someUuid",
                createMessagePlaceholder(EXCEPTION_CONCRETE_MESSAGE_PROPERTY_KEY,
                    "uuid_Value", "some_table_name").translateMessage()),
        ]

        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(dto)

        then:
        assertValidationResults(foundErrors, expectedErrors)
    }
}

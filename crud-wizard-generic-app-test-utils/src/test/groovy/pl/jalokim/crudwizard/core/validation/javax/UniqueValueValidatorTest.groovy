package pl.jalokim.crudwizard.core.validation.javax

import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.messageForValidator
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter

import org.springframework.jdbc.core.JdbcTemplate
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.UniqueValueSampleDto
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter

class UniqueValueValidatorTest extends UnitTestSpec {

    private JdbcTemplate jdbcTemplate = Mock()

    private ValidatorWithConverter validatorWithConverter = createValidatorWithConverter(jdbcTemplate)

    def "should return expected validation messages for some paths, and invoke expected sql queries"() {
        given:
        def expectedErrors = [
            errorEntry("someFieldName", messageForValidator(UniqueValue)),
            errorEntry("fieldWithCustomColumn", messageForValidator(UniqueValue)),
            errorEntry("fieldWithCustomEntityFieldName", messageForValidator(UniqueValue))
        ]
        def invokedStatements = []

        jdbcTemplate.queryForObject(_ as String, _ as Class<?>) >> {args ->
            invokedStatements.add(args[0])
            return 1
        }

        def uniqueValueSampleDto = UniqueValueSampleDto.builder()
            .someFieldName("value1")
            .fieldWithCustomColumn("value2")
            .fieldWithCustomEntityFieldName("value3")
            .build()

        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(uniqueValueSampleDto)

        then:
        invokedStatements.size() == 3
        invokedStatements.contains(
            "select count(SOME_FIELD_NAME) from some_table_name where SOME_FIELD_NAME = '" +
                uniqueValueSampleDto.someFieldName + "'")
        invokedStatements.contains(
            "select count(custom_column_name) from some_table_name where custom_column_name = '" +
                uniqueValueSampleDto.fieldWithCustomColumn + "'")
        invokedStatements.contains(
            "select count(custom_column_name2) from SOME_UNIQUE_VALUE where custom_column_name2 = '" +
                uniqueValueSampleDto.fieldWithCustomEntityFieldName + "'")

        assertValidationResults(foundErrors, expectedErrors)
    }
}

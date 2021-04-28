package pl.jalokim.crudwizard.genericapp.validation.javax

import static pl.jalokim.crudwizard.genericapp.validation.javax.FieldShouldWhenOtherDto.SomeEnum.ENTRY_1
import static pl.jalokim.crudwizard.genericapp.validation.javax.FieldShouldWhenOtherDto.SomeEnum.ENTRY_2
import static pl.jalokim.crudwizard.genericapp.validation.javax.FieldShouldWhenOtherDto.SomeEnum.ENTRY_3
import static pl.jalokim.crudwizard.test.utils.random.DataFakerHelper.randomInteger
import static pl.jalokim.crudwizard.test.utils.random.DataFakerHelper.randomText
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.EXPECTED_MESSAGES
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter

import pl.jalokim.crudwizard.core.rest.response.error.ErrorDto
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter
import spock.lang.Specification
import spock.lang.Unroll

class FieldShouldWhenOtherValidatorTest extends Specification {

    private ValidatorWithConverter validatorWithConverter

    def setup() {
        validatorWithConverter = createValidatorWithConverter()
    }

    @Unroll
    def "given validation should pass: #expected when object has values: #someObject"() {
        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(someObject)

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        someObject                         || expectedErrors
        someObject0() || []
        someObject1() || expectedResult1()
        someObject2() || []
        someObject3() || []
        someObject4() || []
        someObject5() || expectedResult5()
        someObject6() || expectedResult6()
        someObject7() || []
        someObject8() || []
        someObject9() || []
        someObject10() || []
        someObject11() || []
    }

    private ArrayList<ErrorDto> expectedResult1() {
        [
            ErrorDto.errorEntry("realId", message("given.validation.should.pass[1]realId[0]")),
            ErrorDto.errorEntry("personalNumber", message("given.validation.should.pass[1]personalNumber[0]")),
            ErrorDto.errorEntry("someList", message("given.validation.should.pass[1]someList[0]")),
            ErrorDto.errorEntry("someList", message("given.validation.should.pass[1]someList[1]")),
            ErrorDto.errorEntry("notEqualToAll", message("given.validation.should.pass[1]notEqualToAll[0]")),
            ErrorDto.errorEntry("emptyOrNull", message("given.validation.should.pass[1]emptyOrNull[0]")),
        ]
    }

    private ArrayList<ErrorDto> expectedResult5() {
        [
            ErrorDto.errorEntry("someEnum", message("given.validation.should.pass[5]someEnum[0]")),
            ErrorDto.errorEntry("someList", message("given.validation.should.pass[5]someList[0]")),
            ErrorDto.errorEntry("someSet", message("given.validation.should.pass[5]someSet[0]")),
        ]
    }

    private ArrayList<ErrorDto> expectedResult6() {
        [
            ErrorDto.errorEntry("someEnum", message("given.validation.should.pass[5]someEnum[0]")),
            ErrorDto.errorEntry("someSet", message("given.validation.should.pass[5]someSet[0]")),
            ErrorDto.errorEntry("shouldBeNotNull", message("given.validation.should.pass[6]shouldBeNotNull[0]")),
        ]
    }

    private FieldShouldWhenOtherDto someObject0() {
        FieldShouldWhenOtherDto.builder()
            .someEnum(ENTRY_1)
            .whenSomeEnum(100)
            .someList(["12", "13", "11"])
            .someSet(["12", "15"] as Set)
            .shouldBeNotNull("text")
            .isEmptyList(["1"])
            .build()
    }

    private FieldShouldWhenOtherDto someObject1() {
        FieldShouldWhenOtherDto.builder()
            .documentNumber(randomText())
            .personalNumber(randomText())
            .version(randomInteger())
            .someList(["11"])
            .someSet(["12", "15"] as Set)
            .someEnums([ENTRY_2, ENTRY_3])
            .notEqualToAll("text1")
            .notEqualToAllOther("text33")
            .emptyOrNull([1, 2])
            .build()
    }

    private FieldShouldWhenOtherDto someObject2() {
        FieldShouldWhenOtherDto.builder()
            .personalNumber(randomText())
            .someList(["100", "11"])
            .someSet(["100", "999"] as Set)
            .build()
    }

    private FieldShouldWhenOtherDto someObject3() {
        FieldShouldWhenOtherDto.builder()
            .documentNumber(randomText())
            .someSet(["12", "15", "11"] as Set)
            .build()
    }

    private FieldShouldWhenOtherDto someObject4() {
        FieldShouldWhenOtherDto.builder()
            .someEnum(ENTRY_2)
            .whenSomeEnum(100)
            .someList(["text1", "text2", "text3"])
            .someEnums([ENTRY_2, ENTRY_3])
            .build()
    }

    private FieldShouldWhenOtherDto someObject5() {
        FieldShouldWhenOtherDto.builder()
            .whenSomeEnum(100)
            .someList(["10", "15"])
            .someSet(["999", "1000"] as Set)
            .someEnums([])
            .build()
    }

    private FieldShouldWhenOtherDto someObject6() {
        FieldShouldWhenOtherDto.builder()
            .someEnum(ENTRY_3)
            .whenSomeEnum(100)
            .someEnums([])
            .isEmptyList([])
            .build()
    }

    private FieldShouldWhenOtherDto someObject7() {
        FieldShouldWhenOtherDto.builder()
            .someEnum(ENTRY_3)
            .whenSomeEnum(101)
            .build()
    }

    private FieldShouldWhenOtherDto someObject8() {
        FieldShouldWhenOtherDto.builder()
            .notEqualToAll("text10")
            .notEqualToAllOther("text33")
            .someSet(Set.of())
            .someEnums([])
            .build()
    }

    private FieldShouldWhenOtherDto someObject9() {
        FieldShouldWhenOtherDto.builder()
            .notEqualToAll("text1")
            .notEqualToAllOther("text11")
            .build()
    }

    private FieldShouldWhenOtherDto someObject10() {
        FieldShouldWhenOtherDto.builder()
            .emptyOrNull(List.of())
            .emptyOrNullOther(List.of())
            .build()
    }

    private FieldShouldWhenOtherDto someObject11() {
        FieldShouldWhenOtherDto.builder()
            .emptyOrNull(List.of())
            .build()
    }

    private static String message(String suffixCode) {
        EXPECTED_MESSAGES.getMessage(FieldShouldWhenOtherValidatorTest, suffixCode)
    }
}

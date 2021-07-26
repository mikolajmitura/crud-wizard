package pl.jalokim.crudwizard.genericapp.validation.javax

import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.CONTAINS_ALL
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.CONTAINS_ANY
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EMPTY
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EMPTY_OR_NULL
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.MAX
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.MIN
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_BLANK
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_EMPTY
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_EQUAL_TO_ALL
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL
import static pl.jalokim.crudwizard.genericapp.validation.javax.FieldShouldWhenOtherDto.SomeEnum.ENTRY_1
import static pl.jalokim.crudwizard.genericapp.validation.javax.FieldShouldWhenOtherDto.SomeEnum.ENTRY_2
import static pl.jalokim.crudwizard.genericapp.validation.javax.FieldShouldWhenOtherDto.SomeEnum.ENTRY_3
import static pl.jalokim.utils.test.DataFakerHelper.randomInteger
import static pl.jalokim.utils.test.DataFakerHelper.randomLong
import static pl.jalokim.utils.test.DataFakerHelper.randomText
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.EXPECTED_MESSAGES
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter

import java.time.LocalDate
import java.util.concurrent.atomic.AtomicReference
import pl.jalokim.crudwizard.core.rest.response.error.ErrorDto
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOtherValidator
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter
import spock.lang.Specification
import spock.lang.Unroll

@SuppressWarnings("UnusedPrivateField")
class FieldShouldWhenOtherValidatorTest extends Specification {

    private ValidatorWithConverter validatorWithConverter
    private static AtomicReference<FieldShouldWhenOtherStub> fieldShouldWhenOtherStub = new AtomicReference<>()
    private FieldShouldWhenOther mockFieldShouldWhenOther = Mock()

    def setup() {
        fieldShouldWhenOtherStub.set(FieldShouldWhenOtherStub.builder().build())
        validatorWithConverter = createValidatorWithConverter()
        mockFieldShouldWhenOther.field() >> {
            args -> fieldShouldWhenOtherStub.get().getField()
        }
        mockFieldShouldWhenOther.should() >> {
            args -> fieldShouldWhenOtherStub.get().getShould()
        }
        mockFieldShouldWhenOther.fieldValues() >> {
            args -> fieldShouldWhenOtherStub.get().getFieldValues()
        }
        mockFieldShouldWhenOther.whenField() >> {
            args -> fieldShouldWhenOtherStub.get().getWhenField()
        }
        mockFieldShouldWhenOther.is() >> {
            args -> fieldShouldWhenOtherStub.get().getIs()
        }
        mockFieldShouldWhenOther.otherFieldValues() >> {
            args -> fieldShouldWhenOtherStub.get().getOtherFieldValues()
        }
    }

    @Unroll
    def "given validation should pass: #expected when object has values: #someObject"() {
        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(someObject)

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        someObject     || expectedErrors
        someObject0()  || []
        someObject1()  || expectedResult1()
        someObject2()  || []
        someObject3()  || []
        someObject4()  || []
        someObject5()  || expectedResult5()
        someObject6()  || expectedResult6()
        someObject7()  || []
        someObject8()  || []
        someObject9()  || []
        someObject10() || []
        someObject11() || []
        someObject12() || []
        someObject13() || expectedResult13()
        someObject14() || []
        someObject15() || expectedResult15()
        someObject16() || []
        someObject17() || expectedResult17()
        someObject18() || []
    }

    @Unroll
    def "should inform about expected string or collection for field type"() {
        when:
        validatorWithConverter.validateAndReturnErrors(inputObject)

        then:
        Exception ex = thrown()
        def wholeClass = inputObject.getClass().canonicalName
        ex.getCause().message ==
            "field '$fieldName' in class $wholeClass should be one of class: [java.lang.String, java.util.Collection, java.util.Map] when used one of EMPTY, EMPTY_OR_NULL, NOT_EMPTY"

        where:
        inputObject                                             || fieldName
        new ShouldBeStringOrCollectionFields1(someInteger: 12)  || "someInteger"
        new ShouldBeStringOrCollectionFields1(otherField: 12)   || "otherField"
        new ShouldBeStringOrCollectionFields1(someInteger2: 12) || "someInteger2"
    }

    @Unroll
    def "should inform about expected string for field type"() {
        when:
        validatorWithConverter.validateAndReturnErrors(inputObject)

        then:
        Exception ex = thrown()
        def wholeClass = inputObject.getClass().canonicalName
        ex.getCause().message ==
            "field '$fieldName' in class $wholeClass should be one of class: [java.lang.String] when used one of NOT_BLANK"

        where:
        inputObject                                               || fieldName
        new ShouldBeStringOrCollectionFields1(shouldBeString: 12) || "shouldBeString"
    }

    @Unroll
    def "should inform about expected number, string, map or collection for field type"() {
        when:
        validatorWithConverter.validateAndReturnErrors(inputObject)

        then:
        Exception ex = thrown()
        def wholeClass = inputObject.getClass().canonicalName
        ex.getCause().message ==
            "field 'notSupported' in class $wholeClass should be one of class:" +
            " [java.util.Collection, java.util.Map, java.lang.String, java.lang.Number] when used one of MIN, MAX"

        where:
        inputObject                                                                   || _
        new MinMaxTestNotSupportedClasses(someString1: randomText(), notSupported: LocalDate.now()) || _
        new MinMaxTestNotSupportedClasses(someString2: randomText(), notSupported: LocalDate.now()) || _
    }

    @Unroll
    def "for some field status enums other fields values should be empty"() {
        given:
        fieldShouldWhenOtherStub.set(FieldShouldWhenOtherStub.builder()
            .field("firstField")
            .should(expectedFieldState)
            .fieldValues(["12", "11"] as String[])
            .build()
        )

        def validator = new FieldShouldWhenOtherValidator()

        when:
        validator.initialize(mockFieldShouldWhenOther)

        then:
        Exception ex = thrown()
        ex.message == "invalid @FieldShouldWhenOther for field=firstField for: should=$expectedFieldState, field: fieldValues should be empty"

        where:
        expectedFieldState | _
        NULL               | _
        NOT_NULL           | _
        EMPTY              | _
        EMPTY_OR_NULL      | _
        NOT_BLANK          | _
        NOT_EMPTY          | _
    }

    @Unroll
    def "for some field status enums other fields values should be not empty"() {
        given:
        fieldShouldWhenOtherStub.set(FieldShouldWhenOtherStub.builder()
            .field("test")
            .should(NULL)
            .whenField("firstField")
            .is(expectedFieldState)
            .build()
        )

        def validator = new FieldShouldWhenOtherValidator()

        when:
        validator.initialize(mockFieldShouldWhenOther)

        then:
        Exception ex = thrown()
        ex.message == "invalid @FieldShouldWhenOther for whenField=firstField for: is=$expectedFieldState, field: otherFieldValues should not be empty"

        where:
        expectedFieldState | _
        EQUAL_TO_ANY       | _
        CONTAINS_ALL       | _
        CONTAINS_ANY       | _
        NOT_EQUAL_TO_ALL   | _
        MAX                | _
        MIN                | _
    }

    @Unroll
    def "for some field status enums other fields values should be list with one number"() {
        given:
        fieldShouldWhenOtherStub.set(FieldShouldWhenOtherStub.builder()
            .field("firstField")
            .should(expectedFieldState)
            .fieldValues(fieldValues as String[])
            .build()
        )

        def validator = new FieldShouldWhenOtherValidator()

        when:
        validator.initialize(mockFieldShouldWhenOther)

        then:
        Exception ex = thrown()
        ex.message == "invalid @FieldShouldWhenOther for field=firstField for: " +
            "should=$expectedFieldState, field: fieldValues should have only one element with number value"

        where:
        expectedFieldState | fieldValues
        MAX                | ["not number"]
        MIN                | ["1", "12"]
    }

    def "should inform about expected integer type when when field is string type"() {
        when:
        validatorWithConverter.validateAndReturnErrors(new MinMaxTestExpectingIntegerValueInConfig(forTestInvalidStringSize: randomText(), someString1: randomText()))

        then:
        Exception ex = thrown()
        ex.getCause().message == "invalid @FieldShouldWhenOther for field=someString1 for: fieldValues=[12.11], " +
            "value of field: fieldValues should be not floating point number"
    }

    private ArrayList<ErrorDto> expectedResult1() {
        [
            errorEntry("realId", message("given.validation.should.pass[1]realId[0]")),
            errorEntry("personalNumber", message("given.validation.should.pass[1]personalNumber[0]")),
            errorEntry("someList", message("given.validation.should.pass[1]someList[0]")),
            errorEntry("someList", message("given.validation.should.pass[1]someList[1]")),
            errorEntry("notEqualToAll", message("given.validation.should.pass[1]notEqualToAll[0]")),
            errorEntry("emptyOrNull", message("given.validation.should.pass[1]emptyOrNull[0]")),
            errorEntry("blankTestFiled", message("given.validation.should.pass[1]blankTestFiled[0]"))
        ]
    }

    private ArrayList<ErrorDto> expectedResult5() {
        [
            errorEntry("someEnum", message("given.validation.should.pass[5]someEnum[0]")),
            errorEntry("someList", message("given.validation.should.pass[5]someList[0]")),
            errorEntry("someSet", message("given.validation.should.pass[5]someSet[0]")),
            errorEntry("someStringNotEmpty", message("given.validation.should.pass[5]someStringNotEmpty[0]")),
            errorEntry("someStringNotEmpty", message("given.validation.should.pass[5]someStringNotEmpty[1]")),
        ]
    }

    private ArrayList<ErrorDto> expectedResult6() {
        [
            errorEntry("someEnum", message("given.validation.should.pass[5]someEnum[0]")),
            errorEntry("someSet", message("given.validation.should.pass[5]someSet[0]")),
            errorEntry("shouldBeNotNull", message("given.validation.should.pass[6]shouldBeNotNull[0]")),
        ]
    }

    private ArrayList<ErrorDto> expectedResult13() {
        [
            errorEntry("someTextField", message("given.validation.should.pass[13]shouldBeMax[0]"))
        ]
    }

    private ArrayList<ErrorDto> expectedResult15() {
        [
            errorEntry("someMap", message("given.validation.should.pass[15]shouldBeMax[0]"))
        ]
    }

    private ArrayList<ErrorDto> expectedResult17() {
        [
            errorEntry("someMap", message("given.validation.should.pass[17]shouldBeMin[0]"))
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
            .blankTestFiled(" ")
            .whenBlankTestFiled("  test  ")
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
            .someStringNotEmpty("test")
            .someMapNotEmpty(Map.of("test1", "test2"))
            .someCollectionNotEmpty(List.of("1", "2"))
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
            .blankTestFiled("test")
            .whenBlankTestFiled("2")
            .someStringNotEmpty("")
            .someMapNotEmpty(Map.of("test1", "test2"))
            .someCollectionNotEmpty(List.of("1", "2"))
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

    private FieldShouldWhenOtherDto someObject12() {
        FieldShouldWhenOtherDto.builder()
            .someTextField(randomText(2))
            .someListString(List.of(randomText(), randomText(), randomText(), randomText()))
            .build()
    }

    private FieldShouldWhenOtherDto someObject13() {
        FieldShouldWhenOtherDto.builder()
            .someTextField(randomText(4))
            .someListString(List.of(randomText(), randomText(), randomText(), randomText()))
            .build()
    }

    private FieldShouldWhenOtherDto someObject14() {
        FieldShouldWhenOtherDto.builder()
            .someLong(10)
            .someMap(Map.of(randomText(), randomLong()))
            .build()
    }

    private FieldShouldWhenOtherDto someObject15() {
        FieldShouldWhenOtherDto.builder()
            .someLong(10)
            .someMap(Map.of(randomText(), randomLong(),
                randomText(), randomLong(),
                randomText(), randomLong()))
            .build()
    }

    private FieldShouldWhenOtherDto someObject16() {
        FieldShouldWhenOtherDto.builder()
            .someDouble(10.30)
            .someMap(Map.of(randomText(), randomLong(),
                    randomText(), randomLong())
            )
            .build()
    }

    private FieldShouldWhenOtherDto someObject17() {
        FieldShouldWhenOtherDto.builder()
            .someDouble(10.30)
            .someMap(Map.of(randomText(), randomLong()))
            .build()
    }

    private FieldShouldWhenOtherDto someObject18() {
        FieldShouldWhenOtherDto.builder()
            .someDouble(10.51)
            .someMap(Map.of())
            .build()
    }

    private static String message(String suffixCode) {
        EXPECTED_MESSAGES.getMessage(FieldShouldWhenOtherValidatorTest, suffixCode)
    }

    @FieldShouldWhenOther(field = "someInteger", should = EMPTY, whenField = "otherField", is = NOT_EMPTY)
    @FieldShouldWhenOther(field = "someInteger2", should = EMPTY_OR_NULL, whenField = "otherField", is = NOT_EMPTY)
    @FieldShouldWhenOther(field = "shouldBeString", should = NOT_BLANK, whenField = "otherField", is = NOT_EMPTY)
    private static class ShouldBeStringOrCollectionFields1 {

        private Integer someInteger
        private Integer someInteger2
        private Integer otherField
        private Integer shouldBeString
    }

    @FieldShouldWhenOther(field = "object1", should = EMPTY, fieldValues = "12", whenField = "object2", is = NOT_EMPTY, otherFieldValues = "12")
    private static class ShouldDoesNotHaveOtherFieldsValue {

        private String object1
        private String object2
    }

    @FieldShouldWhenOther(field = "notSupported", should = MAX, fieldValues = "12",  whenField = "someString1", is = NOT_NULL)
    @FieldShouldWhenOther(field = "notSupported", should = MIN, fieldValues = "12",  whenField = "someString2", is = NOT_NULL)
    private static class MinMaxTestNotSupportedClasses {

        private LocalDate notSupported
        private String someString1
        private String someString2
    }

    @FieldShouldWhenOther(field = "someString1", should = MIN, fieldValues = "12.11",  whenField = "forTestInvalidStringSize", is = NOT_NULL)
    private static class MinMaxTestExpectingIntegerValueInConfig {

        private String someString1
        private String forTestInvalidStringSize
    }
}

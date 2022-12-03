package pl.jalokim.crudwizard.genericapp.validation.generic

import static pl.jalokim.crudwizard.core.datastorage.RawEntityObjectSamples.createRequestBodyTranslated
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelWithParents
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createSomePersonClassMetaModel
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getAppMessageSource
import static pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage.buildMessageForValidator
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsMetaModel.empty
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.invalidSizeMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notBlankMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.NOT_BLANK_MESSAGE_PROPERTY
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.NOT_NULL_MESSAGE_PROPERTY
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.SIZE_MESSAGE_PROPERTY
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.errorsFromViolationException
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import java.time.Period
import pl.jalokim.crudwizard.core.datastorage.ExampleEnum
import pl.jalokim.crudwizard.core.exception.CustomValidationException
import pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModelSamples
import pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther
import pl.jalokim.crudwizard.genericapp.metamodel.url.PropertyPath
import pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsMetaModel
import pl.jalokim.crudwizard.genericapp.validation.validator.FieldShouldWhenOtherValidator
import pl.jalokim.crudwizard.genericapp.validation.validator.NotNullValidator
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class GenericValidatorTest extends UnitTestSpec {

    static final NOT_NULL_VALIDATOR = new NotNullValidator()
    static final NOT_NULL_VALIDATOR_META_MODEL = ValidatorMetaModelSamples.createValidatorMetaModel(NOT_NULL_VALIDATOR)

    def genericValidator = new GenericValidator()

    @Unroll
    def "validation passed"() {
        given:

        when:
        genericValidator.validate(valueToVerify, classMetaModel, additionalValidators)

        then:
        noExceptionThrown()

        where:
        valueToVerify                 | classMetaModel                                                               | additionalValidators
        "text"                        | createClassMetaModelFromClass(String.class, [NOT_NULL_VALIDATOR_META_MODEL]) | empty()
        createRequestBodyTranslated() | createSomePersonClassMetaModel() | empty()
    }

    @Unroll
    def "validation not passed"() {
        when:
        genericValidator.validate(valueToVerify, classMetaModel, additionalValidators)

        then:
        CustomValidationException ex = thrown()
        def foundErrors = errorsFromViolationException(ex)
        assertValidationResults(foundErrors, expectedErrors)

        where:
        valueToVerify                   | classMetaModel                    | additionalValidators | expectedErrors
        null                            | createClassMetaModelFromClass(String.class,
            [NOT_NULL_VALIDATOR_META_MODEL])                                | empty()              | [
            errorEntry("", notNullMessage(), NOT_NULL_MESSAGE_PROPERTY)
        ]

        invalidPayload1()               | createSomePersonClassMetaModel() |
            buildAdditionalValidatorsMetaModel()                                                   | [
            errorEntry("addresses[0].street", getAppMessageSource().getMessage("GenericValidatorTest.expected.message"),
                buildMessageForValidator(FieldShouldWhenOther)),
            errorEntry("addresses[0].street", notNullMessage(), NOT_NULL_MESSAGE_PROPERTY),
            errorEntry("addresses[1].houseNr", notNullMessage(), NOT_NULL_MESSAGE_PROPERTY),
            errorEntry("surname", notNullMessage(), NOT_NULL_MESSAGE_PROPERTY),
            errorEntry("name", invalidSizeMessage(3, 20), SIZE_MESSAGE_PROPERTY),
        ]

        invalidPayload1()               | createSomePersonClassMetaModel() |
            empty()                                                                                | [
            errorEntry("addresses[1].houseNr", notNullMessage(), NOT_NULL_MESSAGE_PROPERTY),
            errorEntry("surname", notNullMessage(), NOT_NULL_MESSAGE_PROPERTY),
            errorEntry("name", invalidSizeMessage(3, 20), SIZE_MESSAGE_PROPERTY),
        ]

        null                            | createClassMetaModelWithParents() | empty()              | [
            errorEntry("", notNullMessage(), NOT_NULL_MESSAGE_PROPERTY)
        ]

        invalidPayloadWithParentMeta1() | createClassMetaModelWithParents() | empty()              | [
            errorEntry("name", invalidSizeMessage(3, 20), SIZE_MESSAGE_PROPERTY),
            errorEntry("someUnique", notBlankMessage(), NOT_BLANK_MESSAGE_PROPERTY),
            errorEntry("", getAppMessageSource().getMessage("ClassMetaModelWithParentsValidator.m1"), "ClassMetaModelWithParentsValidator.m1"),
        ]

        invalidPayloadWithParentMeta2() | createClassMetaModelWithParents() | empty()              | [
            errorEntry("name", invalidSizeMessage(3, 20), SIZE_MESSAGE_PROPERTY),
            errorEntry("someOtherObject.someField1", getAppMessageSource().getMessage("SomeOtherObjectValidator.m2"), "SomeOtherObjectValidator.m2"),
            errorEntry("someOtherObject.someField2", getAppMessageSource().getMessage("SomeOtherObjectValidator.m3"), "SomeOtherObjectValidator.m3"),
            errorEntry("someOtherObject", getAppMessageSource().getMessage("SomeOtherObjectValidator.m1"), "SomeOtherObjectValidator.m1"),
        ]
    }

    def "expected was Long to validate but was LocalDateTime"() {
        given:

        when:
        genericValidator.validate(createRequestBodyTranslated(), createClassMetaModelWithParents())

        then:
        IllegalStateException ex = thrown()
        ex.message == "Expected metamodel class: java.lang.Long, but give was: java.time.LocalDateTime, field path: applicationDateTime"
    }

    private Map<String, Object> invalidPayload1() {
        def payload = createRequestBodyTranslated()
        payload.name = "J"
        payload.surname = null
        payload.addresses = [
            [
                houseNr : "12/1",
                someEnum: ExampleEnum.ENUM1
            ],
            [
                street  : "second Street",
                someEnum: ExampleEnum.ENUM2
            ]
        ]
        payload
    }

    private static Map<String, Object> invalidPayloadWithParentMeta1() {
        def payload = createRequestBodyTranslated()
        payload.name = "J"
        payload.applicationDateTime = 12341L
        payload.age = Period.ofYears(22)
        payload
    }

    private static Map<String, Object> invalidPayloadWithParentMeta2() {
        def payload = invalidPayloadWithParentMeta1()
        payload.someUnique = randomText()
        payload.someOtherObject = [
            someField1: randomText(1),
            someField2: ""
        ]
        payload
    }

    static AdditionalValidatorsMetaModel buildAdditionalValidatorsMetaModel() {
        def root = PropertyPath.createRoot()
        def addressesPath = root.nextWithName("addresses")
        def addressesAllElementsPath = addressesPath.nextWithAllIndexes()
        def streetPath = addressesPath.nextWithName("street")

        def streetValMetamodel = AdditionalValidatorsMetaModel.builder()
            .propertyPath(streetPath)
            .validatorsByPropertyPath([:])
            .validatorsMetaModel([NOT_NULL_VALIDATOR_META_MODEL])
            .build()

        FieldShouldWhenOtherValidator fieldShouldWhenOtherValidator = new FieldShouldWhenOtherValidator()
        def fieldShouldWhenOtherValidatorMetaModel = ValidatorMetaModelSamples.createValidatorMetaModel(fieldShouldWhenOtherValidator,
            [
                field    : "street",
                should   : ExpectedFieldState.NOT_NULL,
                whenField: "someEnum",
                is       : ExpectedFieldState.NOT_NULL
            ]
        )

        def addressesAllElementsValMetamodel = AdditionalValidatorsMetaModel.builder()
            .propertyPath(addressesAllElementsPath)
            .validatorsByPropertyPath([street: streetValMetamodel])
            .validatorsMetaModel([fieldShouldWhenOtherValidatorMetaModel])
            .build()

        def addressesValMetamodel = AdditionalValidatorsMetaModel.builder()
            .propertyPath(addressesPath)
            .validatorsByPropertyPath(Map.of(PropertyPath.ALL_INDEXES, addressesAllElementsValMetamodel))
            .validatorsMetaModel([])
            .build()

        AdditionalValidatorsMetaModel.builder()
            .propertyPath(root)
            .validatorsMetaModel([])
            .validatorsByPropertyPath([addresses: addressesValMetamodel])
            .build()
    }
}

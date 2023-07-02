package pl.jalokim.crudwizard.genericapp.metamodel.classmodel

import static pl.jalokim.crudwizard.core.exception.EntityNotFoundException.EXCEPTION_CONCRETE_MESSAGE_PROPERTY_KEY
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.translatePlaceholder
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_BLANK
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.BeforeClassValidationUpdater.attachFieldTranslationsWhenNotExist
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoWithId
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createEmptyClassMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createEnumMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithClassName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidEnumMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDtoSamples.createEmptyValidatorMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDtoSamples.createValidValidatorMetaModelDto
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.fieldShouldWhenOtherMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.messageForValidator
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.whenFieldIsInStateThenOthersShould
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import javax.validation.constraints.NotBlank
import org.mapstruct.factory.Mappers
import org.springframework.jdbc.core.JdbcTemplate
import pl.jalokim.crudwizard.core.validation.javax.groups.FirstValidationPhase
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.NotContainsWhiteSpaces
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.samples.SomeRealClass
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationDto
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationMapper
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter
import pl.jalokim.utils.reflection.InvokableReflectionUtils
import spock.lang.Unroll

class ClassMetaModelDtoValidationTest extends UnitTestSpec {

    private ClassMetaModelRepository classMetaModelRepository = Mock()
    private JdbcTemplate jdbcTemplate = Mock()
    private MetaModelContextService metaModelContextService = Mock()
    private MetaModelContext metaModelContext = Mock()
    private AdditionalPropertyMapper additionalPropertyMapper = Mappers.getMapper(AdditionalPropertyMapper)
    private TranslationMapper translationMapper = Mappers.getMapper(TranslationMapper)
    private CommonClassAndFieldMapper commonClassAndFieldMapper = new CommonClassAndFieldMapperImpl(additionalPropertyMapper, translationMapper)
    private FieldMetaModelMapper fieldMetaModelMapper = createFieldMetaModelMapper()
    private FieldMetaModelService fieldMetaModelService = new FieldMetaModelService(fieldMetaModelMapper)

    private ValidatorWithConverter validatorWithConverter = createValidatorWithConverter(jdbcTemplate, metaModelContextService,
        fieldMetaModelService, classMetaModelRepository)

    private FieldMetaModelMapper createFieldMetaModelMapper() {
        def fieldMetaModelMapper = new FieldMetaModelMapperImpl(additionalPropertyMapper, translationMapper, commonClassAndFieldMapper)
        InvokableReflectionUtils.setValueForField(fieldMetaModelMapper, "commonClassAndFieldMapperInjected", commonClassAndFieldMapper)
        fieldMetaModelMapper
    }

    def setup() {
        jdbcTemplate.queryForObject(_ as String, _ as Class<?>) >> {
            args ->
                if (args[0].contains('count(ID)') && args[0].contains('ID = 1000')) {
                    return 1
                }
                if (args[0].contains('count(ID)') && args[0].contains('ID = 123')) {
                    return 1
                }
                return 0
        }

        metaModelContextService.getMetaModelContext() >> metaModelContext
        metaModelContext.getAllCountryCodes() >> ["en_US"]
        classMetaModelRepository.findByClassName(_ as String) >> []
    }

    @Unroll
    def "should return expected messages for default context of ClassMetaModelDto"() {
        when:
        attachFieldTranslationsWhenNotExist(classMetaModelDto)
        def foundErrors = validatorWithConverter.validateAndReturnErrors(classMetaModelDto, FirstValidationPhase)

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        classMetaModelDto                                     | expectedErrors
        createValidClassMetaModelDtoWithName()                | []

        createValidEnumMetaModel()                            | []

        createValidEnumMetaModel().toBuilder()
            .enumMetaModel(null)
            .build()                                          | [
            errorEntry("enumMetaModel",
                whenFieldIsInStateThenOthersShould("classMetaModelDtoType", EQUAL_TO_ANY, ["DEFINITION"],
                    fieldShouldWhenOtherMessage(NOT_NULL, [], "isGenericEnumType", EQUAL_TO_ANY, ["true"]))),
        ]

        createEnumMetaModel("VaLID_ENUM", "Invalid enum", "") | [
            errorEntry("enumMetaModel.enums[1].name", messageForValidator(NotContainsWhiteSpaces)),
            errorEntry("enumMetaModel.enums[2].name", messageForValidator(NotBlank)),
        ]

        createClassMetaModelDtoWithId(100)                    | [
            errorEntry("id", createMessagePlaceholder(EXCEPTION_CONCRETE_MESSAGE_PROPERTY_KEY,
                100, "class_meta_models").translateMessage())
        ]

        createClassMetaModelDtoWithId(1000)                   | []

        createValidClassMetaModelDtoWithClassName()           | []

        createEmptyClassMetaModelDto().toBuilder()
            .translationName(createEmptyClassMetaModelDto().getTranslationName().toBuilder()
                .translationKey(null)
                .build())
            .build()                                          | [
            errorEntry("name", whenFieldIsInStateThenOthersShould("id", NULL, fieldShouldWhenOtherMessage(NOT_NULL, [], "className", NULL, []))),
            errorEntry("translationName.translationKey",
                whenFieldIsInStateThenOthersShould("translationId", NULL, fieldShouldWhenOtherMessage(NOT_BLANK, [], "translationId", NULL, [])))
        ]

        createValidClassMetaModelDtoWithClassName()
            .toBuilder()
            .name(randomText())
            .build()                                          | [
            errorEntry("name", whenFieldIsInStateThenOthersShould("id", NULL, fieldShouldWhenOtherMessage(NULL, [], "className", NOT_NULL, []))),
            errorEntry("className", whenFieldIsInStateThenOthersShould("id", NULL, fieldShouldWhenOtherMessage(NULL, [], "name", NOT_NULL, [])))
        ]

        createValidClassMetaModelDtoWithClassName()
            .toBuilder()
            .fields([FieldMetaModelDto.builder().build()])
            .build()                                          | [
            errorEntry("fields[0].translationFieldName", notNullMessage()),
            errorEntry("fields[0].fieldName", notNullMessage()),
            errorEntry("fields[0].fieldType", notNullMessage())
        ]

        createValidClassMetaModelDtoWithName().toBuilder()
            .genericTypes([createEmptyClassMetaModelDto().toBuilder()
                               .isGenericEnumType(null)
                               .translationName(TranslationDto.builder()
                                   .translationKey("field.translation")
                                   .translationByCountryCode([
                                       FR     : "translation FR",
                                       eng_ENG: null
                                   ])
                                   .build())
                               .build()])
            .validators([createEmptyValidatorMetaModelDto()])
            .build()                                          | [
            errorEntry("genericTypes", whenFieldIsInStateThenOthersShould(
                "classMetaModelDtoType", EQUAL_TO_ANY, ["DEFINITION"],
                fieldShouldWhenOtherMessage(NULL, [], "name", NOT_NULL, []))),
            errorEntry("genericTypes[0].isGenericEnumType", notNullMessage()),
            errorEntry("genericTypes[0].name",
                whenFieldIsInStateThenOthersShould("id", NULL, fieldShouldWhenOtherMessage(NOT_NULL, [], "className", NULL, []))),
            errorEntry("validators[0].className", notNullMessage()),
            errorEntry("genericTypes[0].translationName.translationByCountryCode",
                translatePlaceholder("ProvidedAllLanguages.cannot.find.translation.for.lang", "eng_ENG")),
            errorEntry("genericTypes[0].translationName.translationByCountryCode",
                translatePlaceholder("ProvidedAllLanguages.cannot.find.translation.for.lang", "en_US")),
            errorEntry("genericTypes[0].translationName.translationByCountryCode",
                translatePlaceholder("ProvidedAllLanguages.language.not.supported", "FR")),
            errorEntry("genericTypes[0].translationName.translationByCountryCode",
                translatePlaceholder("ProvidedAllLanguages.language.not.supported", "eng_ENG")),
        ]

        createValidClassMetaModelDtoWithClassName().toBuilder()
            .extendsFromModels([createEmptyClassMetaModelDto().toBuilder()
                                    .translationName(TranslationDto.builder()
                                        .translationId(123)
                                        .build())
                                    .build()])
            .validators([createValidValidatorMetaModelDto()])
            .build()                                          | [
            errorEntry("extendsFromModels", whenFieldIsInStateThenOthersShould(
                "classMetaModelDtoType", EQUAL_TO_ANY, ["DEFINITION"],
                fieldShouldWhenOtherMessage(NULL, [], "className", NOT_NULL, []))),
            errorEntry("extendsFromModels[0].name", whenFieldIsInStateThenOthersShould(
                "id", NULL, fieldShouldWhenOtherMessage(NOT_NULL, [], "className", NULL, [])))
        ]

        createClassMetaModelDtoFromClass(SomeRealClass).toBuilder()
            .fields([
                createValidFieldMetaModelDto("id", Long),
                createValidFieldMetaModelDto("surname", String),
                createValidFieldMetaModelDto("name", UUID),
            ])
            .build()                                          | [
            errorEntry("fields[1].fieldName", getMessage("ForRealClassFieldsCanBeMerged.invalid.field.name")),
            errorEntry("fields[2].fieldType", getMessage("ForRealClassFieldsCanBeMerged.invalid.field.type", String.canonicalName)),
        ]

        createClassMetaModelDtoFromClass(String).toBuilder()
            .translationName(null).build()                    | []
    }
}

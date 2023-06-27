package pl.jalokim.crudwizard.genericapp.translation

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage
import static pl.jalokim.crudwizard.core.translations.LocaleHolder.DEFAULT_LANG_CODE
import static pl.jalokim.crudwizard.core.translations.LocaleHolder.X_LOCALE_NAME_HEADER
import static pl.jalokim.crudwizard.core.translations.LocaleHolder.defaultLocale
import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.APPLICATION_TRANSLATIONS_PATH
import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.CORE_APPLICATION_TRANSLATIONS
import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.HIBERNATE_VALIDATION_MESSAGES
import static pl.jalokim.crudwizard.core.utils.StringCaseUtils.makeLineEndingAsUnix
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoWithGenerics
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createListWithMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createTranslatedFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.sampleEntryMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostWithSimplePerson
import static pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDtoSamples.createValidServiceMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationDtoSamples.sampleTranslationDto
import static pl.jalokim.crudwizard.test.utils.RawOperationsOnEndpoints.createHeaders
import static pl.jalokim.crudwizard.test.utils.RawOperationsOnEndpoints.extractErrorResponseDto
import static pl.jalokim.crudwizard.test.utils.RawOperationsOnEndpoints.extractResponseAsString
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.TEST_APPLICATION_TRANSLATIONS_PATH
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.NOT_BLANK_MESSAGE_PROPERTY
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.NOT_NULL_MESSAGE_PROPERTY
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.utils.collection.Elements.elements
import static pl.jalokim.utils.template.TemplateAsText.fromClassPath

import javax.validation.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import pl.jalokim.crudwizard.GenericAppWithReloadMetaContextSpecification
import pl.jalokim.crudwizard.core.translations.LocaleHolder
import pl.jalokim.crudwizard.core.translations.LocaleUtils
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.EnumEntryMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.EnumMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelService
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointResponseMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.samples.translations.ColorEnum
import pl.jalokim.crudwizard.genericapp.metamodel.samples.translations.SomeDate
import pl.jalokim.crudwizard.genericapp.metamodel.samples.translations.SomeOtherClass
import pl.jalokim.crudwizard.genericapp.metamodel.translation.LanguageTranslationsDto
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationAndSourceDto
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationDto
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationEntity
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationEntryEntity
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationLanguageRepository
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationLanguageService
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationRepository
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationService
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.TranslationArgs
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.NormalSpringService
import pl.jalokim.crudwizard.test.utils.RawOperationsOnEndpoints
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter
import pl.jalokim.utils.collection.Elements
import spock.lang.Unroll

class TranslationsIT extends GenericAppWithReloadMetaContextSpecification {

    static final NOT_NULL_MESSAGE = "value must be provided"

    @Autowired
    private TranslationRepository translationRepository

    @Autowired
    private TranslationLanguageRepository translationLanguageRepository

    @Autowired
    private TranslationLanguageService translationLanguageService

    @Autowired
    private EndpointMetaModelService endpointMetaModelService

    @Autowired
    private RawOperationsOnEndpoints rawOperationsOnEndpoints

    @Autowired
    private TranslationService translationService

    def "return expected javax message from db translations"() {
        given:
        addTranslationToDb(createTranslationDb(DEFAULT_LANG_CODE, NOT_NULL_MESSAGE_PROPERTY, NOT_NULL_MESSAGE))

        def createEndpointMetaModelDto = createValidPostWithSimplePerson().toBuilder()
            .operationName(null)
            .build()

        metaContextTestLoader.reload()

        when:
        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        then:
        ConstraintViolationException tx = thrown()
        def foundErrors = ValidatorWithConverter.errorsFromViolationException(tx)

        assertValidationResults(foundErrors, [
            errorEntry("operationName", NOT_NULL_MESSAGE)
        ])
    }

    def "inform about lack of supporting given locale"() {
        given:
        def createEndpointMetaModelDto = createValidPostWithSimplePerson().toBuilder()
            .baseUrl("test-not-supported-locale")
            .serviceMetaModel(createValidServiceMetaModelDto(NormalSpringService, "testNotSupportedLocale"))
            .build()

        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        def locale = "ko_KR"

        when:
        def httpResponse = rawOperationsOnEndpoints.performWithJsonContent(
            post("/test-not-supported-locale")
                .headers(createHeaders(Map.of(X_LOCALE_NAME_HEADER, locale))), [:])

        then:
        httpResponse.andExpect(status().is(500))
        def errorResponse = extractErrorResponseDto(httpResponse)
        errorResponse.message == "not supported locale: $locale"
    }

    def "cannot build locale under given header"() {
        given:
        def createEndpointMetaModelDto = createValidPostWithSimplePerson().toBuilder()
            .baseUrl("test-not-supported-locale")
            .serviceMetaModel(createValidServiceMetaModelDto(NormalSpringService, "testNotSupportedLocale"))
            .build()

        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        def invalidLocale = "ts5g_ts"

        when:
        def httpResponse = rawOperationsOnEndpoints.performWithJsonContent(
            post("/test-not-supported-locale")
                .headers(createHeaders(Map.of(X_LOCALE_NAME_HEADER, invalidLocale))), [:])

        then:
        httpResponse.andExpect(status().is(500))
        def errorResponse = extractErrorResponseDto(httpResponse)
        errorResponse.message == "locale ${invalidLocale} is invalid from header: X-Locale"
    }

    def "cannot find given translation key"() {
        def endpointUrl = "cannot-find-translation-key"
        given:
        def createEndpointMetaModelDto = EndpointMetaModelDto.builder()
            .operationName(endpointUrl)
            .apiTag(ApiTagDto.builder()
                .name(endpointUrl)
                .build())
            .httpMethod(HttpMethod.GET)
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(createClassMetaModelDtoFromClass(String))
                .successHttpCode(200)
                .build()
            )
            .baseUrl(endpointUrl)
            .serviceMetaModel(createValidServiceMetaModelDto(NormalSpringService, "cannotFindTranslationKey"))
            .build()

        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        when:
        def httpResponse = rawOperationsOnEndpoints.perform(get("/$endpointUrl"))

        then:
        httpResponse.andExpect(status().is(500))
        def errorResponse = extractErrorResponseDto(httpResponse)
        errorResponse.message == "No message found under code 'cannot.find.translation.key' for locale 'en_US'."
    }

    @Unroll
    def "return expected messages for given locale and method argument"() {
        given:
        addSupportedLanguage(givenLocale)
        if (dbTranslationToAdd) {
            addTranslationToDb(dbTranslationToAdd)
        }
        metaContextTestLoader.reloadMetaModelsContext()
        def translations = Map.of(
            "en_US", "english-american version",
            givenLocale, "given locale translation")

        def createEndpointMetaModelDto = createValidPostWithSimplePerson().toBuilder()
            .baseUrl("translate-message")
            .payloadMetamodel(createClassMetaModelDtoFromClass(TranslationArgs).toBuilder()
                .translationName(sampleTranslationDto(translations))
                .fields([
                    createTranslatedFieldMetaModelDto("placeholder", String, translations),
                    createValidFieldMetaModelDto("argumentsByIndexes",
                        createListWithMetaModel(createClassMetaModelDtoFromClass(String)), translations),
                    createValidFieldMetaModelDto("argumentsByName",
                        createClassMetaModelDtoWithGenerics(Map, translations,
                            createClassMetaModelDtoFromClass(String),
                            createClassMetaModelDtoFromClass(String)), translations)
                ])
                .build())
            .serviceMetaModel(createValidServiceMetaModelDto(NormalSpringService, "getMessageWithArgs"))
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(createClassMetaModelDtoFromClass(String))
                .successHttpCode(200)
                .build())
            .build()

        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        def headers = givenLocale ? Map.of(X_LOCALE_NAME_HEADER, givenLocale) : [:]

        when:
        def httpResponse = rawOperationsOnEndpoints.performWithJsonContent(
            post("/translate-message")
                .headers(createHeaders(headers)), body)

        then:
        httpResponse.andExpect(status().is(200))
        def responseText = extractResponseAsString(httpResponse)
        responseText == expectedMessage

        where:
        expectedMessage                                       | givenLocale | body | dbTranslationToAdd
        // handle of requested locale from core
        "jest null"                                           | "pl_PL"     | TranslationArgs.builder()
            .placeholder("whenIs.pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL")
            .build()                                                               | null

        // handle of requested locale and interpolation of arguments via {propertyName} from core
        "powinno should_arg_fieldValues_arg"                  | "pl_PL"     | TranslationArgs.builder()
            .placeholder("pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther.messageWithoutWhen")
            .argumentsByName([
                should     : 'should_arg',
                fieldValues: '_fieldValues_arg'
            ])
            .build()                                                               | null

        // add handle of requested locale and interpolation of arguments via {0} from core
        "Nie można znaleźć zasobu o id: 1 dla tabeli: PERSON" | "pl_PL"     | TranslationArgs.builder()
            .placeholder("EntityNotFoundException.default.concrete.message")
            .argumentsByIndexes([1, "PERSON"])
            .build()                                                               | null

        // add handle of requested locale and interpolation of arguments via {0} from db
        "some value_1 value_2 value_1 values"                 | "pl_PL"     | TranslationArgs.builder()
            .placeholder("some.translation.key")
            .argumentsByIndexes([
                'value_1',
                'value_2',
                'value_1'
            ])
            .build()                                                               | createTranslationDb(
            "pl_PL",
            "some.translation.key",
            "some {0} {1} {0} values"
        )

        // handle of requested locale and interpolation of arguments via {propertyName} from db
        "some value_1 value_2 value_1 values"                 | "pl_PL"     | TranslationArgs.builder()
            .placeholder("some.translation.key")
            .argumentsByName([
                firstArg : 'value_1',
                secondArg: 'value_2'
            ])
            .build()                                                               | createTranslationDb(
            "pl_PL",
            "some.translation.key",
            "some {firstArg} {secondArg} {firstArg} values"
        )

        //  handle of requested locale from hibernate
        "muss null sein"                                      | "de"        | TranslationArgs.builder()
            .placeholder("javax.validation.constraints.Null.message")
            .build()                                                               | null

        //  handle of requested locale from hibernate but overridden in db
        "not null de from db"                                 | "de"        | TranslationArgs.builder()
            .placeholder("javax.validation.constraints.Null.message")
            .build()                                                               | createTranslationDb(
            "de",
            "javax.validation.constraints.Null.message",
            "not null de from db"
        )

        // handle of requested locale and interpolation of arguments via {propertyName} from hibernate
        "muss kleiner oder gleich 123 sein"                   | "de"        | TranslationArgs.builder()
            .placeholder("javax.validation.constraints.Max.message")
            .argumentsByName([
                value: '123',
            ])
            .build()                                                               | null
    }

    def "return all translations with sources for default locale"() {
        given:
        addTranslationToDb(createTranslationDb(DEFAULT_LANG_CODE, NOT_NULL_MESSAGE_PROPERTY, NOT_NULL_MESSAGE))
        metaContextTestLoader.reload()

        when:
        def translationsWithSource = translationService.getTranslationsAndSourceByLocale(getDefaultLocale())

        then:
        verifyAll(findByTranslationKey(translationsWithSource, NOT_NULL_MESSAGE_PROPERTY)) {
            source == "from data base"
            translationKey == NOT_NULL_MESSAGE_PROPERTY
            translationValue == NOT_NULL_MESSAGE
        }

        verifyAll(findByTranslationKey(translationsWithSource, NOT_BLANK_MESSAGE_PROPERTY)) {
            source == HIBERNATE_VALIDATION_MESSAGES
            translationKey == NOT_BLANK_MESSAGE_PROPERTY
            translationValue == "must not be blank"
        }

        verifyAll(findByTranslationKey(translationsWithSource, "ResourceChangedException.default.message")) {
            source == CORE_APPLICATION_TRANSLATIONS
            translationKey == "ResourceChangedException.default.message"
            translationValue == "Resource was modified by other user"
        }

        verifyAll(findByTranslationKey(translationsWithSource, "expected.any.of.chars")) {
            source == APPLICATION_TRANSLATIONS_PATH
            translationKey == "expected.any.of.chars"
            translationValue == "expected one of the characters: {0}"
        }
    }

    @Unroll
    def "return expected values of translations of given metamodel"() {
        given:
        addSupportedLanguage("pl_PL")
        metaContextTestLoader.reloadMetaModelsContext()

        def createEndpointMetaModelDto = createValidPostWithSimplePerson().toBuilder()
            .baseUrl("translate-person")
            .payloadMetamodel(ClassMetaModelDto.builder()
                .name("person")
                .translationName(sampleTranslationDto([
                    "en_US": "Person",
                    "pl_PL": "Osoba",
                ]))
                .isGenericEnumType(false)
                .fields([
                    createTranslatedFieldMetaModelDto("name", String, [
                        "en_US": "Name",
                        "pl_PL": "Imię",
                    ]),
                    createValidFieldMetaModelDto("age", Byte).toBuilder()
                        .translationFieldName(TranslationDto.builder()
                            .translationKey("age.field.translationKEY")
                            .translationByCountryCode([
                                "en_US": "Age",
                                "pl_PL": "Wiek",
                            ])
                            .build()
                        )
                        .build(),
                    // class without given translation key
                    createValidFieldMetaModelDto("birthDay",
                        createClassMetaModelDtoFromClass(SomeDate).toBuilder()
                            .translationName(sampleTranslationDto([
                                "en_US": "Date",
                                "pl_PL": "Data",
                            ]))
                            .fields([
                                createTranslatedFieldMetaModelDto("value", String, [
                                    "en_US": "value",
                                    "pl_PL": "wartość",
                                ]),
                            ])
                            .build(),
                        [
                            "en_US": "Birthday",
                            "pl_PL": "Dzień urodzenia",
                        ]),
                    // real enums
                    createValidFieldMetaModelDto("someColor",
                        createClassMetaModelDtoFromClass(ColorEnum).toBuilder()
                            .translationName(sampleTranslationDto([
                                "en_US": "Color Enum",
                                "pl_PL": "Enum Kolor",
                            ]))
                            .isGenericEnumType(false)
                            .enumMetaModel(EnumMetaModelDto.builder()
                                .enums([
                                    sampleEntryMetaModel("GREEN", [
                                        "en_US": "green",
                                        "pl_PL": "zielony",
                                    ]),
                                    EnumEntryMetaModelDto.builder()
                                        .name("RED")
                                        .translation(TranslationDto.builder()
                                            .translationKey("someColor.red.enumValue")
                                            .translationByCountryCode(
                                                [
                                                    "en_US": "red",
                                                    "pl_PL": "czerwony",
                                                ]
                                            )
                                            .build())
                                        .build()
                                ])
                                .build())
                            .build(),
                        [
                            "en_US": "Eyes Color",
                            "pl_PL": "Kolor Oczu",
                        ]),
                    // metamodel enums
                    createValidFieldMetaModelDto("someEnum",
                        ClassMetaModelDto.builder()
                            .name("SomeEnumName")
                            .translationName(sampleTranslationDto([
                                "en_US": "some Enum",
                                "pl_PL": "jakiś Enum",
                            ]))
                            .isGenericEnumType(true)
                            .enumMetaModel(EnumMetaModelDto.builder()
                                .enums([
                                    sampleEntryMetaModel("FIRST", [
                                        "en_US": "first",
                                        "pl_PL": "pierwszy",
                                    ]),
                                    sampleEntryMetaModel("SECOND", [
                                        "en_US": "second",
                                        "pl_PL": "drugi",
                                    ]),
                                ])
                                .build())
                            .build(),
                        [
                            "en_US": "next enum",
                            "pl_PL": "następny enum",
                        ]),
                    // class with given translation key
                    createValidFieldMetaModelDto("otherClassField",
                        createClassMetaModelDtoFromClass(SomeOtherClass).toBuilder()
                            .translationName(TranslationDto.builder()
                                .translationKey("otherClass.name")
                                .translationByCountryCode([
                                    "en_US": "Other class",
                                    "pl_PL": "Inna klasa",
                                ])
                                .build())
                            .fields([
                                createTranslatedFieldMetaModelDto("id", Long, [
                                    "en_US": "id",
                                    "pl_PL": "identyfikator",
                                ]),
                            ])
                            .build(),
                        [
                            "en_US": "other field name",
                            "pl_PL": "inna nazwa pola",
                        ])
                ])
                .build())
            .serviceMetaModel(createValidServiceMetaModelDto(NormalSpringService, "getTranslatedInfo"))
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(createClassMetaModelDtoFromClass(String))
                .successHttpCode(200)
                .build())
            .build()

        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        def headers = Map.of(X_LOCALE_NAME_HEADER, givenLocale)

        def body = [
            "name"           : "John",
            "age"            : 25,
            "birthDay"       : [
                "value": "1998-03-22"
            ],
            "someColor"      : "RED",
            "someEnum"       : "SECOND",
            "otherClassField": [
                "id": 123
            ]
        ]

        when:
        def httpResponse = rawOperationsOnEndpoints.performWithJsonContent(
            post("/translate-person")
                .headers(createHeaders(headers)), body)

        then:
        httpResponse.andExpect(status().is(200))
        def responseText = extractResponseAsString(httpResponse) + "\n"
        makeLineEndingAsUnix(responseText) == makeLineEndingAsUnix(fromClassPath("expectedTranslations/$expectedFileName").currentTemplateText)

        where:
        givenLocale | expectedFileName
        "pl_PL"     | "person-polish-translation"
        "en_US"     | "person-english-translation"
    }

    def "cannot enable new language due to lack of all translations"() {
        given:
        def newLang = LanguageTranslationsDto.builder()
            .languageCode("pl_PL")
            .languageFullName("Polski")
            .enabled(true)
            .translations([:])
            .build()

        when:
        translationLanguageService.saveOrUpdate(newLang)

        then:
        ConstraintViolationException tx = thrown()
        def foundErrors = ValidatorWithConverter.errorsFromViolationException(tx)
        foundErrors.size() == 1
        foundErrors.get(0).property == 'translations'
        foundErrors.get(0).message.contains("Cannot enable language due to not provided translations")
    }

    def "can save new language without all given translations when not enabled, when enabled then context will refreshed"() {
        given:
        def languages = translationLanguageService.getAllLanguages()
        assert languages.size() == 1
        assert languages.get("en_US") != null

        def newLang = LanguageTranslationsDto.builder()
            .languageCode("pl_PL")
            .languageFullName("Polski")
            .enabled(false)
            .translations(Map.of(
                NOT_NULL_MESSAGE_PROPERTY, "należy podać wartość"
            ))
            .build()

        when:
        translationLanguageService.saveOrUpdate(newLang)

        then:
        def newLanguages = translationLanguageService.getAllLanguages()
        assert newLanguages.size() == 2
        assert newLanguages.keySet() == ["pl_PL", "en_US"] as Set

        def supportedLanguages = translationLanguageService.getAllSupportedLanguages()
        assert supportedLanguages.size() == 1
        assert supportedLanguages.keySet() == ["en_US"] as Set

        inVoidTransaction {
            def translation = translationRepository.findByTranslationKey(NOT_NULL_MESSAGE_PROPERTY)
            translation.getTranslations().size() == 1
            translation.getTranslations().get(0).valueOrPlaceholder == "należy podać wartość"
        }

        and:
        def updateLang = LanguageTranslationsDto.builder()
            .languageCode("pl_PL")
            .languageFullName("Polski")
            .enabled(false)
            .translations(Map.of(
                NOT_NULL_MESSAGE_PROPERTY, "nie może być null",
                NOT_BLANK_MESSAGE_PROPERTY, "nie może być pusta"
            ))
            .build()

        when:
        translationLanguageService.saveOrUpdate(updateLang)

        then:
        inVoidTransaction {
            def translationAfterUpdate = translationRepository.findByTranslationKey(NOT_NULL_MESSAGE_PROPERTY)
            translationAfterUpdate.getTranslations().size() == 1
            translationAfterUpdate.getTranslations().get(0).valueOrPlaceholder == "nie może być null"

            def notBlankTranslation = translationRepository.findByTranslationKey(NOT_BLANK_MESSAGE_PROPERTY)
            notBlankTranslation.getTranslations().size() == 1
            notBlankTranslation.getTranslations().get(0).valueOrPlaceholder == "nie może być pusta"
        }

        and:
        def updateEngLang = LanguageTranslationsDto.builder()
            .languageCode("en_US")
            .languageFullName("English")
            .enabled(true)
            .translations(Map.of(
                NOT_NULL_MESSAGE_PROPERTY, "cannot be null value",
            ))
            .build()

        when:
        translationLanguageService.saveOrUpdate(updateEngLang)

        then:
        inVoidTransaction {
            def translationAfterUpdate = translationRepository.findByTranslationKey(NOT_NULL_MESSAGE_PROPERTY)
            translationAfterUpdate.getTranslations().size() == 2
            translationAfterUpdate.getTranslations().find {
                it.language.languageCode == "en_US"
            }.valueOrPlaceholder == "cannot be null value"
            translationAfterUpdate.getTranslations().find {
                it.language.languageCode == "pl_PL"
            }.valueOrPlaceholder == "nie może być null"

            def notBlankTranslation = translationRepository.findByTranslationKey(NOT_BLANK_MESSAGE_PROPERTY)
            notBlankTranslation.getTranslations().size() == 1
            notBlankTranslation.getTranslations().get(0).valueOrPlaceholder == "nie może być pusta"
        }
        getMessage(NOT_NULL_MESSAGE_PROPERTY) == "cannot be null value"
        LocaleHolder.setLocale(LocaleUtils.createLocale("pl_PL"))
        boolean notSupportedPl = true
        try {
            getMessage(NOT_NULL_MESSAGE_PROPERTY) == "cannot be null value"
        } catch (IllegalArgumentException ex) {
            notSupportedPl = false
        }
        !notSupportedPl
        metaModelContextService.getMetaModelContext().translationsContext
            .getAllLanguages().keySet() == ["en_US"] as Set

        and:
        def translationKeyToPopulate = getTranslationKeysFromSources()
        def translations = new HashMap<String, String>()
        translationKeyToPopulate.forEach {
            translations.put(it, 'some translations')
        }

        LocaleHolder.setLocale(getDefaultLocale())
        def enablePlLang = LanguageTranslationsDto.builder()
            .languageCode("pl_PL")
            .languageFullName("Polski")
            .enabled(true)
            .translations(translations)
            .build()

        when:
        translationLanguageService.saveOrUpdate(enablePlLang)

        then:
        LocaleHolder.setLocale(LocaleUtils.createLocale("pl_PL"))
        getMessage(NOT_NULL_MESSAGE_PROPERTY) == "nie może być null"

        metaModelContextService.getMetaModelContext().translationsContext
            .getAllLanguages().keySet() == ["pl_PL", "en_US"] as Set
    }

    private TranslationAndSourceDto findByTranslationKey(List<TranslationAndSourceDto> translations, String translationKey) {
        translations.find({
            it.translationKey == translationKey
        })
    }

    private addTranslationToDb(TranslationDb translationDb) {
        String langKey = translationDb.langKey
        String translationKey = translationDb.key
        String valueOrPlaceholder = translationDb.translation

        inVoidTransaction {
            def langEntity = translationLanguageRepository.findAll().find {
                it.languageCode == langKey
            }

            def translationEntry = TranslationEntryEntity.builder()
                .language(langEntity)
                .valueOrPlaceholder(valueOrPlaceholder)
                .build()

            def translations = TranslationEntity.builder()
                .translationKey(translationKey)
                .translations([translationEntry])
                .build()
            translationEntry.setTranslation(translations)
            translationRepository.save(translations)
        }
    }

    private addSupportedLanguage(String language) {
        LocaleHolder.setLocale(getDefaultLocale())
        def enablePlLang = LanguageTranslationsDto.builder()
            .languageCode(language)
            .languageFullName(language)
            .enabled(true)
            .translations([:])
            .build()

        try {
            translationLanguageService.saveOrUpdate(enablePlLang)
        } catch (ConstraintViolationException exception) {
            def size = exception.constraintViolations.size()
            if (size == 1) {
                def errorEntry = elements(exception.constraintViolations).asList().get(0)

                def translationKeyToPopulate = Elements.bySplitText(errorEntry.message, System.lineSeparator())
                .skip(1)
                .asList()
                def translations = new HashMap<String, String>()
                translationKeyToPopulate.forEach {
                    translations.put(it, 'some translations')
                }

                enablePlLang = LanguageTranslationsDto.builder()
                    .languageCode(language)
                    .languageFullName(language)
                    .enabled(true)
                    .translations(translations)
                    .build()
                translationLanguageService.saveOrUpdate(enablePlLang)
            } else {
                throw exception
            }
        }

    }

    static class TranslationDb {

        private final String langKey
        private final String key
        private final String translation

        TranslationDb(String langKey, String key, String translation) {
            this.langKey = langKey
            this.key = key
            this.translation = translation
        }
    }

    TranslationDb createTranslationDb(String langKey, String key, String translation) {
        new TranslationDb(langKey, key, translation)
    }

    private Set<String> getTranslationKeysFromSources(List<String> translationsSources = [TEST_APPLICATION_TRANSLATIONS_PATH]) {
        translationService.getTranslationsAndSourceByLocale(defaultLocale)
            .findAll {
                translationsSources.contains(it.source)
            }
            .collect {
                it.translationKey
            } as Set
    }
}

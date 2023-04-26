package pl.jalokim.crudwizard.maintenance.metamodel.translation

import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.NOT_NULL_MESSAGE_PROPERTY

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder
import pl.jalokim.crudwizard.genericapp.metamodel.translation.LanguageTranslationsDto
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationAndSourceDto
import pl.jalokim.crudwizard.maintenance.MaintenanceBaseIntegrationController
import pl.jalokim.crudwizard.maintenance.metamodel.endpoint.OperationsOnEndpointRestController

class TranslationRestControllerIT extends MaintenanceBaseIntegrationController {

    @Autowired
    private OperationsOnEndpointRestController endpointRestController

    def "return all translation for default locale"() {
        when:
        def translations = endpointRestController.getAndReturnCollectionOfObjects(
            "/maintenance/translations/by-locale/en_US", TranslationAndSourceDto)

        then:
        translations.size() > 50
    }

    def "update ang translation"() {
        given:
        def updateEngLang = LanguageTranslationsDto.builder()
            .languageCode("en_US")
            .languageFullName("English")
            .enabled(true)
            .translations(Map.of(
                NOT_NULL_MESSAGE_PROPERTY, "cannot be null value",
            ))
            .build()

        when:
        endpointRestController.putPayload("/maintenance/translations", updateEngLang)

        then:
        AppMessageSourceHolder.getMessage(NOT_NULL_MESSAGE_PROPERTY) == "cannot be null value"
    }
}

package pl.jalokim.crudwizard.genericapp.metamodel.service.translator

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper
import static pl.jalokim.crudwizard.core.datastorage.RawEntityObjectSamples.createRequestBody
import static pl.jalokim.crudwizard.core.datastorage.RawEntityObjectSamples.createRequestBodyTranslated
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createRequestBodyClassMetaModel

import com.fasterxml.jackson.databind.ObjectMapper
import pl.jalokim.crudwizard.genericapp.service.translator.RawEntityObjectTranslator
import spock.lang.Specification

class RawEntityObjectTranslatorTest extends Specification {

    private ObjectMapper objectMapper = createObjectMapper()
    private RawEntityObjectTranslator testCase = new RawEntityObjectTranslator(objectMapper)

    def "should translate to expected raw object entity"() {
        given:
        def classMetaModel = createRequestBodyClassMetaModel()
        def inputRawEntityObject = createRequestBody()

        when:
        def resultRawEntityObject = testCase.translateToRealObjects(inputRawEntityObject, classMetaModel)

        then:
        resultRawEntityObject == createRequestBodyTranslated()
    }
}

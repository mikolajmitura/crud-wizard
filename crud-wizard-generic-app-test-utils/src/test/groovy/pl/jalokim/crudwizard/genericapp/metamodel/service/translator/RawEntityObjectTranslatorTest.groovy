package pl.jalokim.crudwizard.genericapp.metamodel.service.translator

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper
import static pl.jalokim.crudwizard.core.datastorage.RawEntityObjectSamples.createRequestBody
import static pl.jalokim.crudwizard.core.datastorage.RawEntityObjectSamples.createRequestBodyTranslated
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createRequestBodyClassMetaModel

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.LocalDate
import pl.jalokim.crudwizard.core.exception.TechnicalException
import pl.jalokim.crudwizard.core.sample.SamplePersonDto

import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper
import pl.jalokim.crudwizard.genericapp.service.translator.RawEntityObjectTranslator
import spock.lang.Specification

class RawEntityObjectTranslatorTest extends Specification {

    private ObjectMapper objectMapper = createObjectMapper()
    private JsonObjectMapper jsonObjectMapper = new JsonObjectMapper(objectMapper)
    private RawEntityObjectTranslator testCase = new RawEntityObjectTranslator(jsonObjectMapper)

    def "should translate to expected raw object entity"() {
        given:
        def classMetaModel = createRequestBodyClassMetaModel()
        def inputRawEntityObject = createRequestBody()

        when:
        def resultRawEntityObject = testCase.translateToRealObjects(inputRawEntityObject, classMetaModel)

        then:
        resultRawEntityObject == createRequestBodyTranslated()
    }

    def "should inform about that field name not exists"() {
        given:
        def classMetaModel = createRequestBodyClassMetaModel()
        def invalidInputRawEntityObject = createRequestBody()
        invalidInputRawEntityObject.addresses[1].notExistField = null

        when:
        testCase.translateToRealObjects(invalidInputRawEntityObject, classMetaModel)

        then:
        TechnicalException ex = thrown()
        def addressMetaModel = classMetaModel.getFieldByName("addresses").fieldType.genericTypes[0]
        def expectedClassMetaModelName = addressMetaModel.name
        def expectedFieldNames = addressMetaModel.fieldNames
        ex.message == "Cannot find field with name: 'notExistField' in path: 'addresses[1]'" +
            " available fields: $expectedFieldNames in named class meta model: '$expectedClassMetaModelName'"
    }

    def "should inform about problem with conversion to some type"() {
        given:
        def classMetaModel = createRequestBodyClassMetaModel()
        def invalidInputRawEntityObject = createRequestBody()
        invalidInputRawEntityObject.birthDate = ["test"]

        when:
        testCase.translateToRealObjects(invalidInputRawEntityObject, classMetaModel)

        then:
        TechnicalException ex = thrown()
        ex.message == "Cannot convert from value: '[\"test\"]' to class $LocalDate.canonicalName in path: birthDate"
    }

    def "should convert expected String as json from map node"() {
        given:
        def classMetaModel = createRequestBodyClassMetaModel()
        def invalidInputRawEntityObject = createRequestBody()
        invalidInputRawEntityObject.name = [firstName: "test", "secondName": "test2"]

        when:
        def resultRawEntityObject = testCase.translateToRealObjects(invalidInputRawEntityObject, classMetaModel)

        then:
        resultRawEntityObject.name == "{\"firstName\":\"test\",\"secondName\":\"test2\"}"
    }

    def "instead of data for address (should be map) but was some list"() {
        given:
        def classMetaModel = createRequestBodyClassMetaModel()
        def invalidInputRawEntityObject = createRequestBody()
        invalidInputRawEntityObject.addresses[0] = ["test"]

        when:
        testCase.translateToRealObjects(invalidInputRawEntityObject, classMetaModel)

        then:
        TechnicalException ex = thrown()
        ex.message == cannotCastMessage(ArrayList, Map, "addresses[0]", "[\"test\"]")
    }

    def "instead of data for contactData (should be map) but was some String"() {
        given:
        def classMetaModel = createRequestBodyClassMetaModel()
        def invalidInputRawEntityObject = createRequestBody()
        invalidInputRawEntityObject.contactData = "text"

        when:
        testCase.translateToRealObjects(invalidInputRawEntityObject, classMetaModel)

        then:
        TechnicalException ex = thrown()
        ex.message == cannotCastMessage(String, Map, "contactData", "\"text\"")
    }

    def "instead of data for hobbies (should be list) but was some map"() {
        given:
        def classMetaModel = createRequestBodyClassMetaModel()
        def invalidInputRawEntityObject = createRequestBody()
        invalidInputRawEntityObject.hobbies = [ field: "someText"]

        when:
        testCase.translateToRealObjects(invalidInputRawEntityObject, classMetaModel)

        then:
        TechnicalException ex = thrown()
        ex.message == cannotCastMessage(LinkedHashMap, Collection, "hobbies", "{\"field\":\"someText\"}")
    }

    def "should convert meta model with usage new attached object mapper module"() {
        given:
        def classMetaModel = createRequestBodyClassMetaModel()
        def inputRawEntityObject = createRequestBody()
        SimpleModule module = new SimpleModule()
        module.addDeserializer(SamplePersonDto.class, new SamplePersonDeserializer())
        objectMapper.registerModule(module)
        def expectedResult = createRequestBodyTranslated()
        expectedResult.personData = new SamplePersonDto("#####", "######")

        when:
        def resultRawEntityObject = testCase.translateToRealObjects(inputRawEntityObject, classMetaModel)

        then:
        resultRawEntityObject == expectedResult
    }

    static String cannotCastMessage(Class<?> fromClass, Class<?> toClass, String path, String jsonValue) {
        "Cannot cast from: $fromClass.canonicalName to $toClass.canonicalName in path: $path invalid json part: $jsonValue"
    }

    static class SamplePersonDeserializer extends JsonDeserializer<SamplePersonDto> {

        @Override
        SamplePersonDto deserialize(JsonParser p, DeserializationContext deserializationContext) {
            new SamplePersonDto("#####", "######")
        }
    }
}

package pl.jalokim.crudwizard.genericapp.service.translator

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper
import static pl.jalokim.crudwizard.core.datastorage.RawEntityObjectSamples.createRequestBody
import static pl.jalokim.crudwizard.core.datastorage.RawEntityObjectSamples.createRequestBodyAsMap
import static pl.jalokim.crudwizard.core.datastorage.RawEntityObjectSamples.createRequestBodyAsRawJson
import static pl.jalokim.crudwizard.core.datastorage.RawEntityObjectSamples.createRequestBodyTranslated
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createRequestBodyClassMetaModel

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import java.time.LocalDate
import pl.jalokim.crudwizard.core.exception.TechnicalException
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import spock.lang.Specification

class RawEntityObjectTranslatorTest extends Specification {

    private ObjectMapper objectMapper = createObjectMapper()
    private JsonObjectMapper jsonObjectMapper = new JsonObjectMapper(objectMapper)
    private RawEntityObjectTranslator testCase = new RawEntityObjectTranslator(jsonObjectMapper)

    def "should translate to expected raw object entity"() {
        given:
        def classMetaModel = createRequestBodyClassMetaModel()
        def inputRawEntityObject = jsonObjectMapper.asJsonNode(ObjectNodePath.rootNode(), createRequestBodyAsRawJson())

        when:
        def resultRawEntityObject = testCase.translateToRealObjects(inputRawEntityObject, classMetaModel)

        then:
        resultRawEntityObject == createRequestBodyTranslated()
    }

    def "should translate to expected simple raw object"() {
        given:
        def classMetaModel = createClassMetaModelFromClass(Double)
        JsonNode jsonNode = jsonObjectMapper.asJsonNode(ObjectNodePath.rootNode(), "14.01")

        when:
        def resultRawSimpleNumber = testCase.translateToRealObjects(jsonNode, classMetaModel)

        then:
        resultRawSimpleNumber == 14.01
    }

    def "should translate to expected simple array object"() {
        given:
        def classMetaModel = createClassMetaModelFromClass(ArrayList).toBuilder()
            .genericTypes([createClassMetaModelFromClass(String)])
            .build()
        def rawJson = "[\"first\", \"second\"]"
        JsonNode jsonNode = jsonObjectMapper.asJsonNode(ObjectNodePath.rootNode(), rawJson)

        when:
        def resultRawSimpleNumber = testCase.translateToRealObjects(jsonNode, classMetaModel)

        then:
        resultRawSimpleNumber == ["first", "second"]
    }

    def "should inform about that field name not exists"() {
        given:
        def classMetaModel = createRequestBodyClassMetaModel()
        def invalidInputRawEntityObject = createRequestBodyAsMap()
        invalidInputRawEntityObject.addresses[1].notExistField = null
        def jsonNode = asJsonNode(invalidInputRawEntityObject)

        when:
        testCase.translateToRealObjects(jsonNode, classMetaModel)

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
        def invalidInputRawEntityObject = createRequestBodyAsMap()
        invalidInputRawEntityObject.birthDate = ["test"]
        def jsonNode = asJsonNode(invalidInputRawEntityObject)

        when:
        testCase.translateToRealObjects(jsonNode, classMetaModel)

        then:
        TechnicalException ex = thrown()
        ex.message == "Cannot convert from value: '[\"test\"]' to class $LocalDate.canonicalName in path: birthDate"
    }

    def "should convert expected String as json from map node"() {
        given:
        def classMetaModel = createRequestBodyClassMetaModel()
        def invalidInputRawEntityObject = createRequestBodyAsMap()
        invalidInputRawEntityObject.name = [firstName: "test", "secondName": "test2"]
        def jsonNode = asJsonNode(invalidInputRawEntityObject)

        when:
        def resultRawEntityObject = testCase.translateToRealObjects(jsonNode, classMetaModel)

        then:
        resultRawEntityObject.name == "{\"firstName\":\"test\",\"secondName\":\"test2\"}"
    }

    def "instead of data for address (should be map) but was some list"() {
        given:
        def classMetaModel = createRequestBodyClassMetaModel()
        def invalidInputRawEntityObject = createRequestBodyAsMap()
        invalidInputRawEntityObject.addresses[0] = ["test"]
        def jsonNode = asJsonNode(invalidInputRawEntityObject)

        when:
        testCase.translateToRealObjects(jsonNode, classMetaModel)

        then:
        TechnicalException ex = thrown()
        ex.message == cannotCastMessage(ArrayNode, ObjectNode, "addresses[0]", "[\"test\"]")
    }

    def "instead of data for contactData (should be map) but was some String"() {
        given:
        def classMetaModel = createRequestBodyClassMetaModel()
        def invalidInputRawEntityObject = createRequestBodyAsMap()
        invalidInputRawEntityObject.contactData = "text"
        def jsonNode = asJsonNode(invalidInputRawEntityObject)

        when:
        testCase.translateToRealObjects(jsonNode, classMetaModel)

        then:
        TechnicalException ex = thrown()
        ex.message == cannotCastMessage(TextNode, ObjectNode, "contactData", "\"text\"")
    }

    def "instead of data for hobbies (should be list) but was some map"() {
        given:
        def classMetaModel = createRequestBodyClassMetaModel()
        def invalidInputRawEntityObject = createRequestBodyAsMap()
        invalidInputRawEntityObject.hobbies = [ field: "someText"]
        def jsonNode = asJsonNode(invalidInputRawEntityObject)

        when:
        testCase.translateToRealObjects(jsonNode, classMetaModel)

        then:
        TechnicalException ex = thrown()
        ex.message == cannotCastMessage(ObjectNode, ArrayNode, "hobbies", "{\"field\":\"someText\"}")
    }

    def "should convert meta model with usage new attached object mapper module"() {
        given:
        def classMetaModel = createRequestBodyClassMetaModel()
        def inputRawEntityObject = createRequestBody()
        SimpleModule module = new SimpleModule()
        module.addDeserializer(SamplePersonDto.class, new SamplePersonDeserializer())
        objectMapper.registerModule(module)
        def expectedResult = createRequestBodyTranslated()
        expectedResult.personData = new SamplePersonDto(null, "#####", "######")

        when:
        def resultRawEntityObject = testCase.translateToRealObjects(inputRawEntityObject, classMetaModel)

        then:
        resultRawEntityObject == expectedResult
    }

    private JsonNode asJsonNode(Object fromObject) {
        jsonObjectMapper.asJsonNode(ObjectNodePath.rootNode(), fromObject)
    }

    static String cannotCastMessage(Class<?> fromClass, Class<?> toClass, String path, String jsonValue) {
        "Cannot cast from: $fromClass.canonicalName to $toClass.canonicalName in path: $path invalid json part: $jsonValue"
    }

    static class SamplePersonDeserializer extends JsonDeserializer<SamplePersonDto> {

        @Override
        SamplePersonDto deserialize(JsonParser p, DeserializationContext deserializationContext) {
            new SamplePersonDto(null, "#####", "######")
        }
    }
}

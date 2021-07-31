package pl.jalokim.crudwizard.genericapp.service.invoker

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper
import static pl.jalokim.crudwizard.genericapp.service.invoker.BeanMethodMetaModelCreator.findMethodByName

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.MapType
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import pl.jalokim.crudwizard.core.metamodels.JavaTypeMetaModel
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.NormalSpringServiceImpl
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper
import spock.lang.Specification

class BeanMethodMetaModelCreatorTest extends Specification {

    ObjectMapper objectMapper = createObjectMapper()
    JsonObjectMapper jsonObjectMapper = new JsonObjectMapper(objectMapper)
    MethodSignatureMetaModelResolver methodSignatureMetaModelResolver = new MethodSignatureMetaModelResolver(jsonObjectMapper)
    BeanMethodMetaModelCreator testCase = new BeanMethodMetaModelCreator(methodSignatureMetaModelResolver)

    def "should resolve getSmSamplePersonDto method in NormalSpringService class"() {
        given:
        def methodName = "getSamplePersonDto"

        when:
        def resultMethodMetaModel = testCase.createBeanMethodMetaModel(methodName,
            NormalSpringServiceImpl.canonicalName + '$$EnhancerBySpringCGLIB$$mv59dfg4')

        then:
        verifyAll(resultMethodMetaModel) {
            name == methodName
            originalMethod == findMethodByName(NormalSpringServiceImpl, methodName)
            verifyAll(methodSignatureMetaModel) {
                !returnType.isGenericType()
                returnType.isRawClass()
                returnType == JavaTypeMetaModel.createWithRawClass(SamplePersonDto)
                verifyAll(methodArguments[0]) {
                    annotations*.annotationType() as Set == [RequestBody, NotNull] as Set
                    argumentType == JavaTypeMetaModel.createWithRawClass(JsonNode)
                }
                verifyAll(methodArguments[1]) {
                    annotations*.annotationType() as Set == [RequestBody, Validated] as Set
                    argumentType == JavaTypeMetaModel.createWithRawClass(SamplePersonDto)
                }
                verifyAll(methodArguments[2]) {
                    annotations.isEmpty()
                    argumentType == JavaTypeMetaModel.createWithRawClass(String)
                }
                verifyAll(methodArguments[3]) {
                    annotations*.annotationType() as Set == [RequestHeader] as Set
                    argumentType.rawClass == null
                    argumentType.originalType.toString() == "Map<String, Object>"
                    MapType mapType = ((MapType) argumentType.jacksonJavaType)
                    mapType.keyType.toString() == "[simple type, class java.lang.String]"
                    mapType.contentType.toString() == "[simple type, class java.lang.Object]"
                }
                verifyAll(methodArguments[4]) {
                    annotations*.annotationType() as Set == [RequestHeader, NotBlank] as Set
                    argumentType == JavaTypeMetaModel.createWithRawClass(String)
                }
            }
        }
    }

    // TODO #00 test cases
    // resolve bean with generic fields (will be two services one generic, tested will be concrete service)
    // method duplication
    // cannot find class
    // cannot find method
}

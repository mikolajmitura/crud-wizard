package pl.jalokim.crudwizard.genericapp.service.invoker

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.findMethodByName

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.MapType
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.websocket.server.PathParam
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import pl.jalokim.crudwizard.core.exception.TechnicalException
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.genericapp.metamodel.method.JavaTypeMetaModel
import pl.jalokim.crudwizard.genericapp.method.BeanMethodMetaModelCreator
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.MapGenericService
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.NormalSpringService
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper
import spock.lang.Specification

class BeanMethodMetaModelCreatorTest extends Specification {

    ObjectMapper objectMapper = createObjectMapper()
    JsonObjectMapper jsonObjectMapper = new JsonObjectMapper(objectMapper)
    MethodSignatureMetaModelResolver methodSignatureMetaModelResolver = new MethodSignatureMetaModelResolver(jsonObjectMapper)
    BeanMethodMetaModelCreator testCase = new BeanMethodMetaModelCreator(methodSignatureMetaModelResolver)

    def "should resolve getSmSamplePersonDto method in NormalSpringService class"() {
        given:
        def methodName = "getSamplePersonDtoInvalid"

        when:
        def resultMethodMetaModel = testCase.createBeanMethodMetaModel(methodName,
            NormalSpringService.canonicalName + '$$EnhancerBySpringCGLIB$$mv59dfg4', null)

        then:
        verifyAll(resultMethodMetaModel) {
            originalMethod == findMethodByName(NormalSpringService, methodName)
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
                    argumentType.rawClass == Map
                    argumentType.originalType.toString() == "java.util.Map<java.lang.String, java.lang.Object>"
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

    def "should resolve generic types of method via generic parent service"() {
        given:
        def methodName = "processObject"

        when:
        def resultMethodMetaModel = testCase.createBeanMethodMetaModel(methodName,
            MapGenericService.canonicalName + '$$EnhancerBySpringCGLIB$$mv59dfg4', null)

        then:
        verifyAll(resultMethodMetaModel) {
            originalMethod == findMethodByName(MapGenericService, methodName)
            verifyAll(methodSignatureMetaModel) {
                returnType.isGenericType()
                !returnType.isRawClass()
                returnType.rawClass == MapGenericService.SomeGenericValue
                returnType.originalType.toString() == 'pl.jalokim.crudwizard.genericapp.service.invoker.sample.MapGenericService$SomeGenericValue<java.lang.Long, java.lang.Double>'
                returnType.jacksonJavaType.toString() == "[simple type, class $MapGenericService.canonicalName\$SomeGenericValue<$Long.canonicalName,$Double.canonicalName>]"
                verifyAll(methodArguments[0]) {
                    annotations*.annotationType() as Set == [Validated, RequestBody] as Set
                    argumentType.originalType.toString() == "java.util.Map<java.lang.Long, java.util.List<java.lang.String>>"
                }
                verifyAll(methodArguments[1]) {
                    annotations*.annotationType() as Set == [PathParam] as Set
                    argumentType == JavaTypeMetaModel.createWithRawClass(Long)
                }
            }
        }
    }

    def "found more than one method by name"() {
        given:
        def methodName = "duplicatedMethodName"
        def foundMethodsAsText = NormalSpringService.getMethods()
            .findAll {
                it.name == methodName
            }
            .join(System.lineSeparator())

        when:
        testCase.createBeanMethodMetaModel(methodName, NormalSpringService.canonicalName, null)

        then:
        TechnicalException ex = thrown()
        ex.message == "Found more than one method with name '$methodName' in class: $NormalSpringService.canonicalName found methods: $foundMethodsAsText"
    }

    def "cannot find method by name"() {
        given:
        def methodName = "notExistMethodName"

        when:
        testCase.createBeanMethodMetaModel(methodName, NormalSpringService.canonicalName, null)

        then:
        TechnicalException ex = thrown()
        ex.message == "Cannot find method with name: '$methodName' in class $NormalSpringService.canonicalName"
    }
}

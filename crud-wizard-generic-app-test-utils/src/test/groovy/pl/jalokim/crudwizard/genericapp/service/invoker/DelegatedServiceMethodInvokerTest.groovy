package pl.jalokim.crudwizard.genericapp.service.invoker

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper
import static pl.jalokim.crudwizard.core.datastorage.RawEntityObjectSamples.createHttpQueryParamsTranslated
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.findMethodByName
import static pl.jalokim.crudwizard.genericapp.metamodel.service.GenericServiceArgumentSamples.createInputGenericServiceArgument
import static pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload.translatedPayload

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import pl.jalokim.crudwizard.core.exception.TechnicalException
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel
import pl.jalokim.crudwizard.core.metamodels.EndpointResponseMetaModel
import pl.jalokim.crudwizard.core.metamodels.ServiceMetaModel
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.NormalSpringService
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper
import pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload
import pl.jalokim.utils.test.DataFakerHelper
import spock.lang.Specification

class DelegatedServiceMethodInvokerTest extends Specification {

    ObjectMapper objectMapper = createObjectMapper()
    JsonObjectMapper jsonObjectMapper = new JsonObjectMapper(objectMapper)
    DelegatedServiceMethodInvoker testCase = new DelegatedServiceMethodInvoker(jsonObjectMapper)

    MethodSignatureMetaModelResolver methodSignatureMetaModelResolver = new MethodSignatureMetaModelResolver(jsonObjectMapper)
    BeanMethodMetaModelCreator beanMethodMetaModelCreator = new BeanMethodMetaModelCreator(methodSignatureMetaModelResolver)

    def "should invoke method as expected"() {
        given:
        NormalSpringService normalSpringService = new NormalSpringService()
        def methodName = "returnAllInputs"
        def invokerArgs = createValidInvokerArgs(methodName, normalSpringService)
        def genericServiceArgument = invokerArgs.genericServiceArgument

        when:
        def response = testCase.callMethod(genericServiceArgument)

        then:
        response.statusCode == HttpStatus.OK

        verifyAll(response.body) {
            fromJsonNode == invokerArgs.expectedName + invokerArgs.expectedSurname
            jsonNode2 == genericServiceArgument.requestBody
            jsonNodeTranslated == invokerArgs.translatedPayload
            jsonNodeTranslated2 == invokerArgs.translatedPayload
            samplePersonDto == new SamplePersonDto(invokerArgs.expectedName, invokerArgs.expectedSurname)
            headers == genericServiceArgument.headers
            cookieValue == genericServiceArgument.headers["cookie"]
            lastContactAsText == genericServiceArgument.httpQueryTranslated["lastContact"].toString()
            lastContact == genericServiceArgument.httpQueryTranslated["lastContact"]
            notExistQueryParam == null
            numberAsTextAsNumber == 12
            numberAsText == genericServiceArgument.httpQueryTranslated.numberAsText
            contentType == null
            objectIdAsLong == genericServiceArgument.urlPathParams["objectId"]
            objectIdAsText == genericServiceArgument.urlPathParams["objectId"].toString()
            objectUuid == genericServiceArgument.urlPathParams["objectUuid"]
            notExistPathVar == null
            rawJson == invokerArgs.rawJson
            objectIdWithoutAnnotationParam == genericServiceArgument.urlPathParams["objectId"].toString()
        }
    }

    def "cannot resolve some type or lack of annotation at 3rd parameter"() {
        given:
        NormalSpringService normalSpringService = new NormalSpringService()
        def methodName = "getSamplePersonDtoInvalid2"
        def invokerArgs = createValidInvokerArgs(methodName, normalSpringService)
        def genericServiceArgument = invokerArgs.genericServiceArgument

        when:
        testCase.callMethod(genericServiceArgument)

        then:
        TechnicalException ex = thrown()
        def expectedMessage = [
            "Cannot resolve argument with type: rawClass=pl.jalokim.crudwizard.core.sample.SamplePersonDto,",
            "with annotations: [@org.springframework.validation.annotation.Validated(value={})]",
            "at index: 2",
            "in class: $NormalSpringService.canonicalName",
            "with method name: $methodName",
            "in method : ${findMethodByName(NormalSpringService, methodName)}"
        ].join(System.lineSeparator())
        ex.message == expectedMessage
    }

    // TODO #01 test cases
    // required @RequestHeader not exists
    // required @RequestParam not exists
    // required @RequestBody not exists
    // required @PathVariable not exists
    // conversion problem from json to some dto

    DelegatedServiceMethodInvokerArgs createValidInvokerArgs(String methodName, Object serviceInstance) {
        def args = new DelegatedServiceMethodInvokerArgs()
        def jsonValue = "{\"name\":\"$args.expectedName\",\"surname\":\"$args.expectedSurname\"}".toString()
        args.genericServiceArgument = createInputGenericServiceArgument().toBuilder()
            .httpQueryTranslated(createHttpQueryParamsTranslated())
            .requestBody(jsonObjectMapper.asJsonNode(jsonValue))
            .requestBodyTranslated(args.translatedPayload)
            .urlPathParams([objectId: 15, objectUuid: DataFakerHelper.randomText()])
            .endpointMetaModel(EndpointMetaModel.builder()
                .httpMethod(HttpMethod.POST)
                .responseMetaModel(EndpointResponseMetaModel.builder()
                    .successHttpCode(200)
                    .build())
                .serviceMetaModel(ServiceMetaModel.builder()
                    .serviceInstance(serviceInstance)
                    .methodMetaModel(beanMethodMetaModelCreator.createBeanMethodMetaModel(methodName, serviceInstance.class.canonicalName))
                    .methodName(methodName)
                    .build())
                .build())
            .build()
        args.rawJson = jsonValue
        args
    }

    static class DelegatedServiceMethodInvokerArgs {

        String expectedName = DataFakerHelper.randomText()
        String expectedSurname = DataFakerHelper.randomText()
        String rawJson
        TranslatedPayload translatedPayload = translatedPayload([name: expectedName, surname: expectedSurname])
        GenericServiceArgument genericServiceArgument
    }
}

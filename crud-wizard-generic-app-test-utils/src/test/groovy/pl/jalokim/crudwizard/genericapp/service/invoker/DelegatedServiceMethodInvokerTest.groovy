package pl.jalokim.crudwizard.genericapp.service.invoker

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper
import static pl.jalokim.crudwizard.core.datastorage.RawEntityObjectSamples.createHttpQueryParamsTranslated
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.findMethodByName
import static pl.jalokim.crudwizard.genericapp.metamodel.service.GenericServiceArgumentSamples.createInputGenericServiceArgument
import static pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload.translatedPayload

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ValueConstants
import pl.jalokim.crudwizard.core.exception.TechnicalException
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel
import pl.jalokim.crudwizard.core.metamodels.EndpointResponseMetaModel
import pl.jalokim.crudwizard.core.metamodels.ServiceMetaModel
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.core.utils.ReflectionUtils
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.NormalSpringService
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper
import pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload
import pl.jalokim.utils.test.DataFakerHelper
import spock.lang.Specification
import spock.lang.Unroll

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
            samplePersonDto == new SamplePersonDto(null, invokerArgs.expectedName, invokerArgs.expectedSurname)
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

    @Unroll
    def "return ResponseEntity with #expectedHttpStatus and #expetedBody when invoked method: #methodName and #endpointSuccessHttpCode"() {
        given:
        NormalSpringService normalSpringService = new NormalSpringService()
        def invokerArgs = createValidInvokerArgs(methodName, normalSpringService, endpointSuccessHttpCode, httpMethod)
        def genericServiceArgument = invokerArgs.genericServiceArgument

        when:
        def response = testCase.callMethod(genericServiceArgument)

        then:
        response.statusCode == expectedHttpStatus
        response.body == expetedBody

        where:
        expectedHttpStatus    | expetedBody   | endpointSuccessHttpCode | methodName                    | httpMethod
        HttpStatus.OK         | null          | 200                     | "returnVoid"                  | HttpMethod.POST
        HttpStatus.NO_CONTENT | null          | null                    | "returnVoid"                  | HttpMethod.GET
        HttpStatus.CREATED    | 998           | null                    | "returnInteger"               | HttpMethod.POST
        HttpStatus.OK         | "StringValue" | null                    | "returnString"                | HttpMethod.GET
        HttpStatus.NO_CONTENT | null          | null                    | "returnVoid"                  | HttpMethod.DELETE
        HttpStatus.NO_CONTENT | null          | null                    | "returnVoid"                  | HttpMethod.PUT
        HttpStatus.NO_CONTENT | null          | null                    | "returnVoid"                  | HttpMethod.PATCH
        HttpStatus.CREATED    | 998           | 201                     | "returnInteger"               | HttpMethod.PATCH
        HttpStatus.ACCEPTED   | 998           | 202                     | "returnInteger"               | HttpMethod.POST
        HttpStatus.ACCEPTED   | 998           | 202                     | "returnInteger"               | HttpMethod.POST
        HttpStatus.NOT_FOUND  | false         | 400                     | "returnResponseEntityBoolean" | HttpMethod.POST
    }

    def "cannot convert json node to InvalidJavaBean instance"() {
        given:
        NormalSpringService normalSpringService = new NormalSpringService()
        def methodName = "methodWithInvalidJavaBean"
        def invokerArgs = createValidInvokerArgs(methodName, normalSpringService)
        def genericServiceArgument = invokerArgs.genericServiceArgument

        when:
        testCase.callMethod(genericServiceArgument)

        then:
        TechnicalException ex = thrown()
        ex.message == "Cannot convert from value: '$invokerArgs.rawJson' to class $NormalSpringService.InvalidJavaBean.canonicalName"
        ex.cause.message.contains("Cannot construct instance of `$NormalSpringService.canonicalName\$InvalidJavaBean`")
    }

    @Unroll
    def "inform about lack of required method param"() {
        NormalSpringService normalSpringService = new NormalSpringService()
        def invokerArgs = createValidInvokerArgs(methodName, normalSpringService)
        invokerArgs.genericServiceArgument = invokerArgs.genericServiceArgument.toBuilder()
            .requestBody(null)
            .requestBodyTranslated(null)
            .headers(null)
            .build()

        def genericServiceArgument = invokerArgs.genericServiceArgument

        when:
        testCase.callMethod(genericServiceArgument)

        then:
        TechnicalException ex = thrown()
        ex.message == expectedMsgPart

        where:
        methodName                | expectedMsgPart
        "missingReqRequestHeader" | "Cannot find required header value with header name: someRequiredHeader"
        "missingReqRequestParam"  | "Cannot find required http request parameter with name: someRequiredParam"
        "missingReqRequestBody"   |
            "Argument annotated @org.springframework.web.bind.annotation.RequestBody(required=true) is required at index: 1$System.lineSeparator" +
            "in class: $NormalSpringService.canonicalName$System.lineSeparator" +
            "with method name: missingReqRequestBody$System.lineSeparator" +
            "in method : ${ReflectionUtils.findMethodByName(NormalSpringService, 'missingReqRequestBody')}".toString()
        "missingReqPathVariable"  | "Cannot find required path variable value with name: someRequiredVariable"
        "missingReqRequestAllHeaders"   |
            "Argument annotated @org.springframework.web.bind.annotation.RequestHeader(name=\"\", value=\"\", defaultValue=\"$ValueConstants.DEFAULT_NONE\"," +
            " required=true) is required at index: 1$System.lineSeparator" +
            "in class: $NormalSpringService.canonicalName$System.lineSeparator" +
            "with method name: missingReqRequestAllHeaders$System.lineSeparator" +
            "in method : ${ReflectionUtils.findMethodByName(NormalSpringService, 'missingReqRequestAllHeaders')}".toString()
    }

    DelegatedServiceMethodInvokerArgs createValidInvokerArgs(String methodName, Object serviceInstance,
        Integer successHttpCode = 200, HttpMethod httpMethod = HttpMethod.POST) {
        def args = new DelegatedServiceMethodInvokerArgs()
        def jsonValue = "{\"name\":\"$args.expectedName\",\"surname\":\"$args.expectedSurname\"}".toString()
        args.genericServiceArgument = createInputGenericServiceArgument().toBuilder()
            .httpQueryTranslated(createHttpQueryParamsTranslated())
            .requestBody(jsonObjectMapper.asJsonNode(jsonValue))
            .requestBodyTranslated(args.translatedPayload)
            .urlPathParams([objectId: 15, objectUuid: DataFakerHelper.randomText()])
            .endpointMetaModel(EndpointMetaModel.builder()
                .httpMethod(httpMethod)
                .responseMetaModel(EndpointResponseMetaModel.builder()
                    .successHttpCode(successHttpCode)
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

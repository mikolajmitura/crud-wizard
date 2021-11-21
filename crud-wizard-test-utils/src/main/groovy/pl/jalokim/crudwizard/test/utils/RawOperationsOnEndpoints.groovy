package pl.jalokim.crudwizard.test.utils

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig
import pl.jalokim.crudwizard.core.rest.response.error.ErrorResponseDto
import pl.jalokim.crudwizard.test.utils.rest.EndpointActions

@Component("rawOperationsOnRestController")
class RawOperationsOnEndpoints implements EndpointActions {

    @Autowired
    protected MockMvc client

    private static ObjectMapper objectMapper = ObjectMapperConfig.createObjectMapper()

    ResultActions perform(MockHttpServletRequestBuilder request) {
        return client.perform(request)
    }

    ResultActions performWithJsonContent(MockHttpServletRequestBuilder requestBuilder, content) {
        return perform(requestBuilder.contentType(MediaType.APPLICATION_JSON).content(asJsonString(content)))
    }

    ResultActions performWithJsonContent(MockHttpServletRequestBuilder requestBuilder, content, Map<String, Object> parameters) {
        return performWithParameters(requestBuilder.contentType(MediaType.APPLICATION_JSON).content(asJsonString(content)), parameters)
    }

    Object performAndReturnAsJson(MockHttpServletRequestBuilder requestBuilder) {
        return toJson(performAndReturnAsString(requestBuilder))
    }

    String performAndReturnAsString(MockHttpServletRequestBuilder requestBuilder) {
        perform(requestBuilder).andReturn().response.contentAsString
    }

    long postAndReturnLong(String url, Object payload) {
        def httpResponse = performWithJsonContent(MockMvcRequestBuilders.post(url), payload)
        httpResponse.andExpect(status().isCreated())
        extractResponseAsLong(httpResponse)
    }

    void putPayload(String url, Object payload) {
        def httpResponse = performWithJsonContent(MockMvcRequestBuilders.put(url), payload)
        httpResponse.andExpect(status().isNoContent())
    }

    void delete(String url) {
        def httpResponse = perform(MockMvcRequestBuilders.delete(url))
        httpResponse.andExpect(status().isNoContent())
    }

    List<Map> getAndReturnArrayJson(String url, Map parameters = null) {
        def httpResponse = performQuery(url, parameters)
        httpResponse.andExpect(status().isOk())
        extractResponseAsJsonArray(httpResponse)
    }

    Object getAndReturnJson(String url, Map parameters = null) {
        def httpResponse = performQuery(url, parameters)
        httpResponse.andExpect(status().isOk())
        extractResponseAsJson(httpResponse)
    }

    public <T> T getAndReturnObject(String url, Class<T> returnClass) {
        def httpResponse = performQuery(url)
        httpResponse.andExpect(status().isOk())
        extractResponseAsClass(httpResponse, returnClass)
    }

    static def toJson(String text) {
        return new JsonSlurper().parseText(text)
    }

    static Map extractResponseAsJson(ResultActions response) {
        return toJson(extractResponseAsString(response)) as Map
    }

    static Object extractResponseAsJsonObject(ResultActions response) {
        return toJson(extractResponseAsString(response))
    }

    static String extractResponseAsJsonString(ResultActions response) {
        return toJson(extractResponseAsString(response)) as String
    }

    static String extractResponseAsString(ResultActions response) {
        response.andReturn().response.contentAsString
    }

    static List<Map> extractResponseAsJsonArray(ResultActions response) {
        return toJson(extractResponseAsString(response)) as List
    }

    static <T> List<T> extractResponseAsObjectList(ResultActions response, Class<T> elementTargetClass) {
        extractResponseAsJsonArray(response)
        .collect { objectMapper.convertValue(it, elementTargetClass)}
    }

    static ErrorResponseDto extractErrorResponseDto(ResultActions response) {
        extractResponseAsClass(response, ErrorResponseDto)
    }

    static Long extractResponseAsLong(ResultActions response) {
        return StringUtils.trimToNull(response.andReturn().response.contentAsString) as Long
    }

    static Long extractResponseAsId(ResultActions response) {
        extractResponseAsLong(response)
    }

    static List extractResponseAsPageContent(ResultActions response) {
        return extractResponseAsJson(response).content as List
    }

    static <T> T extractResponseAsClass(ResultActions response, Class<T> valueClass) {
        objectMapper.convertValue(extractResponseAsJson(response), valueClass)
    }

    protected String asJsonString(final obj) {
        try {
            return objectMapper.writeValueAsString(obj)
        } catch (Exception e) {
            throw new RuntimeException(e)
        }
    }
}

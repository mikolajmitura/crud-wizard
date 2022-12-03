package pl.jalokim.crudwizard.test.utils

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import pl.jalokim.crudwizard.core.rest.response.error.ErrorResponseDto

abstract class BaseOperationsOnEndpoints<INPUT> extends RawOperationsOnEndpoints {

    long create(INPUT payload) {
        postAndReturnLong(getEndpointUrl(), payload)
    }

    void update(INPUT payload) {
        putPayload(getEndpointUrl(), payload)
    }

    Map notSuccessCreate(INPUT payload) {
        def httpResponse = performWithJsonContent(MockMvcRequestBuilders.post(getEndpointUrl()), payload)
        httpResponse.andExpect(status().isBadRequest())
        extractResponseAsJson(httpResponse)
    }

    Map notSuccessUpdate(INPUT payload) {
        def httpResponse = performWithJsonContent(MockMvcRequestBuilders.put(getEndpointUrl()), payload)
        httpResponse.andExpect(status().isBadRequest())
        extractResponseAsJson(httpResponse)
    }

    ErrorResponseDto notSuccessCreateGetErrors(INPUT payload) {
        def httpResponse = performWithJsonContent(MockMvcRequestBuilders.post(getEndpointUrl()), payload)
        httpResponse.andExpect(status().isBadRequest())
        extractErrorResponseDto(httpResponse)
    }

    ErrorResponseDto notSuccessUpdateGetErrors(INPUT payload) {
        def httpResponse = performWithJsonContent(MockMvcRequestBuilders.put(getEndpointUrl()), payload)
        httpResponse.andExpect(status().isBadRequest())
        extractErrorResponseDto(httpResponse)
    }

    def <T> T getById(Long id, Class<T> returnClass) {
        getAndReturnObject("${getEndpointUrl()}/$id", returnClass)
    }

    Object getById(Long id) {
        ResultActions httpResponse = performWithParameters(MockMvcRequestBuilders.get("${getEndpointUrl()}/$id"))
        httpResponse.andExpect(status().isOk())
        extractResponseAsJsonObject(httpResponse)
    }

    List<Map> getList(Map queryParams = [:]) {
        getAndReturnArrayJson(getEndpointUrl(), queryParams)
    }

    Object getPage(Map queryParams = [:]) {
        ResultActions httpResponse = performQuery(getEndpointUrl(), queryParams)
        httpResponse.andExpect(status().isOk())
        extractResponseAsJsonObject(httpResponse)
    }

    abstract String getEndpointUrl()
}

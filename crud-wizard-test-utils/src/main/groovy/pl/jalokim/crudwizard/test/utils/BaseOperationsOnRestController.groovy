package pl.jalokim.crudwizard.test.utils

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

abstract class BaseOperationsOnRestController<INPUT> extends RawOperationsOnRestController {

    long create(INPUT payload) {
        postAndReturnLong(getEndpointUrl(), payload)
    }

    void update(INPUT payload) {
        putPayload(getEndpointUrl(), payload)
    }

    public <T> T getById(Long id, Class<T> returnClass) {
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

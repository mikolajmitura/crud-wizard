package pl.jalokim.crudwizard.test.utils.rest

import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

trait EndpointActions {

    abstract ResultActions perform(MockHttpServletRequestBuilder request)

    abstract ResultActions performWithJsonContent(MockHttpServletRequestBuilder requestBuilder, content, Map<String, Object> parameters = null)

    abstract Object performAndReturnAsJson(MockHttpServletRequestBuilder requestBuilder)

    abstract String performAndReturnAsString(MockHttpServletRequestBuilder requestBuilder)

    ResultActions performQuery(String url, Map<String, Object> parameters = null) {
        return performWithParameters(MockMvcRequestBuilders.get(url), parameters)
    }

    ResultActions performWithParameters(MockHttpServletRequestBuilder builder, Map<String, Object> parameters = null) {
        if (parameters != null) {
            parameters.forEach {name, value ->
                if (value != null) {
                    if(value instanceof Collection) {
                        builder.param(name, (String[])value.toArray())
                    } else {
                        builder.param(name, value as String)
                    }
                }
            }
        }
        return perform(builder)
    }

}

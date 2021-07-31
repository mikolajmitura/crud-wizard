package pl.jalokim.crudwizard.genericapp.service.invoker;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument;

@Component
public class DelegatedServiceMethodInvoker {

    public ResponseEntity<Object> invokeMethod(GenericServiceArgument genericServiceArgument) {
        // TODO #03 send to generic service or try invoke methods in provided class service or translate to real dto class and others service arguments
        // arguments which can be resolved:
        // by type EndpointMetaModel
        // by type GenericServiceArgument
        // by type HttpServletRequest from GenericServiceArgument
        // by type HttpServletResponse from GenericServiceArgument
        // by @RequestHeader from headers from GenericServiceArgument only as simple values, as String, Numbers or List<simple>
        // by @RequestParam as Map<String, Object> or real java bean provided from httpQueryTranslated from GenericServiceArgument
        // by @RequestHeader as whole map from headers from GenericServiceArgument
        // by @RequestBody as JsonNode, requestBodyTranslated or real java bean
        // by @PathParam by name from urlPathParams

        // TODO test cases
        // cannot resolve some type or lack of annotation
        return null;
    }
}

package pl.jalokim.crudwizard.genericapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenericServiceDelegator {

    public ResponseEntity<Object> findAndInvokeHttpMethod(GenericServiceArgument genericServiceArgument) {
        HttpMethod httpMethod = HttpMethod.valueOf(genericServiceArgument.getRequest().getMethod());
        System.out.println(genericServiceArgument.getRequest().getRequestURI());
        System.out.println(httpMethod);
        // TODO load from context which endpoint it is and which service class should be used or default one.
        // translate raw map request to map with real classes.
        // validate objects in translated object
        // send to generic service or try invoke methods in provided class service or translate to real dto class and others service arguments
        return null;
    }
}

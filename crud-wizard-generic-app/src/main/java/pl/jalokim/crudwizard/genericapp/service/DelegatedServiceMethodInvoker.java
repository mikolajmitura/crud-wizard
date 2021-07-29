package pl.jalokim.crudwizard.genericapp.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class DelegatedServiceMethodInvoker {

    public ResponseEntity<Object> invokeMethod(GenericServiceArgument genericServiceArgument) {
        // TODO send to generic service or try invoke methods in provided class service or translate to real dto class and others service arguments
        return null;
    }
}

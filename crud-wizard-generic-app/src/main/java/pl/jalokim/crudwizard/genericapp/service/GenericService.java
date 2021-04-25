package pl.jalokim.crudwizard.genericapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenericService {

    public ResponseEntity<Object> findAndInvokeHttpMethod(GenericServiceArgument genericServiceArgument) {
        HttpMethod httpMethod = HttpMethod.valueOf(genericServiceArgument.getRequest().getMethod());
        System.out.println(genericServiceArgument.getRequest().getRequestURI());
        System.out.println(httpMethod);
        return null;
    }
}

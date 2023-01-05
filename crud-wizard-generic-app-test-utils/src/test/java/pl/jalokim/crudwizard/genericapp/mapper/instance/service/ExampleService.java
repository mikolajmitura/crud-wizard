package pl.jalokim.crudwizard.genericapp.mapper.instance.service;

import org.springframework.stereotype.Service;

@Service("serviceForMapper")
public class ExampleService {

    public String concatTexts(String text1, String text2) {
        return text1 + " " + text2;
    }
}

package pl.jalokim.crudwizard.genericapp.mapper.instance;

import java.util.Map;
import org.springframework.web.bind.annotation.RequestHeader;
import pl.jalokim.crudwizard.genericapp.mapper.instance.objects.SomeDocumentSource;
import pl.jalokim.crudwizard.genericapp.mapper.instance.objects.SomeDocumentTarget;

public class SomeDocumentMapper {

    SomeDocumentTarget<String, Long> mapDocument(SomeDocumentSource<String> someDocumentSource, @RequestHeader Map<String, Object> httpHeaders) {
        return new SomeDocumentTarget<>(someDocumentSource.getId().toString(),
            someDocumentSource.getSerialNumber(), Long.parseLong((String) httpHeaders.get("x-user")));
    }
}

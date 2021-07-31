package pl.jalokim.crudwizard.genericapp.service.invoker.sample;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import pl.jalokim.crudwizard.core.sample.SamplePersonDto;

public class NormalSpringServiceImpl {

    public SamplePersonDto getSamplePersonDto(@RequestBody @NotNull JsonNode jsonNode,
        @RequestBody @Validated SamplePersonDto samplePersonDto,
        String argWithoutAnnotation,
        @RequestHeader Map<String, Object> headers,
        @RequestHeader("cookie") @NotBlank String cookieValue) {
        return null;
    }
}

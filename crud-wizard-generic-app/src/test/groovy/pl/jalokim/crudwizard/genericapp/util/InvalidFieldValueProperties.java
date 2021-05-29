package pl.jalokim.crudwizard.genericapp.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("some-example.invalid-field-value")
@Data
public class InvalidFieldValueProperties {
    private Long someLong;
}

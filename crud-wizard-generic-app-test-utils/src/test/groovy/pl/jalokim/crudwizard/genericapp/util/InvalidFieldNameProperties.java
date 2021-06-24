package pl.jalokim.crudwizard.genericapp.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("some-example.invalid-field-name")
@Data
public class InvalidFieldNameProperties {
    private Long someLong;
}

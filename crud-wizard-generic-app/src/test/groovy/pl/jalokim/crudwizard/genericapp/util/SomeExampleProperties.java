package pl.jalokim.crudwizard.genericapp.util;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("some-example.properties")
@Data
public class SomeExampleProperties {

    private String someText;
    private long someRawLong;
    private Long someLong;
    private boolean rawBoolean;
    private Boolean objectBoolean;
    private List<String> simpleList;
    private List<SomeObject> listWithObjects;

    @Data
    public static class SomeObject {
        private Double someDouble;
        private double rawDouble;
        private short rawShort;
        private Short objectShort;
    }
}

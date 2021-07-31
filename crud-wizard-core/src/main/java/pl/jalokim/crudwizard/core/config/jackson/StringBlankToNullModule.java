package pl.jalokim.crudwizard.core.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

public class StringBlankToNullModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    StringBlankToNullModule() {
        addDeserializer(String.class, new StdScalarDeserializer<String>(String.class) {
            @Override
            public String deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
                String textValue = jsonParser.getValueAsString();
                return blankTextToNull(textValue);
            }
        });
    }

    public static String blankTextToNull(String textValue) {
        if (StringUtils.isBlank(textValue)) {
            return null;
        }
        return textValue.trim();
    }
}

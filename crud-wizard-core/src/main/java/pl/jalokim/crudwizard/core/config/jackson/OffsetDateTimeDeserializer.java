package pl.jalokim.crudwizard.core.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.OffsetDateTime;
import pl.jalokim.crudwizard.core.datetime.DateTimeFormatterUtils;

public class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException {
        return DateTimeFormatterUtils.fromTextToOffsetDateTime(p.getCodec().readValue(p, String.class));
    }
}

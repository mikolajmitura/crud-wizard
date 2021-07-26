package pl.jalokim.crudwizard.core.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public class EnumToLowerCaseSerializer extends StdSerializer<Enum> {

    private static final long serialVersionUID = 1L;

    EnumToLowerCaseSerializer() {
        super(Enum.class);
    }

    public static String enumAsLowerCase(Enum<?> enumEntry) {
        return enumEntry.toString().toLowerCase();
    }

    @Override
    public void serialize(Enum value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        jsonGenerator.writeString(enumAsLowerCase(value));
    }

}

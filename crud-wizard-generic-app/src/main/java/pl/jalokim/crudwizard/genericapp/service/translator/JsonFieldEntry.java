package pl.jalokim.crudwizard.genericapp.service.translator;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class JsonFieldEntry {

    String name;
    JsonNode jsonNode;
}

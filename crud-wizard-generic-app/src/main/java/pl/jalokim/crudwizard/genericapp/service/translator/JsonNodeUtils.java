package pl.jalokim.crudwizard.genericapp.service.translator;

import static pl.jalokim.utils.collection.Elements.elements;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonNodeUtils {

    static List<JsonFieldEntry> getFieldsOfObjectNode(ObjectNode objectNode) {
        Iterable<Map.Entry<String, JsonNode>> fieldIterable = () -> objectNode.fields();
        return elements(fieldIterable)
            .map(entry -> new JsonFieldEntry(entry.getKey(), entry.getValue()))
            .asList();
    }
}

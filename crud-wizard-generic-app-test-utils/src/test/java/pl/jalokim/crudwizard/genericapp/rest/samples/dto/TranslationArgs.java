package pl.jalokim.crudwizard.genericapp.rest.samples.dto;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TranslationArgs {

    private String placeholder;
    private List<Object> argumentsByIndexes;
    private Map<String, Object> argumentsByName;
}

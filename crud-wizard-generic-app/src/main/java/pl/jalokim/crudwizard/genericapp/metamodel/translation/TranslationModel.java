package pl.jalokim.crudwizard.genericapp.metamodel.translation;

import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TranslationModel {

    Long translationId;
    String translationKey;
    Map<String, String> translationByCountryCode;
}

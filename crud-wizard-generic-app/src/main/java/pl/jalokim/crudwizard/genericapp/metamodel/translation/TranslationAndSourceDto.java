package pl.jalokim.crudwizard.genericapp.metamodel.translation;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TranslationAndSourceDto {

    String source;
    String translationKey;
    String translationValue;
}

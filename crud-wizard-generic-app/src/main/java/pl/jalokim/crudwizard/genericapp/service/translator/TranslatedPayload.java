package pl.jalokim.crudwizard.genericapp.service.translator;

import lombok.Value;

@Value
public class TranslatedPayload {

    Object realValue;

    public static TranslatedPayload translatedPayload(Object value) {
        return new TranslatedPayload(value);
    }
}

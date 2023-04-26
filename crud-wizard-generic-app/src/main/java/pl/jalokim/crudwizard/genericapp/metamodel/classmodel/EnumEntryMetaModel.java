package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationModel;

@EqualsAndHashCode
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder(toBuilder = true)
public class EnumEntryMetaModel {

    private String name;
    private TranslationModel translation;

    public String getTranslated() {
        return getMessage(translation.getTranslationKey());
    }
}

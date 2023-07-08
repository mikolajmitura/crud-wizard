package pl.jalokim.crudwizard.genericapp.metamodel.translation;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_BLANK;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.crudwizard.core.validation.javax.IdExists;
import pl.jalokim.crudwizard.core.validation.javax.WhenFieldIsInStateThenOthersShould;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.validation.ProvidedAllLanguages;

@Jacksonized
@Builder(toBuilder = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@WhenFieldIsInStateThenOthersShould(whenField = "translationId", is = NULL, thenOthersShould = {
    @FieldShouldWhenOther(field = "translationKey", should = NOT_BLANK),
    @FieldShouldWhenOther(field = "translationByCountryCode", should = NOT_NULL),
})
@WhenFieldIsInStateThenOthersShould(whenField = "translationId", is = NOT_NULL, thenOthersShould = {
    @FieldShouldWhenOther(field = "translationKey", should = NULL),
    @FieldShouldWhenOther(field = "translationByCountryCode", should = NULL),
})
public class TranslationDto {

    @IdExists(entityClass = TranslationEntity.class)
    Long translationId;

    String translationKey;

    @ProvidedAllLanguages
    Map<String, String> translationByCountryCode;
}

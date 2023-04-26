package pl.jalokim.crudwizard.genericapp.metamodel.translation;

import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.jalokim.crudwizard.genericapp.metamodel.EntityWithId;

@EqualsAndHashCode(callSuper = true)
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class TranslationLanguageEntity extends EntityWithId<String> {

    @Id
    private String languageCode;

    private String languageFullName;

    private Boolean enabled;

    @Override
    public String getId() {
        return languageCode;
    }
}

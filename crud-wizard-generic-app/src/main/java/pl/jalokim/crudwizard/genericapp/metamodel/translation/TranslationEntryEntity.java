package pl.jalokim.crudwizard.genericapp.metamodel.translation;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class TranslationEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "translation_id")
    private TranslationEntity translation;

    @ManyToOne
    @JoinColumn(name = "language_id")
    private TranslationLanguageEntity language;

    private String valueOrPlaceholder;

    @Override
    public String toString() {
        return "TranslationEntryEntity{" +
            "id=" + id +
            ", translation=" + (translation == null ? null : translation.getId()) +
            ", language=" + language +
            ", valueOrPlaceholder='" + valueOrPlaceholder + '\'' +
            '}';
    }
}

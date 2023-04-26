package pl.jalokim.crudwizard.genericapp.metamodel.translation;


import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import pl.jalokim.crudwizard.core.jpa.BaseRepository;

public interface TranslationRepository extends BaseRepository<TranslationEntity> {

    @Query("select t from TranslationEntity t where t.translationKey in :translationKeys")
    List<TranslationEntity> findAllByTranslationKey(Set<String> translationKeys);

    TranslationEntity findByTranslationKey(String translationKey);
}

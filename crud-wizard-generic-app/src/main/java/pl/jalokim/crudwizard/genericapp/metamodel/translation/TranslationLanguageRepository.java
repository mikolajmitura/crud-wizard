package pl.jalokim.crudwizard.genericapp.metamodel.translation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslationLanguageRepository extends JpaRepository<TranslationLanguageEntity, String> {

    List<TranslationLanguageEntity> findAllByEnabledIsTrue();
}

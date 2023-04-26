package pl.jalokim.crudwizard.genericapp.translation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import pl.jalokim.crudwizard.core.translations.MessageSourceProvider;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationLanguageService;

@Configuration
public class TranslationFromJpaConfig {

    @Bean
    @Order(0)
    public MessageSourceProvider translationsFromJpaProvider(TranslationLanguageService translationLanguageService) {
        MessageSourceFromDb messageSourceFromDb = new MessageSourceFromDb(translationLanguageService);
        return new MessageSourceProvider(messageSourceFromDb);
    }
}

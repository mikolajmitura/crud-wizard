package pl.jalokim.crudwizard

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pl.jalokim.crudwizard.core.translations.LocaleHolder
import pl.jalokim.crudwizard.core.translations.LocaleService

@TestConfiguration
@SpringBootApplication
class TestsApplicationConfig {

    static void main(String[] args) {
        SpringApplication.run(TestsApplicationConfig.class, args)
    }

    @Bean
    LocaleService testLocaleService() {
        return new LocaleService() {
            @Override
            List<Locale> getAllSupportedLocales() {
                [LocaleHolder.getLocale()]
            }
        }
    }
}

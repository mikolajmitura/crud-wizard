package pl.jalokim.crudwizard

import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.createMessageSourceProvider
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.TEST_APPLICATION_TRANSLATIONS_PATH

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import pl.jalokim.crudwizard.core.AppWizardCoreConfig
import pl.jalokim.crudwizard.core.translations.MessageSourceProvider

@TestConfiguration
@SpringBootApplication
@Import(AppWizardCoreConfig.class)
class TestsApplicationConfig {

    static void main(String[] args) {
        SpringApplication.run(TestsApplicationConfig.class, args)
    }

    @Bean
    MessageSourceProvider testProperties() {
        createMessageSourceProvider(TEST_APPLICATION_TRANSLATIONS_PATH)
    }
}


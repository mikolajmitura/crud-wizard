package pl.jalokim.crudwizard

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import pl.jalokim.crudwizard.core.AppWizardCoreConfig

@TestConfiguration
@SpringBootApplication
@Import(AppWizardCoreConfig.class)
class TestsApplicationConfig {

    static void main(String[] args) {
        SpringApplication.run(TestsApplicationConfig.class, args)
    }
}


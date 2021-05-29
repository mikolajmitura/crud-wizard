package pl.jalokim.crudwizard.maintenance

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import pl.jalokim.crudwizard.core.AppWizardCoreConfig

@TestConfiguration
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan(basePackages = "pl.jalokim.crudwizard")
@Import(AppWizardCoreConfig.class)
class MaintenanceTestsApplicationConfig {

    static void main(String[] args) {
        SpringApplication.run(MaintenanceTestsApplicationConfig.class, args)
    }
}

package pl.jalokim.crudwizard.genericapp;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pl.jalokim.crudwizard.core.AppWizardCoreConfig;
import pl.jalokim.crudwizard.genericapp.provider.DefaultBeansConfiguration;

@Configuration
@ComponentScan("pl.jalokim.crudwizard.genericapp")
@EnableConfigurationProperties(DefaultBeansConfiguration.class)
@Import(AppWizardCoreConfig.class)
public class CrudWizardGenericAppConfig {

}

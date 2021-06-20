package pl.jalokim.crudwizard.genericapp;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pl.jalokim.crudwizard.core.AppWizardCoreConfig;

@Configuration
@ComponentScan("pl.jalokim.crudwizard.genericapp")
@Import(AppWizardCoreConfig.class)
public class CrudWizardGenericAppConfig {

}

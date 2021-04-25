package pl.jalokim.crudwizard.core;

import java.time.Clock;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import pl.jalokim.crudwizard.core.translations.MessageSourceFactory;
import pl.jalokim.crudwizard.core.validation.javax.groups.ValidatorWithDefaultGroupFactoryBean;

@Configuration
@ComponentScan("pl.jalokim.crudwizard.core")
public class AppWizardCoreConfig {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    @Primary
    MessageSource appMessageSource() {
        return MessageSourceFactory.createMessageSource();
    }

    @Bean
    @Primary
    public LocalValidatorFactoryBean validatorWithDefaultGroupFactory(MessageSource messageSource) {
        ValidatorWithDefaultGroupFactoryBean bean = new ValidatorWithDefaultGroupFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }
}

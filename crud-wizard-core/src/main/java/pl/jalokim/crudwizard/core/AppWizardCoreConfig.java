package pl.jalokim.crudwizard.core;

import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.createCommonMessageSource;
import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.createMainMessageSource;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    MessageSource appMessageSource() {
        return MessageSourceFactory.createMessageSource();
    }

    @Bean
    MessageSource commonMessageSource() {
        return createCommonMessageSource();
    }

    @Bean
    @Primary
    MessageSource mainMessageSource(@Qualifier("appMessageSource") @Autowired(required = false) MessageSource appMessageSource,
        @Qualifier("commonMessageSource") MessageSource commonMessageSource) {
        return createMainMessageSource(appMessageSource, commonMessageSource);
    }

    @Bean
    @Primary
    public LocalValidatorFactoryBean validatorWithDefaultGroupFactory(MessageSource messageSource) {
        ValidatorWithDefaultGroupFactoryBean bean = new ValidatorWithDefaultGroupFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }
}

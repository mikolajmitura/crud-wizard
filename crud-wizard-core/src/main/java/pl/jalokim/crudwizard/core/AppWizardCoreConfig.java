package pl.jalokim.crudwizard.core;

import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.createCommonMessageSourceProvider;
import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.createHibernateMessageSourceProvider;
import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.createMessageSourceDelegator;

import java.time.Clock;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import pl.jalokim.crudwizard.core.translations.LocaleService;
import pl.jalokim.crudwizard.core.translations.MessageSourceFactory;
import pl.jalokim.crudwizard.core.translations.MessageSourceProvider;
import pl.jalokim.crudwizard.core.validation.javax.MessageInterpolatorFactory;
import pl.jalokim.crudwizard.core.validation.javax.groups.ValidatorWithDefaultGroupFactoryBean;

@Configuration
@ComponentScan("pl.jalokim.crudwizard.core")
@EnableAspectJAutoProxy
public class AppWizardCoreConfig implements BeanDefinitionRegistryPostProcessor {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    @Order(1)
    MessageSourceProvider appMessageSource() {
        return MessageSourceFactory.createMessageSourceProvider();
    }

    @Bean
    @Order(3)
    MessageSourceProvider commonMessageSource() {
        return createCommonMessageSourceProvider();
    }

    @Bean
    @Order(4)
    MessageSourceProvider hibernateMessageSourceProvider() {
        return createHibernateMessageSourceProvider();
    }

    @Bean
    @Primary
    MessageSource mainMessageSource(List<MessageSourceProvider> messageSourceProviders, LocaleService localeService) {
        return createMessageSourceDelegator(messageSourceProviders, localeService);
    }

    @Bean
    @Primary
    public LocalValidatorFactoryBean validatorWithDefaultGroupFactory(MessageSource messageSource) {
        ValidatorWithDefaultGroupFactoryBean bean = new ValidatorWithDefaultGroupFactoryBean();
        bean.setValidationMessageSource(messageSource);
        bean.setMessageInterpolator(MessageInterpolatorFactory.createMessageInterpolator(messageSource));
        return bean;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        removeBeanDefinitionWhenExists(registry, "jacksonObjectMapper");
        removeBeanDefinitionWhenExists(registry, "defaultValidator");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    private void removeBeanDefinitionWhenExists(BeanDefinitionRegistry registry, String beanName) {
        if (registry.containsBeanDefinition(beanName)) {
            registry.removeBeanDefinition(beanName);
        }
    }
}

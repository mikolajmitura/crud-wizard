package pl.jalokim.crudwizard.genericapp.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

/**
 * It removes default DataSourceProperties bean configured by "spring.datasource"
 */
@Component
public class MetaModelSpringBootCustomizer implements BeanDefinitionRegistryPostProcessor {

    public static final String DEFAULT_DATA_SOURCE_PROPERTIES_BEAN_NAME = "spring.datasource-org.springframework.boot.autoconfigure.jdbc.DataSourceProperties";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        registry.removeBeanDefinition(DEFAULT_DATA_SOURCE_PROPERTIES_BEAN_NAME);
    }

    @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}

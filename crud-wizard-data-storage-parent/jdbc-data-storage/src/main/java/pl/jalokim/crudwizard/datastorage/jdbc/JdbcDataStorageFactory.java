package pl.jalokim.crudwizard.datastorage.jdbc;

import static pl.jalokim.crudwizard.datastorage.jdbc.JdbcDataStorage.JDBC_DATA_STORAGE_NAME;
import static pl.jalokim.crudwizard.datastorage.jdbc.JdbcDataStorageProperties.DEFAULT_DATASOURCE_NAME;

import java.util.Map.Entry;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import pl.jalokim.crudwizard.core.utils.CustomPropertiesResolver;

@Configuration
public class JdbcDataStorageFactory implements BeanDefinitionRegistryPostProcessor, BeanFactoryAware {

    public static final String JDBC_TRANSACTION_MANAGER_NAME = "jdbcDataStorageTransactionManager";
    public static final String DATA_SOURCE_PROPERTIES_NAME = "jdbcDataStorageDataSourceProperties";
    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        JdbcDataStorageProperties jdbcDataStorageProperties = beanFactory.getBean(JdbcDataStorageProperties.class);
        Environment environment = beanFactory.getBean(Environment.class);

        // load into context
        beanFactory.getBean(JdbcDataStorageBeansLazyProvider.class);

        CustomPropertiesResolver.bind(jdbcDataStorageProperties, environment);

        for (Entry<String, JdbcDataStorageDataSourceProperties> dataSourceProperties : jdbcDataStorageProperties.getAllDataSourcesProperties().entrySet()) {
            String dataSourceBeanName = buildBeanName(dataSourceProperties.getKey(), DEFAULT_DATASOURCE_NAME);
            var dataSourceProperty = dataSourceProperties.getValue();
            var dataSourcePropertiesProvider = createDataSourcePropertiesProvider(beanDefinitionRegistry, dataSourceBeanName,
                dataSourceProperty);
            var dataSourceProvider = createDataSourceProvider(beanDefinitionRegistry, dataSourceBeanName, dataSourceProperty);
            createJdbcDataStorageTransactionManager(dataSourceBeanName, beanDefinitionRegistry, dataSourceProvider, dataSourceProperty);
            createJdbcDataStorage(dataSourceBeanName, beanDefinitionRegistry, dataSourcePropertiesProvider, dataSourceProvider, dataSourceProperty);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    private Supplier<JdbcDataStorageDataSourceProperties> createDataSourcePropertiesProvider(BeanDefinitionRegistry beanDefinitionRegistry,
        String dataSourceBeanName, JdbcDataStorageDataSourceProperties dataSourceProperties) {
        String dataSourcePropertiesBeanName = buildBeanName(dataSourceBeanName, DATA_SOURCE_PROPERTIES_NAME);
        Supplier<JdbcDataStorageDataSourceProperties> dataSourcePropertiesProvider;
        if (Boolean.FALSE.equals(dataSourceProperties.getDefaults().getDatasourcePropertiesEnabled())) {
            dataSourcePropertiesProvider = () -> getLazyBeansProvider().getDataSourceProperties(dataSourcePropertiesBeanName);
        } else {
            dataSourcePropertiesProvider = () -> dataSourceProperties;
            BeanDefinition beanDefinition = new RootBeanDefinition(JdbcDataStorageDataSourceProperties.class, dataSourcePropertiesProvider);
            beanDefinitionRegistry.registerBeanDefinition(dataSourcePropertiesBeanName, beanDefinition);
            beanDefinition.setPrimary(dataSourceProperties.isPrimary());
        }
        return dataSourcePropertiesProvider;
    }

    private Supplier<DataSource> createDataSourceProvider(BeanDefinitionRegistry beanDefinitionRegistry, String dataSourceBeanName,
        JdbcDataStorageDataSourceProperties dataSourceProperties) {
        Supplier<DataSource> dataSourceProvider;

        if (Boolean.FALSE.equals(dataSourceProperties.getDefaults().getDatasourceEnabled())) {
            dataSourceProvider = () -> getLazyBeansProvider().getDataSource(dataSourceBeanName);
        } else {
            DataSource dataSource = jdbcDataStorageDataSource(dataSourceProperties);
            dataSourceProvider = () -> dataSource;
            BeanDefinition beanDefinition = new RootBeanDefinition(DataSource.class, dataSourceProvider);
            beanDefinitionRegistry.registerBeanDefinition(dataSourceBeanName, beanDefinition);
            beanDefinition.setPrimary(dataSourceProperties.isPrimary());
        }

        return dataSourceProvider;
    }

    private void createJdbcDataStorageTransactionManager(String dataSourceBeanName,
        BeanDefinitionRegistry beanDefinitionRegistry, Supplier<DataSource> dataSourceProvider,
        JdbcDataStorageDataSourceProperties dataSourceProperties) {

        String transactionManagerBeanName = buildBeanName(dataSourceBeanName, JDBC_TRANSACTION_MANAGER_NAME);

        if (!Boolean.FALSE.equals(dataSourceProperties.getDefaults().getTransactionManagerEnabled())) {
            var platformTransactionManagerLazyContext = new BeanSingletonLazyContext<>(() ->
                jdbcDataStorageTransactionManager(dataSourceProvider.get()));

            BeanDefinition beanDefinition = new RootBeanDefinition(PlatformTransactionManager.class, platformTransactionManagerLazyContext::getBeanInstance);
            beanDefinitionRegistry.registerBeanDefinition(transactionManagerBeanName, beanDefinition);
            beanDefinition.setPrimary(dataSourceProperties.isPrimary());
        }
    }

    public PlatformTransactionManager jdbcDataStorageTransactionManager(DataSource jdbcDataStorageDataSource) {
        return new DataSourceTransactionManager(jdbcDataStorageDataSource);
    }

    public DataSource jdbcDataStorageDataSource(DataSourceProperties jdbcDataStorageDataSourceProperties) {
        return DataSourceBuilder.create()
            .driverClassName(jdbcDataStorageDataSourceProperties.getDriverClassName())
            .url(jdbcDataStorageDataSourceProperties.getUrl())
            .username(jdbcDataStorageDataSourceProperties.getUsername())
            .password(jdbcDataStorageDataSourceProperties.getPassword())
            .build();
    }

    private void createJdbcDataStorage(String dataSourceBeanName,
        BeanDefinitionRegistry beanDefinitionRegistry,
        Supplier<JdbcDataStorageDataSourceProperties> dataSourcePropertiesProvider,
        final Supplier<DataSource> dataSourceProvider,
        JdbcDataStorageDataSourceProperties dataSourceProperties) {

        String jdbcDataStorageBeanName = buildBeanName(dataSourceBeanName, JDBC_DATA_STORAGE_NAME);

        if (!Boolean.FALSE.equals(dataSourceProperties.getDefaults().getJdbcDataStorageEnabled())) {
            var jdbcDataStorageLazyContext = new BeanSingletonLazyContext<>(() ->
                new JdbcDataStorage(dataSourcePropertiesProvider.get(), dataSourceProvider.get()));

            BeanDefinition beanDefinition = new RootBeanDefinition(JdbcDataStorage.class, jdbcDataStorageLazyContext::getBeanInstance);
            beanDefinitionRegistry.registerBeanDefinition(jdbcDataStorageBeanName, beanDefinition);
            beanDefinition.setPrimary(dataSourceProperties.isPrimary());
        }
    }

    private String buildBeanName(String dataSourceBeanName, String anotherBeanName) {
        return dataSourceBeanName.equals(DEFAULT_DATASOURCE_NAME) ? anotherBeanName : dataSourceBeanName + "_" + anotherBeanName;
    }

    private JdbcDataStorageBeansLazyProvider getLazyBeansProvider() {
        return JdbcDataStorageBeansLazyProvider.getReferenceProvider().get();
    }
}

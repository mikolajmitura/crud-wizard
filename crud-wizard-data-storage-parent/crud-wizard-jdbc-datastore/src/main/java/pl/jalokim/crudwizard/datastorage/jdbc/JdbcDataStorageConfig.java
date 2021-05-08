package pl.jalokim.crudwizard.datastorage.jdbc;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ComponentScan("pl.jalokim.crudwizard.datastorage.jdbc")
public class JdbcDataStorageConfig {

    private static final String JDBC_DATA_STORAGE_PREFIX = "crud.wizard.jdbc-data-storage";
    private static final String DEFAULTS_PREFIX = JDBC_DATA_STORAGE_PREFIX + ".defaults";

    @Bean
    @Primary
    @ConditionalOnProperty(value = DEFAULTS_PREFIX + ".transaction-manager.enabled", havingValue = "true", matchIfMissing = true)
    public PlatformTransactionManager jdbcDataStorageTransactionManager(DataSource jdbcDataStorageDataSource) {
        return new DataSourceTransactionManager(jdbcDataStorageDataSource);
    }

    @ConfigurationProperties(prefix = JDBC_DATA_STORAGE_PREFIX + ".datasource")
    @Bean
    @Primary
    @ConditionalOnProperty(value = DEFAULTS_PREFIX + ".datasource-properties.enabled", havingValue = "true", matchIfMissing = true)
    public DataSourceProperties jdbcDataStorageDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(value = DEFAULTS_PREFIX + ".datasource.enabled", havingValue = "true", matchIfMissing = true)
    public DataSource jdbcDataStorageDataSource(DataSourceProperties jdbcDataStorageDataSourceProperties) {
        return DataSourceBuilder.create()
            .driverClassName(jdbcDataStorageDataSourceProperties.getDriverClassName())
            .url(jdbcDataStorageDataSourceProperties.getUrl())
            .username(jdbcDataStorageDataSourceProperties.getUsername())
            .password(jdbcDataStorageDataSourceProperties.getPassword())
            .build();
    }
}

package pl.jalokim.crudwizard.datastorage.jdbc;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Bean
    public PlatformTransactionManager applicationTransactionManager(@Qualifier("applicationDataSource") DataSource applicationDataSource) {
        return new DataSourceTransactionManager(applicationDataSource);
    }

    @ConfigurationProperties(prefix = "crud.wizard.application.datasource")
    @Bean
    @Primary
    public DataSourceProperties applicationDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource applicationDataSource(@Qualifier("applicationDataSourceProperties")
        DataSourceProperties applicationDataSourceProperties) {
        return DataSourceBuilder.create()
            .driverClassName(applicationDataSourceProperties.getDriverClassName())
            .url(applicationDataSourceProperties.getUrl())
            .username(applicationDataSourceProperties.getUsername())
            .password(applicationDataSourceProperties.getPassword())
            .build();
    }
}

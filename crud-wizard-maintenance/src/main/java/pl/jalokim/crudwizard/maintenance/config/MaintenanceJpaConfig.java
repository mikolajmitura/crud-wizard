package pl.jalokim.crudwizard.maintenance.config;

import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
    basePackages = "pl.jalokim.crudwizard.maintenance",
    entityManagerFactoryRef = "maintenanceEntityManagerFactory"
)
public class MaintenanceJpaConfig {

    @Bean
    LocalContainerEntityManagerFactoryBean maintenanceEntityManagerFactory(EntityManagerFactoryBuilder builder,
        @Qualifier("maintenanceDataSource") DataSource maintenanceDataSource) {
        return builder.dataSource(maintenanceDataSource)
            .packages("pl.jalokim.crudwizard.maintenance")
            // TODO to remove in future hibernate.hbm2ddl.auto=update
            // TODO use liqubase instead
            .properties(
                Map.of("hibernate.hbm2ddl.auto", "update")
            )
            .build();
    }

    @Bean
    PlatformTransactionManager maintenanceTransactionManager(
        final @Qualifier("maintenanceEntityManagerFactory") LocalContainerEntityManagerFactoryBean maintenanceEntityManagerFactory
    ) {
        EntityManagerFactory emf = maintenanceEntityManagerFactory.getObject();
        if (emf == null) {
            throw new IllegalArgumentException();
        }
        return new JpaTransactionManager(emf);
    }

    @ConfigurationProperties(prefix = "crud.wizard.maintenance.datasource")
    @Bean
    public DataSourceProperties maintenanceDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource maintenanceDataSource(@Qualifier("maintenanceDataSourceProperties")
        DataSourceProperties maintenanceDataSourceProperties) {
        return DataSourceBuilder.create()
            .driverClassName(maintenanceDataSourceProperties.getDriverClassName())
            .url(maintenanceDataSourceProperties.getUrl())
            .username(maintenanceDataSourceProperties.getUsername())
            .password(maintenanceDataSourceProperties.getPassword())
            .build();
    }
}

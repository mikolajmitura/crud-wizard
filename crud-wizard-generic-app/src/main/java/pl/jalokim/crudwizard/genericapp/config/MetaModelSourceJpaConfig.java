package pl.jalokim.crudwizard.genericapp.config;

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
    basePackages = "pl.jalokim.crudwizard.genericapp",
    transactionManagerRef = "metamodelTransactionManager",
    entityManagerFactoryRef = "metamodelEntityManagerFactory"
)
public class MetaModelSourceJpaConfig {

    public static final String METAMODEL_ENTITY_MANAGER = "metamodelEntityManager";

    @Bean
    LocalContainerEntityManagerFactoryBean metamodelEntityManagerFactory(EntityManagerFactoryBuilder builder,
        @Qualifier("metamodelDataSource") DataSource metamodelDataSource) {
        return builder.dataSource(metamodelDataSource)
            .persistenceUnit(METAMODEL_ENTITY_MANAGER)
            .packages("pl.jalokim.crudwizard")
            .build();
    }

    @Bean
    PlatformTransactionManager metamodelTransactionManager(
        final @Qualifier("metamodelEntityManagerFactory") LocalContainerEntityManagerFactoryBean metamodelEntityManagerFactory
    ) {
        EntityManagerFactory emf = metamodelEntityManagerFactory.getObject();
        if (emf == null) {
            throw new IllegalArgumentException();
        }
        return new JpaTransactionManager(emf);
    }

    @ConfigurationProperties(prefix = "crud.wizard.metamodel.datasource")
    @Bean
    public DataSourceProperties metamodelDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource metamodelDataSource(@Qualifier("metamodelDataSourceProperties") DataSourceProperties metamodelDataSourceProperties) {
        return DataSourceBuilder.create()
            .driverClassName(metamodelDataSourceProperties.getDriverClassName())
            .url(metamodelDataSourceProperties.getUrl())
            .username(metamodelDataSourceProperties.getUsername())
            .password(metamodelDataSourceProperties.getPassword())
            .build();
    }
}

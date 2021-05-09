package pl.jalokim.crudwizard.examples.customized.config;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
    basePackages = "pl.jalokim.crudwizard.examples.customized",
    entityManagerFactoryRef = "localContainerEntityManagerFactory"
)
public class CustomizedAppJpaConfig {

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactory(EntityManagerFactoryBuilder builder,
        DataSource appDataSource) {
        return builder.dataSource(appDataSource)
            .packages("pl.jalokim.crudwizard.examples.customized")
            .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactory) {
        EntityManagerFactory emf = localContainerEntityManagerFactory.getObject();
        if (emf == null) {
            throw new IllegalArgumentException();
        }
        return new JpaTransactionManager(emf);
    }
}

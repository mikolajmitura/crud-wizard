package pl.jalokim.crudwizard.datastorage.jdbc

import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import pl.jalokim.crudwizard.JdbcDStoreTestsApplicationConfig
import pl.jalokim.crudwizard.test.utils.BaseIntegrationSpecification
import pl.jalokim.crudwizard.test.utils.cleaner.DatabaseCleanupListener

@ActiveProfiles(["integration", "primary-injection"])
@SpringBootTest(classes = [JdbcDStoreTestsApplicationConfig], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestExecutionListeners(value = [DatabaseCleanupListener], mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class JdbcDataStoragePrimaryInjectionIT extends BaseIntegrationSpecification {

    @Autowired
    private JdbcDataStorage jdbcDataStorage

    @Autowired
    private JdbcDataStorageProperties jdbcDataStorageProperties

    def "should inject other primary datasource"() {
        when:
        def dataSource = (HikariDataSource) jdbcDataStorage.dataSource

        then:
        dataSource.jdbcUrl.contains("custom-jdbc-url")
    }

    @Configuration
    @Profile("primary-injection")
    static class CustomJpaConfig {

        @Bean
        @Primary
        DataSource primaryJdbcDataStorageDataSource() {
            DataSourceBuilder.create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:mem:custom-jdbc-url:MODE=PostgreSQL")
                .username("sa")
                .password("")
                .build()
        }

        @Bean
        @Primary
        JdbcDataStorageDataSourceProperties jdbcDataStorageDataSourceProperties() {
            new JdbcDataStorageDataSourceProperties()
        }
    }
}

package pl.jalokim.crudwizard.datastorage.jdbc

import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import pl.jalokim.crudwizard.JdbcDStoreTestsApplicationConfig
import pl.jalokim.crudwizard.test.utils.BaseIntegrationSpecification
import pl.jalokim.crudwizard.test.utils.cleaner.DatabaseCleanupListener

@ActiveProfiles("integration")
@SpringBootTest(classes = [JdbcDStoreTestsApplicationConfig], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestExecutionListeners(value = [DatabaseCleanupListener], mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class JdbcDataStorageDefaultInjectionIT extends BaseIntegrationSpecification {

    @Autowired
    private JdbcDataStorage jdbcDataStorage

    @Autowired
    @Qualifier("metamodelDataSource")
    private DataSource metamodelDataSource

    @Autowired
    @Qualifier("jdbcDataStorageDataSource")
    private DataSource jdbcDataStorageDataSource

    def "should inject default datasource under jdbc-data-storage property"() {
        when:
        def dataSource = (HikariDataSource) jdbcDataStorage.dataSource
        def metamodelDataSource = (HikariDataSource) metamodelDataSource
        def jdbcDataStorageDataSource = (HikariDataSource) jdbcDataStorageDataSource

        then:
        dataSource.jdbcUrl.contains("datastorage")
        jdbcDataStorageDataSource.jdbcUrl.contains("datastorage")
        metamodelDataSource.jdbcUrl.contains("metamodeldb")
    }
}

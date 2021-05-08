package pl.jalokim.crudwizard.datastorage.jdbc

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import pl.jalokim.crudwizard.JdbcDStoreTestsApplicationConfig
import pl.jalokim.crudwizard.test.utils.cleaner.DatabaseCleanupListener
import spock.lang.Specification

@ActiveProfiles("integration")
@SpringBootTest(classes = [JdbcDStoreTestsApplicationConfig], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestExecutionListeners(value = [DatabaseCleanupListener], mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class JdbcDataStorageDefaultInjectionIT extends Specification {

    @Autowired
    private JdbcDataStorage jdbcDataStorage

    def "should inject default datasource"() {
        when:
        def dataSource = (HikariDataSource) jdbcDataStorage.dataSource

        then:
        dataSource.jdbcUrl.contains("datastorage")
    }
}

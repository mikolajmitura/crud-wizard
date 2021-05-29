package pl.jalokim.crudwizard.datastorage

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import pl.jalokim.crudwizard.JdbcDStoreTestsApplicationConfig
import pl.jalokim.crudwizard.datastorage.jdbc.JdbcDataStorageProperties
import pl.jalokim.crudwizard.test.utils.BaseIntegrationSpecification
import pl.jalokim.crudwizard.test.utils.cleaner.DatabaseCleanupListener

@ActiveProfiles(["integration", "fewdatasources"])
@SpringBootTest(classes = [JdbcDStoreTestsApplicationConfig], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestExecutionListeners(value = [DatabaseCleanupListener], mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class JdbcDataStoragePropertiesIT extends BaseIntegrationSpecification {

    @Autowired
    private JdbcDataStorageProperties jdbcDataStorageProperties

    def "should bind fields as expected"() {
        when:
        def result = jdbcDataStorageProperties.getAllDataSourcesProperties()

        then:
        result.size() == 3
        jdbcDataStorageProperties != null
        verifyAll(jdbcDataStorageProperties) {
            verifyAll(datasource) {
                verifyAll(defaults) {
                    transactionManagerEnabled == true
                    datasourcePropertiesEnabled == true
                    datasourceEnabled == null
                }
                primary == true
                url == "jdbc:h2:mem:datastorage:MODE=PostgreSQL"
                driverClassName == "org.h2.Driver"
                username == "sa"
                password == ""
            }
            verifyAll(datasourceByName['source1']) {
                url == "jdbc:h2:mem:source1:MODE=PostgreSQL"
                driverClassName == "org.h2.Driver"
                username == "sa1"
                password == ""
            }
            verifyAll(datasourceByName['source2']) {
                url == "jdbc:h2:mem:source2:MODE=PostgreSQL"
                driverClassName == "org.h2.Driver"
                username == "sa2"
                password == "pass"
                verifyAll(defaults) {
                    transactionManagerEnabled == null
                    datasourcePropertiesEnabled == null
                    datasourceEnabled == true
                }
            }
        }
    }
}

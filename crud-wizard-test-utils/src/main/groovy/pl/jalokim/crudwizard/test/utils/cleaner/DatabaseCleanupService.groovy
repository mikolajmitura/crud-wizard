package pl.jalokim.crudwizard.test.utils.cleaner

import com.zaxxer.hikari.HikariDataSource
import javax.annotation.PostConstruct
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class DatabaseCleanupService {

    List<String> skipTables = []

    @Autowired(required = false)
    List<SkipTablesForClean> skipTablesForClean

    @Autowired
    List<DataSource> dataSources

    List<TestDataSourceContext> dataBaseContexts

    @PostConstruct
    void postConstruct() {
        dataBaseContexts = dataSources.collect {
            def jdbcURl = ((HikariDataSource) it).jdbcUrl
            if (jdbcURl.startsWith("jdbc:h2:mem:")) {
                new TestDataSourceContext(it)
            } else {
                throw new IllegalStateException("For test cleanup only H2 in memory is allowed but was used URL: $jdbcURl")
            }
        }
        skipTablesForClean?.forEach {
            skipTables.addAll(it.getSkipTables()*.toLowerCase())
        }
    }

    @EventListener(ContextRefreshedEvent)
    def fetchTablesNames() {
        dataBaseContexts.each { TestDataSourceContext dataBaseContext ->
            List<Map<String, Object>> rows = dataBaseContext.getJdbcTemplate().queryForList("SHOW TABLES")
            rows.each {
                String tableName = (String) it.get("TABLE_NAME")
                if (!skipTables.contains(tableName)) {
                    dataBaseContext.addTable(tableName)
                }
            }
        }
    }

    def cleanupDatabase() {
        dataBaseContexts.each { TestDataSourceContext dataBaseContext ->
            def jdbcTemplate = dataBaseContext.getJdbcTemplate()
            jdbcTemplate.update("SET REFERENTIAL_INTEGRITY FALSE")
            dataBaseContext.getNamesOfTables().each {
                jdbcTemplate.update("DELETE FROM " + it)
            }
            jdbcTemplate.update("SET REFERENTIAL_INTEGRITY TRUE")
        }
    }
}

package pl.jalokim.crudwizard.test.utils.cleaner

import javax.annotation.PostConstruct
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class DatabaseCleanupService {

    def tables = []
    List<String> skipTables = []

    @Autowired(required = false)
    List<SkipTablesForClean> skipTablesForClean

    @Autowired
    List<DataSource> dataSources

    List<DataBaseContext> dataBaseContexts

    @PostConstruct
    void postConstruct() {
        dataBaseContexts = dataSources.collect {
            new DataBaseContext(it)
        }
        skipTablesForClean?.forEach {
            skipTables.addAll(it.getSkipTables()*.toLowerCase())
        }
    }

    @EventListener(ContextRefreshedEvent)
    def fetchTablesNames() {
        dataBaseContexts.each { DataBaseContext dataBaseContext ->
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
        dataBaseContexts.each { DataBaseContext dataBaseContext ->
            def jdbcTemplate = dataBaseContext.getJdbcTemplate()
            jdbcTemplate.update("SET REFERENTIAL_INTEGRITY FALSE")
            dataBaseContext.getNamesOfTables().each {
                jdbcTemplate.update("DELETE FROM " + it)
            }
            jdbcTemplate.update("SET REFERENTIAL_INTEGRITY TRUE")
        }
    }
}

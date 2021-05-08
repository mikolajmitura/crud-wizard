package pl.jalokim.crudwizard.datastorage.jdbc;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.datastorage.DataStorage;

@Component
public class JdbcDataStorage implements DataStorage {

    private final DataSource dataSource;

    public JdbcDataStorage(
        @Qualifier("jdbcDataStorageDataSource") @Autowired(required = false) DataSource jdbcDataStorageDataSource,
        DataSource dataSource) {
        if (jdbcDataStorageDataSource != null) {
            this.dataSource = jdbcDataStorageDataSource;
        } else {
            this.dataSource = dataSource;
        }
    }
}

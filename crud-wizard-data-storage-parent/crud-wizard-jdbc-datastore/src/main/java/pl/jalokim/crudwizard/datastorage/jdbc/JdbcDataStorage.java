package pl.jalokim.crudwizard.datastorage.jdbc;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.datastorage.DataStorage;

@Component
public class JdbcDataStorage implements DataStorage {

    private final DataSource dataSource;

    public JdbcDataStorage(@Qualifier("applicationDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }
}

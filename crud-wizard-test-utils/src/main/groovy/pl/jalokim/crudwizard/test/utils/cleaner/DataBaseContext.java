package pl.jalokim.crudwizard.test.utils.cleaner;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.Getter;
import org.springframework.jdbc.core.JdbcTemplate;

@Getter
public class DataBaseContext {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final List<String> namesOfTables = new ArrayList<>();

    public DataBaseContext(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void addTable(String tableName) {
        this.namesOfTables.add(tableName);
    }

}

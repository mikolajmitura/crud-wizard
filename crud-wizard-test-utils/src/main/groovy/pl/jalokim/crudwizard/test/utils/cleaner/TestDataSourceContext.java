package pl.jalokim.crudwizard.test.utils.cleaner;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.sql.DataSource;
import lombok.Getter;
import org.springframework.jdbc.core.JdbcTemplate;

@Getter
public class TestDataSourceContext {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final List<String> namesOfTables = new CopyOnWriteArrayList<>();

    public TestDataSourceContext(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void addTable(String tableName) {
        this.namesOfTables.add(tableName);
    }

}

package pl.jalokim.crudwizard.datastorage.jdbc;

import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import pl.jalokim.crudwizard.genericapp.datastorage.DataStorage;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.string.StringUtils;

@AllArgsConstructor
public class JdbcDataStorage implements DataStorage {

    public static final String JDBC_DATA_STORAGE_NAME = "jdbcDataStorage";
    private final JdbcDataStorageDataSourceProperties dataSourceProperties;
    private final DataSource dataSource;

    @Override
    public String getName() {
        return StringUtils.concatElementsSkipNulls(Elements.elements(
            dataSourceProperties.getPrefixName(), "-", JDBC_DATA_STORAGE_NAME));
    }
}

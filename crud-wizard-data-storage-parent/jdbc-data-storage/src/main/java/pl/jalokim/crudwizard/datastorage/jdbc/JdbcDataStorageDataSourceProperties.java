package pl.jalokim.crudwizard.datastorage.jdbc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

@Data
@EqualsAndHashCode(callSuper = true)
public class JdbcDataStorageDataSourceProperties extends DataSourceProperties {

    private boolean primary;
    private JdbcDataStorageDefaults defaults;
    private String prefixName;

    public JdbcDataStorageDefaults getDefaults() {
        if (defaults == null) {
            defaults = new JdbcDataStorageDefaults();
        }
        return defaults;
    }

    @Data
    public static class JdbcDataStorageDefaults {

        private Boolean transactionManagerEnabled;
        private Boolean datasourcePropertiesEnabled;
        private Boolean datasourceEnabled;
        private Boolean jdbcDataStorageEnabled;
    }
}

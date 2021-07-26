package pl.jalokim.crudwizard.datastorage.jdbc;

import static pl.jalokim.crudwizard.datastorage.jdbc.JdbcDataStorageProperties.JDBC_DATA_STORAGE_PREFIX;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(JDBC_DATA_STORAGE_PREFIX)
@Data
@ConditionalOnProperty(value = JDBC_DATA_STORAGE_PREFIX + ".default-data-storage-properties", havingValue = "true", matchIfMissing = true)
public class JdbcDataStorageProperties {

    public static final String JDBC_DATA_STORAGE_PREFIX = "crud.wizard.jdbc-data-storage";
    public static final String DEFAULT_DATASOURCE_NAME = "jdbcDataStorageDataSource";

    private JdbcDataStorageDataSourceProperties datasource;

    private Map<String, JdbcDataStorageDataSourceProperties> datasourceByName;

    public Map<String, JdbcDataStorageDataSourceProperties> getAllDataSourcesProperties() {
        Map<String, JdbcDataStorageDataSourceProperties> allDatasourceProperties = new HashMap<>(
            Optional.ofNullable(datasourceByName)
                .orElse(new HashMap<>()));

        allDatasourceProperties.forEach((name, datasourceProperties) ->
            datasourceProperties.setPrefixName(name));

        if (datasource != null) {
            allDatasourceProperties.put(DEFAULT_DATASOURCE_NAME, datasource);
        }

        if (allDatasourceProperties.isEmpty()) {
            throw new IllegalArgumentException("Cannot find any data source properties under properties: " + JDBC_DATA_STORAGE_PREFIX);
        }

        var foundPrimaryDataSources = allDatasourceProperties.values().stream()
            .filter(JdbcDataStorageDataSourceProperties::isPrimary)
            .collect(Collectors.toList());

        if (foundPrimaryDataSources.size() > 1) {
            throw new IllegalArgumentException("Cannot be more then primary datasource under properties: " + JDBC_DATA_STORAGE_PREFIX);
        }

        if (foundPrimaryDataSources.size() == 0 && datasource != null) {
            datasource.setPrimary(true);
        }
        return allDatasourceProperties;
    }

}

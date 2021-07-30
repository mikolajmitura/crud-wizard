package pl.jalokim.crudwizard.datastorage.jdbc;

import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
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

    @Override
    public Object saveEntity(ClassMetaModel classMetaModel, Map<String, Object> entity) {
        return null;
    }

    @Override
    public void deleteEntity(ClassMetaModel classMetaModel, Object idObject) {

    }

    @Override
    public Map<String, Object> getEntityById(ClassMetaModel classMetaModel, Object idObject) {
        return null;
    }

    @Override
    public Page<Map<String, Object>> findPageOfEntity(ClassMetaModel classMetaModel, Pageable pageable, Map<String, Object> queryObject) {
        return null;
    }

    @Override
    public List<Map<String, Object>> findEntities(ClassMetaModel classMetaModel, Map<String, Object> queryObject) {
        return null;
    }

}

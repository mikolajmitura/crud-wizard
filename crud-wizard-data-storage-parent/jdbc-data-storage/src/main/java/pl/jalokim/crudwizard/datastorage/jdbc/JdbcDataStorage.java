package pl.jalokim.crudwizard.datastorage.jdbc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQuery;
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
    public Object saveOrUpdate(ClassMetaModel classMetaModel, Object entity) {
        return null;
    }

    @Override
    public Optional<Object> getOptionalEntityById(ClassMetaModel classMetaModel, Object idObject) {
        return Optional.empty();
    }

    @Override
    public Page<Object> findPageOfEntity(Pageable pageable, DataStorageQuery query) {
        return null;
    }

    @Override
    public List<Object> findEntities(DataStorageQuery query) {
        return null;
    }

    @Override
    public void innerDeleteEntity(ClassMetaModel classMetaModel, Object idObject) {

    }

    @Override
    public void delete(DataStorageQuery query) {

    }

    @Override
    public long count(DataStorageQuery query) {
        return 0;
    }
}

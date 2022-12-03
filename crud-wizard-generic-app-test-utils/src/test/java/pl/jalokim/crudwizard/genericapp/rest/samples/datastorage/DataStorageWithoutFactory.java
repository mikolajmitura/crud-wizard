package pl.jalokim.crudwizard.genericapp.rest.samples.datastorage;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.jalokim.crudwizard.genericapp.datastorage.DataStorage;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQuery;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

public class DataStorageWithoutFactory implements DataStorage {

    @Override
    public String getName() {
        return null;
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

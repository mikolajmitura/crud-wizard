package pl.jalokim.crudwizard.core.datastorage.query;

import java.util.List;
import java.util.function.Predicate;
import lombok.Builder;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

@Builder
public class DataStorageQuery {

    private List<String> selectFromFields;
    private ClassMetaModel selectFrom;
    private Predicate<Object> where;
    private Comparable<Object> sortBy;

}

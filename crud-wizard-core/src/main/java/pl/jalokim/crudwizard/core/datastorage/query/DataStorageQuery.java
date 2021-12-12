package pl.jalokim.crudwizard.core.datastorage.query;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

@Builder
@Getter
@EqualsAndHashCode
public class DataStorageQuery {

    private ClassMetaModel selectFrom;
    private AbstractExpression where;
    private List<OrderPath> sortBy;
    /**
     * It can be JPA query, or other that it can be used in specific data storage implementation.
     */
    private Object dataStoreQueryInstance;

    private String nativeQuery;
    private Map<String, Object> nativeQueryArguments;

    // TODO #25 select (fields) from (classmetamodels with join etc)
}

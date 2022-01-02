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

    // TODO #25 should be select from and should be relation to join other classMetaModel
    //  or join to DataStorageQuery
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

    public static DataStorageQuery buildSelectFromAndWhere(ClassMetaModel selectFrom, AbstractExpression where) {
        return DataStorageQuery.builder()
            .selectFrom(selectFrom)
            .where(where)
            .build();
    }
}

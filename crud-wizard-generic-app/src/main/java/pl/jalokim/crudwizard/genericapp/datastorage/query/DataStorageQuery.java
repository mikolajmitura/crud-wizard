package pl.jalokim.crudwizard.genericapp.datastorage.query;

import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
public class DataStorageQuery {

    // TODO #25 should be select from and should be relation to join other classMetaModel
    //  or join to DataStorageQuery
    private ClassMetaModel selectFrom;
    private AbstractExpression where;

    private Pageable pageable;
    private Sort sortBy;
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

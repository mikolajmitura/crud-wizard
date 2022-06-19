package pl.jalokim.crudwizard.genericapp.datastorage.query.inmemory;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQuery;
import pl.jalokim.utils.collection.Elements;

@Component
@RequiredArgsConstructor
public class InMemoryDsQueryRunner {

    private final InMemoryWhereExpressionTranslator inMemoryWhereExpressionTranslator;
    private final InMemoryOrderByTranslator inMemoryOrderByTranslator;

    @SuppressWarnings("PMD.CloseResource")
    public List<Object> runQuery(Stream<Object> source, DataStorageQuery query) {
        Predicate<Object> objectPredicate = inMemoryWhereExpressionTranslator.translateWhereExpression(query.getWhere());
        Comparator<Object> objectComparator = inMemoryOrderByTranslator.translateSortBy(query);

        Elements<Object> objectElements = elements(source)
            .filter(objectPredicate);

        if (objectComparator != null) {
            objectElements = objectElements.sorted(objectComparator);
        }

        return objectElements.asList();
    }
}

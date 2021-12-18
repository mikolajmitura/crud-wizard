package pl.jalokim.crudwizard.core.datastorage.query.inmemory;

import static pl.jalokim.crudwizard.core.datastorage.query.OrderDirection.DESC;
import static pl.jalokim.crudwizard.core.utils.ValueExtractorFromPath.getValueFromPath;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQuery;
import pl.jalokim.crudwizard.core.datastorage.query.OrderPath;

@Component
public class InMemoryOrderByTranslator {

    public Comparator<Object> translateSortBy(DataStorageQuery query) {
        List<OrderPath> sortByPaths = elements(query.getSortBy()).asList();

        Comparator<Object> currentComparator = null;

        for (OrderPath sortByPath : sortByPaths) {
            if (currentComparator == null) {
                currentComparator = comparatorFromOrderPath(sortByPath);
            } else {
                currentComparator = currentComparator.thenComparing(comparatorFromOrderPath(sortByPath));
            }
        }

        return currentComparator;
    }

    @SuppressWarnings("unchecked")
    Comparator<Object> comparatorFromOrderPath(OrderPath orderPath) {
        Comparator<Object> comparator = Comparator.comparing(object -> (Comparable<Object>) getValueFromPath(object, orderPath.getPath()));
        if (DESC.equals(orderPath.getOrderDirection())) {
            return comparator.reversed();
        }
        return comparator;
    }
}

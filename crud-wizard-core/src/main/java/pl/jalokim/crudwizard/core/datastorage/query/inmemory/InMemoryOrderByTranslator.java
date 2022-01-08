package pl.jalokim.crudwizard.core.datastorage.query.inmemory;

import static pl.jalokim.crudwizard.core.utils.ValueExtractorFromPath.getValueFromPath;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQuery;

@Component
public class InMemoryOrderByTranslator {

    public Comparator<Object> translateSortBy(DataStorageQuery query) {
        Sort sort = Optional.ofNullable(query.getSortBy())
            .orElse(Sort.unsorted());
        List<Sort.Order> sortByPaths = elements(sort.iterator()).asList();

        Comparator<Object> currentComparator = null;

        for (Sort.Order sortByPath : sortByPaths) {
            if (currentComparator == null) {
                currentComparator = comparatorFromOrderPath(sortByPath);
            } else {
                currentComparator = currentComparator.thenComparing(comparatorFromOrderPath(sortByPath));
            }
        }

        return currentComparator;
    }

    @SuppressWarnings("unchecked")
    Comparator<Object> comparatorFromOrderPath(Sort.Order orderPath) {
        Comparator<Object> comparator = Comparator.comparing(object -> (Comparable<Object>) getValueFromPath(object, orderPath.getProperty()));
        if (Sort.Direction.DESC.equals(orderPath.getDirection())) {
            return comparator.reversed();
        }
        return comparator;
    }
}

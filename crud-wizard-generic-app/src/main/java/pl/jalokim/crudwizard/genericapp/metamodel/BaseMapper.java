package pl.jalokim.crudwizard.genericapp.metamodel;

import java.util.Collection;
import java.util.List;
import pl.jalokim.utils.collection.Elements;

/**
 *
 * @param <D> Dto
 * @param <E> Entity
 * @param <M> Metamodel representation in generic application context
 */
public interface BaseMapper<D, E, M> {

    D toDto(E entity);

    E toEntity(D dtoObject);

    M toMetaModel(E entity);

    default List<D> toDtoList(Collection<E> entities) {
        return Elements.elements(entities)
            .map(this::toDto)
            .asList();
    }

    default List<E> toEntities(Collection<D> dtoList) {
        return Elements.elements(dtoList)
            .map(this::toEntity)
            .asList();
    }

    default List<M> toMetaModels(Collection<E> entities) {
        return Elements.elements(entities)
            .map(this::toMetaModel)
            .asList();
    }
}

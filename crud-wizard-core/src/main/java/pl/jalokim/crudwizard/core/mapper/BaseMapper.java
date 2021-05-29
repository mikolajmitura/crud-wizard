package pl.jalokim.crudwizard.core.mapper;

import java.util.Collection;
import java.util.List;
import pl.jalokim.utils.collection.Elements;

@SuppressWarnings("InterfaceTypeParameterName")
public interface BaseMapper<DTO, ENTITY> {

    DTO toDto(ENTITY entity);

    ENTITY toEntity(DTO dtoObject);

    default List<DTO> toDtoList(Collection<ENTITY> entities) {
        return Elements.elements(entities)
            .map(this::toDto)
            .asList();
    }

    default List<ENTITY> toEntities(Collection<DTO> dtoList) {
        return Elements.elements(dtoList)
            .map(this::toEntity)
            .asList();
    }
}

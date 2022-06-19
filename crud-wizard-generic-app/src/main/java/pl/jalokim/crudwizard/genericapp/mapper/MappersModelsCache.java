package pl.jalokim.crudwizard.genericapp.mapper;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.genericapp.metamodel.context.ModelsCache;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel;

public class MappersModelsCache extends ModelsCache<MapperMetaModel> {

    private final Map<String, MapperMetaModel> mappersModelByMapperName = new ConcurrentHashMap<>();

    public MapperMetaModel getMapperMetaModelByName(String mapperName) {
        return Optional.ofNullable(mappersModelByMapperName.get(mapperName))
            .orElseThrow(() -> new TechnicalException(createMessagePlaceholder("MappersModelsCache.not.found.mapper", mapperName)));
    }

    public void setMapperModelWithName(String mapperName, MapperMetaModel mapperMetaModel) {
        mappersModelByMapperName.put(mapperName, mapperMetaModel);
    }
}

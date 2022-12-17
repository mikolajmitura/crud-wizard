package pl.jalokim.crudwizard.genericapp.metamodel.context;

import static pl.jalokim.crudwizard.core.datetime.TimeProviderHolder.getTimeProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.RandomUtils;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel;

@EqualsAndHashCode(callSuper = true)
public class TemporaryMetaModelContext extends MetaModelContext {

    private final ObjectCache<String, ClassMetaModel> classMetaModelsByName = new ObjectCache<>();
    private final ObjectCache<String, MapperMetaModel> mapperMetaModelsByName = new ObjectCache<>();
    private final Map<String, ClassMetaModelDto> classMetaModelDtoDefinitionByName = new HashMap<>();

    @Getter
    private final Long sessionTimestamp;

    @Getter
    private final EndpointMetaModelDto createEndpointMetaModelDto;

    public TemporaryMetaModelContext(Long sessionTimestamp, MetaModelContext metaModelContext, EndpointMetaModelDto createEndpointMetaModelDto) {
        this.createEndpointMetaModelDto = createEndpointMetaModelDto;
        this.sessionTimestamp = sessionTimestamp;
        setDataStorages(metaModelContext.getDataStorages());
        setApiTags(metaModelContext.getApiTags());
        setValidatorMetaModels(metaModelContext.getValidatorMetaModels());
        setClassMetaModels(metaModelContext.getClassMetaModels());
        setMapperMetaModels(metaModelContext.getMapperMetaModels());
        setServiceMetaModels(metaModelContext.getServiceMetaModels());
        setEndpointMetaModels(metaModelContext.getEndpointMetaModels());
        setEndpointMetaModelContextNode(metaModelContext.getEndpointMetaModelContextNode());
        setDefaultServiceMetaModel(metaModelContext.getDefaultServiceMetaModel());
        setDefaultPersistMapperMetaModel(metaModelContext.getDefaultPersistMapperMetaModel());
        setDefaultQueryMapperMetaModel(metaModelContext.getDefaultQueryMapperMetaModel());
        setDefaultFinalMapperMetaModel(metaModelContext.getDefaultFinalMapperMetaModel());
        setDefaultExtractIdMapperMetaModel(metaModelContext.getDefaultExtractIdMapperMetaModel());
        setDefaultDataStorageMetaModel(metaModelContext.getDefaultDataStorageMetaModel());
        setDefaultDataStorageQueryProvider(metaModelContext.getDefaultDataStorageQueryProvider());
        setDefaultDataStorageConnectorMetaModels(metaModelContext.getDefaultDataStorageConnectorMetaModels());
    }

    public TemporaryMetaModelContext(MetaModelContext metaModelContext, EndpointMetaModelDto createEndpointMetaModelDto) {
        this(getTimeProvider().getCurrentTimestamp(), metaModelContext, createEndpointMetaModelDto);
    }

    public ClassMetaModel findClassMetaModelById(Long id) {
        return getClassMetaModels().findById(id);
    }

    public MapperMetaModel findMapperMetaModelById(Long id) {
        return getMapperMetaModels().findById(id);
    }

    @Override
    public ClassMetaModel findClassMetaModelByName(String name) {
        return Optional.ofNullable(classMetaModelsByName.findById(name))
            .orElseGet(() -> super.findClassMetaModelByName(name));
    }

    @Override
    public MapperMetaModel findMapperMetaModelByName(String name) {
        return Optional.ofNullable(mapperMetaModelsByName
            .findById(name))
            .orElseGet(() -> super.findMapperMetaModelByName(name));
    }

    public void putToContext(String name, ClassMetaModel classMetaModel) {
        classMetaModelsByName.put(name, classMetaModel);
        getClassMetaModels().put(generateRandomId(getClassMetaModels()), classMetaModel);
    }

    public void putToContext(String name, MapperMetaModel mapperMetaModel) {
        mapperMetaModelsByName.put(name, mapperMetaModel);
        getMapperMetaModels().put(generateRandomId(getMapperMetaModels()), mapperMetaModel);
        getMapperMetaModels().setMapperModelWithName(name, mapperMetaModel);
    }

    public void putDefinitionOfClassMetaModelDto(ClassMetaModelDto classMetaModelDto) {
        classMetaModelDtoDefinitionByName.put(classMetaModelDto.getName(), classMetaModelDto);
    }

    public List<ClassMetaModelDto> getAllClassMetaModelDtoDefinitions() {
        return new ArrayList<>(classMetaModelDtoDefinitionByName.values());
    }

    public ClassMetaModelDto getClassMetaModelDtoByName(String nullableName) {
        return Optional.ofNullable(nullableName)
            .map(classMetaModelDtoDefinitionByName::get)
            .orElse(null);
    }

    private Long generateRandomId(ModelsCache<?> modelsCache) {
        Long generatedId;
        do {
            generatedId = RandomUtils.nextLong(0, Long.MAX_VALUE) * -1L;
        } while (modelsCache.idExists(generatedId));

        return generatedId;
    }
}

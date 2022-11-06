package pl.jalokim.crudwizard.genericapp.metamodel.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.lang3.RandomUtils;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto;

public class TemporaryMetaModelContext extends MetaModelContext {

    private final ObjectCache<String, ClassMetaModel> classMetaModelsByName = new ObjectCache<>();
    private final ObjectCache<String, MapperMetaModel> mapperMetaModelsByName = new ObjectCache<>();
    private final Map<String, ClassMetaModelDto> classMetaModelDtoDefinitionByName = new HashMap<>();
    private final Map<String, MapperMetaModelDto> mapperModelDtoDefinitionByName = new HashMap<>();

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
        setDefaultMapperMetaModel(metaModelContext.getDefaultMapperMetaModel());
        setDefaultDataStorageMetaModel(metaModelContext.getDefaultDataStorageMetaModel());
        setDefaultDataStorageQueryProvider(metaModelContext.getDefaultDataStorageQueryProvider());
        setDefaultDataStorageConnectorMetaModels(metaModelContext.getDefaultDataStorageConnectorMetaModels());
    }

    public TemporaryMetaModelContext(MetaModelContext metaModelContext, EndpointMetaModelDto createEndpointMetaModelDto) {
        this(System.currentTimeMillis(), metaModelContext, createEndpointMetaModelDto);
    }

    public ClassMetaModel findClassMetaModelById(Long id) {
        return getClassMetaModels().findById(id);
    }

    public MapperMetaModel findMapperMetaModelById(Long id) {
        return getMapperMetaModels().findById(id);
    }

    public ClassMetaModel findClassMetaModelByName(String name) {
        return Optional.ofNullable(classMetaModelsByName
            .findById(name))
            .orElseGet(() -> getClassMetaModels().findOneBy(
                givenClassModel -> name.equals(givenClassModel.getName())));
    }

    public MapperMetaModel findMapperMetaModelByName(String name) {
        return Optional.ofNullable(mapperMetaModelsByName
            .findById(name))
            .orElseGet(() -> getMapperMetaModels().getMappersModelByMapperName().get(name));
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

    public void putDefinitionOfMapperMetaModelDto(MapperMetaModelDto mapperMetaModelDto) {
        mapperModelDtoDefinitionByName.put(mapperMetaModelDto.getMapperName(), mapperMetaModelDto);
    }

    public List<ClassMetaModelDto> getAllClassMetaModelDtoDefinitions() {
        return new ArrayList<>(classMetaModelDtoDefinitionByName.values());
    }

    private Long generateRandomId(ModelsCache<?> modelsCache) {
        Long generatedId;
        do {
            generatedId = RandomUtils.nextLong(0, Long.MAX_VALUE) * -1L;
        } while (modelsCache.idExists(generatedId));

        return generatedId;
    }
}

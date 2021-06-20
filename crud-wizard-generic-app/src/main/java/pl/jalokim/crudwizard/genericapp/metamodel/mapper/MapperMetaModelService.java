package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import static pl.jalokim.utils.collection.CollectionUtils.mapToList;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.MapperMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;

@RequiredArgsConstructor
@MetamodelService
public class MapperMetaModelService {

    private final MapperMetaModelEntityRepository mapperMetaModelEntityRepository;
    private final MapperMetaModelMapper mapperMetaModelMapper;

    public List<MapperMetaModel> findAllMetaModels() {
        return mapToList(mapperMetaModelEntityRepository.findAll(), mapperMetaModelMapper::toFullMetaModel);
    }

    public boolean exists(MapperMetaModelDto mapperMetaModelDto) {
        return mapperMetaModelEntityRepository.existsByBeanNameAndClassNameAndMethodName(mapperMetaModelDto.getBeanName(),
            mapperMetaModelDto.getClassName(), mapperMetaModelDto.getMethodName());
    }

    public Long createNewAndGetId(MapperMetaModelDto mapperMetaModelDto) {
        return mapperMetaModelEntityRepository.persist(mapperMetaModelMapper.toEntity(mapperMetaModelDto))
            .getId();
    }
}

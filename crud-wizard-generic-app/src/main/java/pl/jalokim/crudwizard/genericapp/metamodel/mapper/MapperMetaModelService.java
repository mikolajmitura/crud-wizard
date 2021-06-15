package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;

@RequiredArgsConstructor
@MetamodelService
public class MapperMetaModelService {

    private final MapperMetaModelEntityRepository mapperMetaModelEntityRepository;
    private final MapperMetaModelMapper mapperMetaModelMapper;

    public List<MapperMetaModel> findAll() {
        return mapperMetaModelMapper.toDtoList(mapperMetaModelEntityRepository.findAll());
    }
}

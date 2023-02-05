package pl.jalokim.crudwizard.genericapp.metamodel.datastorage;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfigurationMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntity;

@Mapper(
    config = MapperAsSpringBeanConfig.class,
    uses = {AdditionalPropertyMapper.class, MapperGenerateConfigurationMapper.class})
public abstract class DataStorageMetaModelMapper implements BaseMapper<DataStorageMetaModelDto, DataStorageMetaModelEntity, DataStorageMetaModel> {

    @Autowired
    private DataStorageInstances dataStorageInstances;

    @Override
    @Mapping(target = "dataStorage", ignore = true)
    public abstract DataStorageMetaModel toMetaModel(DataStorageMetaModelEntity entity);

    @Mapping(target = "mapperScript", ignore = true) // TODO #53 remove this after impl
    @Mapping(target = "metamodelDtoType", ignore = true)
    public abstract MapperMetaModelDto toMapperMetaModelDto(MapperMetaModelEntity entity);

    @Mapping(target = "classMetaModelDtoType", ignore = true)
    public abstract ClassMetaModelDto classModelToDto(ClassMetaModelEntity classMetaModelEntity);

    public DataStorageMetaModel toFullMetaModel(DataStorageMetaModelEntity entity) {
        return toMetaModel(entity)
            .toBuilder()
            .dataStorage(dataStorageInstances.findDataStorageOrCreate(entity))
            .build();
    }
}

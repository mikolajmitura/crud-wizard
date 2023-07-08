package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.utils.collection.CollectionUtils.isEmpty;
import static pl.jalokim.utils.string.StringUtils.isNotBlank;

import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.genericapp.metamodel.MetaModelDtoType;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;

@UtilityClass
public class ClassMetaModelsUtils {

    public static boolean isClearRawClassFullDefinition(ClassMetaModelDto classMetaModelDto) {
        return classMetaModelDto.getClassMetaModelDtoType().equals(MetaModelDtoType.DEFINITION) &&
            isEmpty(classMetaModelDto.getGenericTypes()) &&
            isEmpty(classMetaModelDto.getValidators()) &&
            isEmpty(classMetaModelDto.getExtendsFromModels()) &&
            isNotBlank(classMetaModelDto.getClassName());
    }

    public static boolean isClearRawClassFullDefinition(ClassMetaModelEntity classMetaModelEntity) {
        return isEmpty(classMetaModelEntity.getGenericTypes()) &&
            isEmpty(classMetaModelEntity.getValidators()) &&
            isEmpty(classMetaModelEntity.getExtendsFromModels()) &&
            isNotBlank(classMetaModelEntity.getClassName());
    }

    public static boolean isClearRawClassFullDefinition(ClassMetaModel classMetaModel) {
        return isEmpty(classMetaModel.getGenericTypes()) &&
            isEmpty(classMetaModel.getValidators()) &&
            isEmpty(classMetaModel.getExtendsFromModels()) &&
            isNotBlank(classMetaModel.getClassName());
    }
}

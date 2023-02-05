package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.newClassMetaModelOrTheSame;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WritePropertyStrategy;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration;

@UtilityClass
public class TargetClassMetaModelUtils {

    static boolean isElementsType(ClassMetaModel targetFieldClassMetaModel) {
        return targetFieldClassMetaModel.isMapType() || targetFieldClassMetaModel.isListType() ||
            targetFieldClassMetaModel.isSetType() || targetFieldClassMetaModel.isArrayType();
    }

    static List<FieldMetaModel> extractAllTargetFields(WritePropertyStrategy writePropertyStrategy, ClassMetaModel targetMetaModel,
        MapperGenerateConfiguration mapperGenerateConfiguration) {
        return Optional.of(targetMetaModel)
            .map(classMetaModel -> {
                if (classMetaModel.isGenericModel()) {
                    return classMetaModel.fetchAllFields();
                }
                FieldMetaResolverConfiguration fieldMetaResolverForRawTarget = mapperGenerateConfiguration.getFieldMetaResolverForRawTarget();
                return newClassMetaModelOrTheSame(classMetaModel, fieldMetaResolverForRawTarget)
                    .fetchAllWriteFields();
            })
            .orElse(List.of()).stream()
            .sorted(writePropertyStrategy.getFieldSorter())
            .collect(Collectors.toUnmodifiableList());
    }
}

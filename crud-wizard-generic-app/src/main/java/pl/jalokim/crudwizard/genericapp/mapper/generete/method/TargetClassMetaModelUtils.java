package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverFactory.findWriteFieldMetaResolver;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WritePropertyStrategy;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.WriteFieldResolver;

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
                // TODO #62 check that below in necessary maybe add fieldMetaResolverForRawTarget to targetMetaModel and invoke getAllWriteFields()
                FieldMetaResolverConfiguration fieldMetaResolverForRawTarget = mapperGenerateConfiguration.getFieldMetaResolverForRawTarget();
                WriteFieldResolver fieldMetaResolver = findWriteFieldMetaResolver(classMetaModel.getRealClass(), fieldMetaResolverForRawTarget);
                fieldMetaResolver.resolveWriteFields(classMetaModel, fieldMetaResolverForRawTarget);
                return classMetaModel.fetchAllWriteFields();
            })
            .orElse(List.of()).stream()
            .sorted(writePropertyStrategy.getFieldSorter())
            .collect(Collectors.toUnmodifiableList());
    }
}

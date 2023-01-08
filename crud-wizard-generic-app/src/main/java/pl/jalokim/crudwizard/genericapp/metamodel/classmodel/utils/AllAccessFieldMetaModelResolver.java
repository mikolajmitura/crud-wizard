package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.AccessFieldType.READ;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.AccessFieldType.WRITE;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.AccessFieldType.WRITE_READ;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverFactory.findFieldMetaResolver;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.AccessFieldType;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolver;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration;
import pl.jalokim.utils.reflection.TypeMetadata;

public class AllAccessFieldMetaModelResolver {

    public static List<FieldMetaModel> resolveFieldsForCurrentType(TypeMetadata typeMetadata,
        FieldMetaResolverConfiguration... fieldMetaResolverConfigurations) {

        Map<String, List<FieldMetaModel>> resolvedFieldsByName = new LinkedHashMap<>();
        if (!typeMetadata.isSimpleType()) {
            elements(fieldMetaResolverConfigurations)
                .forEach(fieldMetaResolverConfiguration -> {
                    FieldMetaResolver fieldMetaResolver = findFieldMetaResolver(typeMetadata.getRawType(), fieldMetaResolverConfiguration);
                    if (!typeMetadata.getRawType().equals(Object.class) &&
                        !typeMetadata.rawClassIsComingFromJavaApi()) {
                        List<FieldMetaModel> foundFields = fieldMetaResolver.findFields(typeMetadata, fieldMetaResolverConfiguration);
                        for (FieldMetaModel fieldMetaModel : foundFields) {
                            List<FieldMetaModel> fieldMetaModels = resolvedFieldsByName
                                .computeIfAbsent(fieldMetaModel.getFieldName(), key -> new ArrayList<>());
                            fieldMetaModels.add(fieldMetaModel);
                        }
                    }
                });
        }
        return elements(resolvedFieldsByName.values())
            .map(AllAccessFieldMetaModelResolver::mergeFields)
            .asList();
    }

    private static FieldMetaModel mergeFields(List<FieldMetaModel> fieldMetaModels) {
        FieldMetaModel firstFieldMetaModel = elements(fieldMetaModels).getFirst();
        List<FieldMetaModel> otherFieldsMetaModels = elements(fieldMetaModels).skip(1).asList();
        for (FieldMetaModel otherFieldsMetaModel : otherFieldsMetaModels) {
            Set<AccessFieldType> currentAccessTypes = elements(firstFieldMetaModel.getAccessFieldType(),
                otherFieldsMetaModel.getAccessFieldType()).asImmutableSet();

            if (currentAccessTypes.equals(Set.of(READ, WRITE)) || otherFieldsMetaModel.getAccessFieldType() == WRITE_READ) {
                firstFieldMetaModel.setAccessFieldType(WRITE_READ);
            }

            String firstFieldJavaType = firstFieldMetaModel.getFieldType().getJavaGenericTypeInfo();
            String otherFieldJavaType = otherFieldsMetaModel.getFieldType().getJavaGenericTypeInfo();
            if (!firstFieldJavaType.equals(otherFieldJavaType)) {
                throw new IllegalStateException("given fields don't have the same java types, firstFieldJavaType:" +
                    firstFieldJavaType + ", otherFieldJavaType: " + otherFieldJavaType);
            }
        }
        return firstFieldMetaModel;
    }
}

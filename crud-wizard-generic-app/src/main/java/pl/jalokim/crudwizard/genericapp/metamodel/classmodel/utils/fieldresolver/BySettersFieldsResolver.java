package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.methodReturnsVoidAndHasArgumentsSize;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createClassMetaModelFor;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.core.utils.StringCaseUtils;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.ParameterMetadata;
import pl.jalokim.utils.reflection.TypeMetadata;

public class BySettersFieldsResolver implements FieldMetaResolver {

    public static BySettersFieldsResolver INSTANCE = new BySettersFieldsResolver();

    @Override
    public List<FieldMetaModel> findDeclaredFields(TypeMetadata typeMetadata, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        return elements(MetadataReflectionUtils.getAllDeclaredNotStaticMethods(typeMetadata.getRawType()))
            .filter(method -> method.getName().startsWith("set"))
            .filter(MetadataReflectionUtils::isPublicMethod)
            .filter(method -> methodReturnsVoidAndHasArgumentsSize(method, 1))
            .map(typeMetadata::getMetaForMethod)
            .map(methodMetadata -> {
                String fieldName = StringCaseUtils.firstLetterToLowerCase(methodMetadata.getName().substring(3));
                ParameterMetadata parameterMetadata = methodMetadata.getParameters().get(0);

                return (FieldMetaModel) FieldMetaModel.builder()
                    .fieldName(fieldName)
                    .fieldType(createClassMetaModelFor(parameterMetadata.getTypeOfParameter(),
                        fieldMetaResolverConfiguration, fieldName, typeMetadata))
                    .build();
            })
            .asList();
    }

    @Override
    public List<FieldMetaModel> getAllAvailableFieldsForWrite(ClassMetaModel classMetaModel) {
        return classMetaModel.fetchAllFields();
    }
}

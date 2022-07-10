package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.methodReturnsNonVoidAndHasArgumentsSize;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createNotGenericClassMetaModel;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Method;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import pl.jalokim.crudwizard.core.utils.StringCaseUtils;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.TypeMetadata;

@Slf4j
public class ByGettersFieldsResolver implements FieldMetaResolver {

    public static ByGettersFieldsResolver INSTANCE = new ByGettersFieldsResolver();

    @Override
    public List<FieldMetaModel> findDeclaredFields(TypeMetadata typeMetadata, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        return elements(MetadataReflectionUtils.getAllDeclaredNotStaticMethods(typeMetadata.getRawType()))
            .filter(method -> method.getName().startsWith("get"))
            .filter(MetadataReflectionUtils::isPublicMethod)
            .filter(method -> methodReturnsNonVoidAndHasArgumentsSize(method, 0))
            .filter(this::notReturnGroovyMetaClassMethod)
            .filter(method -> {
                try {
                    typeMetadata.getMetaForMethod(method);
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            })
            .map(typeMetadata::getMetaForMethod)
            .map(methodMetadata -> {
                String fieldName = StringCaseUtils.firstLetterToLowerCase(methodMetadata.getName().substring(3));
                return (FieldMetaModel) FieldMetaModel.builder()
                    .fieldName(fieldName)
                    .fieldType(createNotGenericClassMetaModel(methodMetadata.getReturnType(),
                        fieldMetaResolverConfiguration, fieldName, typeMetadata))
                    .build();
            })
            .asList();
    }

    private boolean notReturnGroovyMetaClassMethod(Method method) {
        return !method.getReturnType().getCanonicalName().equals("groovy.lang.MetaClass");
    }

    @Override
    public List<FieldMetaModel> getAllAvailableFieldsForWrite(ClassMetaModel classMetaModel) {
        return classMetaModel.fetchAllFields();
    }
}

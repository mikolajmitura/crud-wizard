package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.TypeMetadata;

@RequiredArgsConstructor
public abstract class GenericModelType {

    @Getter
    protected final ClassMetaModelDtoTempContext context;

    public abstract String getTypeName();

    public abstract TypeMetadata extractTypeMetadata();

    public abstract GenericModelType getFieldTypeByName(String fieldName, GenericModelTypeFactory genericModelTypeFactory);

    public static TypeMetadata typeMetadataByClassName(String rawClassName) {
        return MetadataReflectionUtils.getTypeMetadataFromClass(ClassUtils.loadRealClass(rawClassName));
    }
}

package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ClassMetaModelConstants {

    public static final ClassMetaModel STRING_MODEL = ClassMetaModel.builder()
        .realClass(String.class)
        .build();

    public static final ClassMetaModel MAP_STRING_STRING_MODEL = ClassMetaModel.builder()
        .realClass(Map.class)
        .genericTypes(List.of(STRING_MODEL, STRING_MODEL))
        .build();

    public static final ClassMetaModel MAP_STRING_OBJECT_MODEL = ClassMetaModel.builder()
        .realClass(Map.class)
        .genericTypes(List.of(STRING_MODEL,
            ClassMetaModel.builder()
                .realClass(Object.class)
                .build()
        ))
        .build();
}

package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Type;
import java.util.List;
import lombok.Getter;
import pl.jalokim.utils.collection.CollectionUtils;
import ru.vyarus.java.generics.resolver.context.container.ParameterizedTypeImpl;

public class TypeNameWrapper implements Type {

    @Getter
    private final Type wrappedType;

    public TypeNameWrapper(Type wrappedType) {
        if (wrappedType == null) {
            throw new IllegalStateException("wrappedType is null");
        }
        this.wrappedType = wrappedType;
    }

    /**
     * Class ParameterizedTypeImpl is not show full name of classes.
     */
    @Override
    public String getTypeName() {
        if (wrappedType instanceof ParameterizedTypeImpl) {
            ParameterizedTypeImpl parameterizedType = (ParameterizedTypeImpl) wrappedType;

            List<Type> types = elements(parameterizedType.getActualTypeArguments()).asList();
            String genericParts = "";
            if (CollectionUtils.isNotEmpty(types)) {
                genericParts = "<" + elements(types)
                    .map(TypeNameWrapper::new)
                    .map(TypeNameWrapper::getTypeName)
                    .asConcatText(", ") + ">";
            }

            return new TypeNameWrapper(parameterizedType.getRawType()).getTypeName() + genericParts;
        }
        return wrappedType.getTypeName();
    }

    @Override
    public String toString() {
        return getTypeName();
    }
}

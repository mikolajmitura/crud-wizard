package pl.jalokim.crudwizard.core.metamodels;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.Value;

@Value
public class EnumClassMetaModel {

    public static final String ENUM_VALUES_PREFIX = "_ENUM_VALUES_";

    ClassMetaModel declaredIn;

    public List<String> getEnumValues() {
        String[] enumValuesArray = declaredIn.getPropertyRealValue(ENUM_VALUES_PREFIX);
        return elements(enumValuesArray).asList();
    }

    @Override
    public String toString() {
        return "EnumClassMetaModel{" + "declaredIn=" + declaredIn.getName() + '}';
    }
}

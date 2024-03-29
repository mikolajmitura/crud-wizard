package pl.jalokim.crudwizard.core.validation.javax.utils;

import static pl.jalokim.utils.collection.Elements.elements;

import com.google.common.base.CaseFormat;
import java.lang.reflect.Field;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@UtilityClass
public class TableMetadataExtractor {

    public static String getTableNameFromEntity(Class<?> entityClass) {
        return Optional.ofNullable(entityClass.getAnnotation(Table.class))
            .map(Table::name)
            .orElseGet(() -> {
                String tableNameAsClass = entityClass.getSimpleName()
                    .replace("Entity", "");
                return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, tableNameAsClass);
            });
    }

    public static String getNameOfIdFieldInEntity(Class<?> entityClass) {
        return elements(MetadataReflectionUtils.getAllFields(entityClass))
            .filter(MetadataReflectionUtils::isNotStaticField)
            .filter(field -> field.getAnnotation(Id.class) != null)
            .map(TableMetadataExtractor::getNameOfColumnFromField)
            .findFirst()
            .orElseThrow(() -> new TechnicalException("cannot find id field in entity: " + entityClass.getCanonicalName()));
    }

    public static String getNameOfColumnFromField(Field field) {
        return Optional.ofNullable(field.getAnnotation(Column.class))
            .map(Column::name)
            .orElseGet(() -> CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, field.getName()));
    }
}

package pl.jalokim.crudwizard.core.validation.javax.utils;

import static pl.jalokim.utils.collection.Elements.elements;

import com.google.common.base.CaseFormat;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

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
            .map(field -> Optional.ofNullable(field.getAnnotation(Column.class))
            .map(Column::name)
            .orElseGet(field::getName))
            .findFirst()
            .orElseThrow(() -> new TechnicalException("cannot find id field in entity: " + entityClass.getCanonicalName()));
    }
}

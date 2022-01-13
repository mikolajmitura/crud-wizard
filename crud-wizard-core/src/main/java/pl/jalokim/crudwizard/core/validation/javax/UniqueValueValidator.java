package pl.jalokim.crudwizard.core.validation.javax;

import com.google.common.base.CaseFormat;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Table;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import pl.jalokim.crudwizard.core.utils.ValueExtractorFromPath;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@RequiredArgsConstructor
public class UniqueValueValidator implements BaseConstraintValidatorWithDynamicMessage<UniqueValue, String> {

    public static final String SELECT_FORMAT = "select count(%s) from %s where %s = '%s'";
    private final JdbcTemplate jdbcTemplate;

    private String tableName;
    private String entityFieldName;
    private Class<?> entityClass;

    @Override
    public void initialize(UniqueValue constraintAnnotation) {
        entityFieldName = StringUtils.isNotBlank(constraintAnnotation.entityFieldName()) ? constraintAnnotation.entityFieldName() : null;
        entityClass = constraintAnnotation.entityClass();
        tableName = Optional.ofNullable(constraintAnnotation.entityClass().getAnnotation(Table.class))
            .map(Table::name)
            .orElseGet(() -> {
                String tableNameAsClass = constraintAnnotation.entityClass().getSimpleName()
                    .replace("Entity", "");
                return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, tableNameAsClass);
            });
    }

    @Override
    public boolean isValidValue(String value, ConstraintValidatorContext context) {
        String fieldName = Optional.ofNullable(entityFieldName)
            .orElseGet(() -> ValueExtractorFromPath.getValueFromPath(context, "basePath.currentLeafNode").toString());

        String columnName = Optional.ofNullable(MetadataReflectionUtils.getField(entityClass, fieldName).getAnnotation(Column.class))
            .map(Column::name)
            .orElseGet(() -> CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, fieldName));

        return fetchSqlCountValue(value, columnName) == 0;
    }

    private Integer fetchSqlCountValue(String value, String columnName) {
        return jdbcTemplate.queryForObject(String.format(SELECT_FORMAT,
            columnName, tableName, columnName, value), Integer.class);
    }
}

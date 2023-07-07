package pl.jalokim.crudwizard.core.validation.javax;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage.buildMessageForValidator;
import static pl.jalokim.crudwizard.core.validation.javax.utils.TableMetadataExtractor.getNameOfColumnFromField;
import static pl.jalokim.crudwizard.core.validation.javax.utils.TableMetadataExtractor.getTableNameFromEntity;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.getValueOfField;

import java.util.Optional;
import java.util.function.Predicate;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;
import pl.jalokim.crudwizard.core.utils.InstanceLoader;
import pl.jalokim.crudwizard.core.utils.ValueExtractorFromPath;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@RequiredArgsConstructor
public class UniqueValueValidator implements BaseConstraintValidator<UniqueValue, Object> {

    public static final String SELECT_FORMAT = "select count(%s) from %s where %s = '%s'";
    private final JdbcTemplate jdbcTemplate;

    private String tableName;
    private String entityFieldName;
    private String forField;
    private Class<?> entityClass;
    private Predicate<Object> validateOnlyWhenPredicate;

    @Override
    public void initialize(UniqueValue constraintAnnotation) {
        entityFieldName = StringUtils.isNotBlank(constraintAnnotation.entityFieldName()) ? constraintAnnotation.entityFieldName() : null;
        entityClass = constraintAnnotation.entityClass();
        forField = StringUtils.isBlank(constraintAnnotation.forField()) ? null : constraintAnnotation.forField();
        tableName = getTableNameFromEntity(constraintAnnotation.entityClass());
        validateOnlyWhenPredicate = InstanceLoader.getInstance().createInstanceOrGetBean(constraintAnnotation.runOnlyWhen());
    }

    @Override
    public boolean isValidValue(Object value, ConstraintValidatorContext context) {
        if (validateOnlyWhenPredicate.test(value)) {
            MessagePlaceholder messagePlaceholder = createMessagePlaceholder(buildMessageForValidator(UniqueValue.class));
            Object valueToCheck = value;
            if (StringUtils.isNotBlank(forField)) {
                valueToCheck = getValueOfField(valueToCheck, forField);
                if (valueToCheck == null) {
                    return true;
                }
                customMessage(context, messagePlaceholder, forField);
            } else {
                customMessage(context, messagePlaceholder);
            }

            String fieldName = Optional.ofNullable(entityFieldName)
                .orElseGet(() -> Optional.ofNullable(forField)
                    .orElseGet(() -> ValueExtractorFromPath.getValueFromPath(context, "basePath.currentLeafNode").toString())
                );
            String columnName = getNameOfColumnFromField(MetadataReflectionUtils.getField(entityClass, fieldName));
            return fetchSqlCountValue(valueToCheck, columnName) == 0;
        }
        return true;
    }

    private Integer fetchSqlCountValue(Object value, String columnName) {
        return jdbcTemplate.queryForObject(String.format(SELECT_FORMAT,
            columnName, tableName, columnName, value.toString()), Integer.class);
    }
}

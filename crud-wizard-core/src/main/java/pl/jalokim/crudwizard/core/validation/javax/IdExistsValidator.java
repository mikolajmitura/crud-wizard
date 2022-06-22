package pl.jalokim.crudwizard.core.validation.javax;

import static pl.jalokim.crudwizard.core.exception.EntityNotFoundException.EXCEPTION_CONCRETE_MESSAGE_PROPERTY_KEY;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.validation.javax.utils.TableMetadataExtractor.getNameOfIdFieldInEntity;
import static pl.jalokim.crudwizard.core.validation.javax.utils.TableMetadataExtractor.getTableNameFromEntity;

import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;

// TODO #1 create test for that ExistsIdValidator
@RequiredArgsConstructor
public class IdExistsValidator implements BaseConstraintValidator<IdExists, Object> {

    public static final String SELECT_FORMAT = "select count(%s) from %s where %s = %s";
    private final JdbcTemplate jdbcTemplate;

    private String tableName;
    private String idColumnName;

    @Override
    public void initialize(IdExists constraintAnnotation) {
        Class<?> entityClass = constraintAnnotation.entityClass();
        tableName = getTableNameFromEntity(entityClass);
        idColumnName = getNameOfIdFieldInEntity(entityClass);
    }

    @Override
    public boolean isValidValue(Object idValue, ConstraintValidatorContext context) {
        customMessage(context, createMessagePlaceholder(EXCEPTION_CONCRETE_MESSAGE_PROPERTY_KEY,
            idValue, tableName));
        return fetchSqlCountValue(idValue, idColumnName) == 1;
    }

    private Integer fetchSqlCountValue(Object idValue, String idColumnName) {
        Object wrappedValue = idValue instanceof String ?
            StringUtils.wrap((String) idValue, '\'') : idValue;

        return jdbcTemplate.queryForObject(String.format(SELECT_FORMAT,
            idColumnName, tableName, idColumnName, wrappedValue), Integer.class);
    }
}

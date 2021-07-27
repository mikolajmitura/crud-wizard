package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsPlaceholder;
import static pl.jalokim.crudwizard.core.utils.ElementsUtils.nullableElements;
import static pl.jalokim.crudwizard.core.utils.NullableHelper.helpWithNulls;

import java.util.Map;
import javax.validation.ConstraintValidatorContext;
import pl.jalokim.crudwizard.core.metamodels.url.UrlMetamodel;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.url.BaseUrlModelResolver;

public class PathParamsAndUrlValidator implements BaseConstraintValidatorWithDynamicMessage<PathParamsAndUrl, EndpointMetaModelDto> {

    @Override
    public boolean isValid(EndpointMetaModelDto value, ConstraintValidatorContext context) {
        return isValidWithoutCustomMessage(value, context);
    }

    @Override
    public boolean isValidValue(EndpointMetaModelDto endpointMetaModelDto, ConstraintValidatorContext context) {
        var baseUrl = endpointMetaModelDto.getBaseUrl();
        var pathParams = endpointMetaModelDto.getPathParams();
        if (nonNull(baseUrl)) {
            return isValidValue(endpointMetaModelDto, context, baseUrl, pathParams);
        }
        return true;
    }

    private boolean isValidValue(EndpointMetaModelDto endpointMetaModelDto, ConstraintValidatorContext context, String baseUrl, ClassMetaModelDto pathParams) {
        UrlMetamodel urlMetamodel = BaseUrlModelResolver.resolveUrl(baseUrl);
        var pathVariablesNames = nullableElements(urlMetamodel.getPathVariablesNames()).asSet();

        if (isNotEmpty(pathVariablesNames) && nonNull(pathParams.getFields())) {
            boolean allHaveClassName = nullableElements(pathParams.getFields())
                .map(FieldMetaModelDto::getFieldType)
                .map(ClassMetaModelDto::getClassName)
                .allMatch(className -> nonNull(className) && (
                    String.class.getCanonicalName().equals(className)
                        || Long.class.getCanonicalName().equals(className))
                );

            if (!allHaveClassName) {
                customMessage(context, wrapAsPlaceholder(PathParamsAndUrl.class, "allFieldsShouldHasClassName"));
                return false;
            }
            setupCustomMessage(endpointMetaModelDto, context);
            var fieldsNames = nullableElements(pathParams.getFields())
                .map(FieldMetaModelDto::getFieldName)
                .asSet();

            return pathVariablesNames.equals(fieldsNames);
        }
        return true;
    }

    @Override
    public Map<String, Object> messagePlaceholderArgs(EndpointMetaModelDto endpointMetaModelDto, ConstraintValidatorContext context) {
        return Map.of(
            "baseUrl", ofNullable(endpointMetaModelDto.getBaseUrl())
            .orElse(EMPTY),
            "fieldNames", nullableElements(helpWithNulls(() -> endpointMetaModelDto.getPathParams().getFields()))
                .map(FieldMetaModelDto::getFieldName)
                .asConcatText(", ")
        );
    }
}

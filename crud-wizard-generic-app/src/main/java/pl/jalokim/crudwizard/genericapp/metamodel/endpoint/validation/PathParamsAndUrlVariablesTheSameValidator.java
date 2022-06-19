package pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsPlaceholder;
import static pl.jalokim.crudwizard.core.utils.NullableHelper.helpWithNulls;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintValidatorContext;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlMetamodel;
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlModelResolver;

public class PathParamsAndUrlVariablesTheSameValidator
    implements BaseConstraintValidatorWithDynamicMessage<PathParamsAndUrlVariablesTheSame, EndpointMetaModelDto> {

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

    private boolean isValidValue(EndpointMetaModelDto endpointMetaModelDto, ConstraintValidatorContext context,
        String baseUrl, ClassMetaModelDto pathParams) {
        UrlMetamodel urlMetamodel = UrlModelResolver.resolveUrl(baseUrl);
        var pathVariablesNames = elements(urlMetamodel.getPathVariablesNames()).asList();

        var pathParamsFields = elements(
            ofNullable(pathParams)
                .map(ClassMetaModelDto::getFields)
                .orElse(List.of()))
            .asSet();

        if (isNotEmpty(pathVariablesNames) || isNotEmpty(pathParamsFields)) {
            boolean allHaveClassName = elements(pathParamsFields)
                .map(FieldMetaModelDto::getFieldType)
                .map(ClassMetaModelDto::getClassName)
                .allMatch(className -> nonNull(className) && (
                    String.class.getCanonicalName().equals(className)
                        || Long.class.getCanonicalName().equals(className))
                );

            if (!allHaveClassName) {
                customMessage(context, wrapAsPlaceholder(PathParamsAndUrlVariablesTheSame.class, "allFieldsShouldHasClassName"));
                return false;
            }
            setupCustomMessage(endpointMetaModelDto, context);
            var fieldsNames = elements(pathParamsFields)
                .map(FieldMetaModelDto::getFieldName)
                .asSet();

            return new HashSet<>(pathVariablesNames).equals(fieldsNames);
        }
        return true;
    }

    @Override
    public Map<String, Object> messagePlaceholderArgs(EndpointMetaModelDto endpointMetaModelDto, ConstraintValidatorContext context) {
        return Map.of(
            "baseUrl", ofNullable(endpointMetaModelDto.getBaseUrl())
                .orElse(EMPTY),
            "fieldName", MessagePlaceholder.wrapAsExternalPlaceholder("pathParams"),
            "fieldNames", elements(helpWithNulls(() -> endpointMetaModelDto.getPathParams().getFields()))
                .map(FieldMetaModelDto::getFieldName)
                .asConcatText(", ")
        );
    }
}

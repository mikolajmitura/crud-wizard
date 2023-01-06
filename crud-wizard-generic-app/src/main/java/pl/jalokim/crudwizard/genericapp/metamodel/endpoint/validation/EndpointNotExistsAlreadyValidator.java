package pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import pl.jalokim.crudwizard.core.translations.AppMessageSource;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage;
import pl.jalokim.crudwizard.genericapp.metamodel.context.EndpointMetaModelContextNodeUtils;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlMetamodel;
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlModelResolver;
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlPart;
import pl.jalokim.utils.collection.CollectionUtils;

@Component
@RequiredArgsConstructor
public class EndpointNotExistsAlreadyValidator implements BaseConstraintValidatorWithDynamicMessage<EndpointNotExistsAlready, EndpointMetaModelDto> {

    private final ApplicationContext applicationContext;
    private final EndpointMetaModelContextNodeUtils endpointMetaModelContextNodeUtils;

    @Override
    public boolean isValid(EndpointMetaModelDto value, ConstraintValidatorContext context) {
        return isValidWithoutCustomMessage(value, context);
    }

    @Override
    public boolean isValidValue(EndpointMetaModelDto endpointMetaModelDto, ConstraintValidatorContext context) {
        if (endpointMetaModelDto.getBaseUrl() != null && endpointMetaModelDto.getHttpMethod() != null) {
            return endpointNotDuplicated(new EndpointValidationContext(context, endpointMetaModelDto));
        }
        return true;
    }

    private boolean endpointNotDuplicated(EndpointValidationContext validationContext) {
        verifyInEndpointMetaModelContext(validationContext);
        verifyInSpringRestControllers(validationContext);
        return validationContext.notFound.get();
    }

    private void verifyInEndpointMetaModelContext(EndpointValidationContext validationContext) {
        var newUrl = validationContext.getNewUrlMetamodel().getRawUrl();
        var newHttpMethod = validationContext.getNewHttpMethod();
        var foundEndpointMetaModel = endpointMetaModelContextNodeUtils.findEndpointMetaModelByUrlDuringCreate(newUrl, newHttpMethod);

        if (foundEndpointMetaModel != null) {
            customMessage(validationContext.getContext(), MessagePlaceholder.createMessagePlaceholder(
                AppMessageSource.buildPropertyKey(EndpointNotExistsAlready.class, "crudWizardController"), Map.of(
                    "url", validationContext.getEndpointMetaModelDto().getBaseUrl(),
                    "httpMethod", validationContext.getNewHttpMethod().toString(),
                    "foundUrl", foundEndpointMetaModel.getUrlMetamodel().getRawUrl(),
                    "foundOperationName", foundEndpointMetaModel.getOperationName()
                )
            ));
        }
        validationContext.getNotFound().set(foundEndpointMetaModel == null);
    }

    private void verifyInSpringRestControllers(EndpointValidationContext validationContext) {
        var requestMappingHandlerMapping = applicationContext
            .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        var methodsByRestController = requestMappingHandlerMapping
            .getHandlerMethods();

        methodsByRestController.forEach((key, value) ->
            verifyExistenceInSpringContext(validationContext, key, value)
        );
    }

    private void verifyExistenceInSpringContext(EndpointValidationContext validationContext, RequestMappingInfo key, HandlerMethod value) {
        var currentHttpMethods = elements(key.getMethodsCondition().getMethods())
            .map(RequestMethod::toString)
            .asSet();

        List<UrlPart> newUrlParts = validationContext.getNewUrlParts();
        HttpMethod newHttpMethod = validationContext.getNewHttpMethod();
        if (currentHttpMethods.contains(newHttpMethod.toString())) {
            for (String pattern : key.getPatternsCondition().getPatterns()) {
                UrlMetamodel currentRestControllerUrlModel = UrlModelResolver.resolveUrl(pattern);
                List<UrlPart> currentUrlParts = currentRestControllerUrlModel.getUrlParts();
                if (newUrlParts.size() == currentRestControllerUrlModel.getUrlParts().size()) {
                    verifyTheUrlIsTheSame(validationContext, value, newUrlParts, currentUrlParts);
                }
            }
        }
    }

    private void verifyTheUrlIsTheSame(EndpointValidationContext validationContext, HandlerMethod value,
        List<UrlPart> newUrlParts, List<UrlPart> currentUrlParts) {
        for (int i = 0; i < newUrlParts.size(); i++) {
            UrlPart newUrlPart = newUrlParts.get(i);
            UrlPart currentUrlPart = currentUrlParts.get(i);

            boolean theSameUrlPart = newUrlPart.isPathVariable() && currentUrlPart.isPathVariable() ||
                newUrlPart.getOriginalValue().equals(currentUrlPart.getOriginalValue());

            if (!theSameUrlPart) {
                break;
            }

            if (CollectionUtils.isLastIndex(newUrlParts, i)) {
                customMessage(validationContext.getContext(), MessagePlaceholder.createMessagePlaceholder(
                    AppMessageSource.buildPropertyKey(EndpointNotExistsAlready.class, "springRestController"), Map.of(
                        "url", validationContext.getEndpointMetaModelDto().getBaseUrl(),
                        "httpMethod", validationContext.getNewHttpMethod().toString(),
                        "restClassAndMethod", value.toString()
                    )
                ));

                validationContext.getNotFound().set(false);
            }
        }
    }

    @Value
    private static class EndpointValidationContext {

        AtomicBoolean notFound = new AtomicBoolean(true);
        ConstraintValidatorContext context;
        EndpointMetaModelDto endpointMetaModelDto;
        UrlMetamodel newUrlMetamodel;
        HttpMethod newHttpMethod;
        List<UrlPart> newUrlParts;

        public EndpointValidationContext(ConstraintValidatorContext context, EndpointMetaModelDto endpointMetaModelDto) {
            this.context = context;
            this.endpointMetaModelDto = endpointMetaModelDto;
            newUrlMetamodel = UrlModelResolver.resolveUrl(endpointMetaModelDto.getBaseUrl());
            newHttpMethod = endpointMetaModelDto.getHttpMethod();
            newUrlParts = newUrlMetamodel.getUrlParts();
        }
    }
}

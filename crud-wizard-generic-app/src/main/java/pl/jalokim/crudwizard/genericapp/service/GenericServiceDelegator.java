package pl.jalokim.crudwizard.genericapp.service;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.genericapp.metamodel.context.EndpointMetaModelContextNodeUtils;
import pl.jalokim.crudwizard.genericapp.service.invoker.DelegatedServiceMethodInvoker;
import pl.jalokim.crudwizard.genericapp.service.translator.RawEntityObjectTranslator;
import pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;
import pl.jalokim.crudwizard.genericapp.validation.generic.GenericValidator;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenericServiceDelegator {

    private final DelegatedServiceMethodInvoker delegatedServiceMethodInvoker;
    private final EndpointMetaModelContextNodeUtils endpointMetaModelContextNodeUtils;
    private final RawEntityObjectTranslator rawEntityObjectTranslator;
    private final GenericValidator genericValidator;

    public ResponseEntity<Object> findAndInvokeHttpMethod(GenericServiceArgument genericServiceArgument) {
        var newGenericServiceArgument = searchForEndpointByRequest(genericServiceArgument);
        var foundEndpoint = newGenericServiceArgument.getEndpointMetaModel();

        ValidationSessionContext validationContext = new GenericServiceValidationSessionContext();
        newGenericServiceArgument = newGenericServiceArgument.toBuilder()
            .httpQueryTranslated(rawEntityObjectTranslator.translateToRealObjects(
                genericServiceArgument.getHttpQueryParams(), foundEndpoint.getQueryArguments()))
            .requestBodyTranslated(TranslatedPayload.translatedPayload(rawEntityObjectTranslator.translateToRealObjects(
                genericServiceArgument.getRequestBody(), foundEndpoint.getPayloadMetamodel())))
            .validationContext(validationContext)
            .build();

        genericValidator.validate(newGenericServiceArgument.getHttpQueryTranslated(), foundEndpoint.getQueryArguments());
        if (Optional.ofNullable(foundEndpoint.getInvokeValidation()).orElse(true)) {
            genericValidator.validate(newGenericServiceArgument.getRequestBodyTranslated().getRealValue(),
                foundEndpoint.getPayloadMetamodel(), foundEndpoint.getPayloadMetamodelAdditionalValidators());
        }

        ResponseEntity<Object> methodInvocationResult = delegatedServiceMethodInvoker.callMethod(newGenericServiceArgument);
        validationContext.throwExceptionWhenErrorsOccurred();

        return methodInvocationResult;
    }

    private GenericServiceArgument searchForEndpointByRequest(GenericServiceArgument genericServiceArgument) {
        var httpMethod = HttpMethod.valueOf(genericServiceArgument.getRequest().getMethod());
        var requestedUrl = genericServiceArgument.getRequest().getRequestURI();
        var foundEndpointInfo = endpointMetaModelContextNodeUtils.findEndpointByUrl(requestedUrl, httpMethod);

        if (foundEndpointInfo.isNotFound()) {
            throw new EntityNotFoundException(createMessagePlaceholder("error.url.not.found", requestedUrl));
        }

        return genericServiceArgument.toBuilder()
            .endpointMetaModel(foundEndpointInfo.getEndpointMetaModel())
            .urlPathParams(foundEndpointInfo.getUrlPathParams())
            .build();
    }
}

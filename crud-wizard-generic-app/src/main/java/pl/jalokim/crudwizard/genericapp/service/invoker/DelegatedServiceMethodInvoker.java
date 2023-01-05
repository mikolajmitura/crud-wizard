package pl.jalokim.crudwizard.genericapp.service.invoker;

import static pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgumentConfig.SERVICE_EXPECTED_ARGS_TYPE;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointResponseMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.EndpointQueryAndUrlMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgument;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgumentProvider;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericServiceArgumentMethodProvider;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModel;
import pl.jalokim.crudwizard.genericapp.method.AbstractMethodInvoker;
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

@Component
@RequiredArgsConstructor
@SuppressWarnings({"PMD.GodClass"})
public class DelegatedServiceMethodInvoker extends AbstractMethodInvoker<GenericServiceArgument, ResponseEntity<Object>> {

    private static final Map<HttpMethod, HttpStatus> STATUS_BY_METHOD = Map.of(
        HttpMethod.POST, HttpStatus.CREATED,
        HttpMethod.GET, HttpStatus.OK,
        HttpMethod.DELETE, HttpStatus.NO_CONTENT,
        HttpMethod.PUT, HttpStatus.NO_CONTENT,
        HttpMethod.PATCH, HttpStatus.NO_CONTENT
    );

    /**
     * arguments which can be resolved:
     *
     * by type EndpointMetaModel from {@link GenericServiceArgument.endpointMetaModel}
     *
     * by type {@link GenericServiceArgument}
     *
     * by type HttpServletRequest from {@link GenericServiceArgument.request}
     *
     * by type HttpServletResponse from {@link GenericServiceArgument.response}
     *
     * by type JsonNode from {@link GenericServiceArgument.requestBody}
     *
     * by type TranslatedPayload from {@link GenericServiceArgument.requestBodyTranslated}
     *
     * by type ValidationSessionContext from {@link GenericServiceArgument.validationContext} for validation in service see {@link ValidationSessionContext} for
     * usage of api, you can add validation message for some object node.
     *
     * by @RequestHeader as whole map {@link GenericServiceArgument.headers}
     *
     * by @RequestHeader by name from {@link GenericServiceArgument.headers} only as simple values
     *
     * by @RequestParam as map from {@link GenericServiceArgument.httpQueryTranslated} from GenericServiceArgument
     *
     * by @RequestParam by name from {@link GenericServiceArgument.httpQueryTranslated} converted to real simple class
     *
     * by @RequestBody as JsonNode from {@link GenericServiceArgument.requestBody}, as TranslatedPayload from {@link
     * GenericServiceArgument.requestBodyTranslated}, as String as raw json, or real java bean, dto translated from {@link GenericServiceArgument.requestBody}
     *
     * by @PathVariable by name from {@link GenericServiceArgument.urlPathParams}
     *
     * example:
     * <pre>
     * &#64;Service
     * public class UserService {
     *
     *   public List&#60;String&#62; updateUser(JsonNode userAsJsonNode, &#64;PathVariable Long userId,
     *      &#64;RequestParam Map&#60;String, Object&#62; allQueryParams,
     *      Map&#60;String, String&#62; allHeaders){
     *          // your service code
     *   }
     *
     *   public List&#60;String&#62; updateUserByJsonNode(&#64;RequestBody UserDto userDto, &#64;PathVariable("user-id") Long userId,
     *      &#64;RequestParam queryArg1, &#64;RequestParam("someVarName") queryArg1,
     *      &#64;RequestHeader headerName, &#64;RequestHeader("x-cookie) cookie){
     *        // your service code
     *   }
     * }
     * </pre>
     */

    @Override
    public ResponseEntity<Object> callMethod(GenericServiceArgument genericServiceArgument) {
        EndpointMetaModel endpointMetaModel = genericServiceArgument.getEndpointMetaModel();
        return resolveReturnObject(super.callMethod(genericServiceArgument), endpointMetaModel);
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Object> resolveReturnObject(Object result, EndpointMetaModel endpointMetaModel) {
        var successHttpCodeOptional = Optional.ofNullable(endpointMetaModel.getResponseMetaModel())
            .map(EndpointResponseMetaModel::getSuccessHttpCode);
        if (result == null) {
            return successHttpCodeOptional
                .map(integer -> ResponseEntity.status(integer).build())
                .orElseGet(() -> ResponseEntity.noContent().build());
        } else if (result instanceof ResponseEntity) {
            return (ResponseEntity<Object>) result;
        }
        HttpStatus httpStatus = successHttpCodeOptional
            .map(HttpStatus::resolve)
            .orElseGet(() -> STATUS_BY_METHOD.get(endpointMetaModel.getHttpMethod()));

        return ResponseEntity.status(httpStatus)
            .body(result);
    }

    @Override
    protected BeanAndMethodMetaModel getBeanAndMethodMetaModel(GenericServiceArgument genericServiceArgument) {
        return genericServiceArgument.getEndpointMetaModel().getServiceMetaModel().getServiceBeanAndMethod();
    }

    @Override
    protected Object getInstanceForInvoke(GenericServiceArgument genericServiceArgument) {
        EndpointMetaModel endpointMetaModel = genericServiceArgument.getEndpointMetaModel();
        ServiceMetaModel serviceMetaModel = endpointMetaModel.getServiceMetaModel();
        return serviceMetaModel.getServiceInstance();
    }

    @Override
    protected EndpointQueryAndUrlMetaModel getEndpointQueryAndUrlMetaModel(GenericServiceArgument genericServiceArgument) {
        EndpointMetaModel endpointMetaModel = genericServiceArgument.getEndpointMetaModel();
        return EndpointQueryAndUrlMetaModel.builder()
            .queryArgumentsModel(endpointMetaModel.getQueryArguments())
            .pathParamsModel(endpointMetaModel.getPathParams())
            .build();
    }

    @Override
    protected ClassMetaModel getTypeOfInputDueToMetaModel(GenericServiceArgument genericServiceArgument) {
        EndpointMetaModel endpointMetaModel = genericServiceArgument.getEndpointMetaModel();
        return endpointMetaModel.getPayloadMetamodel();
    }

    @Override
    protected List<GenericMethodArgument> getGenericMethodAdditionalConfig() {
        return SERVICE_EXPECTED_ARGS_TYPE;
    }

    @Override
    protected GenericMethodArgumentProvider createDataProvider(GenericServiceArgument genericServiceArgument) {
        return new GenericServiceArgumentMethodProvider(genericServiceArgument.getEndpointMetaModel(),
            genericServiceArgument.getRequest(),
            genericServiceArgument.getResponse(),
            genericServiceArgument.getRequestBodyTranslated(),
            genericServiceArgument.getRequestBody(),
            genericServiceArgument.getHeaders(),
            genericServiceArgument.getHttpQueryTranslated(),
            genericServiceArgument.getUrlPathParams(),
            genericServiceArgument,
            genericServiceArgument.getValidationContext());
    }
}

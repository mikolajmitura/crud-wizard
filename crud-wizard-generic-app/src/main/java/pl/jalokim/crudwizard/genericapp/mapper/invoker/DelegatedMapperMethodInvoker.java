package pl.jalokim.crudwizard.genericapp.mapper.invoker;

import static pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgumentConfig.MAPPER_EXPECTED_ARGS_TYPE;

import java.util.List;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.EndpointQueryAndUrlMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMapperArgumentMethodProvider;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgument;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgumentProvider;
import pl.jalokim.crudwizard.genericapp.method.AbstractMethodInvoker;
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument;

@Component
public class DelegatedMapperMethodInvoker extends AbstractMethodInvoker<InvokerGenericMapperArgument, Object> {

    @Override
    protected BeanAndMethodMetaModel getBeanAndMethodMetaModel(InvokerGenericMapperArgument methodArgument) {
        return methodArgument.getMapperMetaModel().getMethodMetaModel();
    }

    @Override
    protected Object getInstanceForInvoke(InvokerGenericMapperArgument methodArgument) {
        return methodArgument.getMapperMetaModel().getMapperInstance();
    }

    @Override
    protected GenericMethodArgumentProvider createDataProvider(InvokerGenericMapperArgument methodArgument) {
        GenericServiceArgument genericServiceArgument = methodArgument.getGenericServiceArgument();
        return new GenericMapperArgumentMethodProvider(genericServiceArgument.getEndpointMetaModel(),
            genericServiceArgument.getRequest(),
            genericServiceArgument.getResponse(),
            genericServiceArgument.getRequestBodyTranslated(),
            genericServiceArgument.getRequestBody(),
            genericServiceArgument.getHeaders(),
            genericServiceArgument.getHttpQueryTranslated(),
            genericServiceArgument.getUrlPathParams(),
            methodArgument.getMapperArgument()
        );
    }

    @Override
    protected EndpointQueryAndUrlMetaModel getEndpointQueryAndUrlMetaModel(InvokerGenericMapperArgument methodArgument) {
        GenericServiceArgument genericServiceArgument = methodArgument.getGenericServiceArgument();
        EndpointMetaModel endpointMetaModel = genericServiceArgument.getEndpointMetaModel();
        return EndpointQueryAndUrlMetaModel.builder()
            .queryArgumentsModel(endpointMetaModel.getQueryArguments())
            .pathParamsModel(endpointMetaModel.getPathParams())
            .build();
    }

    @Override
    protected ClassMetaModel getTypeOfInputDueToMetaModel(InvokerGenericMapperArgument methodArgument) {
        return methodArgument.getMapperArgument().getSourceMetaModel();
    }

    @Override
    protected List<GenericMethodArgument> getGenericMethodAdditionalConfig() {
        return MAPPER_EXPECTED_ARGS_TYPE;
    }
}

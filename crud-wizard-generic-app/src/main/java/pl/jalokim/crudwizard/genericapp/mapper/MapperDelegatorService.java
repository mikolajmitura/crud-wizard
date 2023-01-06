package pl.jalokim.crudwizard.genericapp.mapper;

import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperType.BEAN_OR_CLASS_NAME;
import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperType.GENERATED;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.invokeMethod;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.genericapp.mapper.defaults.BaseGenericMapper;
import pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedMapper;
import pl.jalokim.crudwizard.genericapp.mapper.invoker.DelegatedMapperMethodInvoker;
import pl.jalokim.crudwizard.genericapp.mapper.invoker.InvokerGenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodArgumentMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodSignatureMetaModel;

@Service
@RequiredArgsConstructor
public class MapperDelegatorService {

    private final DelegatedMapperMethodInvoker delegatedMapperMethodInvoker;

    @SuppressWarnings({"PMD.ConfusingTernary"})
    public Object mapToTarget(MapperMetaModel mapperMetaModel, GenericMapperArgument mapperArgument) {
        if (GENERATED.equals(mapperMetaModel.getMapperType())) {
            GeneratedMapper generatedMapper = (GeneratedMapper) mapperMetaModel.getMapperInstance();
            return generatedMapper.mainMap(mapperArgument);
        } else if (BEAN_OR_CLASS_NAME.equals(mapperMetaModel.getMapperType())) {
            if (mapperMetaModel.getMapperInstance() instanceof BaseGenericMapper) {
                return ((BaseGenericMapper) mapperMetaModel.getMapperInstance())
                    .mapToTarget(mapperArgument);
            } else if (itIsGenericMapperMethod(mapperMetaModel)) {
                return invokeMethod(mapperMetaModel.getMapperInstance(), mapperMetaModel.getMethodMetaModel().getOriginalMethod(), mapperArgument);
            }
            return delegatedMapperMethodInvoker.callMethod(InvokerGenericMapperArgument.builder()
                .mapperMetaModel(mapperMetaModel)
                .mapperArgument(mapperArgument)
                .build());
        }
        // TODO #53 call mapper script somehow...
        throw new UnsupportedOperationException("not supported mapping for: " + mapperMetaModel.getMapperType());
    }

    private boolean itIsGenericMapperMethod(MapperMetaModel mapperMetaModel) {
        BeanAndMethodMetaModel methodMetaModel = mapperMetaModel.getMethodMetaModel();
        MethodSignatureMetaModel methodSignatureMetaModel = methodMetaModel.getMethodSignatureMetaModel();
        List<MethodArgumentMetaModel> methodArguments = methodSignatureMetaModel.getMethodArguments();
        return methodArguments.size() == 1 &&
            methodArguments.get(0).getArgumentType().getRawClass().equals(GenericMapperArgument.class);
    }
}

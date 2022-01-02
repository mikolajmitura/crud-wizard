package pl.jalokim.crudwizard.genericapp.mapper;

import static pl.jalokim.utils.reflection.InvokableReflectionUtils.invokeMethod;

import java.util.List;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.core.metamodels.BeanMethodMetaModel;
import pl.jalokim.crudwizard.core.metamodels.MapperMetaModel;
import pl.jalokim.crudwizard.core.metamodels.MethodArgumentMetaModel;
import pl.jalokim.crudwizard.core.metamodels.MethodSignatureMetaModel;

@Service
public class MapperDelegatorService {

    public Object mapToTarget(MapperMetaModel mapperMetaModel, GenericMapperArgument mapperArgument) {
        if (mapperMetaModel.getMapperScript() != null) {
            // TODO call mapper script somehow...
            throw new UnsupportedOperationException("Mapper script has not supported yet!");
        } else if (itIsGenericMapperMethod(mapperMetaModel)) {
            return invokeMethod(mapperMetaModel.getMapperInstance(), mapperMetaModel.getMethodMetaModel().getOriginalMethod(), mapperArgument);
        } else {
            throw new UnsupportedOperationException("Other mapper than generic with generic method has not supported yet!");
        }
    }

    private boolean itIsGenericMapperMethod(MapperMetaModel mapperMetaModel) {
        BeanMethodMetaModel methodMetaModel = mapperMetaModel.getMethodMetaModel();
        MethodSignatureMetaModel methodSignatureMetaModel = methodMetaModel.getMethodSignatureMetaModel();
        List<MethodArgumentMetaModel> methodArguments = methodSignatureMetaModel.getMethodArguments();
        return methodArguments.size() == 1
            && methodArguments.get(0).getArgumentType().getRawClass().equals(GenericMapperArgument.class);
    }
}

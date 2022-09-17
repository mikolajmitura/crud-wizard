package pl.jalokim.crudwizard.genericapp.mapper;

import static pl.jalokim.utils.reflection.InvokableReflectionUtils.invokeMethod;

import java.util.List;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodArgumentMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodSignatureMetaModel;

@Service
public class MapperDelegatorService {

    @SuppressWarnings({"PMD.ConfusingTernary"})
    public Object mapToTarget(MapperMetaModel mapperMetaModel, GenericMapperArgument mapperArgument) {
//        if (mapperMetaModel.getMapperScript() != null) {
//            // TODO #53 call mapper script somehow...
//            throw new UnsupportedOperationException("Mapper script has not supported yet!");
//        } else

        // TODO #1 how mapper should be invoked
        //  when is generated and when is provided as bean
        //  when generated mapper should be put to class loader? In mapper, or during save to db?
        //  should be some mappers type for distinguish
        //      which is for query, for persistence and for final
        //      should be some defaults implementations which will be saved to db with which default mapper invoke
        //      already logic of that is in DefaultGenericMapper which is not good due to fact that GenericMapperArgument always should have source and target type defined

        // TODO #1 mapper_delegator should delegate to mapper arguments as expected in BeansAndMethodsExistsValidator:
        //  for normal mapper COMMON_EXPECTED_ARGS_TYPE + MAPPER_EXPECTED_ARGS_TYPE
        //  for final result mapper when data source only one then COMMON_EXPECTED_ARGS_TYPE + MAPPER_EXPECTED_ARGS_TYPE
        //  for final result mapper when more than one data sources then COMMON_EXPECTED_ARGS_TYPE + input can be GenericMapperArgument.class, JoinedResultsRow.class

            if (itIsGenericMapperMethod(mapperMetaModel)) {
            return invokeMethod(mapperMetaModel.getMapperInstance(), mapperMetaModel.getMethodMetaModel().getOriginalMethod(), mapperArgument);
        } else {
            throw new UnsupportedOperationException("Other mapper than generic with generic method has not supported yet!");
        }
    }

    private boolean itIsGenericMapperMethod(MapperMetaModel mapperMetaModel) {
        BeanAndMethodMetaModel methodMetaModel = mapperMetaModel.getMethodMetaModel();
        MethodSignatureMetaModel methodSignatureMetaModel = methodMetaModel.getMethodSignatureMetaModel();
        List<MethodArgumentMetaModel> methodArguments = methodSignatureMetaModel.getMethodArguments();
        return methodArguments.size() == 1
            && methodArguments.get(0).getArgumentType().getRawClass().equals(GenericMapperArgument.class);
    }
}

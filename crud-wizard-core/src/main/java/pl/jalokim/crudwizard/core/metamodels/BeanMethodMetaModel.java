package pl.jalokim.crudwizard.core.metamodels;

import java.lang.reflect.Method;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.utils.reflection.TypeMetadata;

@Value
@Builder
public class BeanMethodMetaModel {

    String name;

    Method originalMethod;

    List<MethodArgumentMetaModel> methodArguments;

    TypeMetadata returnType;
    // TODO #04 'returnType' some returnType class meta info
    // maybe instead of TypeMetadata from java-utils use from generics-resolver

    // TODO #05 add method arguments with annotations meta model.
    // arguments which can be resolved:
    // by type EndpointMetaModel
    // by type GenericServiceArgument
    // by type HttpServletRequest from GenericServiceArgument
    // by type HttpServletResponse from GenericServiceArgument
    // by @RequestHeader from headers from GenericServiceArgument only as simple values, as String, Numbers or List<simple>
    // by @RequestParam as RawEntityObject or just Map or real java bean provided from httpQueryTranslated from GenericServiceArgument
    // by @RequestHeader as whole map from headers from GenericServiceArgument
    // by @RequestBody as RawEntityObject or Map or real java bean
}

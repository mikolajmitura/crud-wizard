package pl.jalokim.crudwizard.genericapp.mapper.invoker;

import lombok.Builder;
import lombok.Getter;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel;
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument;

@Builder
@Getter
public class InvokerGenericMapperArgument {

    GenericServiceArgument genericServiceArgument;
    MapperMetaModel mapperMetaModel;
    GenericMapperArgument mapperArgument;
}

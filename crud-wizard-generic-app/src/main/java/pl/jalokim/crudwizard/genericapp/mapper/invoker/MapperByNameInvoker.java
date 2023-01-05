package pl.jalokim.crudwizard.genericapp.mapper.invoker;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel;

@Component
@RequiredArgsConstructor
public class MapperByNameInvoker {

    private final MetaModelContextService metaModelContextService;
    private final DelegatedMapperMethodInvoker delegatedMapperMethodInvoker;

    public Object mapWithMapper(String mapperName, GenericMapperArgument genericMapperArgument,
        Object mapFrom, ClassMetaModel sourceMetaModel, ClassMetaModel targetMetaModel) {

        MetaModelContext metaModelContext = metaModelContextService.getMetaModelContext();
        MapperMetaModel mapperMetaModelByName = metaModelContext.getMapperMetaModels().getMapperMetaModelByName(mapperName);

        GenericMapperArgument newMapperArgument = genericMapperArgument.toBuilder()
            .sourceMetaModel(sourceMetaModel)
            .targetMetaModel(targetMetaModel)
            .sourceObject(mapFrom)
            .build();

        return delegatedMapperMethodInvoker.callMethod(InvokerGenericMapperArgument.builder()
            .mapperMetaModel(mapperMetaModelByName)
            .mapperArgument(newMapperArgument)
            .build());
    }
}

package pl.jalokim.crudwizard.genericapp.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.MapperMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;

@Component
@RequiredArgsConstructor
public class GeneratedMapperInvoker {

    private final MetaModelContextService metaModelContextService;

    public Object mapWithMapper(String mapperName, GenericMapperArgument genericMapperArgument, Object mapFrom) {

        MetaModelContext metaModelContext = metaModelContextService.getMetaModelContext();
        MapperMetaModel mapperMetaModelByName = metaModelContext.getMapperMetaModels().getMapperMetaModelByName(mapperName);

        GenericMapperArgument newMapperArgument = genericMapperArgument.toBuilder()
            .sourceMetaModel(null)
            .targetMetaModel(null)
            .sourceObject(mapFrom)
            .build();

        return ((GeneratedMapper) mapperMetaModelByName.getMapperInstance())
            .mainMap(newMapperArgument);
    }
}

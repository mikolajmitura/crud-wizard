package pl.jalokim.crudwizard.genericapp.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel;

@Component
@RequiredArgsConstructor
public class GeneratedMapperInvoker {

    private final MetaModelContextService metaModelContextService;

    // TODO #1 here should be opportunity for invoke other mappers by name not only generated ones
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

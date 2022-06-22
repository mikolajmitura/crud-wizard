package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Optional;
import javax.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.validation.javax.groups.FirstValidationPhase;
import pl.jalokim.crudwizard.core.validation.javax.groups.ValidationUtils;
import pl.jalokim.crudwizard.core.validation.javax.groups.WithoutDefaultGroup;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryMetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto;

@Component
@RequiredArgsConstructor
public class TemporaryContextLoader {

    private final ValidatorFactory validatorFactory;
    private final MetaModelContextService metaModelContextService;
    private final ClassMetaModelMapper classMetaModelMapper;

    public void loadTemporaryContextFor(EndpointMetaModelDto createEndpointMetaModelDto) {

        ValidationUtils.validateBean(validatorFactory.getValidator(), createEndpointMetaModelDto,
            FirstValidationPhase.class, WithoutDefaultGroup.class);

        MetaModelContext metaModelContext = metaModelContextService.loadNewMetaModelContext();
        TemporaryMetaModelContext temporaryMetaModelContext = new TemporaryMetaModelContext(metaModelContext);
        TemporaryModelContextHolder.setTemporaryContext(temporaryMetaModelContext);

        updateOrCreateClassMetaModelInContext(createEndpointMetaModelDto.getPayloadMetamodel());
        updateOrCreateClassMetaModelInContext(createEndpointMetaModelDto.getQueryArguments());
        updateOrCreateClassMetaModelInContext(createEndpointMetaModelDto.getPathParams());
        Optional.ofNullable(createEndpointMetaModelDto.getResponseMetaModel())
            .ifPresent(responseModel -> updateOrCreateClassMetaModelInContext(responseModel.getClassMetaModel()));
        elements(createEndpointMetaModelDto.getDataStorageConnectors())
            .forEach(connector ->
                updateOrCreateClassMetaModelInContext(connector.getClassMetaModelInDataStorage())
            );
    }

    public void updateOrCreateClassMetaModelInContext(ClassMetaModelDto classMetaModelDto) {
        classMetaModelMapper.toModelFromDto(classMetaModelDto);
    }
}

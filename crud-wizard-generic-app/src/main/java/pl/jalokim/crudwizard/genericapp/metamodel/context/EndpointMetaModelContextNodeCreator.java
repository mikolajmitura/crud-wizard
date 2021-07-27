package pl.jalokim.crudwizard.genericapp.metamodel.context;

import static pl.jalokim.crudwizard.core.utils.ElementsUtils.nullableElements;

import java.util.concurrent.atomic.AtomicReference;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EndpointMetaModelContextNodeCreator {

    public static void loadEndpointNodes(MetaModelContext metaModelContext) {
        var rootEndpointMetaModelNode = metaModelContext.getEndpointMetaModelContextNode();

        metaModelContext.getEndpointMetaModels().getModelsById().values()
            .forEach(endpointMetaModel -> {
                var currentEndpointMetaModelNodeRef = new AtomicReference<>(rootEndpointMetaModelNode);
                nullableElements(endpointMetaModel.getUrlMetamodel().getUrlParts())
                    .forEachWithIndexed(indexedUrlPart -> {
                        currentEndpointMetaModelNodeRef.set(currentEndpointMetaModelNodeRef.get()
                            .putNextNodeOrGet(indexedUrlPart.getValue()));
                        if (indexedUrlPart.isLast()) {
                            currentEndpointMetaModelNodeRef.get().putEndpointByMethod(endpointMetaModel);
                        }
                    });
            });
    }
}

package pl.jalokim.crudwizard.genericapp.metamodel.context;

import static pl.jalokim.crudwizard.core.utils.NullableHelper.helpWithNulls;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.EndpointMetaModelContextNode.createRootMetaModelNode;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.Data;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQueryProvider;
import pl.jalokim.crudwizard.genericapp.mapper.MappersModelsCache;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagMetamodel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModel;

@Data
public class MetaModelContext {

    private ModelsCache<DataStorageMetaModel> dataStorages;
    private ModelsCache<ApiTagMetamodel> apiTags;
    private ModelsCache<ValidatorMetaModel> validatorMetaModels;
    private ModelsCache<ClassMetaModel> classMetaModels;
    private MappersModelsCache mapperMetaModels;
    private ModelsCache<ServiceMetaModel> serviceMetaModels;
    private ModelsCache<EndpointMetaModel> endpointMetaModels;

    private EndpointMetaModelContextNode endpointMetaModelContextNode = createRootMetaModelNode();

    private ServiceMetaModel defaultServiceMetaModel;
    private MapperMetaModel defaultMapperMetaModel;
    private DataStorageMetaModel defaultDataStorageMetaModel;
    private DataStorageQueryProvider defaultDataStorageQueryProvider;
    private List<DataStorageConnectorMetaModel> defaultDataStorageConnectorMetaModels;

    public static <I, R> List<R> getListFromContext(List<I> inputList,
        Supplier<ModelsCache<R>> gettingModelById, Function<I, Long> gettingId) {
        return helpWithNulls(() -> inputList.stream()
            .map(entity -> getFromContext(gettingModelById, () -> gettingId.apply(entity)))
            .collect(Collectors.toUnmodifiableList()));
    }

    public static <R> R getFromContextByEntity(Supplier<ModelsCache<R>> gettingModelById, Supplier<BaseEntity> gettingId) {
        return gettingModelById.get().getById(helpWithNulls(() -> gettingId.get().getId()));
    }

    public static <R> R getFromContext(Supplier<ModelsCache<R>> gettingModelById, Supplier<Long> gettingId) {
        return gettingModelById.get().getById(helpWithNulls(gettingId));
    }

}

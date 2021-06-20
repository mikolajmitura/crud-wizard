package pl.jalokim.crudwizard.genericapp.metamodel.context;

import static pl.jalokim.crudwizard.core.utils.NullableHelper.helpWithNulls;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.Data;
import pl.jalokim.crudwizard.core.metamodels.ApiTagMetamodel;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.DataStorageConnectorMetaModel;
import pl.jalokim.crudwizard.core.metamodels.DataStorageMetaModel;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;
import pl.jalokim.crudwizard.core.metamodels.MapperMetaModel;
import pl.jalokim.crudwizard.core.metamodels.ServiceMetaModel;
import pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseEntity;

@Data
public class MetaModelContext {

    private ModelsCache<DataStorageMetaModel> dataStorages;
    private ModelsCache<ApiTagMetamodel> apiTags;
    private ModelsCache<ValidatorMetaModel> validatorMetaModels;
    private ModelsCache<ClassMetaModel> classMetaModels;
    private ModelsCache<MapperMetaModel> mapperMetaModels;
    private ModelsCache<ServiceMetaModel> serviceMetaModels;
    private ModelsCache<EndpointMetaModel> endpointMetaModels;

    private ServiceMetaModel defaultServiceMetaModel;
    private MapperMetaModel defaultMapperMetaModel;
    private DataStorageMetaModel defaultDataStorageMetaModel;
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

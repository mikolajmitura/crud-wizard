package pl.jalokim.crudwizard.genericapp.metamodel.context;

import static pl.jalokim.crudwizard.core.utils.NullableHelper.helpWithNulls;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.Data;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModel;

@Data
public class MetaModelContext {

    private ModelsCache<DataStorageMetaModel> dataStorages;
    private ModelsCache<ApiTagDto> apiTags;
    private ModelsCache<ValidatorMetaModel> validatorMetaModels;
    private ModelsCache<ClassMetaModel> classMetaModels;
    private ModelsCache<MapperMetaModel> mapperMetaModels;
    private ModelsCache<ServiceMetaModel> serviceMetaModels;
    private ModelsCache<EndpointMetaModelDto> endpointMetaModels;

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

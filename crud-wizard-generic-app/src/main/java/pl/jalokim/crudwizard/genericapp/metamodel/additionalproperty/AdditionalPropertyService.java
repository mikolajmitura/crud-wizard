package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;

@MetamodelService
@AllArgsConstructor
public class AdditionalPropertyService {

    private static final AtomicReference<AdditionalPropertyService> INSTANCE = new AtomicReference<>();

    private final AdditionalPropertyRepository additionalPropertyRepository;
    private final RawWithAdditionalPropertiesCustomRepository rawWithAdditionalPropertiesCustomRepository;

    @PostConstruct
    public void postConstruct() {
        INSTANCE.set(this);
    }

    public List<AdditionalPropertyEntity> fetchAdditionalPropertiesFor(WithAdditionalPropertiesEntity withAdditionalPropertiesEntity) {
        return additionalPropertyRepository.findByOwnerIdAndOwnerClass(
            withAdditionalPropertiesEntity.getId(),
            withAdditionalPropertiesEntity.getClass().getSimpleName());
    }

    public void persist(WithAdditionalPropertiesEntity withAdditionalPropertiesEntity) {
        rawWithAdditionalPropertiesCustomRepository.persist(withAdditionalPropertiesEntity);
    }

    public static AdditionalPropertyService getInstance() {
        return INSTANCE.get();
    }
}
